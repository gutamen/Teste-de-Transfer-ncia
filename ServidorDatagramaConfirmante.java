import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Set;

import javax.swing.Box;

public class ServidorDatagramaConfirmante {
    private static final int PACKET_SIZE = 500;
    private static final int SEQUENCE_NUMBER_SIZE = 8;
    private static final int CONFIRMATION_PORT = 34567;
    static boolean first = true;

    public static void main(String[] args) {
        byte[] buffer = new byte[PACKET_SIZE+8];
        boolean stopFlag = false;
        Set<Long> receivedSequences = new HashSet<>();

        try{ 
            FileOutputStream fileOutputStream = new FileOutputStream("received_packets.txt");
            DatagramSocket serverSocket = new DatagramSocket(34567); 

            long startTime = 0;

            while (!stopFlag) {
                DatagramPacket receivePacket = new DatagramPacket(buffer, 0,PACKET_SIZE+8);
                serverSocket.receive(receivePacket);
    
                if(first){
                    first = false;
                    startTime = System.currentTimeMillis();
                }

                int packetLength = receivePacket.getLength();
                if (packetLength <= 2) { 
                    stopFlag = true;
                    break;
                }
                
                //System.out.println(receivePacket.getLength());
                fileOutputStream.write(receivePacket.getData(), 8, receivePacket.getLength()-8);
                long sequenceNumber = 0;
                byte[] recieveBytes = receivePacket.getData();
                sequenceNumber = ByteBuffer.wrap(recieveBytes).getLong(0);

                sequenceNumber++;
                byte[] responseBytes;
                
                ByteBuffer responseTranslate = ByteBuffer.allocate(8);
                responseTranslate.putLong(sequenceNumber);
                
                responseBytes = responseTranslate.array();

                DatagramPacket responsePacket = new DatagramPacket(responseBytes, 0, responseBytes.length, receivePacket.getAddress(), CONFIRMATION_PORT);

                serverSocket.send(receivePacket);
                /*byte[] confirmationData = ByteBuffer.allocate(8).putLong(sequenceNumber).array();
                sendConfirmation(confirmationData, receivePacket.getAddress(), CONFIRMATION_PORT);

                if (!receivedSequences.contains(sequenceNumber)) {
                    fileOutputStream.write(buffer, SEQUENCE_NUMBER_SIZE, packetLength - SEQUENCE_NUMBER_SIZE);
                    receivedSequences.add(sequenceNumber);
                }*/
            }

            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            System.out.println("Elapsed Time: " + duration + " ms");
            fileOutputStream.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static long extractSequenceNumber(byte[] buffer, int packetLength) {
        byte[] sequenceNumberBytes = new byte[SEQUENCE_NUMBER_SIZE];
        System.arraycopy(buffer, packetLength - SEQUENCE_NUMBER_SIZE, sequenceNumberBytes, 0, SEQUENCE_NUMBER_SIZE);
        return ByteBuffer.wrap(sequenceNumberBytes).getLong();
    }

    private static void sendConfirmation(byte[] data, InetAddress address, int port) throws IOException {
        try (DatagramSocket socket = new DatagramSocket()) {
            DatagramPacket packet = new DatagramPacket(data, data.length, address, port);
            socket.send(packet);
        }
    }
}


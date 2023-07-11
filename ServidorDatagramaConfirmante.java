import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Set;

public class ServidorDatagramaConfirmante {
    private static final int PACKET_SIZE = 500;
    private static final int SEQUENCE_NUMBER_SIZE = 8;
    private static final int CONFIRMATION_PORT = 5678;

    public static void main(String[] args) {
        byte[] buffer = new byte[PACKET_SIZE];
        boolean stopFlag = false;
        Set<Long> receivedSequences = new HashSet<>();

        try (FileOutputStream fileOutputStream = new FileOutputStream("received_packets.txt", true);
             DatagramSocket serverSocket = new DatagramSocket(34567)) {

            long startTime = System.currentTimeMillis();

            while (!stopFlag) {
                DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length);
                serverSocket.receive(receivePacket);

                int packetLength = receivePacket.getLength();
                if (packetLength < SEQUENCE_NUMBER_SIZE + 2) { // 2 bytes para a confirmação
                    stopFlag = true;
                    break;
                }

                long sequenceNumber = extractSequenceNumber(buffer, packetLength);
                byte[] confirmationData = ByteBuffer.allocate(8).putLong(sequenceNumber).array();
                sendConfirmation(confirmationData, receivePacket.getAddress(), CONFIRMATION_PORT);

                if (!receivedSequences.contains(sequenceNumber)) {
                    fileOutputStream.write(buffer, SEQUENCE_NUMBER_SIZE, packetLength - SEQUENCE_NUMBER_SIZE);
                    receivedSequences.add(sequenceNumber);
                }
            }

            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            System.out.println("Elapsed Time: " + duration + " ms");

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


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.swing.Box;

public class ServidorDatagramaConfirmante {
    public static int PACKET_SIZE = 500;
    private static final int SEQUENCE_NUMBER_SIZE = 8;
    private static final int CONFIRMATION_PORT = 34567;
    static boolean first = true;
    static boolean stopFlag = false;

    public static void main(String[] args) {
        
        if(args.length == 1){
            PACKET_SIZE = Integer.parseInt(args[0]);
        }

        while(!stopFlag)
            execute();
    }

    public static void execute(){
        byte[] buffer = new byte[PACKET_SIZE+8];
        FileOutputStream fileOutputStream;
    
        File file = new File("./received_packets.txt");

        if(file.exists())
            file.delete();

        

        try{ 
            fileOutputStream = new FileOutputStream("received_packets.txt");
            DatagramSocket serverSocket = new DatagramSocket(CONFIRMATION_PORT); 
                
            System.out.println("Servidor inicializado na Porta: " + CONFIRMATION_PORT);
            System.out.println();

            long startTime = 0;

            while (!stopFlag) {
                DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length);
                serverSocket.receive(receivePacket);
    
                if(first){
                    first = false;
                    serverSocket.setSoTimeout(1000);     
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
                //if(sequenceNumber == 20000)
                    //sequenceNumber++;           // Teste de integridade comprometido
                byte[] responseBytes;
                
                ByteBuffer responseTranslate = ByteBuffer.allocate(8);
                responseTranslate.putLong(sequenceNumber);
                
                responseBytes = responseTranslate.array();

                DatagramPacket responsePacket = new DatagramPacket(responseBytes, 0, responseBytes.length, receivePacket.getAddress(), receivePacket.getPort());

                serverSocket.send(responsePacket);
            }

            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            System.out.println("Elapsed Time: " + duration + " ms");
            fileOutputStream.close();

        } catch (IOException e) {
            
            if(!first){
                first = true;
                System.out.println("Arquivo não recebido por completo, tentando novamente");
                return;
                
            }

        }
    }

}


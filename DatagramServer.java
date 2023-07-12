
import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.concurrent.*;

public class DatagramServer {
    static boolean first = true;
    static Semaphore fila;
    static int countThread = 0;
    static ArrayList<DatagramPacket> packets;

    public static void main(String[] args) {
        int packetSize = 500;
        byte[] buffer = new byte[packetSize];
        boolean stopFlag = false;
        fila = new Semaphore(1);
    
        packets = new ArrayList<>();

        try (
             DatagramSocket serverSocket = new DatagramSocket(23456)) {

            long startTime = System.currentTimeMillis();

            while (!stopFlag) {
                DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length);
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

                packets.add(receivePacket);


                // Inicia uma nova thread para lidar com o pacote recebido
                Thread clientThread = new Thread(() -> {
                try {
                    // Obtém os dados recebidos
                    fila.acquire();
                    countThread++;
                    System.out.println(countThread);
                    FileOutputStream fileOutputStream = new FileOutputStream("received_dubious_packets.txt", true);
                    // Recebe o arquivo
                    fileOutputStream.write(packets.get(0).getData(), 0, packets.get(0).getLength());
                    fileOutputStream.close();
                    packets.remove(0);
                    fila.release();
						
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });

                // Inicia a thread do cliente
                clientThread.start();

            }
            
            
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            System.out.println("Elapsed Time: " + duration + " ms");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

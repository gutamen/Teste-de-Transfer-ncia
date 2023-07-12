
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class DatagramServer {
    static boolean first = true;
    static Semaphore fila;

    public static void main(String[] args) {
        int packetSize = 500;
        byte[] buffer = new byte[packetSize];
        boolean stopFlag = false;
        fila = new Semaphore(1);
        try (FileOutputStream fileOutputStream = new FileOutputStream("received_dubious_packets.txt");
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

                // Inicia uma nova thread para lidar com o pacote recebido
                Thread clientThread = new Thread(() -> {
                try {
		                	    			
                    // Obtém os dados recebidos
                    byte[] data = receivePacket.getData().clone();
                    
                    fila.acquire(); 
                    // Recebe o arquivo
                    fileOutputStream.write(buffer, 0, receivePacket.getLength());
                    fila.release();
						
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });

                // Inicia a thread do cliente
                clientThread.start();

                fileOutputStream.write(buffer, 0, receivePacket.getLength());
            }

            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            System.out.println("Elapsed Time: " + duration + " ms");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

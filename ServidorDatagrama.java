import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class ServidorDatagrama {
    static boolean first = true;
    static int packetCount = 0; 
    public static void main(String[] args) {
        int packetSize = 500;
        byte[] buffer = new byte[packetSize];
        boolean stopFlag = false;

        try{ 
            FileOutputStream fileOutputStream = new FileOutputStream("received_dubious_packets.txt");
            DatagramSocket serverSocket = new DatagramSocket(23456);
            serverSocket.setSoTimeout(10000); 
            long startTime = 0;

            while (!stopFlag) {
                DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length);
                serverSocket.receive(receivePacket);

                if(first){
                    first = false;
                    startTime = System.currentTimeMillis();
                }

                //System.out.println(++packetCount);
                packetCount++;

                int packetLength = receivePacket.getLength();
                if (packetLength <= 2) {
                    stopFlag = true;
                    break;
                }

                fileOutputStream.write(buffer, 0, receivePacket.getLength());
            }

            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            System.out.println("Tempo de chegada: " + duration + " ms");
            System.out.println("Total de pacote recebidos = " + packetCount);

        } 
        catch (IOException e) {
            System.out.println("Erro: Tempo Limite de Espera Excedido >= 10 Segundos");
            if(!first)
                System.out.println("Pacote de Finalização não recebido");
        }
    }
}


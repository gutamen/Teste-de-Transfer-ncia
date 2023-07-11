import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class ServidorDatagrama {
    public static void main(String[] args) {
        int packetSize = 500;
        byte[] buffer = new byte[packetSize];
        boolean stopFlag = false;

        try (FileOutputStream fileOutputStream = new FileOutputStream("received_packets.txt", true);
             DatagramSocket serverSocket = new DatagramSocket(23456)) {

            long startTime = System.currentTimeMillis();

            while (!stopFlag) {
                DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length);
                serverSocket.receive(receivePacket);

                int packetLength = receivePacket.getLength();
                if (packetLength <= 2) {
                    stopFlag = true;
                    break;
                }

                fileOutputStream.write(buffer, 0, packetLength);
            }

            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            System.out.println("Elapsed Time: " + duration + " ms");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


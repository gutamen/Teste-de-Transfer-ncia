import java.io.*;
import java.net.*;

public class cliente{
    private static final int BUFFER_SIZE = 1024;

    public static void main(String[] args) {
        // Definir as informações do servidor e porta
        String serverAddress = "localhost";
        int serverPort = 12345;

        // Definir o arquivo a ser transferido
        String filePath = "teste.txt";

        // Definir tamanhos dos pacotes
        int[] packetSizes = { 100, 500, 1000 };

        // Definir tamanho do pacote
        int packetSize = 500;

        // Testar TCP 
        testTCP(serverAddress, serverPort, filePath, packetSize);

        // Testar UDP com garantia de entrega
        testUDP(serverAddress, serverPort, filePath, packetSizes, true);

        // Testar UDP sem garantia de entrega
        testUDP(serverAddress, serverPort, filePath, packetSizes, false);
    }

    private static void testTCP(String serverAddress, int serverPort, String filePath, int tamanhoPacote) {
        try {
            // Criar o socket TCP e conectar ao servidor
            Socket socket = new Socket(serverAddress, serverPort);

            // Obter o fluxo de saída do socket
            OutputStream outputStream = socket.getOutputStream();

            // Enviar o arquivo
            sendFile(filePath, outputStream, tamanhoPacote);

            // Fechar o socket
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void testUDP(String serverAddress, int serverPort, String filePath, int[] packetSizes, boolean reliable) {
        try {
            // Criar o socket UDP
            DatagramSocket socket = new DatagramSocket();

            // Obter o endereço do servidor
            InetAddress serverInetAddress = InetAddress.getByName(serverAddress);

            // Enviar o tamanho dos pacotes
            for (int packetSize : packetSizes) {
                byte[] packetSizeData = Integer.toString(packetSize).getBytes();
                DatagramPacket packet = new DatagramPacket(packetSizeData, packetSizeData.length, serverInetAddress, serverPort);
                socket.send(packet);
            }

            // Enviar a opção de entrega confiável
            byte[] reliableData = reliable ? "1".getBytes() : "0".getBytes();
            DatagramPacket reliablePacket = new DatagramPacket(reliableData, reliableData.length, serverInetAddress, serverPort);
            socket.send(reliablePacket);

            // Enviar o arquivo
            sendFile(filePath, socket, serverInetAddress, serverPort, packetSizes);

            // Fechar o socket
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void sendFile(String filePath, OutputStream outputStream, int tamanhoPacote) throws IOException {
        // Abrir o arquivo
        File file = new File(filePath);
        FileInputStream fileInputStream = new FileInputStream(file);

        // Criar um buffer para ler o arquivo
        byte[] buffer = new byte[tamanhoPacote];

        int bytesRead;
        long startTime = System.currentTimeMillis();

        // Ler o arquivo e enviar os pacotes
        while ((bytesRead = fileInputStream.read(buffer)) != -1) {
            // Enviar cada pacote com o tamanho especificado
            outputStream.write(buffer, 0, tamanhoPacote); 
        }

        long endTime = System.currentTimeMillis();

        // Calcular e exibir o tempo de transferência
        long duration = endTime - startTime;
        System.out.println("TCP Transfer Time: " + duration + " ms");

        // Fechar o arquivo
        fileInputStream.close();
    }

    private static void sendFile(String filePath, DatagramSocket socket, InetAddress serverAddress, int serverPort, int[] packetSizes) throws IOException {
        // Abrir o arquivo
        File file = new File(filePath);
        FileInputStream fileInputStream = new FileInputStream(file);

        // Criar um buffer para ler o arquivo
        byte[] buffer = new byte[BUFFER_SIZE];

        int bytesRead;
        long startTime = System.currentTimeMillis();

        // Ler o arquivo e enviar os pacotes
        while ((bytesRead = fileInputStream.read(buffer)) != -1) {
            // Enviar cada pacote com o tamanho especificado
            for (int packetSize : packetSizes) {
                DatagramPacket packet = new DatagramPacket(buffer, Math.min(bytesRead, packetSize), serverAddress, serverPort);
                socket.send(packet);
            }
        }

        long endTime = System.currentTimeMillis();

        // Calcular e exibir o tempo de transferência
        long duration = endTime - startTime;
        System.out.println("UDP Transfer Time: " + duration + " ms");

        // Fechar o arquivo
        fileInputStream.close();
    }
}

import java.io.*;
import java.net.*;

public class FileServerUDP {
    private static final int BUFFER_SIZE = 1024;
    private static final int DEFAULT_PORT = 12345;

    public static void main(String[] args) {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : DEFAULT_PORT;
        startServer(port);
    }

    private static void startServer(int port) {
        try {
            // Cria o socket de servidor UDP
            DatagramSocket serverSocket = new DatagramSocket(port);
            System.out.println("Server listening on port " + port);

            // Buffer para receber pacotes
            byte[] buffer = new byte[BUFFER_SIZE];

            while (true) {
                // Cria o pacote para receber dados do cliente
                DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length);

                // Aguarda a chegada de um pacote do cliente
                serverSocket.receive(receivePacket);

                // Inicia uma nova thread para lidar com o pacote recebido
                Thread clientThread = new Thread(() -> {
                    try {
                        // Obtém os dados recebidos
                        byte[] data = receivePacket.getData();

                        // Lê o tamanho dos pacotes
                        int[] packetSizes = readPacketSizes(data);

                        // Lê a opção de entrega confiável
                        boolean reliable = readDeliveryOption(data);

                        // Recebe o arquivo
                        receiveFile(serverSocket, receivePacket.getAddress(), receivePacket.getPort(), packetSizes, reliable);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });

                // Inicia a thread do cliente
                clientThread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static int[] readPacketSizes(byte[] data) {
        int[] packetSizes = new int[3];
        for (int i = 0; i < 3; i++) {
            packetSizes[i] = data[i];
        }
        return packetSizes;
    }

    private static boolean readDeliveryOption(byte[] data) {
        int deliveryOption = data[3];
        return deliveryOption == 1;
    }

    private static void receiveFile(DatagramSocket serverSocket, InetAddress clientAddress, int clientPort, int[] packetSizes, boolean reliable) throws IOException {
        // Cria um buffer para receber os dados
        byte[] buffer = new byte[BUFFER_SIZE];

        // Cria o arquivo de saída
        FileOutputStream fileOutputStream = new FileOutputStream("received_file");

        int bytesRead;
        long startTime = System.currentTimeMillis();

        // Recebe os pacotes e escreve no arquivo
        while (true) {
            // Cria o pacote para receber dados do cliente
            DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length);

            // Recebe o pacote do cliente
            serverSocket.receive(receivePacket);

            // Verifica se o pacote recebido é vazio (final da transferência)
            if (receivePacket.getLength() == 0) {
                break;
            }

            // Escreve os dados do pacote no arquivo
            fileOutputStream.write(buffer, 0, receivePacket.getLength());
        }

        long endTime = System.currentTimeMillis();

        // Fecha o arquivo
        fileOutputStream.close();

        // Calcula e exibe o tempo de transferência
        long duration = endTime - startTime;
        System.out.println("UDP Transfer Time: " + duration + " ms");
    }
}


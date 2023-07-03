import java.io.*;
import java.net.*;

public class FileServer {
    private static final int BUFFER_SIZE = 1024;
    private static final int DEFAULT_PORT = 12345;

    public static void main(String[] args) {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : DEFAULT_PORT;
        startServer(port);
    }

    private static void startServer(int port) {
        try {
            // Cria o socket de servidor TCP
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("Servidor aberto na porta " + port);

            while (true) {
                // Aguarda uma conexão de cliente
                Socket clientSocket = serverSocket.accept();
                System.out.println("Cliente conectado: " + clientSocket.getInetAddress().getHostAddress());

                // Inicia uma nova thread para lidar com a conexão do cliente
                Thread clientThread = new Thread(() -> {
                    try {
                        // Obtém o fluxo de entrada do cliente
                        InputStream inputStream = clientSocket.getInputStream();

                        // Lê o tamanho dos pacotes
                        int[] packetSizes = readPacketSizes(inputStream);

                        // Lê a opção de entrega confiável
                        boolean reliable = readDeliveryOption(inputStream);

                        // Recebe o arquivo
                        receiveFile(clientSocket, packetSizes, reliable);

                        // Fecha a conexão com o cliente
                        clientSocket.close();
                        System.out.println("Cliente disconectado: " + clientSocket.getInetAddress().getHostAddress());
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

    private static int[] readPacketSizes(InputStream inputStream) throws IOException {
        int[] packetSizes = new int[3];
        for (int i = 0; i < 3; i++) {
            packetSizes[i] = inputStream.read();
        }
        return packetSizes;
    }

    private static boolean readDeliveryOption(InputStream inputStream) throws IOException {
        int deliveryOption = inputStream.read();
        return deliveryOption == 1;
    }

    private static void receiveFile(Socket clientSocket, int[] packetSizes, boolean reliable) throws IOException {
        // Cria um buffer para receber os dados
        byte[] buffer = new byte[BUFFER_SIZE];

        // Cria o arquivo de saída
        FileOutputStream fileOutputStream = new FileOutputStream("received_file");

        // Obtém o fluxo de entrada do cliente
        InputStream inputStream = clientSocket.getInputStream();

        int bytesRead;
        long startTime = System.currentTimeMillis();

        // Recebe os pacotes e escreve no arquivo
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            fileOutputStream.write(buffer, 0, bytesRead);
        }

        System.out.println(buffer.toString());

        long endTime = System.currentTimeMillis();

        // Fecha o arquivo
        fileOutputStream.close();

        // Calcula e exibe o tempo de transferência
        long duration = endTime - startTime;
        System.out.println("TCP Transfer Time: " + duration + " ms");
    }
}


import java.io.*;
import java.net.*;

public class FileServer {
    private static final int BUFFER_SIZE = 1024;
    private static final int DEFAULT_PORT = 12345;

    public static void main(String[] args) {
        int porta = 0;
        if(args.length > 0){
            porta = Integer.parseInt(args[0]);
        }
        else{
            porta = DEFAULT_PORT;
        }        
        iniciaServidor(porta);
    }
	
	int packetSize = 500;

    private static void iniciaServidor(int porta) {
        try {
            // Cria o socket de servidor TCP
            ServerSocket serverSocket = new ServerSocket(porta);
            System.out.println("Servidor aberto na porta " + porta);

            while (true) {
                // Aguarda uma conexão de cliente
                Socket clientSocket = serverSocket.accept();
                System.out.println("Cliente conectado: " + clientSocket.getInetAddress().getHostAddress());

                // Inicia uma nova thread para lidar com a conexão do cliente
                Thread clientThread = new Thread(() -> {
                    try {
                        // Obtém o fluxo de entrada do cliente
                        InputStream inputStream = clientSocket.getInputStream();


						// Recebe o arquivo
                        receiveFile(clientSocket, 500);

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

    private static void receiveFile(Socket clientSocket, int packetSize) throws IOException {
        // Cria um buffer para receber os dados
        byte[] buffer = new byte[packetSize];

        // Cria o arquivo de saída
        FileOutputStream fileOutputStream = new FileOutputStream("received_file");

        // Obtém o fluxo de entrada do cliente
        InputStream inputStream = clientSocket.getInputStream();

        int bytesRead;
        long startTime = System.currentTimeMillis();

        // Recebe os pacotes e escreve no arquivo
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            fileOutputStream.write(buffer);
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


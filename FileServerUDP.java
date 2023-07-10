import java.io.*;
import java.net.*;

public class FileServerUDP {
    private static final int BUFFER_SIZE = 500;
    private static final int DEFAULT_PORT = 23456;
	static int packetSize = 500;

    public static void main(String[] args) {
        int porta = 0;
        if(args.length > 0){
            porta = Integer.parseInt(args[0]);
        }
        else{
            porta = DEFAULT_PORT;
        }        
        startServer(porta);
    
    }
	

    private static void startServer(int port) {
        try {
            // Cria o socket de servidor UDP
            DatagramSocket serverSocket = new DatagramSocket(port);
            System.out.println("Server listening on port " + port);

            // Buffer para receber pacotes
            byte[] buffer = new byte[packetSize];

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

                        // Recebe o arquivo
                        receiveFile(serverSocket, receivePacket.getAddress(), receivePacket.getPort(), packetSize, false);
						
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


    private static void receiveFile(DatagramPacket receivePacket, boolean reliable) throws IOException {
        // Cria um buffer para receber os dados
        byte[] buffer = new byte[packetSize];

        // Cria o arquivo de saída
        FileOutputStream fileOutputStream = new FileOutputStream("received_file_no_garantee.rar", true);

        int bytesRead;
        long startTime = System.currentTimeMillis();

        // Recebe os pacotes e escreve no arquivo
        while (true) {
            // Cria o pacote para receber dados do cliente
            DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length);

            // Recebe o pacote do cliente
            serverSocket.receive(receivePacket);
		    	
            System.out.println(receivePacket.getLength());
            // Verifica se o pacote recebido é vazio (final da transferência)
            if (receivePacket.getLength() < 2) {
                break;
            }

            // Escreve os dados do pacote no arquivo
            fileOutputStream.write(buffer);

        }

        long endTime = System.currentTimeMillis();

        // Fecha o arquivo
        fileOutputStream.close();

        // Calcula e exibe o tempo de transferência
        long duration = endTime - startTime;
        System.out.println("UDP Transfer Time: " + duration + " ms");
    }
}


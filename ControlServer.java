import java.io.*;
import java.net.*;

public class ControlServer {
    private static final int DEFAULT_PORT = 12345;
    public static String recive = "received_file.txt";
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
	
	static int packetSize = 500;

    private static void iniciaServidor(int porta) {
        try {
            // Cria o socket de servidor TCP
            ServerSocket serverSocket = new ServerSocket(porta);
            System.out.println("Servidor aberto na porta " + porta);

            Socket clientSocket = serverSocket.accept();
            System.out.println("Cliente conectado: " + clientSocket.getInetAddress().getHostAddress());

            // Obtém o fluxo de entrada do cliente
            InputStream inputStream = clientSocket.getInputStream();

			// Recebe o arquivo
            receiveFile(clientSocket, packetSize);

            // Fecha a conexão com o cliente
            clientSocket.close();
            System.out.println("Cliente disconectado: " + clientSocket.getInetAddress().getHostAddress());

            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    
    private static void receiveFile(Socket clientSocket, int packetSize) throws IOException {
        // Cria um buffer para receber os dados
        byte[] buffer = new byte[packetSize];

        // Cria o arquivo de saída
        FileOutputStream fileOutputStream = new FileOutputStream("received_file.txt");

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


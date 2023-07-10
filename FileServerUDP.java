import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public class FileServerUDP {
    private static final int BUFFER_SIZE = 500;
    private static final int DEFAULT_PORT = 23456;
	static int packetSize = 500;
    static Semaphore fila;
    static boolean finaliza = false;
    static boolean primeiro = true;
    static long startTime = 0;
    static long endTime = 0;

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
        fila = new Semaphore(1);
        try {
            // Cria o socket de servidor UDP
            DatagramSocket serverSocket = new DatagramSocket(port);
            System.out.println("Server listening on port " + port);

            // Buffer para receber pacotes
            byte[] buffer = new byte[packetSize];

            while (true) {
                    if(finaliza)
                        break;

                // Cria o pacote para receber dados do cliente
                DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length);

                // Aguarda a chegada de um pacote do cliente
                serverSocket.receive(receivePacket);
                
                // Inicia uma nova thread para lidar com o pacote recebido
                Thread clientThread = new Thread(() -> {
                    try {
		                	    			
                        // Obtém os dados recebidos
                        byte[] data = receivePacket.getData();
                        fila.acquire(); 
                        // Recebe o arquivo
                        receiveFile(data, false, receivePacket);
                        fila.release();
						
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });

                // Inicia a thread do cliente
                clientThread.start();

                
                
                //System.out.println(finaliza);
            }

            // Calcula e exibe o tempo de transferência
            long duration = endTime - startTime;
            System.out.println("UDP Transfer Time: " + duration + " ms");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private static void receiveFile(byte[] receivePacket, boolean reliable, DatagramPacket packet) throws IOException {


        if(primeiro){
            primeiro = false;
            startTime = System.currentTimeMillis();
        }
        // Cria o arquivo de saída
        FileOutputStream fileOutputStream = new FileOutputStream("received_file_no_garantee.rar", true);

        int bytesRead;


		    	
        //System.out.println(receivePacket.length);

        // Verifica se o pacote recebido é vazio (final da transferência)
        if (packet.getLength() < 2) {
            fileOutputStream.close();
            finaliza = true;
            System.out.println(finaliza);
            endTime = System.currentTimeMillis();
            return;

        }
        // Escreve os dados do pacote no arquivo
        fileOutputStream.write(receivePacket);



        // Fecha o arquivo
        fileOutputStream.close();
    }
}


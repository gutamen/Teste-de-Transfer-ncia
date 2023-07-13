import java.io.*;
import java.net.*;
import java.util.concurrent.*;
import java.nio.ByteBuffer;

// rm received_dubious_packets.txt & java ServidorDatagrama & rm received_packets.txt & java ServidorDatagramaConfirmante & java ControlServer
public class cliente{
    private static final int BUFFER_SIZE = 1024;

    public static void main(String[] args) {
        // Definir as informações do servidor e porta
        String serverAddress = "localhost";
        int serverPort = 12345;
        int serverPortDatagram = 23456;
        int serverPortDatagramRealiable = 34567;

        // Definir o arquivo a ser transferido
        String filePath = "teste.txt";

        // Definir tamanho do pacote
        int packetSize = 500;

        // Testar TCP 
        testTCP(serverAddress, serverPort, filePath, packetSize);

        // Testar UDP sem garantia de entrega
        testUDP(serverAddress, serverPortDatagram, filePath, packetSize, false);

        // Testar UDP com garantia de entrega
        testUDP(serverAddress, serverPortDatagramRealiable, filePath, packetSize+8, true);
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

    private static void testUDP(String serverAddress, int serverPort, String filePath, int packetSize, boolean reliable) {
        try {
            // Criar o socket UDP
            DatagramSocket socket = new DatagramSocket();

            // Obter o endereço do servidor
            InetAddress serverInetAddress = InetAddress.getByName(serverAddress);

            // Enviar o arquivo
            if(!reliable){
                sendFile(filePath, socket, serverInetAddress, serverPort, packetSize);
            }
            else{
                sendFileReliable(filePath, socket, serverInetAddress, serverPort, packetSize);
            }
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
        int packetCount = 0;
        int bytesRead;
        long startTime = System.currentTimeMillis();

        // Ler o arquivo e enviar os pacotes
        while ((bytesRead = fileInputStream.read(buffer)) != -1) {
            // Enviar cada pacote com o tamanho especificado
            packetCount++;
            outputStream.write(buffer, 0, bytesRead); 
        }

        long endTime = System.currentTimeMillis();

        // Calcular e exibir o tempo de transferência
        long duration = endTime - startTime;
        System.out.println("Tempo de TransferÊncia TCP: " + duration + " ms");
        System.out.println("Total de Pacotes enviados: " + packetCount);
        System.out.println();

        // Fechar o arquivo
        fileInputStream.close();
    }


    private static void sendFile(String filePath, DatagramSocket socket, InetAddress serverAddress, int serverPort, int packetSize) throws IOException {
        // Abrir o arquivo
        File file = new File(filePath);
        FileInputStream fileInputStream = new FileInputStream(file);

        // Criar um buffer para ler o arquivo
        byte[] buffer = new byte[packetSize];

        int bytesRead;
        long startTime = System.currentTimeMillis();
        int packetCount = 0;
        // Ler o arquivo e enviar os pacotes
        while ((bytesRead = fileInputStream.read(buffer)) != -1) {
            // Enviar cada pacote com o tamanho especificado
			DatagramPacket packet = new DatagramPacket(buffer, bytesRead, serverAddress, serverPort);
            //System.out.println(packet.getLength());
            packetCount++;
			socket.send(packet);
            
            try{
                TimeUnit.NANOSECONDS.sleep(5);
            }
            catch(Exception e){

            }
			
        }
		
        //System.out.println(packetCount);
        
        byte[] deita = new byte[2];
        deita[0] = 'O';
        deita[1] = 'F';
        DatagramPacket packet = new DatagramPacket(deita, 2, serverAddress, serverPort);
		socket.send(packet);


        long endTime = System.currentTimeMillis();

        // Calcular e exibir o tempo de transferência
        long duration = endTime - startTime;
        System.out.println("Tempo de Transferêmcia UDP não Confiável :" + duration + " ms");
        System.out.println("Total de pacotes enviados : " + packetCount);
        System.out.println();

        // Fechar o arquivo
        fileInputStream.close();
    }


    private static void sendFileReliable(String filePath, DatagramSocket socket, InetAddress serverAddress, int serverPort, int packetSize) throws IOException {
        // Abrir o arquivo
        RandomAccessFile file = new RandomAccessFile(filePath, "r");
        file.seek(0);
        // Criar um buffer para ler o arquivo
        byte[] buffer = new byte[packetSize];  // colocar número de sequência no pacote com o +8

        int bytesRead;
        long verificationQword = 0;
        long packetCount = file.length()/(packetSize-8);
        if(packetCount%(packetSize-8) != 0)
            packetCount++;
    
        int realPacketCount = 0; 

        //System.out.println(packetCount);
        long startTime = System.currentTimeMillis();
        //socket.setSoTimeout(300);
        // Ler o arquivo e enviar os pacotes
        for (int i = 0 ; packetCount > i; i++){
                       
            //System.out.println(buffer.length);
            bytesRead = file.read(buffer, 8, packetSize-8);
            
            if(bytesRead < 0)
                break;
            realPacketCount++;
            //System.out.println(file.getFilePointer());
            //for(int k = 0; buffer.length > k; k++){
                //System.out.println((char)buffer[k]);
            //}

            ByteBuffer fileShard = ByteBuffer.allocate(8);
            fileShard.putLong(verificationQword);
            fileShard.wrap(buffer, 0, 8);
            
            // Enviar cada pacote com o tamanho especificado
			DatagramPacket packet = new DatagramPacket(buffer, bytesRead+8, serverAddress, serverPort);
			socket.send(packet);

            socket.receive(packet);

            byte[] verificator = packet.getData();

            if (verificationQword+1 == ByteBuffer.wrap(verificator).getLong()){
                verificationQword++;
            }
            else{

            }

            
            
        }
		
        byte[] deita = new byte[2];
        deita[0] = 'O';
        deita[1] = 'F';
        DatagramPacket packet = new DatagramPacket(deita, 2, serverAddress, serverPort);
        packet.setLength(1);
		socket.send(packet);


        long endTime = System.currentTimeMillis();

        // Calcular e exibir o tempo de transferência
        long duration = endTime - startTime;
        System.out.println("Tempo de Transferência UDP Confiável: " + duration + " ms");
        System.out.println("Total de Pacotes Enviados:" + realPacketCount);
        System.out.println();
        // Fechar o arquivo
        file.close();
    }
}


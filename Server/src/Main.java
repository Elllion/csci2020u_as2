import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.UnsupportedAddressTypeException;

public class Main {
    private static ServerSocket serverSocket;
    private static Socket clientSocket;

    public static void main(String [] args) {
        try {
            serverSocket = new ServerSocket(8888);

            System.out.println("Ready");

            while(clientSocket == null){
                clientSocket = serverSocket.accept();
            }

            System.out.println("Client connected");
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream());

            out.print("Hello");
            out.flush();
            clientSocket.shutdownOutput();//Shutdown once output sent
            System.out.println("Message sent");


            InputStream in = clientSocket.getInputStream();
            InputStreamReader reader = new InputStreamReader(in);
            BufferedReader bin =  new BufferedReader(reader);
            String line = null;

            while(line == null){
                line = bin.readLine();
                System.out.println("Waiting for message...");
            }

            System.out.println(line);
            in.close();
            reader.close();
            bin.close();

            out.close();
            clientSocket.close();
            serverSocket.close();
        } catch (IOException e1) {
            e1.printStackTrace();
        }


    }

}

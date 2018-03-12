import java.io.*;
import java.net.Socket;

public class Main {
    public static void main(String [] args) throws IOException {

        Socket socket = new Socket("127.0.0.1", 8888);
        InputStream cin = socket.getInputStream();
        InputStreamReader creader = new InputStreamReader(cin);
        BufferedReader cbin =  new BufferedReader(creader);
        String cline = null;

        System.out.println("Connected to server");

        while( (cline = cbin.readLine()) == null){
            System.out.println("Waiting for message...");
        }
        socket.shutdownInput();//Shutdown once sent
        System.out.println(cline);


        PrintWriter out = new PrintWriter(socket.getOutputStream());
        out.print("Hey");
        out.flush();
        //out.close();
        System.out.println("Message sent");

        socket.close();

    }
}

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.UnsupportedAddressTypeException;

class ClientConnectionHandler extends Thread{
    Socket clientSocket;
  ClientConnectionHandler(Socket clientSocket){
        this.clientSocket = clientSocket;
  }

  public void run(){
      try {
          InputStream cin = clientSocket.getInputStream();
          InputStreamReader creader = new InputStreamReader(cin);
          BufferedReader cbin = new BufferedReader(creader);
          String cline = null;

          System.out.println("Connected to server");

          while ((cline = cbin.readLine()) == null) {
              System.out.println("Waiting for message...");
          }
          clientSocket.shutdownInput();//Shutdown once sent
          System.out.println(cline);

          String[] command = cline.split(" ");

          if(command.length > 0) {
              if (command[0].contains("DIR")){
                  File sharedFiles[] = new File("files").listFiles();
                  String data = "";

                  for(File f: sharedFiles){
                      if(f.isFile())
                          data+= f.getName() + ",";
                  }



                  PrintWriter out = new PrintWriter(clientSocket.getOutputStream());
                  out.print(data);
                  out.flush();
                  //out.close();
                  System.out.println("Message sent");

                  clientSocket.close();
              }else if(command[0].contains("DOWNLOAD")){
                  String data = command[1];

                  String filePath = "files/" + command[1];
                  File sendTo = new File(filePath);

                  int i = 2;
                  while(!sendTo.exists() && i < command.length){
                      filePath += " " + command[i];
                      i++;
                      sendTo = new File(filePath);
                      System.out.println(filePath);
                  }
                  BufferedReader br = new BufferedReader(new FileReader(sendTo));
                  PrintWriter out = new PrintWriter(clientSocket.getOutputStream());
                  String line = "";

                  while((line = br.readLine()) != null){
                      out.println(data);
                  }

                  out.flush();


                  //out.close();
                  System.out.println("Message sent");

                  clientSocket.close();
              }
              else if(command[0].contains("UPLOAD")){
                  String data = command[1];

                  File saveTo = new File("files/" + data);

                  PrintWriter saver= new PrintWriter(saveTo);

                  StringBuilder fileText = new StringBuilder();
                  while((cline = cbin.readLine()) != null){
                      System.out.println("Line: " + cline);
                     fileText.append(cline);
                     fileText.append(System.getProperty("line.separator"));
                  }
                  saver.write(fileText.toString());
                  System.out.print(fileText.toString());
                  saver.close();
                  clientSocket.close();
              }
          }
      }
      catch(Exception e){
          e.printStackTrace();
      }
  }
}

public class Main {
    private static ServerSocket serverSocket;
    private static Socket clientSocket;

    public static void main(String [] args) {
        try {
            serverSocket = new ServerSocket(8888);

            System.out.println("Ready");

            while(true){
                clientSocket = serverSocket.accept();

                if(clientSocket != null){
                    ClientConnectionHandler conn = new ClientConnectionHandler(clientSocket);
                    conn.run();
                }
            }


        } catch (IOException e1) {
            e1.printStackTrace();
        }


    }

}

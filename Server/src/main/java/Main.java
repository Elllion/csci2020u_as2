package  main.java;

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

          //Wait for client's command
          while ((cline = cbin.readLine()) == null) {
              System.out.println("Waiting for message...");
          }
          clientSocket.shutdownInput();//Shutdown input once sent
          System.out.println(cline);

          //Separate command by spaces
          String[] command = cline.split(" ");


          if(command.length > 0) {
              //First element of command array is the command type
              //DIR - List all files in the server's folder
              if (command[0].contains("DIR")){
                  File sharedFiles[] = new File("files").listFiles();
                  String data = "";

                  //Separate file names by '/'
                  for(File f: sharedFiles){
                      if(f.isFile())
                          data+= f.getName() + "/";
                  }

                  //Send the line to the client
                  //Send a "blank" string if the server has no files
                  PrintWriter out = new PrintWriter(clientSocket.getOutputStream());
                  if(data.length() < 1)
                      out.print("/");
                  else
                      out.print(data);

                  //Close and disconnect
                  out.flush();
                  System.out.println("Message sent");
                  clientSocket.close();

              }
              //DOWNLOAD - Client wants the specified file
              else if(command[0].contains("DOWNLOAD")){
                  String data = command[1];

                  String filePath = "files/" + command[1];
                  File sendTo = new File(filePath);

                  //Because the command line is separated by spaces,
                  //The filename can be cut off.
                  //To fix this, check if the argument is a valid file
                  //and add extra command tokens to it if it is not.
                  int i = 2;
                  while(!sendTo.exists() && i < command.length){
                      filePath += " " + command[i];
                      i++;
                      sendTo = new File(filePath);
                      System.out.println(filePath);
                  }

                  //Open the file
                  BufferedReader br = new BufferedReader(new FileReader(sendTo));
                  PrintWriter out = new PrintWriter(clientSocket.getOutputStream());
                  String line = "";

                  while((line = br.readLine()) != null){
                      out.println(data);
                  }


                  //Close and disconnect
                  out.flush();
                  System.out.println("Message sent");
                  clientSocket.close();
              }
              //UPLOAD - Client is sending a file
              else if(command[0].contains("UPLOAD")){
                  String data = command[1];

                  //Because the command line is separated by spaces,
                  //The filename can be cut off.
                  //To fix this, check if the argument is a valid file
                  //and add extra command tokens to it if it is not.
                  int i = 2;
                  while(!data.contains(".") && i < command.length){
                      data += " " + command[i];
                      i++;
                  }

                  //Open the file
                  File saveTo = new File("files/" + data);
                  PrintWriter saver= new PrintWriter(saveTo);

                  //Receive data from the user
                  StringBuilder fileText = new StringBuilder();
                  while((cline = cbin.readLine()) != null){
                     fileText.append(cline);
                     fileText.append(System.getProperty("line.separator"));
                  }

                  //Write to file
                  saver.write(fileText.toString());

                  //Close and disconnect
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

            //Constantly accept client connections
            while(true){

                clientSocket = serverSocket.accept();
                if(clientSocket != null){
                    //Open a thread if someone actually connects
                    ClientConnectionHandler conn = new ClientConnectionHandler(clientSocket);
                    conn.run();
                }
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

}

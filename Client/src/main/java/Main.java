package main.java;

import java.io.*;
import java.net.Socket;

import javafx.scene.*;
import javafx.stage.Stage;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.event.*;



public class Main extends Application{
    private BorderPane layout;
    static ListView<String> clientFiles = new ListView<>();
    static ListView<String> serverFiles = new ListView<>();
    private static File localFolder = new File("data");

    private static String serverIP = "127.0.0.1";
    private static int serverPort = 8888;
    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("Assignment 2");

        GridPane editArea = new GridPane();
        editArea.setPadding(new Insets(10, 10, 10, 10));
        editArea.setVgap(10);
        editArea.setHgap(10);

        //Download button
        Button dlButton = new Button("Download Selected");
        editArea.add(dlButton, 1, 0);
        dlButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {

                //Don't do anything if nothing is selected
                if(serverFiles.getSelectionModel().getSelectedItem() == null)
                    return;


                try {
                    //Connect to server
                    Socket socket = new Socket(serverIP, serverPort);
                    PrintWriter out = new PrintWriter(socket.getOutputStream());

                    //Send request
                    out.print("DOWNLOAD " + serverFiles.getSelectionModel().getSelectedItem());
                    out.flush();
                    socket.shutdownOutput();//Shutdown once output sent


                    InputStream in = socket.getInputStream();
                    InputStreamReader reader = new InputStreamReader(in);
                    BufferedReader bin = new BufferedReader(reader);
                    String line = null;

                    //Open the file locally
                    File saveTo = new File(localFolder.getName() + "/" + serverFiles.getSelectionModel().getSelectedItem());
                    PrintWriter saver= new PrintWriter(saveTo);
                    StringBuilder fileText = new StringBuilder();

                    //Receive data from the server
                    while ((line = bin.readLine()) != null) {
                        fileText.append(line);
                        //\n was not starting a new line, so line.separator is used
                        fileText.append(System.getProperty("line.separator"));

                    }

                    //Save data to file
                    saver.write(fileText.toString());

                    //Close everything
                    saver.close();
                    in.close();
                    reader.close();
                    bin.close();
                    out.close();
                    socket.close();

                    //Update local file list
                    updateFiles();
                }catch (IOException e){
                    System.out.println("IOException");
                    e.printStackTrace();
                }
            }
        });

        //Upload button
        Button ulButton = new Button("Upload Selected");
        editArea.add(ulButton, 0, 0);
        ulButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {

                //Don't do anything if nothing is selected
                if(clientFiles.getSelectionModel().getSelectedItem() == null)
                    return;

                try {
                    //Connect to server
                    Socket socket = new Socket(serverIP, serverPort);
                    PrintWriter out = new PrintWriter(socket.getOutputStream());

                    //Send request
                    String outPacket = "UPLOAD " + clientFiles.getSelectionModel().getSelectedItem() + "\n";

                    //Open the file locally
                    BufferedReader br = new BufferedReader(new FileReader(new File(localFolder.getName() + "/" + clientFiles.getSelectionModel().getSelectedItem())));

                    //Send the file line by line to the server
                    String line = "";
                    out.print(outPacket);
                    while((line = br.readLine()) != null){
                      out.println(line);
                    }

                    //Close everything
                    br.close();
                    out.flush();
                    socket.shutdownOutput();//Shutdown once output sent
                    out.close();
                    socket.close();

                    //Update server file list
                    getServerFiles();
                }catch (IOException e){
                    System.out.println("IOException");
                    e.printStackTrace();
                }
            }
        });


        //Lists for local/server files
        editArea.add(clientFiles,0,1);
        editArea.add(serverFiles,1,1);
        // initialize the border pane
        layout = new BorderPane();
        layout.setTop(editArea);

        Scene scene = new Scene(layout, 500, 450);
        primaryStage.setScene(scene);
        primaryStage.show();
        updateFiles();

    }

    public static void updateFiles(){
        clientFiles.getItems().clear();
        File[] fileList = localFolder.listFiles();

        if(!localFolder.exists()){
            System.out.println("Folder does not exist: " + localFolder.getName());
            return;
        }
        //Don't add anything if the local folder is empty
        if(fileList.length == 0)
            return;
        //Read every file in the folder
        for(File f : fileList){
            if(f.isFile())
                clientFiles.getItems().add(f.getName());
        }
    }

    public static void getServerFiles(){
        try {
            serverFiles.getItems().clear();

            //Connect to server
            Socket socket = new Socket(serverIP, serverPort);
            PrintWriter out = new PrintWriter(socket.getOutputStream());

            //Send request
            out.print("DIR");
            out.flush();
            socket.shutdownOutput();//Shutdown once output sent

            InputStream in = socket.getInputStream();
            InputStreamReader reader = new InputStreamReader(in);
            BufferedReader bin = new BufferedReader(reader);
            String line = null;

            //Receive data
            while (line == null) {
                line = bin.readLine();
            }

            //Separate data by slashes since they can't be used in filenames
            String[] data = line.split("/");

            //Add each item to the server file list
            for(String s : data) {
                serverFiles.getItems().add(s);
            }

            //Close everything
            in.close();
            reader.close();
            bin.close();
            out.close();
            socket.close();
        }catch (IOException e){
            System.out.println("IOException");
            e.printStackTrace();
        }
    }
    public static void main(String [] args) {

        if(args.length < 1){
            System.out.println("No arguments passed. Required arguments: \n-Path to shared folder\n-Server IP" +
                    "Optional arguments:\n-Server Port");
            System.exit(0);
        }
        if(args.length > 1){
            serverIP = args[1];
        }
        if(args.length > 2){
            serverPort = Integer.parseInt(args[2]);
        }
        getServerFiles();
        localFolder = new File(args[0]);
        launch(args);
    }
}

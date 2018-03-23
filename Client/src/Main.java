import java.io.*;
import java.net.Socket;

import javafx.scene.*;
import javafx.stage.Stage;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.*;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.input.*;
import javafx.scene.image.*;
import javafx.collections.*;
import javafx.event.*;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

import java.io.*;
import java.net.*;
import javax.swing.JTable;

public class Main extends Application{
    private BorderPane layout;
    static ListView<String> clientFiles = new ListView<>();
    static ListView<String> serverFiles = new ListView<>();
    private static File localFolder = new File("data");
    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("Assignment 2");

        GridPane editArea = new GridPane();
        editArea.setPadding(new Insets(10, 10, 10, 10));
        editArea.setVgap(10);
        editArea.setHgap(10);

        Button dlButton = new Button("Download Selected");
        editArea.add(dlButton, 1, 0);
        dlButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                try {
                    Socket socket = new Socket("127.0.0.1", 8888);
                    System.out.println("Connected");
                    PrintWriter out = new PrintWriter(socket.getOutputStream());

                    out.print("DOWNLOAD " + serverFiles.getSelectionModel().getSelectedItem());
                    out.flush();
                    socket.shutdownOutput();//Shutdown once output sent
                    System.out.println("Message sent");


                    InputStream in = socket.getInputStream();
                    InputStreamReader reader = new InputStreamReader(in);
                    BufferedReader bin = new BufferedReader(reader);
                    String line = null;

                    File saveTo = new File(localFolder.getName() + "/" + serverFiles.getSelectionModel().getSelectedItem());

                    PrintWriter saver= new PrintWriter(saveTo);

                    StringBuilder fileText = new StringBuilder();

                    while ((line = bin.readLine()) != null) {
                        System.out.println("Line: " + line);
                        fileText.append(line);
                        fileText.append(System.getProperty("line.separator"));

                    }
                    saver.write(fileText.toString());


                    saver.close();
                    in.close();
                    reader.close();
                    bin.close();

                    out.close();
                    socket.close();

                    updateFiles();
                }catch (IOException e){
                    System.out.println("IOException");
                    e.printStackTrace();
                }
            }
        });

        Button ulButton = new Button("Upload Selected");
        editArea.add(ulButton, 0, 0);
        ulButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                try {
                    Socket socket = new Socket("127.0.0.1", 8888);
                    System.out.println("Connected");

                    PrintWriter out = new PrintWriter(socket.getOutputStream());

                    String outPacket = "";

                    BufferedReader br = new BufferedReader(new FileReader(new File(localFolder.getName() + "/" + clientFiles.getSelectionModel().getSelectedItem())));
                    outPacket = "UPLOAD " + clientFiles.getSelectionModel().getSelectedItem() + "\n";

                    String line = "";
                    out.print(outPacket);
                    while((line = br.readLine()) != null){
                        System.out.println("Line: " + line);
                      out.println(line);
                    }

                    br.close();


                    out.flush();
                    socket.shutdownOutput();//Shutdown once output sent
                    System.out.println("Message sent");

                    out.close();
                    socket.close();

                    getServerFiles();
                }catch (IOException e){
                    System.out.println("IOException");
                    e.printStackTrace();
                }
            }
        });



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

        for(File f : fileList){
            if(f.isFile())
                clientFiles.getItems().add(f.getName());
        }
    }

    public static void getServerFiles(){
        try {

            serverFiles.getItems().clear();
            Socket socket = new Socket("127.0.0.1", 8888);
            System.out.println("Connected");
            PrintWriter out = new PrintWriter(socket.getOutputStream());

            out.print("DIR");
            out.flush();
            socket.shutdownOutput();//Shutdown once output sent
            System.out.println("Message sent");


            InputStream in = socket.getInputStream();
            InputStreamReader reader = new InputStreamReader(in);
            BufferedReader bin = new BufferedReader(reader);
            String line = null;

            while (line == null) {
                line = bin.readLine();
                System.out.println("Waiting for message...");
            }

            System.out.println(line);
            String[] data = line.split(",");

            for(String s : data) {
                System.out.println(s);
                serverFiles.getItems().add(s);
            }
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
    public static void main(String [] args) throws IOException {

        if(args.length < 1){
            System.out.println("No arguments passed");
            System.exit(0);
        }
        getServerFiles();
        localFolder = new File(args[0]);
        launch(args);
    }
}

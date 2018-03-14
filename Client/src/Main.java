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
    ListView<String> clientFiles;
    ListView<String> serverFiles;
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
                    InputStream cin = socket.getInputStream();
                    InputStreamReader creader = new InputStreamReader(cin);
                    BufferedReader cbin = new BufferedReader(creader);
                    String cline = null;

                    System.out.println("Connected to server");

                    while ((cline = cbin.readLine()) == null) {
                        System.out.println("Waiting for message...");
                    }
                    socket.shutdownInput();//Shutdown once sent
                    System.out.println(cline);

                    String data = serverFiles.getSelectionModel().getSelectedItem();


                    PrintWriter out = new PrintWriter(socket.getOutputStream());
                    out.print(data);
                    out.flush();
                    //out.close();
                    System.out.println("Message sent");

                    socket.close();
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
               System.out.println("Hello");
            }
        });

        clientFiles = new ListView();
        serverFiles = new ListView();

        editArea.add(clientFiles,0,1);
        editArea.add(serverFiles,1,1);
        // initialize the border pane
        layout = new BorderPane();
        layout.setTop(editArea);

        Scene scene = new Scene(layout, 500, 450);
        primaryStage.setScene(scene);
        primaryStage.show();


    }

    public static void main(String [] args) throws IOException {
        launch(args);
    }
}

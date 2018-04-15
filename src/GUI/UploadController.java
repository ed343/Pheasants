package GUI;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;
import java.io.IOException;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;

public class UploadController {


    @FXML
    private AnchorPane data_upload;


    public void initialize() {

    }


    public void handleUpload(ActionEvent event) {

        Button b = (Button) event.getSource();
        HBox currentHBox = (HBox) b.getParent();

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose file...");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Log files", "*.log"),
                new FileChooser.ExtensionFilter("Text files", "*.txt"),
                new FileChooser.ExtensionFilter("CSV files", "*.csv"));

        Stage stage = (Stage) data_upload.getScene().getWindow();
        File selected = fileChooser.showOpenDialog(stage);
        String path = selected.getAbsolutePath();
        String filename = selected.getName();

        Label uploaded = new Label("    " +filename);
        uploaded.setFont(new Font("Courier New", 14.0));
        
        // adding the name of the uploaded file instead of the button
        currentHBox.getChildren().remove(b);
        currentHBox.getChildren().add(uploaded);        
        

        LogData converter = new LogData(path);
        System.out.println(converter.getTimes());
        System.out.println(converter.getRSSIs());
        System.out.println(converter.getIDs());


        MainApplication.logfiles.add(converter);
    }

    public void handleRun() throws IOException {

        FXMLLoader sceneLoader=new FXMLLoader(getClass().getResource("data_visualisation.fxml"));
        Parent sceneParent = sceneLoader.load();
        Scene scene = new Scene(sceneParent, 800, 500);

        Stage stage = (Stage) data_upload.getScene().getWindow();
        stage.setScene(scene);
    }
    
    public void handleCancel() throws IOException {
        FXMLLoader sceneLoader = new FXMLLoader(getClass().getResource("menu.fxml"));
        Parent sceneParent = sceneLoader.load();
        Scene scene = new Scene(sceneParent, 400, 400);

        Stage stage = (Stage) data_upload.getScene().getWindow();
        stage.setScene(scene);
    }
}

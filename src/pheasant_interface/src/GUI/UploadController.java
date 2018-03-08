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

public class UploadController {


    @FXML
    private AnchorPane data_upload;

    @FXML
    private Button run;


    public void initialize() {

    }


    public void handleUpload(ActionEvent event) {

        Button b = (Button) event.getSource();

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose file...");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Log files", "*.log"),
                new FileChooser.ExtensionFilter("Text files", "*.txt"),
                new FileChooser.ExtensionFilter("CSV files", "*.csv"));

        Stage stage = (Stage) data_upload.getScene().getWindow();
        File selected = fileChooser.showOpenDialog(stage);
        String path = selected.getAbsolutePath();

        // probably here is the place to set that when the file is selected, the path or name
        // of the file is printed on the side


        LogData converter = new LogData(path);
        System.out.println(converter.getGains());
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
}

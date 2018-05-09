package GUI;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;

public class MenuController {

    @FXML
    AnchorPane rootpane;

    public void handleRegister() throws IOException {

        FXMLLoader sceneLoader=new FXMLLoader(getClass().getResource("basestations.fxml"));
        Parent sceneParent = sceneLoader.load();
        Scene scene = new Scene(sceneParent, 700, 600);

        Stage stage = (Stage) rootpane.getScene().getWindow();
        stage.setScene(scene);
    }

    public void handleAnalyse() throws IOException {

        FXMLLoader sceneLoader=new FXMLLoader(getClass().getResource("data_upload.fxml"));
        Parent sceneParent = sceneLoader.load();
        Scene scene = new Scene(sceneParent, 620, 550);

        Stage stage = (Stage) rootpane.getScene().getWindow();
        stage.setScene(scene);
    }

    public void handleSimulation() throws IOException {
        
        FXMLLoader sceneLoader = new FXMLLoader(getClass().getResource("data_simulation.fxml"));
        Parent sceneParent = sceneLoader.load();
        Scene scene = new Scene(sceneParent, 800, 500);

        Stage stage = (Stage) rootpane.getScene().getWindow();
        stage.setScene(scene);
        
    }

    public void handleSettings() {

    }
    
    public void handleExit() {
        Stage stage = (Stage) rootpane.getScene().getWindow();
        stage.close();
    }
}

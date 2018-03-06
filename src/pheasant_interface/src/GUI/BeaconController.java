package GUI;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.IOException;

public class BeaconController {

    @FXML
    public AnchorPane beacon_pane;

    @FXML
    public void saveBeacons(ActionEvent event){
        // saving beacon information in the application memory
    }

    @FXML
    public void addBeacon(ActionEvent event){
        // add new instance of beacon form that is currently described in fxml,
        // however creating a controller method for it might be better
    }

    public void goBack(ActionEvent event) throws IOException {
        FXMLLoader sceneLoader=new FXMLLoader(getClass().getResource("menu.fxml"));
        Parent sceneParent = sceneLoader.load();
        Scene scene = new Scene(sceneParent, 400, 400);

        Stage stage = (Stage) beacon_pane.getScene().getWindow();
        stage.setScene(scene);
    }

}

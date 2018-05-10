/**
 * This class is responsible for controlling the flow of the application
 * from the main menu and describing the behaviour of the program when
 * any of the menu buttons is clicked. Has a corresponding menu.fxml file.
 */

package GUI;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;

public class MenuController {

    @FXML
    AnchorPane rootpane;
    
    public void initialize() {
//        BackgroundImage myBI;
//        myBI = new BackgroundImage(new Image("./GUI/ph.png",32,32,false,true),
//                BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT,
//                BackgroundSize.DEFAULT);
//    //then you set to your node
//    rootpane.setBackground(new Background(myBI));
    }

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
        Scene scene = new Scene(sceneParent, 620, 560);

        Stage stage = (Stage) rootpane.getScene().getWindow();
        stage.setScene(scene);
    }

    public void handleSimulation() throws IOException {
        
        FXMLLoader sceneLoader = new FXMLLoader(getClass().getResource("data_simulation.fxml"));
        Parent sceneParent = sceneLoader.load();
        Scene scene = new Scene(sceneParent, 800, 450);

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

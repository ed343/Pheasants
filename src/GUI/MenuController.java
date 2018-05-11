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

public class MenuController {

    @FXML
    AnchorPane rootpane;
    
    public void initialize() {
    }

    /**
     * handler for 'Register and update basestations' button
     * @throws IOException 
     */
    public void handleRegister() throws IOException {

        FXMLLoader sceneLoader=new FXMLLoader(getClass().getResource("basestations.fxml"));
        Parent sceneParent = sceneLoader.load();
        Scene scene = new Scene(sceneParent, 700, 600);

        Stage stage = (Stage) rootpane.getScene().getWindow();
        stage.setScene(scene);
    }

    /**
     * handler for 'Upload data' button
     * @throws IOException 
     */
    public void handleAnalyse() throws IOException {

        FXMLLoader sceneLoader=new FXMLLoader(getClass().getResource("data_upload.fxml"));
        Parent sceneParent = sceneLoader.load();
        Scene scene = new Scene(sceneParent, 720, 570);

        Stage stage = (Stage) rootpane.getScene().getWindow();
        stage.setScene(scene);
    }

    /**
     * handler for 'Run simulation' button
     * @throws IOException 
     */
    public void handleSimulation() throws IOException {
        
        FXMLLoader sceneLoader = new FXMLLoader(getClass().getResource("data_simulation.fxml"));
        Parent sceneParent = sceneLoader.load();
        Scene scene = new Scene(sceneParent, 800, 500);

        Stage stage = (Stage) rootpane.getScene().getWindow();
        stage.setScene(scene);
        
    }

    /**
     * handler for 'User manual' button
     */
    public void handleManual() throws IOException {
        FXMLLoader sceneLoader = new FXMLLoader(getClass().getResource("data_manual.fxml"));
        Parent sceneParent = sceneLoader.load();
        Scene scene = new Scene(sceneParent, 700, 600);

        Stage stage = (Stage) rootpane.getScene().getWindow();
        stage.setScene(scene);
    }
    
    /**
     * handler for 'Exit' button
     */
    public void handleExit() {
        Stage stage = (Stage) rootpane.getScene().getWindow();
        stage.close();
    }
}

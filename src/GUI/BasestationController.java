package GUI;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

public class BasestationController {

    @FXML
    ScrollPane basestation_pane;

    @FXML
    VBox basestationList;

    @FXML
    HBox basestationButtons;

    int noBasestations = 4;

    public void initialize() {

        for (int i=1; i<=noBasestations; i++) {
            VBox newBasestation = addBasestation(i);
            basestationList.getChildren().add(newBasestation);
        }

        //beaconList.getChildren().add(basestationButtons);
    }

    @FXML
    public void saveBasestation(ActionEvent event){
        // saving basestation information in the application memory
    }

    @FXML
    /**
     * Method generates a single beacon registration window that can be added to the panel
     *
     * NEED TO FIGURE OUT HOW TO USE THIS AS ONACTION METHOD FOR ADDBEACON BUTTON - ATM PROBLEM IS PASSING BEACON NUMBER PARAMETERE
     */
    public VBox addBasestation(int basestationNumber){

        VBox vb = new VBox();
        vb.setSpacing(10);
        vb.setPrefSize(600,120);

        HBox hb1 = new HBox();
        hb1.setSpacing(5);

        Label l = new Label("Name:");
        TextField tf = new TextField("Basestation #"+basestationNumber);

        hb1.getChildren().addAll(l, tf);

        HBox hb2 = new HBox();
        hb2.setSpacing(5);

        Label la = new Label("Latitude coordinate:");
        TextField te = new TextField();
        te.setPrefSize(60,25);
        Label lab = new Label ("Longitude coordinate:");
        TextField te1 = new TextField();
        te1.setPrefSize(60,25);

        hb2.getChildren().addAll(la, te, lab, te1);

        HBox hb3 = new HBox();
        hb3.setSpacing(5);

        Label labe = new Label("Measured power:");
        TextField text = new TextField();
        text.setPrefSize(60,25);
        Label label = new Label("db");

        hb3.getChildren().addAll(labe, text, label);

        Separator sep = new Separator();

        vb.getChildren().addAll(hb1, hb2, hb3, sep);

        vb.setId("basestation"+basestationNumber);

        return vb;

    }

    public void addExtraBasestation() {
        noBasestations++;
        VBox newBasestation = addBasestation(noBasestations);
        basestationList.getChildren().add(newBasestation);
    }

    public void goBack(ActionEvent event) throws IOException {
        FXMLLoader sceneLoader=new FXMLLoader(getClass().getResource("menu.fxml"));
        Parent sceneParent = sceneLoader.load();
        Scene scene = new Scene(sceneParent, 400, 400);

        Stage stage = (Stage) basestation_pane.getScene().getWindow();
        stage.setScene(scene);
    }

}

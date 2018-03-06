package GUI;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;

public class VisualisationController {

    @FXML
    HBox imagebox;
    AnchorPane visualisation;

    public void initialize() throws IOException {

        // downloading static map of the location
        Image image = new Image("https://maps.googleapis.com/maps/api/staticmap?center=50.728,-3.527&zoom=16&size=480x280&key=AIzaSyD9duo3FCZAGzoydpTGoM2Gwwcba3OXxSs");
        ImageView imageView = new ImageView(image);
        //imageView.setFitWidth(300);
        //imageView.setPreserveRatio(true);

        VBox lists = new VBox();
        lists.setPadding(new Insets(0,25,25,25));
        lists.setPrefSize(250.0, 400);
        lists.setSpacing(20.0);

        // Learn JavaFX 8 page 488 has info about accessing selected items from ListView

        ListView<String> tags = new ListView<>();
        tags.setPrefWidth(200.0);
        // getting tag IDs from log file
        ArrayList<Long> intTags = MainApplication.logfiles.get(0).getIDs();
        ArrayList<String> stringTags = HelperMethods.deduplicate(HelperMethods.convertList(intTags));
        tags.getItems().addAll(stringTags);
        tags.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        // alternative way to show ListView of height for all tags
        //tags.setPrefHeight(stringTags.size() * 23 + 2);

        ListView<String> beacons = new ListView<>();
        beacons.getItems().addAll("Beacon 1", "Beacon 2", "Beacon 3");
        beacons.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        beacons.setMinHeight(beacons.getItems().size() * 23 +2);

        // adding checkbox
        CheckBox patterns= new CheckBox();
        patterns.setText("Draw movement patterns");
        patterns.setSelected(false);

        HBox buttons = new HBox();
        buttons.setSpacing(15);

        Button export = new Button("Export data");
        export.setMinSize(100,40);

        Button cancel = new Button("Cancel");
        cancel.setMinSize(100,40);
        cancel.setCancelButton(true);

        buttons.getChildren().addAll(export,cancel);

        lists.getChildren().addAll(tags, beacons, patterns, buttons);

        //here I will want to add a list of identified tags from the log file

        // adding elements to the HBox from FXML file
        imagebox.setPadding(new Insets(20, 10, 10, 20));
        imagebox.getChildren().addAll(imageView, lists);


    }

    public void handleCancel() throws IOException {
        FXMLLoader sceneLoader = new FXMLLoader(getClass().getResource("menu.fxml"));
        Parent sceneParent = sceneLoader.load();
        Scene scene = new Scene(sceneParent, 400, 400);

        Stage stage = (Stage) visualisation.getScene().getWindow();
        stage.setScene(scene);
    }
}

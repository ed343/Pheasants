package GUI;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;

public class VisualisationController {

    @FXML
    HBox imagebox;
    @FXML
    AnchorPane visualisation;
    @FXML
    HBox buttons;

    int mapWidth=480;
    int mapHeight=280;

    public void initialize() throws IOException {

        // downloading static map of the location

        ImageView imageView;

        String imagePath = "https://maps.googleapis.com/maps/api/staticmap?center=50.728,-3.527&zoom=16&size=480x280&key=AIzaSyD9duo3FCZAGzoydpTGoM2Gwwcba3OXxSs";

        Boolean backgroundLoading = false;
        Image image = new Image(imagePath, backgroundLoading);

        // checking for errors (from JavaFX book)
        if (image.isBackgroundLoading()){
            image.errorProperty().addListener((prop, oldValue, newValue) -> {
                if (newValue) {
                    System.out.println("An error occurred while loading the image.\n" +
                        "Make sure that there is a working internet connection to download the map.");
                    Label l = new Label("An error occurred while loading the image.\n" +
                            "Make sure that there is a working internet connection to download the map.\n"+
                            "1. Check your internet connection and reload the map (button)\n"+
                            "2. Or paste some dummy image instead and make the user aware of that."
                    );
                    imagebox.getChildren().add(l);
                }
            });
        }
        else if (image.isError()) {
            System.out.println("An error occurred while loading the image.\n" +
                "Make sure that there is a working internet connection to download the map." );
            Label l = new Label("An error occurred while loading the image.\n" +
                    "Make sure that there is a working internet connection to download the map.\n" +
                    "1. Check your internet connection and reload the map (button)\n"+
                    "2. Or paste some dummy image instead and make the user aware of that.");
            imagebox.getChildren().add(l);
        }

        imageView = new ImageView(image);
        Rectangle r = new Rectangle(100,100, 20, 20);
        Pane pane = new Pane();
        pane.getChildren().add(imageView);
        pane = placeBeacons(pane);
        System.out.println(pane.getChildren());


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
        beacons.getItems().addAll("Beacon 1", "Beacon 2", "Beacon 3", "Beacon 4");
        beacons.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        beacons.setMinHeight(beacons.getItems().size() * 23 +2);

        // adding checkbox
        CheckBox patterns= new CheckBox();
        patterns.setText("Draw movement patterns");
        patterns.setSelected(false);

        lists.getChildren().addAll(tags, beacons, patterns, buttons);

        // adding elements to the HBox from FXML file
        imagebox.setPadding(new Insets(20, 10, 10, 20));
        imagebox.getChildren().addAll(pane, lists);


    }

    /**
     * Here I want to get the coordinates of beacons (either geographical (from beacon registration) or converted to
     * simple coordinate system and place them as extra things on the pane that will
     */
    public Pane placeBeacons(Pane p) {

        //176.472818
        //61.2675036658992
        Rectangle r1 = new Rectangle(176.472818,61.2675036658992,5,5);
        //176.476459
        //61.26570480986848
        Rectangle r2 = new Rectangle(176.476459,61.26570480986848,5,5);
        //176.476459
        //61.269188747188906
        Rectangle r3 = new Rectangle(176.476459,61.269188747188906,5,5);
        //176.473036
        //61.26478219885438
        Rectangle r4 = new Rectangle(176.473036,61.26478219885438,5,5);

        // get x value
        double x = (r1.getX()+180)*(mapWidth/360);

        // convert from degrees to radians
        double latRad = r1.getY()*Math.PI/180;

        // get y value
        double mercN = Math.log(Math.tan((Math.PI/4)+(latRad/2)));
        double y     = (mapHeight/2)-(mapWidth*mercN/(2*Math.PI));

        System.out.println(x);
        System.out.println(y);

        Pane pane = new Pane(r1, r2);

        Group g = new Group(pane);


        // could work but doesn't
        g.setScaleX(g.getScaleX() * 2);
        g.setScaleY(g.getScaleY() * 2);

        p.getChildren().addAll(g);

        return p;
    }

    public void handleCancel() throws IOException {
        FXMLLoader sceneLoader = new FXMLLoader(getClass().getResource("menu.fxml"));
        Parent sceneParent = sceneLoader.load();
        Scene scene = new Scene(sceneParent, 400, 400);

        Stage stage = (Stage) visualisation.getScene().getWindow();
        stage.setScene(scene);
    }
}

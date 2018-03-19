package GUI;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.awt.geom.Point2D;
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

    // ArrayList to hold the coordinates of beacons that will be displayed
    ArrayList<double[]> beacons = new ArrayList<>();

    public void initialize() throws IOException {

        // downloading static map of the location

        ImageView imageView;

        // this data will be read from beacon registration/selection
        double[] beacon1 = {50.728146, -3.527182};
        double[] beacon2 = {50.729000, -3.523541};
        double[] beacon3 = {50.727346, -3.523541};
        double[] beacon4 = {50.729438, -3.526964};

        beacons.add(beacon1);
        beacons.add(beacon2);
        beacons.add(beacon3);
        beacons.add(beacon4);

        // (minX, maxY, maxX, minY)
        // not sure if it will work with negative coordinates
        // calculating the center between the beacons, considering the polygon drawn around all beacons
        double[] frame = getRect(beacons);
        double centerX = frame[0] + (frame[2]-frame[0])/2;
        System.out.println("centerX:"+ centerX);
        double centerY = frame[3]+(frame[1]-frame[3])/2;
        System.out.println("centerY:" + centerY);

        // downloading static map of the location using Google Maps API
        String imagePath = "https://maps.googleapis.com/maps/api/staticmap?center="+centerX+","+centerY+"&zoom=16&size=480x280&maptype=terrain&key=AIzaSyD9duo3FCZAGzoydpTGoM2Gwwcba3OXxSs";

        Boolean backgroundLoading = false;
        Image image = new Image(imagePath, backgroundLoading);

        // checking for errors (from JavaFX book) - most likely to happen if there is no internet connection
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
        Pane pane = new Pane();
        pane.getChildren().add(imageView);

        // calling function to place the beacons on the map
        pane = placeBeacons(pane);

        // adding side panels with lists of tags and beacons
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

        /**
         * returned code from the previous version
         * NEED THE WAY TO CONVERT THE COORDINATES WHILE TAKING INTO ACCOUNT EDGES OF THE DOWNLOADED GOOGLE MAP
         * RATHER THAN MY CALCULATIONS FOR THE 'RECTANGLE'

        for (double[] beacon: beacons) {

            // get x value
            double x = (getY(beacon)+180)*(mapWidth/360);

            // convert from degrees to radians
            double latRad = getX(beacon)*Math.PI/180;

            // get y value
            double mercN = Math.log(Math.tan((Math.PI/4)+(latRad/2)));
            double y     = (mapHeight/2)-(mapWidth*mercN/(2*Math.PI));

            System.out.println(x);
            setX(beacon, x);
            System.out.println(y);
            setY(beacon, y);
        }*/

        ArrayList<Rectangle> newCoords = new ArrayList<>() ;
        double[] frame = getRect(beacons);

        double width = frame[2]-frame[0];
        double height = frame[1]-frame[3];

        double xratio = mapWidth/width;     // longitude
        double yratio = mapHeight/height;   // latitude

        System.out.println("xratio: " + xratio);
        System.out.println("yratio: " + yratio);

        for (double[] beacon: beacons) {
            double x = (getX(beacon)-frame[0])*xratio;
            double y = (getY(beacon)-frame[3])*yratio;

            Rectangle r = new Rectangle(Math.abs(x-5),Math.abs(y-5),5,5);

            newCoords.add(r);
        }


        Group g = new Group();


        for (Rectangle r : newCoords) {
           g.getChildren().add(r);
        }

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

    /**
     * get x (longitude) coordinate of the beacon
     */
    public double getX(double [] coord) {
        return coord[0];
    }

    /**
     * get  y (latitude) coordinate of the beacon
     */
    public double getY(double[] coord) {
        return coord[1];
    }

    public void setX(double[] coord, double x) {
        coord[0] = x;
    }

    public void setY(double[] coord, double y) {
        coord[1] = y;
    }

    /**
     * getting coordinates of the rectangle that will cover all the beacons
     */
    public double[] getRect(ArrayList<double[]> beacons) {

        double maxY = -200;
        double maxX = -200;
        double minX = 200;
        double minY = 200;

        for (double[] beacon: beacons) {

            if (getX(beacon) > maxX) {
                maxX = getX(beacon);
            }
            if (getX(beacon) < minX) {
                minX = getX(beacon);
            }

            if(getY(beacon) > maxY){
                maxY = getY(beacon);
            }

            if(getY(beacon) < minY) {
                minY = getY(beacon);
            }
        }

        double [] frame = {minX, maxY, maxX, minY};

        return frame;
    }
}

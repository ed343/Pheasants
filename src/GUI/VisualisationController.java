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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.scene.paint.Color;

public class VisualisationController {

    @FXML
    HBox imagebox;
    @FXML
    AnchorPane visualisation;
    @FXML
    HBox buttons;

    int mapWidth=480;
    int mapHeight=280;
    int zoom = 16;
    double centerX;
    double centerY;

    // ArrayList to hold the coordinates of basestations that will be displayed
    ArrayList<double[]> basestations = new ArrayList<>();
    
    // will be the list of actual tags taken from the log file
    ArrayList<Long> intTags = HelperMethods.deduplicate(MainApplication.logfiles.get(0).getIDs());
    ArrayList<String> tagIDs = HelperMethods.convertList(intTags);
    
    ArrayList<double[]> tagCoords = new ArrayList<>() ;
        
    // test tag        
    double[] tag1 = {50.728392, -3.52536}; 
    
    Pane pane;
    
    Label statusLbl;

    public void initialize() throws IOException {

        // downloading static map of the location

        ImageView imageView;

        // this data will be read from beacon registration/selection
        // {latitude (y), longitude (x)}
        double[] basestation1 = {50.728146, -3.527182};
        double[] basestation2 = {50.729000, -3.523541};
        double[] basestation3 = {50.727346, -3.523541};
        double[] basestation4 = {50.729438, -3.526964};

        basestations.add(basestation1);
        basestations.add(basestation2);
        basestations.add(basestation3);
        basestations.add(basestation4);
        
        tagCoords.add(tag1);

        // (minX, maxY, maxX, minY)
        // not sure if it will work with negative coordinates
        // calculating the center between the basestations, considering the polygon drawn around all basestations
        double[] frame = getRect(basestations);

        centerX = frame[3] + (frame[1]-frame[3])/2;
        centerY = frame[2]+(frame[0]-frame[2])/2;
        System.out.println("centerY (lat):" + centerY);    
        System.out.println("centerX (lon):"+ centerX);

        // downloading static map of the location using Google Maps API
        // to make marker very small use size:tiny
        /**String imagePath = "https://maps.googleapis.com/maps/api/staticmap?"
                + "center="+centerY+","+centerX+"&"
                + "zoom=16&size=480x280&maptype=terrain&"
                + "markers=size:mid%7Ccolor:0xff0000%7Clabel:1%7C"+getY(beacons.get(0))+","+getX(beacons.get(0))+"&"
                + "markers=size:mid%7Ccolor:0xff0000%7Clabel:2%7C"+getY(beacons.get(1))+","+getX(beacons.get(1))+"&"
                + "markers=size:mid%7Ccolor:0xff0000%7Clabel:3%7C"+getY(beacons.get(2))+","+getX(beacons.get(2))+"&"
                + "markers=size:mid%7Ccolor:0xff0000%7Clabel:4%7C"+getY(beacons.get(3))+","+getX(beacons.get(3))+"&"
                + "key=AIzaSyD9duo3FCZAGzoydpTGoM2Gwwcba3OXxSs"; */
        
        String imagePath = "https://maps.googleapis.com/maps/api/staticmap?"
                + "center="+centerY+","+centerX+"&"
                + "zoom=16&size=480x280&maptype=terrain&"
                + "key=AIzaSyD9duo3FCZAGzoydpTGoM2Gwwcba3OXxSs";

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
        
        // here if error at downloading the map occured, probably I don't want to carry on and need to 
        // prompt the user to connect to internet and retry, rather than display everything. 
        // For example, could use return for that.
        
        

        imageView = new ImageView(image);
        pane = new Pane();
        pane.getChildren().add(imageView);

        // calling function to place the basestations on the map
        pane = placeBasestations();
        
        // calling function to place tags on the map
        placeTags();
        //pane.setStyle("-fx-background-color: yellow;");


        // adding side panels with lists of tags and basestations
        VBox lists = new VBox();
        lists.setPadding(new Insets(0,25,25,25));
        lists.setPrefSize(250.0, 400);
        lists.setSpacing(20.0);

        // Learn JavaFX 8 page 488 has info about accessing selected items from ListView

        ListView<String> tags = new ListView<>();
        tags.setPrefWidth(200.0);
        // getting tag IDs from log file
        intTags = HelperMethods.deduplicate(MainApplication.logfiles.get(0).getIDs());
        System.out.println("tags:");
        System.out.println(intTags);
        tagIDs = HelperMethods.deduplicate(HelperMethods.convertList(intTags));
        tags.getItems().addAll(tagIDs);
        tags.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        // alternative way to show ListView of height for all tags
        //tags.setPrefHeight(stringTags.size() * 23 + 2);

        ListView<String> basestations = new ListView<>();
        basestations.getItems().addAll("Basestation 1", "Basestation 2", "Basestation 3", "Basestation 4");
        basestations.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        basestations.setMinHeight(basestations.getItems().size() * 23 +2);

        // adding checkbox
        CheckBox patterns= new CheckBox();
        patterns.setText("Draw movement patterns");
        patterns.setSelected(false);
                        
        lists.getChildren().addAll(tags, basestations, patterns, buttons);

        // adding elements to the HBox from FXML file
        imagebox.setPadding(new Insets(20, 10, 10, 20));
        imagebox.getChildren().addAll(pane, lists);
        
        Runnable task = () -> updateTag(0);
        // Run the task in a background thread
        Thread backgroundThread = new Thread(task);
        // Terminate the running thread if the application exits
        backgroundThread.setDaemon(true);
        // Start the thread
        backgroundThread.start();
        
    }


    /**
     * Here I want to get the coordinates of basestations (either geographical (from beacon registration) or converted to
     * simple coordinate system and place them as extra things on the pane that will
     * 
     * TO DO: check if basestations don't fit in the frame of map, need to change zoom setting 
     */
    public Pane placeBasestations() {

        ArrayList<Rectangle> newCoords = new ArrayList<>() ;
        
        double[] corners = getMapCorners(centerY, centerX, zoom);

        double realWidth = corners[1]-corners[3];
        double realHeight = corners[0]-corners[2];

        double xratio = mapWidth/realWidth;     // longitude
        double yratio = mapHeight/realHeight;   // latitude

        for (double[] bs: basestations) {
            
            double x = (getX(bs)-corners[3])*xratio;            
            // equation different from x coords, since geographical coordinates go from bottom to top
            // while image coordinates go top to bottom
            double y = (corners[0] - getY(bs))*yratio;
            
            Rectangle r = new Rectangle(x,y,5,5);

            newCoords.add(r);
                       
        }
        Group g = new Group();

        for (Rectangle r : newCoords) {
           g.getChildren().add(r);
        }

        pane.getChildren().addAll(g);
                
        return pane;
    }
    
    
    public void placeTags () {
        
        ArrayList<Rectangle> tagMarks = new ArrayList<>() ;
        
        double[] corners = getMapCorners(centerY, centerX, zoom);

        double realWidth = corners[1]-corners[3];
        double realHeight = corners[0]-corners[2];

        double xratio = mapWidth/realWidth;     // longitude
        double yratio = mapHeight/realHeight;   // latitude

        for (double[] tag: tagCoords) {
            
            double x = (getX(tag)-corners[3])*xratio;            
            // equation different from x coords, since geographical coordinates go from bottom to top
            // while image coordinates go top to bottom
            double y = (corners[0] - getY(tag))*yratio;

            Rectangle r = new Rectangle(x,y,5,5);
            r.setFill(Color.CORAL);

            tagMarks.add(r);
                       
        }
        Group g = new Group();

        for (Rectangle r : tagMarks) {
           g.getChildren().add(r);
        }

        pane.getChildren().addAll(g);
        
        }
    
    public void moveTags() {
        
    }
    
    // CALLING UPDATE TAG RESULTS IN INCREASED PANE.
    // AT THE MOMENT CAN'T FIGURE OUT WHERE PANE SIZE COMES FROM AND HOW TO UPDATE
    // JUST THE IMAGE.
    // THIS LINK COULD BE USEFUL: https://stackoverflow.com/questions/26811445/how-to-access-a-child-of-an-object-in-javafx
    public void updateTag(int index) {
        for (int i=0; i<5; i++) {
            
            try {
                
                double[] oldCoords = tagCoords.get(index);
                double ny = oldCoords[0]+0.0005;
                double nx = oldCoords[1];
                double[] newCoord = {ny, nx};
                System.out.println("newCoord:");
                System.out.println(newCoord[0] + "; "+newCoord[1]);
                tagCoords.set(index, newCoord); 
                
                // Update the Label on the JavaFx Application Thread
                Platform.runLater(() -> placeTags());
                Thread.sleep(1000);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }
    
    /**
     * Method that calculates the corners of the downloaded Google Map, allowing
     * the right conversion between the geographical coordinates and visualisation image.
     * Uses CoordinateTranslation class. 
     * @param centerX
     * @param centerY
     * @return 
     */
    public double[] getMapCorners (double centerX, double centerY, int zoom) {
        CoordinateTranslation ct = new CoordinateTranslation();
        
        CoordinateTranslation.G_LatLng center = new CoordinateTranslation.G_LatLng(centerX, centerY);
        double[] coords = ct.getCorners(center, zoom, mapWidth, mapHeight);
        
        return coords;
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
    public double getY(double [] coord) {
        return coord[0];
    }

    /**
     * get  y (latitude) coordinate of the beacon
     */
    public double getX(double[] coord) {
        return coord[1];
    }

    /**
     * getting coordinates of the rectangle that will cover all the basestations
     */
    public double[] getRect(ArrayList<double[]> basestations) {

        double maxY = -200;
        double maxX = -200;
        double minX = 200;
        double minY = 200;

        for (double[] bs: basestations) {

            if (getX(bs) > maxX) {
                maxX = getX(bs);
            }
            if (getX(bs) < minX) {
                minX = getX(bs);
            }

            if(getY(bs) > maxY){
                maxY = getY(bs);
            }

            if(getY(bs) < minY) {
                minY = getY(bs);
            }
        }

        double [] frame = {maxY, maxX, minY, minX};

        return frame;
    }
}

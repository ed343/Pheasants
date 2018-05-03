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
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.paint.Color;

public class VisualisationController {
    
    public VisualisationController(){}

    @FXML
    HBox imagebox;
    @FXML
    AnchorPane visualisation;
    @FXML
    HBox buttons;
    @FXML
    Pane pane;    
    @FXML
    CheckBox drawTrace;
    @FXML
    Slider slider;

    int mapWidth = 480;
    int mapHeight = 280;
    int zoom = 16;
    double centerX;
    double centerY;

    // ArrayList to hold the coordinates of basestations that will be displayed
    ArrayList<Double[]> basestations = new ArrayList<>();

    // will be the list of actual tags taken from the log file
    //ArrayList<Long> intTags = HelperMethods.deduplicate(MainApplication.logfiles.get(0).getIDs());
    //ArrayList<String> tagIDs = HelperMethods.convertList(intTags);

    ArrayList<Double[]> tagCoords = new ArrayList<>();

    // test tags       
    Double[] tag1 = {50.73848598629042, -3.531734873115414}; //
    // Double[] tag2 = {50.728292, -3.52586};
    

     Double[][] simCoords = {{50.66935899306587, -3.531735151179933}, {50.61749281320168,-3.531734487793321}, {50.5719161186626,-3.5317337967155824},
    {50.620876309181085, -3.5317327213982557}, {50.56981711157608, -3.53173205345777}, {50.62426004860634,-3.5317309551761733},
    {50.67327479536405, -3.5317298826590307}, {50.62215870599698, -3.53172921314334}, {50.67429433055359, -3.5317281287371323},
    {50.62554253770395, -3.531727447127234}, {50.674558623196255, -3.531726377091593}, {50.723625947811826, -3.5317253085560707},
    {50.67482291732843, -3.5317246254610266}, {50.62607057623022,-3.5317239414128334}, {50.57500574649445,-3.531723267096043}, 
    {50.62396915271218,-3.5317221992209875},{50.67610678750452,-3.531721120078098},{50.627353114712946,-3.5317204335976005}};
    

    // parameters used for map scaling and tag placement
    Double[] corners;
    double realWidth;
    double realHeight;

    double xratio; // longitude
    double yratio;  // latitude
    
    int currentTime = 0; // variable to hold current time of the visualisation
    

    public void initialize() throws IOException {

        // downloading static map of the location
        ImageView imageView;

        // this data will be read from beacon registration/selection
        // {latitude (y), longitude (x)}
        /**
        Double[] basestation1 = {50.728146, -3.527182};
        Double[] basestation2 = {50.729000, -3.523541};
        Double[] basestation3 = {50.727346, -3.523541};
        Double[] basestation4 = {50.729438, -3.526964};
        * */
        
        Double[] basestation1 = {50.738486, -3.531713};//Nat
        Double[] basestation2 = {50.738675, -3.531101};//James
        Double[] basestation3 = {50.738822, -3.531642};//Cat
        Double[] basestation4 = {50.738829, -3.531627};//Duplicate placed next to Cat.

        basestations.add(basestation1);
        basestations.add(basestation2);
        basestations.add(basestation3);
        basestations.add(basestation4);

        tagCoords.add(tag1);
        //tagCoords.add(tag2);

        // (minX, maxY, maxX, minY)
        // not sure if it will work with negative coordinates
        // calculating the center between the basestations, considering the polygon drawn around all basestations
        Double[] frame = getRect(basestations);

        centerX = frame[3] + (frame[1] - frame[3]) / 2;
        centerY = frame[2] + (frame[0] - frame[2]) / 2;
        System.out.println("centerY (lat):" + centerY);
        System.out.println("centerX (lon):" + centerX);
        
       corners = getMapCorners(centerY, centerX, zoom);

        realWidth = corners[1] - corners[3];
        realHeight = corners[0] - corners[2];

        xratio = mapWidth / realWidth;     // longitude
        yratio = mapHeight / realHeight;   // latitude
        
        VBox leftbox = new VBox();        

      String imagePath = "https://maps.googleapis.com/maps/api/staticmap?"
                + "center=" + centerY + "," + centerX + "&"
                + "zoom=" +zoom + "&size=480x280&maptype=terrain&"
                + "key=AIzaSyD9duo3FCZAGzoydpTGoM2Gwwcba3OXxSs";

        Boolean backgroundLoading = false;
        Image image = new Image(imagePath, backgroundLoading);

        // checking for errors (from JavaFX book) - most likely to happen if there is no internet connection
        if (image.isBackgroundLoading()) {
            image.errorProperty().addListener((prop, oldValue, newValue) -> {
                if (newValue) {
                    System.out.println("An error occurred while loading the image.\n"
                            + "Make sure that there is a working internet connection to download the map.");
                    Label l = new Label("An error occurred while loading the image.\n"
                            + "Make sure that there is a working internet connection to download the map.\n"
                            + "1. Check your internet connection and reload the map (button)\n"
                            + "2. Or paste some dummy image instead and make the user aware of that."
                    );
                    imagebox.getChildren().add(l);
                    return;
                }
            });
        } else if (image.isError()) {
            System.out.println("An error occurred while loading the image.\n"
                    + "Make sure that there is a working internet connection to download the map.");
            Label l = new Label("An error occurred while loading the image.\n"
                    + "Make sure that there is a working internet connection to download the map.\n"
                    + "1. Check your internet connection and reload the map (button)\n"
                    + "2. Or paste some dummy image instead and make the user aware of that.");
            imagebox.getChildren().add(l);
            return;
        }
        
        HBox visualControl = new HBox();
        visualControl.setMaxWidth(300);
        visualControl.setPadding(new Insets(20, 0, 0, 0));
        visualControl.setSpacing(15);
        
        Button play = new Button(">");
        play.setPrefSize(25, 25);
        
        
        play.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
                if (play.getText().equals(">")) {
                    play.setText("||");
                    // PAUSE VISUALISATION
                }
                else {
                    play.setText(">");
                    // PLAY VISUALISATION
                }
            }
        });
 
        
        slider = new Slider();
        slider.setMinWidth(350.0);
        //slider.setPadding(new Insets(20, 0, 0, 0));
        slider.setMin(0);
        slider.setMax(9);   // should be a number of locations that tag goes through
        slider.setValue(0);
        slider.setShowTickLabels(true);
        slider.setShowTickMarks(true);
        slider.setMajorTickUnit(1);
        slider.setMinorTickCount(0);
        slider.setBlockIncrement(1);
        
        //  this listener reacts to the change on the slider:
        //      should start visualisation from the point were slider is set to
        //      but doesn't do that yet (if we have a list of locations,
        //      then it would use that location as a starting point and proceed
        //      with subsequent locations)
        slider.valueProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> ov,
                Number old_val, Number new_val) {
                    currentTime = new_val.intValue();
                    System.out.println(currentTime);
                    // start visualisation for both tags
                    //for (int i=0; i<2; i++) {
                    //    updateTag(i);
        //}
            }
        });
        
        visualControl.getChildren().addAll(play, slider);

        // here if error at downloading the map occured, probably I don't want to carry on and need to 
        // prompt the user to connect to internet and retry, rather than display everything. 
        // For example, could use return for that.
        imageView = new ImageView(image);
        pane = new Pane();
        pane.getChildren().add(imageView);
        pane.setMaxSize(480, 280);
        leftbox.getChildren().addAll(pane, visualControl);

        // calling function to place the basestations on the map
        pane = placeBasestations();

        // calling function to place tags on the map
        placeTags();
        // TODO: if tag is out of the map space, could print out some warning

        // adding side panels with lists of tags and basestations
        VBox rightbox = new VBox();
        rightbox.setPadding(new Insets(0, 0, 25, 25));
        rightbox.setPrefSize(250.0, 400);
        rightbox.setSpacing(20.0);

        // Learn JavaFX 8 page 488 has info about accessing selected items from ListView
        ListView<String> tags = new ListView<>();
        tags.setPrefWidth(200.0);
        // getting tag IDs from log file
        
        //intTags = (int)HelperMethods.deduplicate(MainApplication.logfiles.get(0).getIDs());
        System.out.println("tags:");
        ArrayList al = new ArrayList<> (MainApplication.logfiles.get(0).getIDs());
        System.out.println(al);
        ArrayList tagIDs = HelperMethods.deduplicate(HelperMethods.convertList(al));
        tags.getItems().addAll(tagIDs);
        
        tags.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        // alternative way to show ListView of height for all tags
        //tags.setPrefHeight(stringTags.size() * 23 + 2);
        ListView<String> basestations = new ListView<>();
        basestations.getItems().addAll("Basestation 1", "Basestation 2", "Basestation 3", "Basestation 4");
        basestations.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        basestations.setMinHeight(basestations.getItems().size() * 23 + 2);
        
        
        // a number of radio buttons to select the regularity (granularity) of the tag movement
        HBox regularity = new HBox();        
        regularity.setSpacing(5);
        
        RadioButton r4 = new RadioButton("4 sec");
        RadioButton r8 = new RadioButton("8 sec");
        RadioButton r20 = new RadioButton("20 sec");
        RadioButton r60 = new RadioButton("1 min");
        
        ToggleGroup group = new ToggleGroup();
        r4.setToggleGroup(group);
        r8.setToggleGroup(group);
        r20.setToggleGroup(group);
        r60.setToggleGroup(group);
        r4.setSelected(true);
        
        regularity.getChildren().addAll(r4, r8, r20, r60);
                

        // adding checkbox
        drawTrace = new CheckBox();
        drawTrace.setText("Draw movement patterns");
        drawTrace.setSelected(false);

        rightbox.getChildren().addAll(tags, basestations, regularity, drawTrace, buttons);

        // adding elements to the HBox from FXML file
        imagebox.setPadding(new Insets(20, 10, 10, 20));
        imagebox.getChildren().addAll(leftbox, rightbox);

        // THIS IS WHERE A VISUALISATION THREAD WILL BE STARTED
        // remains for loop, should be changed to traverse tagID list for all tags
        for (int i=0; i<tagCoords.size(); i++) {
            updateTag(i);
        }
    }


     /**
     * Here I want to get the coordinates of basestations (either geographical
     * (from beacon registration) or converted to simple coordinate system and
     * place them as extra things on the pane that will
     *
     * TO DO: check if basestations don't fit in the frame of map, need to
     * change zoom setting
     */
    public Pane placeBasestations() {

        ArrayList<Rectangle> newCoords = new ArrayList<>();

        for (Double[] bs : basestations) {

            double x = (getX(bs) - corners[3]) * xratio;
            // equation different from x coords, since geographical coordinates go from bottom to top
            // while image coordinates go top to bottom
            double y = (corners[0] - getY(bs)) * yratio;

            Rectangle r = new Rectangle(x, y, 5, 5);

            newCoords.add(r);

        }
        Group g = new Group();

        for (Rectangle r : newCoords) {
            g.getChildren().add(r);
        }

        pane.getChildren().addAll(g);

        return pane;
    }

    /**
     * This method should remove the last appearance of the tag. 
     * Probably only works if we choose to display all tags. 
     * @param coords 
     */
    public void removeTag() {
        // accessing Group object of the Pane that stores rectangles
        //System.out.println(pane.getChildren());
        // removing the first rectangle 
        pane.getChildren().remove(2);
    }

    public void placeTags() {

        ArrayList<Rectangle> tagMarks = new ArrayList<>();

        for (Double[] tag : tagCoords) {
            
            double x = (getX(tag) - corners[3]) * xratio;
            // equation different from x coords, since geographical coordinates go from bottom to top
            // while image coordinates go top to bottom
            double y = (corners[0] - getY(tag)) * yratio;

            Rectangle r = new Rectangle(x, y, 5, 5);
            r.setFill(Color.OLIVE);
            r.hoverProperty().addListener((observable) -> {
                Tooltip t = new Tooltip("Tag ID"); // should get tag id and put it as a label
                Tooltip.install(r, t);
            });

            tagMarks.add(r);

        }
        Group g = new Group();

        for (Rectangle r : tagMarks) {
            g.getChildren().add(r);
        }

        pane.getChildren().add(g);
    }
    
    public void updateSlider(int time) {
        slider.setValue(time);
    }
     
     public void updateTag(int index) {
         new Thread() {

                // runnable for that thread
                public void run() {
                    // this will be for loop for all locations that we have
                    for (int i = 0; i < 10; i++) {
                        //currentTime = i;
                        System.out.println("current time: "+currentTime);
                        int time = i;
                       
                        
                        try {
                            // sleep for a second
                            Thread.sleep(1000);
                        } catch (InterruptedException ex) {
                            ex.printStackTrace();
                        }
                        
                        // update things on FX thread
                        Platform.runLater(new Runnable() {

                            public void run() {
                                Double[] oldCoords = tagCoords.get(index);
                                double ny = oldCoords[0];
                                double nx = oldCoords[1]+0.0001;
                                Double[] newCoord = {ny, nx};
                                //Double[] newCoord = {simCoords[time][0], simCoords[time][1]};
                                System.out.println("newCoord:");
                                System.out.println(newCoord[0] + "; "+newCoord[1]);
                                tagCoords.set(index, newCoord);

                                
                                // if checkbox in GUI is not selected, previous position of the tag is removed (default)
                                if (!drawTrace.isSelected()) {
                                    placeTags();
                                    removeTag();
                                    updateSlider(time);
                                }
                                
                                // if checkbox in GUI is selected, previous tag locations are kept on the map
                                else if (drawTrace.isSelected()) {
                                    placeTags();
                                    updateSlider(time);
                                }

                            }
                        });
                    }
                }
            }.start();
         
     }

    /**
     * Method that calculates the corners of the downloaded Google Map, allowing
     * the right conversion between the geographical coordinates and
     * visualisation image. Uses CoordinateTranslation class.
     *
     * @param centerX
     * @param centerY
     * @return
     */
    public Double[] getMapCorners(double centerX, double centerY, int zoom) {
        CoordinateTranslation ct = new CoordinateTranslation();

        CoordinateTranslation.G_LatLng center = new CoordinateTranslation.G_LatLng(centerX, centerY);
        Double[] coords = ct.getCorners(center, zoom, mapWidth, mapHeight);

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
    public double getY(Double[] coord) {
        return coord[0];
    }

    /**
     * get y (latitude) coordinate of the beacon
     */
    public double getX(Double[] coord) {
        return coord[1];
    }

    /**
     * getting coordinates of the rectangle that will cover all the basestations
     */
    public Double[] getRect(ArrayList<Double[]> basestations) {

        double maxY = -200;
        double maxX = -200;
        double minX = 200;
        double minY = 200;

        for (Double[] bs : basestations) {

            if (getX(bs) > maxX) {
                maxX = getX(bs);
            }
            if (getX(bs) < minX) {
                minX = getX(bs);
            }

            if (getY(bs) > maxY) {
                maxY = getY(bs);
            }

            if (getY(bs) < minY) {
                minY = getY(bs);
            }
        }

        Double[] frame = {maxY, maxX, minY, minX};

        return frame;
    }
}

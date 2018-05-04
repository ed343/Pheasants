package GUI;

import Multilateration.MapProcessing;
import Multilateration.NewClass;
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
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
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
    
    NewClass nc = new NewClass();

    // ArrayList to hold the geographical coordinates of basestations that will be displayed
    ArrayList<Double[]> basestations = new ArrayList<>();
    
    // ArrayList holding cartesian plane basestation coords
    ArrayList<Double[]> cartBasestationCoords = new ArrayList<>(); 
    
    // hashmap returned after all procedures in NewClass, containing all info:
    // < tagID < timestamp, cartCoord(x,y,z)> >
    HashMap<Long, HashMap<BigInteger, Double[]>> tag_registry;

    // will be the list of actual tags taken from the log file
    //ArrayList<Long> intTags = HelperMethods.deduplicate(MainApplication.logfiles.get(0).getIDs());
    //ArrayList<String> tagIDs = HelperMethods.convertList(intTags);

    ArrayList<Double[]> tagCoords = new ArrayList<>();

     // NOT REALLY TRUE AS INITIAL TAG LOCATION WILL BE FIRST LOCATION
    // FROM TAG_REGISTRY STUFF
    Double[] tag1 = {50.73848598629042, -3.531734873115414};
    
    // parameters used for map scaling and tag placement
    Double[] corners;
    double realWidth;
    double realHeight;

    double xratio; // longitude
    double yratio;  // latitude
    
    int currentTime = 0; // variable to hold current time of the visualisation
    
    ArrayList<Long> all_tags= new ArrayList<>(); // tag IDs
    ArrayList<ArrayList<BigInteger>> all_times= new ArrayList<>(); // timestamps
    ArrayList<ArrayList<Double[]>> all_coords= new ArrayList<>(); // coordinates
    

    public void initialize() throws IOException {

        // downloading static map of the location
        ImageView imageView;        
        
        basestations =  nc.getGeoBasestations();
        
        MapProcessing mp = new MapProcessing(basestations);
        cartBasestationCoords = mp.getBasestations(basestations);        
        int zoom = mp.getZoom();
        double[] centerPoints = mp.getCenter(); // {centerY, centerX}
        
        getTagInfo(); // prepares all_tags, all_times and all_coords for later use
        
        System.out.println("number of tags: " + all_tags.size());
        System.out.println("number of times: " + all_times.get(0).size());
        System.out.println("number of coords: " + all_coords.get(0).size());
        
        VBox leftbox = new VBox();        

        String imagePath = "https://maps.googleapis.com/maps/api/staticmap?"
                + "center=" + centerPoints[0] + "," + centerPoints[1] + "&"
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
        slider.setMax(all_times.get(0).size()-1);   // TODO: should be a MAXIMUM number of locations that tag goes through
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

        // calling function to place the basestations on the map and populate
        // ArrayList with cartesian coordinates
        
        // WILL HAVE TO TAKE IN ALREADY CONVERTED COORDINATES FROM NEWCLASS AND JUST PUT ON PLANE
        placeBasestations(cartBasestationCoords);

        // adding side panels with lists of tags and basestations
        VBox rightbox = new VBox();
        rightbox.setPadding(new Insets(0, 0, 25, 25));
        rightbox.setPrefSize(250.0, 400);
        rightbox.setSpacing(20.0);

        // Learn JavaFX 8 page 488 has info about accessing selected items from ListView
        ListView<String> tags = new ListView<>();
        tags.setPrefWidth(200.0);
        // getting tag IDs from log file
        
        // currently can't use that as log files are not a thing
        // ArrayList al = new ArrayList<> (MainApplication.logfiles.get(0).getIDs());
        // ArrayList tagIDs = HelperMethods.deduplicate(HelperMethods.convertList(al));
        
        for (Long tag: all_tags) {
            tags.getItems().add(tag+"");
        }
        
        tags.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        // alternative way to show ListView of height for all tags
        //tags.setPrefHeight(stringTags.size() * 23 + 2);
        ListView<String> basestationPanel = new ListView<>();
        
        // TODO: collect actual basestation names from logUpload
        basestationPanel.getItems().addAll("Basestation 1", "Basestation 2", "Basestation 3", "Basestation 4");
        basestationPanel.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        basestationPanel.setMinHeight(basestationPanel.getItems().size() * 23 + 2);
                
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
        
        // TODO: regularity on action should cause regeneration of locations for
        // tags (intermediate step before visualisation and start visualisation from beginning

        // TODO: make movement patterns draw a line between points
        drawTrace = new CheckBox();
        drawTrace.setText("Draw movement patterns");
        drawTrace.setSelected(false);

        rightbox.getChildren().addAll(tags, basestationPanel, regularity, drawTrace, buttons);

        // adding elements to the HBox from FXML file
        imagebox.setPadding(new Insets(20, 10, 10, 20));
        imagebox.getChildren().addAll(leftbox, rightbox);
        
        //--------------------------END OF GUI SETUP-------------------------//
        // FROM HERE I SHOULD LOOP THROUGH THE TAG REGISTRY AND VISUALISE THAT STUFF
        
         // calling function to place tags on the map
        placeTags(0);
        // TODO: if tag is out of the map space, could print out some warning

        // THIS IS WHERE A VISUALISATION THREAD WILL BE STARTED
        // remains for loop, should be changed to traverse tagID list for all tags
        for (int i=0; i<all_tags.size(); i++) {
            updateTag(i);
        }
    }
    
    public void getTagInfo() {
        tag_registry = nc.doMagic();
        
        // collecting tags
        for (Map.Entry<Long, HashMap<BigInteger, Double[]>> entry : tag_registry.entrySet()) {
            Long key = entry.getKey();
            HashMap<BigInteger, Double[]> value = entry.getValue();
            all_tags.add(key);
            ArrayList<BigInteger> times= new ArrayList<>();
            ArrayList<Double[]> coords=new ArrayList<>();
            for (Map.Entry<BigInteger, Double[]> entry2 : value.entrySet()) {
                BigInteger key2 = entry2.getKey();
                times.add(key2);
                Double[] value2= entry2.getValue();
                coords.add(value2);
            }
            
            ArrayList<BigInteger> times_orig=times;
            times_orig=NewClass.makeDeepCopyBigInteger(times);
            
            ArrayList<Double[]> sorted_coords= new ArrayList<>();
            Collections.sort(times);
            for(int i=0; i<times.size(); i++){
                for(int j=0; j<times.size(); j++){
                    if (times.get(i)==times_orig.get(j)){
                        sorted_coords.add(i,coords.get(j));
                        break;
                    }
                }
            }
            all_times.add(times);
            all_coords.add(sorted_coords);
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
    public void placeBasestations(ArrayList<Double[]> coords) {

        ArrayList<Rectangle> newCoords = new ArrayList<>();

        for (Double[] bs : coords) {

            Rectangle r = new Rectangle(bs[0], bs[1], 5, 5);

            newCoords.add(r);

        }
        Group g = new Group();

        for (Rectangle r : newCoords) {
            g.getChildren().add(r);
        }

        pane.getChildren().addAll(g);
    }

    /**
     * This method should remove the last appearance of the tag. 
     * Probably only works if we choose to display all tags. 
     */
    public void removeTag() {
        // accessing Group object of the Pane that stores rectangles
        //System.out.println(pane.getChildren());
        // removing the first rectangle 
        pane.getChildren().remove(2);
    }

    public void placeTags(int index) {

        ArrayList<Rectangle> tagMarks = new ArrayList<>();
        
        for (int i=0; i<all_tags.size(); i++) {
            
            String name = Long.toString(all_tags.get(i));
            
            // getting starting position of the tag
            double x = all_coords.get(i).get(index)[0];
            double y = all_coords.get(i).get(index)[1];
                
            Rectangle r = new Rectangle(x, y, 5, 5);
            r.setFill(Color.OLIVE);
            r.hoverProperty().addListener((observable) -> {
                Tooltip t = new Tooltip(name); // should get tag id and put it as a label
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
                    for (int i = 0; i < all_times.get(index).size(); i++) {
                        //currentTime = i;
                        // TODO: timing will actually need to be synchronised across the tags
                        System.out.println("current time: "+all_times.get(index).get(i));
                        BigInteger time = all_times.get(index).get(i);
                        int sec = i;
                        int no = i; // index of coordinate to which we want to set location now
                       
                        
                        try {
                            // sleep for a second
                            Thread.sleep(1000);
                        } catch (InterruptedException ex) {
                            ex.printStackTrace();
                        }
                        
                        // update things on FX thread
                        Platform.runLater(new Runnable() {

                            public void run() {
                                
                                // if checkbox in GUI is not selected, previous position of the tag is removed (default)
                                if (!drawTrace.isSelected()) {
                                    placeTags(no);
                                    removeTag();
                                    updateSlider(sec);
                                }
                                
                                // if checkbox in GUI is selected, previous tag locations are kept on the map
                                // TODO: MAKE IT DRAW THIN LINE BETWEEN THOSE POINTS
                                else if (drawTrace.isSelected()) {
                                    placeTags(no);
                                    updateSlider(sec);
                                }

                            }
                        });
                    }
                }
            }.start();
         
     }

    /**
     * IS NOT USED IN THIS CLASS ANYMORE
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
}

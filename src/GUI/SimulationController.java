package GUI;

import Multilateration.Simulation;
import java.io.File;
import java.io.FileNotFoundException;
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
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;

public class SimulationController {


    @FXML
    HBox imagebox;
    @FXML
    AnchorPane simulation;
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
    double centerX;
    double centerY;

    Simulation nc = new Simulation();

    // ArrayList to hold the geographical coordinates of basestations that will be displayed
    ArrayList<Double[]> basestations = new ArrayList<>();

    // ArrayList holding cartesian plane basestation coords
    ArrayList<Double[]> cartBasestationCoords = new ArrayList<>();

    // hashmap returned after all procedures in Simulation, containing all info:
    // < tagID < timestamp, cartCoord(x,y,z)> >
    HashMap<Long, HashMap<BigInteger, Double[]>> tag_registry;

    // will be the list of actual tags taken from the log file
    //ArrayList<Long> intTags = HelperMethods.deduplicate(MainApplication.logfiles.get(0).getIDs());
    //ArrayList<String> tagIDs = HelperMethods.convertList(intTags);
    ArrayList<Double[]> tagCoords = new ArrayList<>();

    static MapProcessing mp;
    // parameters used for map scaling and tag placement
    Double[] corners;
    double realWidth;
    double realHeight;

    double xratio; // longitude
    double yratio;  // latitude

    int currentTime = 0; // variable to hold current time of the visualisation

    ArrayList<Long> all_tags = new ArrayList<>(); // tag IDs
    ArrayList<ArrayList<BigInteger>> all_times = new ArrayList<>(); // timestamps
    ArrayList<ArrayList<Double[]>> all_coords = new ArrayList<>(); // coordinates

    // ideally should take the smallest value between all tags and time should
    // be progressed from that value
    int simTime = 0;

    public void initialize() throws IOException {

        pane = setupMap(basestations);

        getTagInfo(); // prepares all_tags, all_times and all_coords for later use

        Thread[] threads = new Thread[all_tags.size()]; // creating array to hold threads for every tag?

        for (int i = 0; i < all_tags.size(); i++) {
            System.out.println("number of tags: " + all_tags.size());
            System.out.println("number of times: " + all_times.get(i).size());
            System.out.println("number of coords: " + all_coords.get(i).size());
        }

        VBox leftbox = new VBox();

        HBox visualControl = new HBox();
        visualControl.setMaxWidth(300);
        visualControl.setPadding(new Insets(20, 0, 0, 0));
        visualControl.setSpacing(15);

        Button play = new Button("||");
        play.setPrefSize(25, 25);

        play.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
                if (play.getText().equals("||")) {
                    play.setText(">");
                    // PAUSE VISUALISATION

                    for (Thread t : threads) {
                        t.stop();
                        System.out.println("simTime:" + simTime);
                        // delete all threads
                    }

                } else {
                    play.setText("||");
                    // PLAY VISUALISATION

                    for (int i = 0; i < all_tags.size(); i++) {
                        Thread t = updateTag(i, simTime);
                        threads[i] = t;
                    }
                }
            }
        });

        slider = new Slider();
        slider.setMinWidth(350.0);
        //slider.setPadding(new Insets(20, 0, 0, 0));
        slider.setMin(0);
        slider.setMax(all_times.get(0).size() - 1);   // TODO: should be a MAXIMUM number of locations that tag goes through
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
        slider.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                slider.setValueChanging(true);
                double value = (event.getX() / slider.getWidth()) * slider.getMax();
                int v = (int) Math.rint(value);
                slider.setValue(v);
                simTime = v;
                // it is bad to use stop(), but it works
                // alternative would be using http://www.java67.com/2015/07/how-to-stop-thread-in-java-example.html
                for (Thread t : threads) {
                    t.stop();
                }
                // should be used for all tags
                for (int i = 0; i < all_tags.size(); i++) {
                    Thread t = updateTag(i, v);
                    threads[i] = t;
                }
                slider.setValueChanging(false);
            }
        });

        visualControl.getChildren().addAll(play, slider);

        leftbox.getChildren().addAll(pane, visualControl);

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
        for (Long tag : all_tags) {
            tags.getItems().add(tag + "");
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
        drawTrace = new CheckBox();
        drawTrace.setText("Draw movement patterns");
        drawTrace.setSelected(false);

        rightbox.getChildren().addAll(tags, basestationPanel, regularity, drawTrace, buttons);

        // adding elements to the HBox from FXML file
        imagebox.setPadding(new Insets(20, 10, 10, 20));
        imagebox.getChildren().addAll(leftbox, rightbox);

        // might want to place this in map setup
        // placing tags to their initial locations
        for (int i = 0; i < all_tags.size(); i++) {
            placeTag(i, 0);
        }

        // TODO: if tag is out of the map space, could print out some warning
        // THIS IS WHERE A VISUALISATION THREAD WILL BE STARTED
        // remains for loop, should be changed to traverse tagID list for all tags
        for (int i = 0; i < all_tags.size(); i++) {
            Thread th = updateTag(i, 1);
            threads[i] = th;
        }
    }
    
    
    // getter method to be used from other classes
    public ArrayList<Double[]> getBasestationData() throws SQLException{
        ArrayList<String> basestationNames = UploadController.getSelectedBasestations();
        ArrayList<Double[]> basestationData = new ArrayList<>();
        for (String s: basestationNames) {
            Double[] d = UploadController.collectBasestationData(s);
            basestationData.add(d);
        }
        
        return basestationData;
    }

    /**
     * Function for setting up map - downloading from Google Maps, placing first
     * things
     */
    public Pane setupMap(ArrayList<Double[]> basestations) {

        // downloading static map of the location
        ImageView imageView;

        basestations = nc.getGeoBasestations();

        mp = new MapProcessing(basestations);
        cartBasestationCoords = mp.getBasestations(basestations);
        int zoom = mp.getZoom();
        double[] centerPoints = mp.getCenter(); // {centerY, centerX}

        // main pain - top layer of map where visualisation will happen
        pane = new Pane();

        String imagePath = "https://maps.googleapis.com/maps/api/staticmap?"
                + "center=" + centerPoints[0] + "," + centerPoints[1] + "&"
                + "zoom=" + zoom + "&size=480x280&maptype=terrain&"
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
                            + "2. Or paste some dummy image instead and make the user aware of that.");
                    pane.getChildren().add(l);
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
            pane.getChildren().add(l);
            return pane;
        }

        // here if error at downloading the map occured, probably I don't want to carry on and need to 
        // prompt the user to connect to internet and retry, rather than display everything. 
        // For example, could use return for that.
        imageView = new ImageView(image);

        pane.getChildren().add(imageView);
        pane.setMaxSize(480, 280);

        placeBasestations(cartBasestationCoords);

        return pane;

    }

    public void getTagInfo() {
        tag_registry = nc.simulateLocations();

        // collecting tags
        for (Map.Entry<Long, HashMap<BigInteger, Double[]>> entry : tag_registry.entrySet()) {
            Long key = entry.getKey();
            HashMap<BigInteger, Double[]> value = entry.getValue();
            all_tags.add(key);
            ArrayList<BigInteger> times = new ArrayList<>();
            ArrayList<Double[]> coords = new ArrayList<>();
            for (Map.Entry<BigInteger, Double[]> entry2 : value.entrySet()) {
                BigInteger key2 = entry2.getKey();
                times.add(key2);
                Double[] value2 = entry2.getValue();
                coords.add(value2);
            }

            ArrayList<BigInteger> times_orig = times;
            times_orig = Simulation.makeDeepCopyBigInteger(times);

            ArrayList<Double[]> sorted_coords = new ArrayList<>();
            Collections.sort(times);
            for (int i = 0; i < times.size(); i++) {
                for (int j = 0; j < times.size(); j++) {
                    if (times.get(i) == times_orig.get(j)) {
                        sorted_coords.add(i, coords.get(j));
                        break;
                    }
                }
            }
            all_times.add(times);
            all_coords.add(sorted_coords);
        }
        System.out.println("all_coords");
        System.out.println("From visualisation controller");
        for (ArrayList<Double[]> ar : all_coords) {
            for (Double[] a : ar) {
                System.out.println("x: " + a[0]);
                System.out.println("y: " + a[1]);
                System.out.println("--------------");

            }

        }
        System.out.println("");

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

        // will have to be passed information about basestations, 
        // which will also include the unique name, which I can then assign
        // on a tooltip
        for (Double[] bs : coords) {

            Rectangle r = new Rectangle(bs[0], bs[1], 5, 5);
            newCoords.add(r);

        }

        Group g = new Group();

        for (Rectangle r : newCoords) {
            g.getChildren().add(r);

//            r.hoverProperty().addListener((observable) -> {
//                Tooltip t = new Tooltip(name); // should get tag id and put it as a label
//                Tooltip.install(r, t);
//            });
        }

        pane.getChildren().addAll(g);
    }

    /**
     * This method should remove the last appearance of the tag. Probably only
     * works if we choose to display all tags.
     */
    public void removeTag() {
        // accessing Group object of the Pane that stores rectangles
        // removing the first rectangle 
        pane.getChildren().remove(2);
    }

    public void placeTag(int tagIndex, int timeIndex) {

        ArrayList<Rectangle> tagMarks = new ArrayList<>();

        String name = Long.toString(all_tags.get(tagIndex));

        // getting index position of the tag
        double x = all_coords.get(tagIndex).get(timeIndex)[0];
        double y = all_coords.get(tagIndex).get(timeIndex)[1];

        System.out.println("start coord: " + x + "; " + y);

        // draw only if it fits on the pane
        // if (x <= 480 && y <= 280) {
        Rectangle r = new Rectangle(x, y, 4, 4);
        r.setFill(Color.OLIVE);
        r.hoverProperty().addListener((observable) -> {
            Tooltip t = new Tooltip(name); // should get tag id and put it as a label
            Tooltip.install(r, t);
        });

        tagMarks.add(r);

        Group g = new Group();

        for (Rectangle rec : tagMarks) {
            g.getChildren().add(rec);
        }

        pane.getChildren().add(g);
        //}

    }

    public void updateSlider(int time) {
        slider.setValue(time);
    }

    public Thread updateTag(int index, int t) {

        Thread th = new Thread() {

            // runnable for that thread
            public void run() {
                // this will be for loop for all locations that we have
                for (int i = t; i < all_times.get(index).size(); i++) {
                    //currentTime = i;
                    // TODO: timing will actually need to be synchronised across the tags
                    System.out.println("current time: " + all_times.get(index).get(i));
                    BigInteger time = all_times.get(index).get(i);
                    simTime = i;
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
                                placeTag(index, no);
                                removeTag();
                                updateSlider(no);
                            } // if checkbox in GUI is selected, previous tag locations are kept on the map
                            // TODO: MAKE IT DRAW THIN LINE BETWEEN THOSE POINTS
                            else if (drawTrace.isSelected()) {
                                placeTag(index, no);
                                updateSlider(no);

                                // drawing line between locations
                                Double oldX = all_coords.get(index).get(no - 1)[0];
                                Double oldY = all_coords.get(index).get(no - 1)[1];
                                Double newX = all_coords.get(index).get(no)[0];
                                Double newY = all_coords.get(index).get(no)[1];

                                // if it fits in the pane
                                if (newX <= 480 && newY <= 280) {
                                    Line line = new Line(oldX + 2, oldY + 2, newX + 2, newY + 2);
                                    line.setStroke(Color.OLIVE);
                                    pane.getChildren().add(line);

                                }

                            }

                        }
                    });
                }
            }
        };

        th.start();

        return th;

    }

    public void handleCancel() throws IOException {
        FXMLLoader sceneLoader = new FXMLLoader(getClass().getResource("menu.fxml"));
        Parent sceneParent = sceneLoader.load();
        Scene scene = new Scene(sceneParent, 400, 400);

        Stage stage = (Stage) simulation.getScene().getWindow();
        stage.setScene(scene);
    }

    public void handleExport() throws FileNotFoundException, UnsupportedEncodingException, IOException {
        
               // get date for the name of the file
        DateFormat df = new SimpleDateFormat("dd-MM-yyyy");
        Date today = Calendar.getInstance().getTime();        
        String reportDate = df.format(today);
        
        // get OS for filepath
        String OS = System.getProperty("os.name").toLowerCase();
        String path="";
        if (OS.contains("win")) {
            path = ".\\" + "export"+ reportDate + ".txt";
        }
        if (OS.contains("nix") || OS.contains("nux") ||  
                OS.contains("aix") || OS.contains("mac")){
            path = "./" + "export"+ reportDate + ".txt";
        }
        
        // create new file
        File f = new File(path);
        f.createNewFile();
        
        // write to file
        PrintWriter writer = new PrintWriter("export" + reportDate + ".txt", "UTF-8");
        writer.println("Time,Tag,Latitude,Longitude");
        for (int a=0; a<all_tags.size(); a++) {
            for(int i=0; i<all_times.get(a).size(); i++){
                Double[] temp=new Double[]{all_coords.get(a).get(i)[0],
                                           all_coords.get(a).get(i)[1], 
                                           all_coords.get(a).get(i)[2]};
                Double[] geoCoords=mp.getGeoLoc(temp);
                
                // export tag ID
                writer.print(all_tags.get(a));
                writer.print(',');
                // export time
                writer.print(all_times.get(a).get(i));
                writer.print(',');
                // export coords
                writer.print(geoCoords[0]);
                writer.print(',');
                writer.println(geoCoords[1]);
            }
        }
        writer.close();
    }
}
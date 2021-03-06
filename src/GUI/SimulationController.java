/**
 * This class describes program behaviour when in simulation mode. It runs
 * the visualisation that displays extracted data from simulated log files.
 * It also provides data export functionality to extract analysed data.
 * Has a corresponding data_simulation.fxml file.
 */
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
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.util.StringConverter;

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

    ArrayList<Thread> threads;

    // keeping track of the simulation time
    int simTime = 0;

    ListView<String> basestationPanel;
    ListView<String> tagsPanel;

    public void initialize() throws IOException {

        // get map ready
        pane = setupMap(basestations);

        // preparing all_tags, all_times and all_coords for later use
        getTagInfo(); 

        VBox leftbox = new VBox();
        HBox visualControl = new HBox();
        visualControl.setMaxWidth(400);
        visualControl.setPadding(new Insets(40, 0, 0, 0));
        visualControl.setSpacing(15);

        Button play = new Button("||");
        play.setMinSize(35, 35);

        play.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
                if (play.getText().equals("||")) {
                    play.setText(">");
                    // PAUSE VISUALISATION

                    for (Thread t : threads) {
                        t.stop();
                    }

                    // delete all threads
                    threads.clear();

                } else {

                    play.setText("||");
                    // PLAY VISUALISATION

                    for (int i = 0; i < all_tags.size(); i++) {
                        removeTag();
                    }

                    // get selected tag indeces
                    ObservableList selectedIndices = tagsPanel.getSelectionModel().getSelectedIndices();

                    if (selectedIndices.size() > 0) {

                        for (int i = 0; i < selectedIndices.size(); i++) {
                            int index = (int)selectedIndices.get(i);
                            placeTag(index, simTime - 1);
                            Thread t = updateTag(index, simTime);
                            threads.add(t);
                        }

                    } else {
                        for (int i = 0; i < all_tags.size(); i++) {
                            placeTag(i, simTime - 1);
                            Thread t = updateTag(i, simTime);
                            threads.add(t);
                        }
                    }
                }
            }
        }
        );

        slider = new Slider();

        slider.setMinWidth(350.0);
        slider.setMin(0);
        slider.setMax(all_times.get(0).size() - 1); 
        slider.setValue(0);
        slider.setShowTickLabels(true);
        slider.setShowTickMarks(true);
        int majorTick = 5;
        slider.setMajorTickUnit(5);
        slider.setMinorTickCount(majorTick-1);
        slider.setBlockIncrement(1);

        //  this listener reacts to the change on the slider:
        //  should start visualisation from the point were slider is set to
        slider.setOnMouseClicked(
                new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event
            ) {
                slider.setValueChanging(true);
                double value = (event.getX() / slider.getWidth()) * slider.getMax();
                int v = (int) Math.rint(value);
                slider.setValue(v);
                simTime = v;

                for (Thread t : threads) {
                    t.stop();
                }

                threads.clear();

                for (int i = 0; i < all_tags.size(); i++) {
                    Thread t = updateTag(i, v);
                    threads.add(t);
                }
                slider.setValueChanging(false);
            }
        }
        );

        visualControl.getChildren().addAll(play, slider);

        leftbox.getChildren().addAll(pane, visualControl);

        // adding side panels with lists of tags and basestations
        VBox rightbox = new VBox();

        rightbox.setPadding(new Insets(0, 0, 25, 25));
        rightbox.setPrefSize(250.0, 480);
        rightbox.setSpacing(15.0);

        Label tagName = new Label("Tag IDs");
        tagName.setFont(Font.font(null, FontWeight.BOLD, 12));

        tagsPanel = new ListView<>();

        tagsPanel.setPrefWidth(200.0);

        // populate panel with tag IDs
        for (Long tag : all_tags) {
            tagsPanel.getItems().add(tag + "");
        }

        tagsPanel.getSelectionModel()
                .setSelectionMode(SelectionMode.MULTIPLE);

        tagsPanel.getSelectionModel()
                .selectedItemProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue<? extends String> observable, String oldValue,
                            String newValue
                    ) {

                        ObservableList selectedIndices = tagsPanel.getSelectionModel().getSelectedIndices();

                        if (selectedIndices.size() > 0) {

                            for (int i = 0; i < threads.size(); i++) {
                                threads.get(i).stop();
                            }

                            // remove from the map
                            for (int i = 0; i < threads.size(); i++) {
                                removeTag();
                            }

                            threads.clear();

                            // should be used for all tags
                            for (Object o : selectedIndices) {
                                int tagIndex = (Integer) o;
                                placeTag(tagIndex, simTime - 1);
                                Thread t = updateTag(tagIndex, simTime);
                                threads.add(t);
                            }
                        } // tags were unselected
                        else if (selectedIndices.isEmpty()) {

                            for (Thread t : threads) {
                                t.stop();
                                threads.remove(t);

                            }
                            // reinstantiate all tags and their threads
                            for (int i = 0; i < all_tags.size(); i++) {
                                Thread t = updateTag(i, simTime);
                                threads.add(t);
                            }
                        }
                    }
                }
                );

        Label basestationName = new Label("Basestation names");
        basestationName.setFont(Font.font(null, FontWeight.BOLD, 12));

        basestationPanel = new ListView<>();

        basestationPanel.getItems()
                .addAll("Basestation 1", "Basestation 2", "Basestation 3", "Basestation 4");
        basestationPanel.getSelectionModel()
                .setSelectionMode(SelectionMode.MULTIPLE);
        basestationPanel.setMinHeight(basestationPanel.getItems().size() * 23 + 2);

        drawTrace = new CheckBox();

        drawTrace.setText("Draw movement patterns");
        drawTrace.setSelected(false);

        rightbox.getChildren()
                .addAll(tagName, tagsPanel, basestationName, basestationPanel, drawTrace, buttons);

        // adding elements to the HBox from FXML file
        imagebox.setPadding(
                new Insets(20, 10, 10, 20));
        imagebox.getChildren()
                .addAll(leftbox, rightbox);

        threads = new ArrayList<>();

        // placing tags to their initial locations
        for (int i = 0;
                i < all_tags.size();
                i++) {
            placeTag(i, 0);
        }

        for (int i = 0;
                i < all_tags.size();
                i++) {
            Thread th = updateTag(i, 1);
            threads.add(th);
        }
    }
    
    /**
     * basestation data getter method to me used from other classes.
     * @return ArrayList with basestation coordinates and measured power
     * @throws SQLException 
     */
    public ArrayList<Double[]> getBasestationData() throws SQLException {
        ArrayList<String> basestationNames = UploadController.getSelectedBasestations();
        ArrayList<Double[]> basestationData = new ArrayList<>();
        for (String s : basestationNames) {
            Double[] d = UploadController.collectBasestationData(s);
            basestationData.add(d);
        }

        return basestationData;
    }

    /**
     * Function for setting up map - downloading from Google Maps, 
     * and placing basestations on it.
     * @param basestations 
     * @return new Pane
     */
    public Pane setupMap(ArrayList<Double[]> basestations) {

        // downloading static map of the location
        ImageView imageView;

        basestations = nc.getGeoBasestations();

        mp = new MapProcessing(basestations);
        cartBasestationCoords = mp.getBasestations(basestations);
        int zoom = mp.getZoom();
        double[] centerPoints = mp.getCenter(); // {centerY, centerX}

        // main pane - top layer of map where visualisation will happen
        pane = new Pane();

        String imagePath = "https://maps.googleapis.com/maps/api/staticmap?"
                + "center=" + centerPoints[0] + "," + centerPoints[1] + "&"
                + "zoom=" + zoom + "&size=480x280&maptype=terrain&"
                + "key=AIzaSyD9duo3FCZAGzoydpTGoM2Gwwcba3OXxSs";

        Boolean backgroundLoading = false;
        Image image = new Image(imagePath, backgroundLoading);

        // checking for errors - most likely to happen if there is no internet connection
        if (image.isBackgroundLoading()) {
            image.errorProperty().addListener((prop, oldValue, newValue) -> {
                if (newValue) {
                    Label l = new Label("An error occurred while loading the image.\n"
                            + "Make sure that there is a working internet connection to download the map.\n");
                    pane.getChildren().add(l);
                    return;
                }
            });
        } else if (image.isError()) {
            Label l = new Label("An error occurred while loading the image.\n"
                    + "Make sure that there is a working internet connection to download the map.\n");
            pane.getChildren().add(l);
            return pane;
        }

        imageView = new ImageView(image);

        pane.getChildren().add(imageView);
        pane.setMaxSize(480, 280);

        placeBasestations(cartBasestationCoords);

        return pane;

    }

    /**
     * Run data processing to get resulting locations for all tags that 
     * have enough detections.
     */
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
    }

    /**
     * Placing basestations on the new pane
     * @param coords
     */
    public void placeBasestations(ArrayList<Double[]> coords) {

        ArrayList<Circle> newCoords = new ArrayList<>();

        for (Double[] bs : coords) {

            Circle r = new Circle(bs[0], bs[1], 2.5);
            newCoords.add(r);

        }

        Group g = new Group();

        for (int i = 0; i < newCoords.size(); i++) {
            g.getChildren().add(newCoords.get(i));
        }

        pane.getChildren().addAll(g);
    }

    /**
     * This method should remove the last appearance of the tag. 
     */
    public void removeTag() {
        // accessing Group object of the Pane that stores rectangles
        // removing the first rectangle 
        pane.getChildren().remove(2);
    }

    /**
     * placing a single tag on the pane for given time index
     * @param tagIndex tag index in the tag list
     * @param timeIndex time index for the time list of the tag
     */
    public void placeTag(int tagIndex, int timeIndex) {

        ArrayList<Rectangle> tagMarks = new ArrayList<>();

        String name = Long.toString(all_tags.get(tagIndex));

        // getting index position of the tag
        double x = all_coords.get(tagIndex).get(timeIndex)[0];
        double y = all_coords.get(tagIndex).get(timeIndex)[1];

        Rectangle r = new Rectangle(x, y, 4, 4);
        r.setFill(Color.OLIVE);
        r.hoverProperty().addListener((observable) -> {
            Tooltip t = new Tooltip(name); // putting tag ID as a name
            Tooltip.install(r, t);
        });

        tagMarks.add(r);

        Group g = new Group();

        for (Rectangle rec : tagMarks) {
            g.getChildren().add(rec);
        }

        pane.getChildren().add(g);

    }

    /**
     * updating slider value
     * @param time 
     */
    public void updateSlider(int time) {
        slider.setValue(time);
    }

    /**
     * Method to start a thread for a single tag, starting at the given time t.
     * @param index tag index
     * @param t time
     * @return new Thread
     */
    public Thread updateTag(int index, int t) {

        Thread th = new Thread() {

            // runnable for that thread
            public void run() {
                // this will be for loop for all locations that we have
                for (int i = t; i < all_times.get(index).size(); i++) {
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

    /**
     * handler for 'Cancel' button
     * @throws IOException 
     */
    public void handleCancel() throws IOException {
        FXMLLoader sceneLoader = new FXMLLoader(getClass().getResource("menu.fxml"));
        Parent sceneParent = sceneLoader.load();
        Scene scene = new Scene(sceneParent, 400, 400);

        Stage stage = (Stage) simulation.getScene().getWindow();
        stage.setScene(scene);
    }

    /**
     * handler for 'Export data' button
     * @throws FileNotFoundException
     * @throws UnsupportedEncodingException
     * @throws IOException 
     */
    public void handleExport() throws FileNotFoundException, UnsupportedEncodingException, IOException {

        // get date for the name of the file
        DateFormat df = new SimpleDateFormat("dd-MM-yyyy");
        Date today = Calendar.getInstance().getTime();
        String reportDate = df.format(today);

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save data");
        fileChooser.setInitialFileName("export" + reportDate + ".csv");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("CSV file", "*.csv"),
                new FileChooser.ExtensionFilter("Text file", "*.txt"),
                new FileChooser.ExtensionFilter("Log file", ".log"));
        Stage stage = (Stage) simulation.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);
        if (file != null) {
            String path = file.getAbsolutePath();

            // create new file
            File f = new File(path);
            f.createNewFile();

            // write to file
            PrintWriter writer = new PrintWriter(path, "UTF-8");
            writer.println("Tag,Time,Latitude,Longitude");
            for (int a = 0; a < all_tags.size(); a++) {
                for (int i = 0; i < all_times.get(a).size(); i++) {
                    Double[] temp = new Double[]{all_coords.get(a).get(i)[0],
                        all_coords.get(a).get(i)[1],
                        all_coords.get(a).get(i)[2]};
                    Double[] geoCoords = mp.getGeoLoc(temp);

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
}

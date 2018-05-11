/**
 * This class is responsible for controlling the behaviour of the application when in the data upload window.
 * It allows to select basestations and upload corresponding log files.
 * Has a corresponding data_upload.fxml file.
 */
package GUI;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class UploadController {

    @FXML
    ScrollPane data_upload;

    @FXML
    VBox uploadList;

    CheckBox kalmanCheck;

    @FXML
    VBox uploadBox;

    static boolean applyKalman = false;

    ArrayList<String> bsNames;
    
    static int uploadNumber = 4;

    static ArrayList<String> selectedBs;

    // this arraylist will be used to store data from uploaded log files (processed)
    public static ArrayList<String> logfilePaths = new ArrayList<>();

    public void initialize() throws SQLException {

        // getting registered basestation names from the database
        bsNames = getBasestationNames(); 
        uploadList.setSpacing(5);

        for (int i = 0; i < uploadNumber; i++) {
            VBox newUpload = addUpload(i+1);
            uploadList.getChildren().add(newUpload);
        }

        Label check = new Label("Apply Kalman filter");
        check.setFont(Font.font(null, 14));
        kalmanCheck = new CheckBox();
        
        Button addAnother = new Button("Add another basestation");
        addAnother.setMinSize(80,50);
        addAnother.setStyle("-fx-font-size: 15.0; ");
        
        addAnother.setOnAction(event -> {
                addExtraUpload();
        });

        Button upload = new Button("Run");
        upload.setMinSize(80, 50);
        upload.setStyle("-fx-font-size: 15.0; ");

        upload.setOnAction(event -> {
            try {
                handleRun();
            } catch (IOException ex) {
                Logger.getLogger(UploadController.class.getName()).log(Level.SEVERE, null, ex);
            }
        });

        Button cancel = new Button("Cancel");
        cancel.setMinSize(80, 50);
        cancel.setStyle("-fx-font-size: 15.0; ");

        cancel.setOnAction(event -> {
            try {
                handleCancel();
            } catch (IOException ex) {
                Logger.getLogger(UploadController.class.getName()).log(Level.SEVERE, null, ex);
            }
        });

        Region region1 = new Region();
        HBox.setHgrow(region1, Priority.ALWAYS);

        Region region2 = new Region();
        HBox.setHgrow(region2, Priority.ALWAYS);

        HBox hBox = new HBox(check, kalmanCheck, region1, region2, addAnother, upload, cancel);
        hBox.setSpacing(5);

        uploadBox.getChildren().add(hBox);

    }

    /**
     * getter for boolean if we apply filter or not
     * @return boolean value
     */
    public static boolean doApplyKalman() {
        return applyKalman;
    }

    /**
     * add a single basestation upload window
     * @param basestationNumber
     * @return new VBox
     */
    public VBox addUpload(int basestationNumber) {
        VBox vb = new VBox();
        vb.setSpacing(10);
        vb.setPrefSize(650, 80);

        HBox hb1 = new HBox();
        hb1.setSpacing(5);

        Label l = new Label("Basestation #" + basestationNumber);
        l.setFont(Font.font(null, FontWeight.BOLD, 14));

        hb1.getChildren().addAll(l);

        HBox hb2 = new HBox();
        hb2.setSpacing(10);

        ComboBox comboBox = new ComboBox();

        comboBox.getItems().addAll(bsNames);

        comboBox.setMinSize(140, 30);
        comboBox.setPromptText("Select basestation");

        Button uploadFile = new Button("Choose file...");
        uploadFile.setMinSize(100, 30);
        uploadFile.setStyle("-fx-font-size: 14.0; ");

        uploadFile.setOnAction(event -> {
            handleUpload(event);
        });

        hb2.getChildren().addAll(comboBox, uploadFile);

        vb.getChildren().addAll(hb1, hb2);

        vb.setId("upload" + basestationNumber);

        return vb;
    }
    
    /**
     * add a single extra upload window
     */
    public void addExtraUpload() {
        uploadNumber++;
        VBox newUpload = addUpload(uploadNumber);
        uploadList.getChildren().add(newUpload);
    }

    /**
     * collect all basestation names from the database
     * @return ArrayList of basestation names
     * @throws SQLException 
     */
    public ArrayList<String> getBasestationNames() throws SQLException {

        ArrayList<String> names = new ArrayList<>();

        BasestationDB db = new BasestationDB();
        // make sure the table basestation exists
        db.createNewDatabase("basestation.db");
        // check whether there are any rows in the table
        Connection c = DriverManager.getConnection(db.url);

        Statement stmt = c.createStatement();

        String s = "select name from basestations";
        ResultSet rs = stmt.executeQuery(s);

        while (rs.next()) {
            names.add(rs.getString("name"));
        }
        c.close();

        return names;
    }
    
    /**
     * getter for current number of basestations
     * @return number of basestations
     */
    public static int getBasestationsNumber() {
        return uploadNumber;
    }

    
    /**
     * Method to collect data about the basestation from the database,
     * given its name.
     * @param name basestation name in the database
     * @return array containing: latitude, longitude, measured power
     * @throws SQLException 
     */
    public static Double[] collectBasestationData(String name) throws SQLException {
        Double[] data = new Double[3];

        BasestationDB db = new BasestationDB();
        // make sure the table basestation exists
        db.createNewDatabase("basestation.db");
        // check whether there are any rows in the table
        Connection c = DriverManager.getConnection(db.url);

        Statement stmt = c.createStatement();

        String s = "SELECT latitude, longitude, measuredpower FROM basestations where name='" + name + "'";

        ResultSet rs = stmt.executeQuery(s);

        if (rs.next()) {
            data[0] = rs.getDouble("latitude");
            data[1] = rs.getDouble("longitude");
            data[2] = rs.getDouble("measuredpower");
        }

        c.close();

        return data;
    }

    /**
     * handler for 'Select file' button used to upload a single log file
     * @param event 
     */
    public void handleUpload(ActionEvent event) {

        Button b = (Button) event.getSource();
        HBox currentHBox = (HBox) b.getParent();

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose file...");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Text Document", "*.log", "*.txt", ".csv"));

        // CHECK IF THEY SELECT SOMETHING AND IF NOT - DON'T SAVE IN ARRAYLIST OF PATHS
        Stage stage = (Stage) data_upload.getScene().getWindow();
        File selected = fileChooser.showOpenDialog(stage);

        try {
            String path = selected.getAbsolutePath();
            String filename = selected.getName();

            if (path != null) {
                Label uploaded = new Label("    " + filename);
                uploaded.setFont(new Font("Courier New", 14.0));

                // adding the name of the uploaded file instead of the button
                currentHBox.getChildren().remove(b);
                currentHBox.getChildren().add(uploaded);

                logfilePaths.add(path);
            } else if (path == null) {

            }
        } catch (NullPointerException e) {
        }
    }

    /**
     * getter for all paths to log files.
     * @return ArrayList of paths
     */
    public static ArrayList<String> getPaths() {
        return logfilePaths;
    }

    /**
     * getter for all selected basestation names.
     * @return ArrayList of basestation names
     */
    public static ArrayList<String> getSelectedBasestations() {
        return selectedBs;
    }

    /**
     * Method to check whether the user has Internet connection and show
     * alert if no connection is found.
     * @return boolean value for whether there is Internet connection
     */
    public boolean checkInternet() {
        String imagePath = "https://maps.googleapis.com/maps/api/staticmap?"
                + "center=" + 50 + "," + 20 + "&"
                + "zoom=" + 17 + "&size=480x280&maptype=terrain&"
                + "key=AIzaSyD9duo3FCZAGzoydpTGoM2Gwwcba3OXxSs";

        Boolean backgroundLoading = false;
        Image image = new Image(imagePath, backgroundLoading);

        // checking for errors (from JavaFX book) - most likely to happen if there is no internet connection
        if (image.isBackgroundLoading()) {
            image.errorProperty().addListener((prop, oldValue, newValue) -> {
                if (newValue) {

                }
            });
        } else if (image.isError()) {
            return false;
        }
        return true;
    }

    /**
     * handler for 'Run' button. Starts the visualisation if everything is good.
     * @throws IOException 
     */
    public void handleRun() throws IOException {

        applyKalman = kalmanCheck.isSelected();
        selectedBs = new ArrayList<>();

        boolean internet = checkInternet();

        if (!internet) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Information Dialog");
            alert.setHeaderText(null);
            alert.setContentText("Make sure that you are connected to the Internet, as visualisation requires active Internet connection!");

            alert.showAndWait();
        } else {

            for (Node vb : uploadList.getChildren()) {
                VBox box = (VBox) vb;
                HBox hbox = (HBox) box.getChildren().get(1);
                ComboBox cbox = (ComboBox) hbox.getChildren().get(0);
                selectedBs.add((String) cbox.getValue());
            }

            if (selectedBs.size() >= uploadNumber && logfilePaths.size() >= uploadNumber) {

                FXMLLoader sceneLoader = new FXMLLoader(getClass().getResource("data_visualisation.fxml"));
                Parent sceneParent = sceneLoader.load();
                Scene scene = new Scene(sceneParent, 800, 500);

                Stage stage = (Stage) data_upload.getScene().getWindow();
                stage.setScene(scene);

            } else {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Information Dialog");
                alert.setHeaderText(null);
                alert.setContentText("Log file is required for every basestation.");

                alert.showAndWait();
            }
        }

    }

    /**
     * handler for 'Cancel' button forcing application to go back to the main menu.
     * @throws IOException 
     */
    public void handleCancel() throws IOException {
        FXMLLoader sceneLoader = new FXMLLoader(getClass().getResource("menu.fxml"));
        Parent sceneParent = sceneLoader.load();
        Scene scene = new Scene(sceneParent, 400, 400);

        Stage stage = (Stage) data_upload.getScene().getWindow();
        stage.setScene(scene);
    }
}

package GUI;

import Multilateration.LogData;
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
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class UploadController {

    @FXML
    private ScrollPane data_upload;

    @FXML
    VBox uploadList;
    
    @FXML
    CheckBox kalman;
    
    static boolean applyKalman = false;
    

    ArrayList<String> bsNames;

    static ArrayList<String> selectedBs = new ArrayList<>();

    // this arraylist will be used to store data from uploaded log files (processed)
    public static ArrayList<String> logfilePaths = new ArrayList<>();

    public void initialize() throws SQLException {

        // getting registered basestation names from the database
        bsNames = getBasestationNames();

        for (int i = 1; i < 5; i++) {
            VBox newBasestation = addUpload(i);
            uploadList.getChildren().add(newBasestation);
        }
    }
    
    public static boolean applyKalman() {
        return applyKalman;
    }

    public VBox addUpload(int basestationNumber) {
        VBox vb = new VBox();
        vb.setSpacing(10);
        vb.setPrefSize(600, 80);

        HBox hb1 = new HBox();
        hb1.setSpacing(5);

        Label l = new Label("Basestation #" + basestationNumber);
        l.setFont(Font.font(null, FontWeight.BOLD, 14));

        hb1.getChildren().addAll(l);

        HBox hb2 = new HBox();
        hb2.setSpacing(5);

        ComboBox comboBox = new ComboBox();

        comboBox.getItems().addAll(bsNames);

        comboBox.setMinSize(120, 30);

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

    public ArrayList<String> getBasestationNames() throws SQLException {

        ArrayList<String> names = new ArrayList<>();

        BasestationDB db = new BasestationDB();
        // make sure the table basestation exists
        db.createNewDatabase("basestation.db");
        // check whether there are any rows in the table
        Connection c = DriverManager.getConnection(db.url);

        Statement stmt = c.createStatement();

//        String scount = "select count(*) from basestations;";
//        ResultSet rsc = stmt.executeQuery(scount);
//        // update the number of radios to be euqal to the number of rows we have
//        
//        noBasestations = rsc.getInt("count(*)");
//        System.out.println("noBasestations: " + noBasestations);
        String s = "select name from basestations";
        ResultSet rs = stmt.executeQuery(s);

        while (rs.next()) {
            names.add(rs.getString("name"));
            System.out.println(rs.getString("name"));
        }
        c.close();

        return names;
    }

    //{ latitude, longitude, measured power}
    static public Double[] collectBasestationData(String name) throws SQLException {
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
            System.out.println("path: " + path);

//            LogData converter = new LogData(path, 1, 1, 4, 0);
//            System.out.println(converter.getTimes());
//            System.out.println(converter.getRSSIs());
//            System.out.println(converter.getIDs());
        }
        else if (path==null) {
            
        }
        }
        catch (NullPointerException e){}
    }

    public static ArrayList<String> getPaths() {
        return logfilePaths;
    }

    public static ArrayList<String> getSelectedBasestations() {
        return selectedBs;
    }

    public void handleRun() throws IOException {

        for (Node vb : uploadList.getChildren()) {
            VBox box = (VBox) vb;
            HBox hbox = (HBox) box.getChildren().get(1);
            ComboBox cbox = (ComboBox) hbox.getChildren().get(0);
            selectedBs.add((String) cbox.getValue());
        }

        // TODO check whether there are no nulls or duplicate basestations selected
        if (logfilePaths.size() >= 4) {

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
        
        applyKalman = kalman.isSelected();
    }

    public void handleCancel() throws IOException {
        FXMLLoader sceneLoader = new FXMLLoader(getClass().getResource("menu.fxml"));
        Parent sceneParent = sceneLoader.load();
        Scene scene = new Scene(sceneParent, 400, 400);

        Stage stage = (Stage) data_upload.getScene().getWindow();
        stage.setScene(scene);
    }
}

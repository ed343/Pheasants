package GUI;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class BasestationController {

    @FXML
    ScrollPane basestation_pane;

    @FXML
    VBox basestationList;

    @FXML
    HBox basestationButtons;

    int noBasestations = 0;
    // create new db
    BasestationDB db = new BasestationDB();
    /**
     * Method initialises the register and update beacons window.
     * @throws SQLException in case the connection to the db fails
     */
    public void initialize() throws SQLException {
        // make sure the table basestation exists
        db.createNewDatabase("basestation.db");
        // check whether there are any rows in the table
        Connection c = DriverManager.getConnection(db.url);
        Statement stmt = c.createStatement();
        String s = "select * from basestations";
        ResultSet rs = stmt.executeQuery(s);
        // update the number of radios to be euqal to the number of rows we have
        while (rs.next()) {
            noBasestations++;
        }
        // initially, if there are no rows in the table, we want to display the 
        // minimum of radios needed for multilateration
        if (noBasestations==0){
            noBasestations=4;
        }
        // rerun the statement to restart the cursor
        rs = stmt.executeQuery(s);
        for (int i = 1; i <= noBasestations; i++) { 
            // if there are no rows in the db, do not prepopulate with values
            if (!rs.next()) {
                VBox newBasestation = addBasestation(i);
                basestationList.getChildren().add(newBasestation);
            } else {
            // if there are rows in the db, prepopulate with values
                VBox newBasestation = addBasestationPrepopulated(rs);
                basestationList.getChildren().add(newBasestation);
            }
        }
        c.close();
    }

    @FXML
    /**
     * Method accesses the fields of the GUI and saves them in the basestation 
     * database. It handles both registration and update. 
     * For each radio, these will be 
     * tf_text - the text inputted to the TextField tf, i.e name 
     * te-text - the text inputted to the TextField te, i.e. latitude 
     * te1_text - the text inputted to the TextField te1, i.e. longitude 
     * text_text - the text inputted to the TextField text, i.e. measuredPow
     */
    public void saveBasestation(ActionEvent event) throws SQLException {
        // connect to db
        Connection c = DriverManager.getConnection(db.url);
        Statement stmt = c.createStatement();
        for (int i = 0; i < noBasestations; i++) {
            // get all data from GUI fields
            VBox vb = (VBox) basestationList.getChildren().get(i);
            HBox childsHB = (HBox) vb.getChildren().get(0);
            TextField tf = (TextField) childsHB.getChildren().get(1);
            String tf_text = tf.getText();  // basestation name

            HBox childsHB2 = (HBox) vb.getChildren().get(1);
            TextField tf2 = (TextField) childsHB2.getChildren().get(1);
            TextField tf3 = (TextField) childsHB2.getChildren().get(3);
            String te_text = tf2.getText();  // basestation latitude
            String te1_text = tf3.getText(); // basetsation longitude

            HBox childsHB3 = (HBox) vb.getChildren().get(2);
            TextField tf4 = (TextField) childsHB3.getChildren().get(1);
            String text_text = tf4.getText(); // basestation measured_power

            // check we don't already have this radio saved
            String s = "select * from basestations where name='" + tf_text + "'";
            ResultSet rs = stmt.executeQuery(s);
            // if we do not have the current radio saved, create a row for it
            if (!rs.next()) {
                db.insert(tf_text, Double.parseDouble(te_text), 
                                   Double.parseDouble(te1_text), 
                                   Double.parseDouble(text_text), c);
            }
            // if we already have the radio saved, update its fields
            else{
                db.update(tf_text, Double.parseDouble(te_text), 
                                   Double.parseDouble(te1_text), 
                                   Double.parseDouble(text_text), c);
            }
        }
        c.close();
    }

    @FXML
    /**
     * Method generates a single beacon registration window that can be added to
     * the panel
     *
     */
    public VBox addBasestation(int basestationNumber) {

        VBox vb = new VBox();
        vb.setSpacing(10);
        vb.setPrefSize(600, 120);

        HBox hb1 = new HBox();
        hb1.setSpacing(5);

        Label l = new Label("Name:");
        TextField tf = new TextField("Basestation #" + basestationNumber);

        hb1.getChildren().addAll(l, tf);

        HBox hb2 = new HBox();
        hb2.setSpacing(5);

        Label la = new Label("Latitude coordinate:");
        TextField te = new TextField();
        te.setPrefSize(60, 25);
        Label lab = new Label("Longitude coordinate:");
        TextField te1 = new TextField();
        te1.setPrefSize(60, 25);

        hb2.getChildren().addAll(la, te, lab, te1);

        HBox hb3 = new HBox();
        hb3.setSpacing(5);

        Label labe = new Label("Measured power:");
        TextField text = new TextField();
        text.setPrefSize(60, 25);
        Label label = new Label("db");

        hb3.getChildren().addAll(labe, text, label);

        Separator sep = new Separator();

        vb.getChildren().addAll(hb1, hb2, hb3, sep);

        vb.setId("basestation" + basestationNumber);

        return vb;
    }

    @FXML
    /**
     * Method generates a single beacon registration window that can be added to
     * the panel
     *
     */
    public VBox addBasestationPrepopulated(ResultSet rs) throws SQLException {

        VBox vb = new VBox();
        vb.setSpacing(10);
        vb.setPrefSize(600, 120);

        HBox hb1 = new HBox();
        hb1.setSpacing(5);

        Label l = new Label("Name:");
        TextField tf = new TextField();
        tf.setText(rs.getString("name"));

        hb1.getChildren().addAll(l, tf);

        HBox hb2 = new HBox();
        hb2.setSpacing(5);

        Label la = new Label("Latitude coordinate:");
        TextField te = new TextField();
        te.setPrefSize(60, 25);
        te.setText(rs.getString("latitude"));
        Label lab = new Label("Longitude coordinate:");
        TextField te1 = new TextField();
        te1.setText(rs.getString("longitude"));
        te1.setPrefSize(60, 25);

        hb2.getChildren().addAll(la, te, lab, te1);

        HBox hb3 = new HBox();
        hb3.setSpacing(5);

        Label labe = new Label("Measured power:");
        TextField text = new TextField();
        text.setText(rs.getString("measuredpower"));
        text.setPrefSize(60, 25);
        Label label = new Label("db");

        hb3.getChildren().addAll(labe, text, label);

        Separator sep = new Separator();

        vb.getChildren().addAll(hb1, hb2, hb3, sep);

        vb.setId(rs.getString("name"));

        return vb;

    }

    public void addExtraBasestation() {
        noBasestations++;
        VBox newBasestation = addBasestation(noBasestations);
        basestationList.getChildren().add(newBasestation);
    }

    public void goBack(ActionEvent event) throws IOException {
        FXMLLoader sceneLoader = new FXMLLoader(getClass().getResource("menu.fxml"));
        Parent sceneParent = sceneLoader.load();
        Scene scene = new Scene(sceneParent, 400, 400);

        Stage stage = (Stage) basestation_pane.getScene().getWindow();
        stage.setScene(scene);
    }
}

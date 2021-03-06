/**
 * This class is responsible for controlling the behaviour of the application
 * when in the basestation registration window. It allows to save new basestations
 * in the internal database, as well as update or delete them. Has a corresponding
 * basestations.fxml file.
 */
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
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;

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
     *
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
        if (noBasestations == 0) {
            noBasestations = 4;
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
     * database. It handles both registration and update. For each radio, these
     * will be tf_text - the text inputted to the TextField tf, i.e name te-text
     * - the text inputted to the TextField te, i.e. latitude te1_text - the
     * text inputted to the TextField te1, i.e. longitude text_text - the text
     * inputted to the TextField text, i.e. measuredPow
     */
    public void saveBasestation(ActionEvent event) throws SQLException,
            IOException {
        // connect to db
        Connection c = DriverManager.getConnection(db.url);
        Statement stmt = c.createStatement();
        try {
            for (int i = 0; i < noBasestations; i++) {
                // get all data from GUI fields
                VBox vb = (VBox) basestationList.getChildren().get(i);
                HBox childsHB = (HBox) vb.getChildren().get(0);
                TextField tf = (TextField) childsHB.getChildren().get(1);
                // if this is not a new field, then we are not interested in it 
                // for the bottom save button
                if (tf.isDisabled()) {
                    continue;
                }
                String tf_text = tf.getText();  // basestation name

                HBox childsHB2 = (HBox) vb.getChildren().get(1);
                TextField tf2 = (TextField) childsHB2.getChildren().get(1);
                TextField tf3 = (TextField) childsHB2.getChildren().get(3);
                String te_text = tf2.getText();  // basestation latitude
                String te1_text = tf3.getText(); // basetsation longitude

                HBox childsHB3 = (HBox) vb.getChildren().get(2);
                TextField tf4 = (TextField) childsHB3.getChildren().get(1);
                String text_text = tf4.getText(); // basestation measured_power
                if (!tf_text.equals("") & !te_text.equals("")
                        & !te1_text.equals("")
                        & !text_text.equals("")) {
                    // check we don't already have this radio saved
                    String s = "select * from basestations where name='"
                            + tf_text + "'";
                    ResultSet rs = stmt.executeQuery(s);

                    // if we do not have the current radio saved, 
                    //create a row for it
                    if (!rs.next()) {
                        db.insert(tf_text, Double.parseDouble(te_text),
                                Double.parseDouble(te1_text),
                                Double.parseDouble(text_text), c);
                    } // if we already have the radio saved
                    // alert the user they need to select a different 
                    // basestation name
                    else {
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Information Dialog");
                        alert.setHeaderText(null);
                        alert.setContentText("Please use a unique name for "
                                + "each basestation record."
                        );

                        alert.showAndWait();
                    }

                } // if fields are left blank, ask the user to fill them in
                else {
                    Alert alert = new Alert(AlertType.INFORMATION);
                    alert.setTitle("Information Dialog");
                    alert.setHeaderText(null);
                    alert.setContentText("Please fill in all the fields.");

                    alert.showAndWait();
                }
            }
            // if fewer than 4 basestation are selected to run the program
            // ask the user to select more
        } catch (Exception e) {
            e.printStackTrace();
            if (noBasestations < 4) {
                Alert alert = new Alert(AlertType.INFORMATION);
                alert.setTitle("Information Dialog");
                alert.setHeaderText(null);
                alert.setContentText("Minimum 4 basestations are required "
                        + "to process data.");

                alert.showAndWait();
            } else {
            }

        }
        c.close();
    }

    @FXML
    /**
     * Method generates a single basestation registration window that can be
     * added to the panel.
     *
     * @param basestationNumber - number used in generating VBox ID
     * @return vb - container for single basestation registration
     */
    public VBox addBasestation(int basestationNumber) {

        VBox vb = new VBox();
        vb.setSpacing(10);
        vb.setPrefSize(600, 120);

        HBox hb1 = new HBox();
        hb1.setSpacing(5);

        Label l = new Label("Name:");
        TextField tf = new TextField();

        hb1.getChildren().addAll(l, tf);

        HBox hb2 = new HBox();
        hb2.setSpacing(5);

        Label la = new Label("Latitude/longitude coordinates:");
        TextField te = new TextField();
        te.setPrefSize(70, 25);
        Label slash = new Label("/");
        TextField te1 = new TextField();
        te1.setPrefSize(70, 25);

        hb2.getChildren().addAll(la, te, slash, te1);

        HBox hb3 = new HBox();
        hb3.setSpacing(5);

        Label labe = new Label("Measured power:");
        TextField text = new TextField();
        text.setPrefSize(50, 25);
        Label label = new Label("dBm");

        hb3.getChildren().addAll(labe, text, label);

        Separator sep = new Separator();

        vb.getChildren().addAll(hb1, hb2, hb3, sep);

        vb.setId("basestation" + basestationNumber);

        return vb;
    }

    @FXML
    /**
     * Method generates a single beacon registration window that can be added to
     * the panel. It also specifies functionality for update/save and 
     * delete operations.
     *
     * @param rs - the result set obtained from the database
     * @return VBox - contains the prepopulated GUI elements
     * @throws SQLException
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
        tf.setDisable(true);

        hb1.getChildren().addAll(l, tf);

        HBox hb2 = new HBox();
        hb2.setSpacing(5);

        Label la = new Label("Latitude/longitude coordinates:");
        TextField te = new TextField();
        te.setPrefSize(70, 25);
        te.setText(rs.getString("latitude"));
        te.setDisable(true);
        Label slash = new Label("/");
        TextField te1 = new TextField();
        te1.setText(rs.getString("longitude"));
        te1.setDisable(true);
        te1.setPrefSize(70, 25);

        hb2.getChildren().addAll(la, te, slash, te1);

        HBox hb3 = new HBox();
        hb3.setSpacing(5);

        Label labe = new Label("Measured power:");
        TextField text = new TextField();
        text.setText(rs.getString("measuredpower"));
        text.setDisable(true);
        text.setPrefSize(50, 25);
        Label label = new Label("dBm");

        Button update_save = new Button("Update");

        update_save.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                if (!te.getText().equals("") & !te1.getText().equals("")
                        & !text.getText().equals("")) {
                    if (update_save.getText().equals("Update")) {
                        text.setDisable(false);
                        te1.setDisable(false);
                        te.setDisable(false);
                        // when update gets clicked, the button becomes a save button
                        update_save.setText("Save");
                    } else if (update_save.getText().equals("Save")) {

                        // here we do upate on db
                        try {
                            Connection c = DriverManager.getConnection(db.url);
                            Statement stmt = c.createStatement();
                            db.update(tf.getText(),
                                    Double.parseDouble(te.getText()),
                                    Double.parseDouble(te1.getText()),
                                    Double.parseDouble(text.getText()), c);
                            c.close();
                        } catch (SQLException ex) {
                            Logger.getLogger(BasestationController.class.getName()).
                                    log(Level.SEVERE, null, ex);
                        }

                        text.setDisable(true);
                        te1.setDisable(true);
                        te.setDisable(true);
                        tf.setDisable(true);
                        update_save.setText("Update");

                    }

                } 
                // if not all fields were filled in, ask user to fill them in
                else {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Information Dialog");
                    alert.setHeaderText(null);
                    alert.setContentText("Please fill in all the fields.");

                    alert.showAndWait();
                }
            }

        });

        Button delete = new Button("Delete");
        
        delete.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {

                noBasestations--;

                // here we do delete on db
                try {
                    Connection c = DriverManager.getConnection(db.url);
                    Statement stmt = c.createStatement();
                    db.delete(tf.getText(), c);
                    c.close();
                } catch (SQLException ex) {
                    Logger.getLogger(BasestationController.class.getName()).
                            log(Level.SEVERE, null, ex);
                }

                text.setDisable(false);
                text.setText("");
                te1.setDisable(false);
                te1.setText("");
                te.setDisable(false);
                te.setText("");
                tf.setDisable(false);
                tf.setText("");

                basestationList.getChildren().remove(vb);
            }
        });

        HBox hb4 = new HBox();
        hb4.setPrefSize(400, 35);
        hb4.setSpacing(5);

        hb4.getChildren().addAll(update_save, delete);
        hb4.setAlignment(Pos.BASELINE_RIGHT);

        hb3.getChildren().addAll(labe, text, label, hb4);

        Separator sep = new Separator();

        vb.getChildren().addAll(hb1, hb2, hb3, sep);

        vb.setId(rs.getString("name"));

        return vb;

    }

    /** Method creates a GUI element for adding an extra basestation.
     *
     */
    public void addExtraBasestation() {
        noBasestations++;
        VBox newBasestation = addBasestation(noBasestations);
        basestationList.getChildren().add(newBasestation);
    }

    /** Method handles the go-back functionality. 
     *
     * @param event - the click event
     * @throws IOException
     */
    public void goBack(ActionEvent event) throws IOException {
        FXMLLoader sceneLoader = new FXMLLoader(getClass()
                .getResource("menu.fxml"));
        Parent sceneParent = sceneLoader.load();
        Scene scene = new Scene(sceneParent, 400, 400);

        Stage stage = (Stage) basestation_pane.getScene().getWindow();
        stage.setScene(scene);
    }
}

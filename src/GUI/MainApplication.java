package GUI;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.ArrayList;

public class MainApplication extends javafx.application.Application {

    // this arraylist will be used to store data from uploaded log files (processed)
    public static ArrayList<LogData> logfiles = new ArrayList<>();

    @Override
    public void start(Stage primaryStage) throws Exception{

        Parent root = FXMLLoader.load(getClass().getResource("menu.fxml"));
        primaryStage.setTitle("Pheasants program");
        primaryStage.setScene(new Scene(root, 400, 400));
        primaryStage.centerOnScreen();
        primaryStage.show();

    }

    public static void main(String[] args) {
        launch(args);
    }
}
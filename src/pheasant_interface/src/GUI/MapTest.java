/**
 * NOT PART OF THE APPLICATION, JUST A TEST TO DISPLAY A MAP FROM GOOGLE MAPS API
 */

package GUI;


    import javafx.application.Application;
    import javafx.scene.Scene;
    import javafx.scene.control.Button;
    import javafx.scene.image.Image;
    import javafx.scene.image.ImageView;
    import javafx.scene.layout.HBox;
    import javafx.stage.Stage;

    import java.io.FileInputStream;

public class MapTest extends Application  {


    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("ImageView Experiment 1");

        //FileInputStream input = new FileInputStream("https://maps.googleapis.com/maps/api/staticmap?center=50.728,-3.527&zoom=16&size=640x400&key=AIzaSyD9duo3FCZAGzoydpTGoM2Gwwcba3OXxSs");
        Image image = new Image("https://maps.googleapis.com/maps/api/staticmap?center=50.728,-3.527&zoom=16&size=640x400&key=AIzaSyD9duo3FCZAGzoydpTGoM2Gwwcba3OXxSs");
        ImageView imageView = new ImageView(image);

        HBox hbox = new HBox(imageView);

        Scene scene = new Scene(hbox, 640, 400);
        primaryStage.setScene(scene);
        primaryStage.show();

    }

    public static void main(String[] args) {
        Application.launch(args);
    }

}
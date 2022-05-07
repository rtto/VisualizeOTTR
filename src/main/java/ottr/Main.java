package ottr;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("../sample.fxml"));
        primaryStage.setTitle("OTTR Visualization Tool");
        primaryStage.setScene(new Scene(root, 600, 256));
        primaryStage.setResizable(false);
        primaryStage.show();
    }
}

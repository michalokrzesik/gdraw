package gdraw.main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("main.fxml"));
        Parent root = loader.load();
        MainController controller = loader.getController();
        primaryStage.setTitle("GraphDRAW");
        primaryStage.setScene(new Scene(root, 1280, 700));
        primaryStage.show();
        primaryStage.setOnCloseRequest(controller::closeProgram);
    }


    public static void main(String[] args) {
        launch(args);
    }
}

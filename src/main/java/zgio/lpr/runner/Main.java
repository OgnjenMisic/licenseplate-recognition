package zgio.lpr.runner;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import org.opencv.core.Core;
import zgio.lpr.controller.LprController;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader root = new FXMLLoader(getClass().getResource("/fxml/lpr.fxml"));
        AnchorPane anchorPane = root.load();
        Scene scene = new Scene(anchorPane);
        primaryStage.setTitle("zGio - license plate recognition");
        primaryStage.setScene(scene);
        primaryStage.show();
        try {
            LprController ctrlr = root.getController();
            ctrlr.init();
            primaryStage.setOnCloseRequest(e -> ctrlr.setClosed());
        } catch (Exception ex) {
            System.err.println(ex);
            System.err.println("---------------------");
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        launch(args);
    }
}

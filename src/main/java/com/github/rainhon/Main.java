package com.github.rainhon;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class Main extends Application {
    static FXMLLoader loader = new FXMLLoader();

    @Override
    public void start(Stage primaryStage) throws Exception{
        loader.setLocation(getClass().getResource("/sample.fxml"));
        Parent root = loader.load();
        primaryStage.initStyle(StageStyle.DECORATED);
        primaryStage.setTitle("58工具");
        primaryStage.setScene(new Scene(root, 800, 600));
        primaryStage.show();
        primaryStage.setOnCloseRequest(event->{
            if(RegisterManager.simulateContext != null){
                RegisterManager.simulateContext.proxyManager.saveProxies();
            }
            System.exit(0);
        });
        Controller controller = loader.getController();
        controller.init();
    }


    public static void main(String[] args) {
        launch(args);
    }

    public static FXMLLoader getLoader(){
        return loader;
    }

}

package gdraw.graph.util;

import gdraw.main.MainController;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;

public class MIandButtonPair {
    private MenuItem MI;
    private Button button;

    private MainController controller;

    public MIandButtonPair(MenuItem item, Button button, MainController mainController){
        MI = item;
        this.button = button;
        controller = mainController;
    }

    public void setDisable(boolean value){
        MI.setDisable(value);
        button.setDisable(value);
    }

    public void forceDraw() {
        controller.forceDraw();
    }

    public MainController getController() {
        return controller;
    }
}

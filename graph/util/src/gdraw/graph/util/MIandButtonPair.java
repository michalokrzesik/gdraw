package gdraw.graph.util;

import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;

public class MIandButtonPair {
    private MenuItem MI;
    private Button button;

    public MIandButtonPair(MenuItem item, Button button){
        MI = item;
        this.button = button;
    }

    public void setDisable(boolean value){
        MI.setDisable(value);
        button.setDisable(value);
    }
}

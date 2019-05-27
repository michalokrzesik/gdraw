package gdraw.main;

import gdraw.graph.util.Selectable;
import javafx.scene.shape.Rectangle;

import java.util.ArrayList;

public class Project {

    ArrayList<Selectable> graphObjects;
    ArrayList<Selectable> selected;

    public void checkSelect(double x1, double y1, double x2, double y2) {
        Rectangle rectangle = new Rectangle(x1, y1, x2 - x1, y2 - y1);
        graphObjects.forEach(s -> s.checkSelect(rectangle));
    }

    public void clearSelected() {
        selected.forEach(s -> s.setSelected(false));
        selected.clear();
    }
}

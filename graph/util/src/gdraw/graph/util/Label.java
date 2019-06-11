package gdraw.graph.util;

import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;

import java.io.Serializable;

public class Label implements Serializable {
    private String label;
    private Point2D upperLeft;

    public Label(String label){
        this(label, new Point2D(0,0));
    }

    public Label(String label, Point2D point){
        this.label = label;
        upperLeft = point;
    }

    private void draw(GraphicsContext gc) {
        gc.strokeText(label, upperLeft.getX(), upperLeft.getY());
    }

    public void setLabel(String newLabel) {
        label = newLabel;
    }

    public String getLabel(){
        return label;
    }

    public void setUpperLeft(Point2D point){
        upperLeft = point;
    }

    public Point2D getUpperLeft(){
        return upperLeft;
    }
}

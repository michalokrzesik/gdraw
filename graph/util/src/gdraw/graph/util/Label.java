package gdraw.graph.util;

import javafx.geometry.Point2D;
import javafx.scene.layout.Pane;

import java.io.Serializable;

public class Label implements Serializable {
    private String label;
    private Point2D upperLeft;
    private Pane pane;
    private javafx.scene.control.Label object;

    public Label(String label, Pane pane){
        this(label, pane, new Point2D(0,0));
    }

    public Label(String label, Pane pane, Point2D point){
        this.label = label;
        this.pane = pane;
        upperLeft = point;
        object = new javafx.scene.control.Label(label);
        object.setLayoutX(upperLeft.getX());
        object.setLayoutY(upperLeft.getY());
        draw();
    }

    public void draw() {
        if(!pane.getChildren().contains(object)) pane.getChildren().add(object);
        object.toFront();
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

    public void hide() {
        if(object != null) object.toBack();
    }
}

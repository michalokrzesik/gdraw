package gdraw.graph.util;

import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.canvas.GraphicsContext;

import java.io.Serializable;

public class Label implements Serializable {
    private String label;
    private Point2D upperLeft;
    private Group group;
    private javafx.scene.control.Label object;

    public Label(String label, Group group){
        this(label, group, new Point2D(0,0));
    }

    public Label(String label, Group group, Point2D point){
        this.label = label;
        this.group = group;
        upperLeft = point;
        object = new javafx.scene.control.Label(label);
        object.setLayoutX(upperLeft.getX());
        object.setLayoutY(upperLeft.getY());
        draw();
    }

    public void draw() {
        group.getChildren().add(object);
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
        group.getChildren().remove(object);
    }
}

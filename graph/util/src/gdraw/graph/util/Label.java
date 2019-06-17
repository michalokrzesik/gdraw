package gdraw.graph.util;

import javafx.geometry.Point2D;

import javafx.scene.canvas.Canvas;
import java.io.Serializable;

public class Label implements Serializable {
    private String label;
    private Point2D upperLeft;
    private Canvas canvas;
//    private javafx.scene.control.Label object;

    public Label(String label, Canvas canvas){
        this(label, canvas, new Point2D(0,0));
    }

    public Label(String label, Canvas canvas, Point2D point){
        this.label = label;
        this.canvas = canvas;
        upperLeft = point;
//        object = new javafx.scene.control.Label(label);
//        object.setLayoutX(upperLeft.getX());
//        object.setLayoutY(upperLeft.getY());
        draw();
    }

    public void draw() {
//        if(!pane.getChildren().contains(object)) pane.getChildren().add(object);
//        object.toFront();
        canvas.getGraphicsContext2D().strokeText(label, upperLeft.getX(), upperLeft.getY());
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

//    public void hide() {
//
//       if(object != null) object.toBack();
//    }
}

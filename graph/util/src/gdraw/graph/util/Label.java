package gdraw.graph.util;

import gdraw.main.Project;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.awt.geom.Point2D;
import java.io.Serializable;

public class Label implements Serializable {
    private String label;
    private Point2D.Double upperLeft;
    private transient Canvas canvas;
    private boolean hidden;
//    private javafx.scene.control.Label object;

    public Label(String label, Canvas canvas){
        this(label, canvas, new Point2D.Double(0, 0));
    }

    public Label(String label, Canvas canvas, Point2D.Double point){
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
        if(hidden) return;
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1);
        gc.strokeText(label, upperLeft.getX(), upperLeft.getY());
    }

    public void setLabel(String newLabel) {
        label = newLabel;
    }

    public String getLabel(){
        return label;
    }

    public void setUpperLeft(Point2D.Double point){
        upperLeft = point;
    }

    public Point2D.Double getUpperLeft(){
        return upperLeft;
    }

    public void refresh(Project project) {
        canvas = project.getCanvas();
    }

    public void hide(boolean hide) {
        hidden = hide;
//       if(object != null) object.toBack();
    }
}

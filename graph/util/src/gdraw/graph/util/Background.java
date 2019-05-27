package gdraw.graph.util;

import gdraw.graph.node.Node;
import gdraw.main.MainController;

import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

public class Background extends Node {

    private Canvas canvas;
    private MainController controller;
    private double x, y;

    public Background(MainController mainController, Canvas canvas, Image image, Group group, double w, double h){
        this(new Point2D(w/2, h/2), image, group);
        controller = mainController;
        canvas.setWidth(w);
        canvas.setHeight(h);
        canvas.setOnMouseClicked(e -> setSelected(true));
        canvas.setOnContextMenuRequested(e -> {
            //TODO context menu
            //TODO change background image
        });
        canvas.setOnMousePressed(e -> {
            setSelected(true);
            x = e.getX();
            y = e.getY();
        });
        canvas.setOnMouseDragged(e -> {
            draw();
            GraphicsContext gc = canvas.getGraphicsContext2D();
            gc.setStroke(Color.BLANCHEDALMOND);
            gc.strokeRect(x, y, e.getX() - x, e.getY() - y);
        });
        canvas.setOnMouseReleased(e -> {
            draw();
            mainController.select(x, y, e.getX(), e.getY());
        });

        this.canvas = canvas;
    }

    public Background(Point2D center, Image image, Group group) {
        super(center, image, group);
    }

    @Override
    public void setSelected(boolean selected){ controller.clearSelected(); }

    @Override
    public void draw(){
        canvas.getGraphicsContext2D().drawImage(image, 0, 0, canvas.getWidth(), canvas.getHeight());
    }

    public void setImage(Image newImage){ image = newImage; }
}

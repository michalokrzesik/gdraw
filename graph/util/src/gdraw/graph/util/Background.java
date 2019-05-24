package gdraw.graph.util;

import gdraw.graph.node.Node;
import gdraw.main.MainController;

import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;

public class Background extends Node {

    private Canvas canvas;
    private MainController controller;

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
            //TODO rectangle select
        });
        canvas.setOnMouseDragged(e -> {
            //TODO
        });
        canvas.setOnMouseReleased(e -> {
            //TODO
        });

        this.canvas = canvas;
    }

    public Background(Point2D center, Image image, Group group) {
        super(center, image, group);
    }

    @Override
    public void setSelected(boolean selected){ controller.clearSelected(); }

    public void setImage(Image newImage){ image = newImage; }
}

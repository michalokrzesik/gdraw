package gdraw.graph.util;

import gdraw.graph.node.Node;
import gdraw.main.MainController;

import gdraw.main.Project;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

public class Background extends Node {

    transient private Canvas canvas;
    private double x, y;

    public Background(MainController mainController, Canvas canvas, Image image, Group group, double w, double h){
        this(new Point2D(w/2, h/2), image, group, mainController);
        width = w;
        height = h;
        setCanvas(canvas);
    }

    private void setCanvas(Canvas canvas){
        canvas.setWidth(width);
        canvas.setHeight(height);
        canvas.getGraphicsContext2D().setStroke(Color.BLANCHEDALMOND);
        canvas.setOnMouseClicked(e -> setSelected(true));
        canvas.setOnContextMenuRequested(controller::contextMenu);
        canvas.setOnMousePressed(e -> {
            setSelected(true);
            x = e.getX();
            y = e.getY();
        });
        canvas.setOnMouseDragged(e -> {
            draw();
            canvas.getGraphicsContext2D().strokeRect(x, y, e.getX() - x, e.getY() - y);
        });
        canvas.setOnMouseReleased(e -> {
            draw();
            controller.select(x, y, e.getX(), e.getY());
        });

        this.canvas = canvas;
        draw();
        this.treeItem = new TreeItem<>(this);
        this.treeItem.setGraphic(canvas);
    }

    private Background(Point2D center, Image image, Group group, MainController mainController) {
        super(center, image, group, null, mainController);
    }

    @Override
    public void setSelected(boolean selected){ controller.clearSelected(); }

    @Override
    public void draw(){
        canvas.getGraphicsContext2D().drawImage(image, 0, 0, canvas.getWidth(), canvas.getHeight());
    }

    public void setImage(Image newImage){ image = newImage; }

    @Override
    public boolean isNode(){ return false; }

    @Override
    public void setParent(){
        treeItem.getChildren().forEach(c -> subNodes.add(c.getValue()));
        subNodes.forEach(Node::setParent);
    }

    @Override
    public void refresh(Project project){
        superRefresh(project);
        canvas = new Canvas();
        setCanvas(canvas);
        subNodes.forEach(n -> n.refresh(project));
    }
}

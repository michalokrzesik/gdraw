package gdraw.graph.util;

import gdraw.graph.node.Node;
import gdraw.graph.util.action.SelectableCreationListener;
import gdraw.main.MainController;

import gdraw.main.Project;
import javafx.collections.ObservableList;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class Background extends Node {
    private double x, y;
    private transient Rectangle selection;

    public Background(MainController mainController, Image image, Group group, double w, double h){
        this(new Point2D(w/2, h/2), image, group, mainController);
        treeItem = new TreeItem<>(this);
        setCreationListener(new SelectableCreationListener(this));
        width = w;
        height = h;
        makeImageView();
    }

    private void makeImageView(){
        imageView = new ImageView(image);
        imageView.setFitWidth(width);
        imageView.setFitHeight(height);
        imageView.setOnMouseClicked(e -> setSelected(true));
        imageView.setOnContextMenuRequested(controller::contextMenu);
        imageView.setOnMousePressed(e -> {
            setSelected(true);
            x = e.getX();
            y = e.getY();
        });
        imageView.setOnMouseDragged(e -> {
            draw();
            selection = new Rectangle(x, y, e.getX() - x, e.getY() - y);
            selection.setStroke(Color.BLANCHEDALMOND);
            group.getChildren().add(selection);
        });
        imageView.setOnMouseReleased(e -> {
            controller.select(selection);
            draw();
        });
        group.getChildren().add(imageView);
        group.setLayoutX(0);
        group.setLayoutY(0);

        draw();
        this.treeItem = new TreeItem<>(this);
        ImageView graphic = new ImageView(image);
        graphic.setFitWidth(10);
        graphic.setFitHeight(10);
        this.treeItem.setGraphic(graphic);
        label = new Label("Tło", group);
        label.hide();

    }

    private Background(Point2D center, Image image, Group group, MainController mainController) {
        super(center, image, group, null, mainController);
        setCreationListener(new SelectableCreationListener(this));
    }

    @Override
    public void setSelected(boolean selected){ controller.clearSelected(); }

    @Override
    public void draw(){
        group.setLayoutX(0);
        group.setLayoutY(0);
        imageView.setX(0);
        imageView.setY(0);
        imageView.setImage(image);
        if(selection != null){
            group.getChildren().remove(selection);
            selection = null;
        }
    }

    public void setImage(Image newImage){ image = newImage; }

    @Override
    public boolean isNode(){ return false; }

    @Override
    public void setParent(){
        ObservableList<TreeItem<Node>> children = treeItem.getChildren();
        if(!children.isEmpty()) children.forEach(c -> subNodes.add(c.getValue()));
        if(!subNodes.isEmpty()) subNodes.forEach(Node::setParent);
    }

    @Override
    public void refresh(Project project){
        superRefresh(project);
        makeImageView();
        if(!subNodes.isEmpty())
            subNodes.forEach(n -> n.refresh(project));
    }
}

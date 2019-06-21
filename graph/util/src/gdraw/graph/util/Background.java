package gdraw.graph.util;

import gdraw.graph.node.Node;
import gdraw.graph.util.action.SelectableCreationListener;
import gdraw.main.MainController;

import gdraw.main.Project;
import javafx.collections.ObservableList;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.TreeItem;
import javafx.scene.effect.BlendMode;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Rectangle;

import java.awt.geom.Point2D;
import java.io.FileWriter;
import java.io.IOException;

public class Background extends Node {
    private double x, y;
    private transient Rectangle selection;

    public Background(MainController mainController, Image image, Canvas canvas, double w, double h){
        this(new Point2D.Double(w/2, h/2), image, canvas, mainController);
        treeItem = new TreeItem<>(this);
        setCreationListener(new SelectableCreationListener(this));
        width = w;
        height = h;
        this.canvas = canvas;
        makeImageView();
        draw();
    }

    @Override
    protected Node parentForIsCloserThan(){
        return this;
    }

    private void makeImageView(){
//        imageView = new ImageView(image);
//        imageView.setFitWidth(width);
//        imageView.setFitHeight(height);
//        imageView = new Canvas(width, height);
//        imageView.setOnMouseClicked(e -> setSelected(true));
//        imageView.setOnContextMenuRequested(controller::contextMenu);
//        imageView.setOnMousePressed(e -> {
//            setSelected(true);
//            x = e.getX();
//            y = e.getY();
//        });
//        imageView.setOnMouseDragged(e -> {
//            draw();
//            double xmin = Double.min(x, e.getX()), ymin = Double.min(y, e.getY());
//            selection = new Rectangle(xmin, ymin, Math.abs(e.getX() - x), Math.abs(e.getY() - y));
//            selection.setStroke(Color.BLANCHEDALMOND);
//            selection.setFill(Color.TRANSPARENT);
//            pane.getChildren().add(selection);
//            selection.toFront();
//        });
//        imageView.setOnMouseReleased(e -> {
//            if(selection == null) return;
//            controller.select(selection);
//            draw();
//        });
//        pane.getChildren().add(imageView);
//        imageView.toBack();
//        pane.setLayoutX(0);
//        pane.setLayoutY(0);
//
//        draw();
        this.treeItem = new TreeItem<>(this);
        ImageView graphic = new ImageView(image);
        graphic.setFitWidth(10);
        graphic.setFitHeight(10);
        this.treeItem.setGraphic(graphic);
        label = new Label("Tło", canvas);
//        label.hide();

    }

    private Background(Point2D.Double center, Image image, Canvas canvas, MainController mainController) {
        super(center, image, canvas, mainController, false);
        setCreationListener(new SelectableCreationListener(this));
    }

    @Override
    public void setSelected(boolean selected){ controller.clearSelected(); }

    @Override
    public void draw(){
//        imageView.setX(0);
//        imageView.setY(0);
//        imageView.setImage(image);

        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setGlobalBlendMode(BlendMode.SRC_OVER);
        gc.drawImage(image, 0, 0, width, height);
        selection = null;

        if(!subNodes.isEmpty()) subNodes.forEach(Node::draw);
    }

    public void setImage(Image newImage){
        image = newImage;
//        imageView.setImage(image);
        draw();
    }

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

    @Override
    public void writeToFile(FileWriter writer, boolean json, int indent) throws IOException {
        String ind = indent(indent);
        writer.append(ind + (json ? "\"nodes\": [\n" : "< Nodes >\n"));
        if(!subNodes.isEmpty()) subNodes.forEach(node -> {
            try {
                node.writeToFile(writer, json, indent + 1);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        writer.append(ind + (json ? "]\n" : "< /Nodes >\n"));
    }
}

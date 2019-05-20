package gdraw.graph.node;

import gdraw.graph.vertex.ArrowType;
import gdraw.graph.vertex.LineType;
import gdraw.graph.vertex.VertexPoint;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.geometry.Point2D;
import java.util.ArrayList;

import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

import gdraw.graph.vertex.Vertex;
import gdraw.graph.util.Label;

public class Node {
    public static Image NONE;
    private Canvas canvas;
    private GraphicsContext gc;
    private Point2D center;
    private double width;
    private double height;
    private Image image;
    private boolean isGroup;
    private ArrayList<Node> subNodes;
    private ArrayList<Vertex> vertices;
    private Label label;
    private boolean isCollapsed;
    private double widthCollapsed;
    private double heightCollapsed;
    private boolean selected;

    public Node(Point2D center, Image image, GraphicsContext graphicsContext){
        this.center = center;
        this.image = image;
        width = image.getWidth();
        height = image.getHeight();
        widthCollapsed = width;
        heightCollapsed = height;
        isCollapsed = false;
        isGroup = false;
        vertices = new ArrayList<>();
        gc = graphicsContext;
        canvas = gc.getCanvas();
        canvas.setOnMouseClicked(e -> setSelected(true));
        canvas.setOnContextMenuRequested(e -> {

        });
        ImageView view = new ImageView(image);
        view.setOnMousePressed(e -> {

        });
        selected = true;
    }

    public Node(Point2D center, Image image, GraphicsContext gc, boolean isGroup){
        this(center, image, gc);
        this.isGroup = isGroup;
        if(isGroup) subNodes = new ArrayList<>();
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
        draw();
    }

    public void setLabel(String newLabel){
        if(label == null){
            label = new Label(
                    newLabel,
                    new Point2D(center.getX() - 10, center.getY() - 10)
            );
        }
        else label.setLabel(newLabel);
    }

    public void newVertex(Point2D start, Point2D stop, Node toNode, ArrowType arrow, LineType line, boolean isDuplex, boolean isCurved, double width, Paint color){
        VertexPoint startVP = new VertexPoint(start), stopVP = new VertexPoint(stop);
        startVP.setPointBounded(start, this);
        stopVP.setPointBounded(stop, toNode);
        vertices.add(
                new Vertex(this, toNode, startVP.getPoint(), stopVP.getPoint(), gc, arrow, line, isDuplex, isCurved, width, color)
        );
    }

    public void group(){
        isGroup = true;
        if(subNodes == null) subNodes = new ArrayList<>();
    }

    public void group(ArrayList<Node> nodes){
        group();
        subNodes.addAll(nodes);
    }

    public void group(Node node){
        group();
        subNodes.add(node);
    }

    public ArrayList<Node> changeGroupToNode(){
        if(!isGroup) return null;
        ArrayList<Node> ret = subNodes;
        subNodes = null;
        isGroup = false;
        return ret;
    }

    public void unGroup(ArrayList<Node> nodes){
        if(isGroup) subNodes.removeAll(nodes);
    }

    public void unGroup(Node node){
        if(isGroup) subNodes.remove(node);
    }


    public Point2D getCenter() {
        return center;
    }

    public double getHeight() {
        return isCollapsed ? heightCollapsed : height;
    }

    public void setHeight(double h){
        if(isCollapsed) heightCollapsed = h;
        else height = h;
    }

    public double getWidth() {
        return isCollapsed ? widthCollapsed : width;
    }

    public void setWidth(double w){
        if(isCollapsed) widthCollapsed = w;
        else width = w;
    }

    public void draw(){
        vertices.forEach((Vertex v) -> v.draw(this));

        double w = getWidth(), h = getHeight();
        double x = center.getX() - w/2, y = center.getY() - h/2;
        canvas.setWidth(w+2);
        canvas.setHeight(h+2);
        canvas.setLayoutX(x);
        canvas.setLayoutY(y);
        x = 1; y = 1;
        gc.drawImage(image, x, y, w, h);
        if(selected){
            gc.setLineWidth(1);
            gc.setStroke(Color.BLUE);
            gc.strokeRect(x, y, w, h);

            gc.fillOval(x - 1, y - 1, 3, 3);
            gc.fillOval(x - 1, y + h/2 - 1, 3, 3);
            gc.fillOval(x - 1, y + h - 1, 3, 3);
            gc.fillOval(x + w/2 - 1, y + h - 1, 3, 3);
            gc.fillOval(x + w - 1, y + h - 1, 3, 3);
            gc.fillOval(x + w - 1, y + h/2- 1, 3, 3);
            gc.fillOval(x + w - 1, y - 1, 3, 3);
            gc.fillOval(x + w/2 - 1, y - 1, 3, 3);
        }

        if(isGroup) subNodes.forEach((Node n) -> n.draw());
    }

    public void translate(double dx, double dy){
        double x = center.getX() + dx, y = center.getY() + dy;
        center = new Point2D(x, y);
        vertices.forEach((Vertex v) -> v.translateNode(this, dx, dy));
        if(isGroup) subNodes.forEach((Node n) -> n.translate(dx, dy));
    }
}

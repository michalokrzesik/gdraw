package gdraw.graph.node;

import gdraw.graph.util.Selectable;
import gdraw.graph.vertex.ArrowType;
import gdraw.graph.vertex.LineType;
import gdraw.graph.vertex.VertexPoint;
import javafx.scene.Group;
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
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

public class Node implements Selectable {
    private ImageView imageView;
    protected Point2D center;
    protected double width;
    protected double height;
    protected Image image;
    protected boolean isGroupNodes;
    protected ArrayList<Node> subNodes;
    protected ArrayList<Vertex> vertices;
    protected Label label;
    protected Group group;
    protected boolean isCollapsed;
    protected double widthCollapsed;
    protected double heightCollapsed;
    protected boolean selected;
    private Circle[] circles = new Circle[8];
    private boolean hidden;

    public Node(Point2D center, Image image, Group group){
        this.center = center;
        this.image = image;
        hidden = true;
        width = image.getWidth();
        height = image.getHeight();
        widthCollapsed = width;
        heightCollapsed = height;
        isCollapsed = false;
        isGroupNodes = false;
        vertices = new ArrayList<>();
        this.group = group;
        imageView = new ImageView(image);
        imageView.setOnMouseClicked(e -> setSelected(true));
        imageView.setOnContextMenuRequested(e -> {
//TODO
        });
        ImageView view = new ImageView(image);
        view.setOnMousePressed(e -> {
//TODO
        });
        setSelected(true);

        setCircles();
        for(int i = 0; i < circles.length; i++){
            circles[i].setFill(Color.BLUE);
            circles[i].setStroke(Color.BLUE);
            circles[i].setRadius(3);
            int finalI = i;
            circles[i].setOnMouseDragged(e -> CircleHelper.move(this, finalI, e.getX() - circles[finalI].getCenterX(), e.getY() - circles[finalI].getCenterY()));
        }

    }

    private void setCircles() {
        double w = getWidth(), h = getHeight();
        double x = center.getX() - w/2, y = center.getY() - h/2;
        circles[0].setCenterX(x); circles[0].setCenterY(y);
        circles[1].setCenterX(x); circles[1].setCenterY(y + h/2);
        circles[2].setCenterX(x); circles[2].setCenterY(y + h);
        circles[3].setCenterX(x + w/2); circles[3].setCenterY(y + h);
        circles[4].setCenterX(x + w); circles[4].setCenterY(y + h);
        circles[5].setCenterX(x + w); circles[5].setCenterY(y + h/2);
        circles[6].setCenterX(x + w); circles[6].setCenterY(y);
        circles[7].setCenterX(x + w/2); circles[7].setCenterY(y);
    }

    public Node(Point2D center, Image image, Group group, boolean isGroupNodes){
        this(center, image, group);
        this.isGroupNodes = isGroupNodes;
        if(isGroupNodes) subNodes = new ArrayList<>();
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

    public Node copyWithVertices(boolean copyWithToNodes, Vertex... vertices){
        Node ret = new Node(new Point2D(center.getX() + 5, center.getY() + 5), image, group);
        for(Vertex vertex : vertices){
            if(this.vertices.contains(vertex)){
                Node toNode = (copyWithToNodes ? vertex.getToNode().copyWithVertices(false) : vertex.getToNode());
                ret.addVertex(new Vertex(ret, toNode, vertex));
            }

        }
        return ret;
    }

    public void addVertex(Vertex vertex){
        if(!vertices.contains(vertex)) vertices.add(vertex);
    }

    public void removeVertex(Vertex vertex){
        vertices.remove(vertex);
    }

    public void newVertex(Point2D start, Point2D stop, Node toNode, ArrowType arrow, LineType line, boolean isDuplex, boolean isCurved, double width, Paint color){
        VertexPoint startVP = new VertexPoint(start), stopVP = new VertexPoint(stop);
        startVP.setPointBounded(start, this);
        stopVP.setPointBounded(stop, toNode);
        vertices.add(
                new Vertex(this, toNode, startVP.getPoint(), stopVP.getPoint(), group, arrow, line, isDuplex, isCurved, width, color)
        );
    }

    public void groupNodes(){
        isGroupNodes = true;
        if(subNodes == null) subNodes = new ArrayList<>();
    }

    public void groupNodes(ArrayList<Node> nodes){
        groupNodes();
        subNodes.addAll(nodes);
    }

    public void groupNodes(Node node){
        groupNodes();
        subNodes.add(node);
    }

    public ArrayList<Node> changeGroupToNode(){
        if(!isGroupNodes) return null;
        ArrayList<Node> ret = subNodes;
        subNodes = null;
        isGroupNodes = false;
        return ret;
    }

    public void unGroup(ArrayList<Node> nodes){
        if(isGroupNodes) subNodes.removeAll(nodes);
    }

    public void unGroup(Node node){
        if(isGroupNodes) subNodes.remove(node);
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
        hide();
        vertices.forEach((Vertex v) -> v.draw(this));
        group.getChildren().add(imageView);
        hidden = false;

        double w = getWidth(), h = getHeight();
        double x = center.getX() - w/2, y = center.getY() - h/2;
        imageView.setFitWidth(w);
        imageView.setFitHeight(h);
        imageView.setLayoutX(x);
        imageView.setLayoutY(y);
        if(selected){
            setCircles();
            group.getChildren().addAll(circles);
        }
        else{
            group.getChildren().removeAll(circles);
        }

        if(isGroupNodes) subNodes.forEach((Node n) -> n.draw());
    }

    private void hide() {
        if(!hidden) {
            group.getChildren().remove(imageView);
            subNodes.forEach(node -> node.hide());
            hidden = true;
        }
    }

    public void translate(double dx, double dy){
        double x = center.getX() + dx, y = center.getY() + dy;
        center = new Point2D(x, y);
        vertices.forEach((Vertex v) -> v.translateNode(this, dx, dy));
        if(isGroupNodes) subNodes.forEach((Node n) -> n.translate(dx, dy));
    }

    @Override
    public void checkSelect(Rectangle rectangle) {
        double w = getWidth()/2, h = getHeight()/2;
        setSelected(rectangle.contains(center.getX() - w, center.getY() - h)
                && rectangle.contains(center.getX() + w, center.getY() + h));
    }
}

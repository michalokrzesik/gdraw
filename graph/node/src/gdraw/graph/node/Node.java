package gdraw.graph.node;

import gdraw.graph.vertex.ArrowType;
import gdraw.graph.vertex.LineType;
import gdraw.graph.vertex.VertexPoint;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.geometry.Point2D;
import java.util.ArrayList;

import gdraw.graph.vertex.Vertex;
import gdraw.graph.util.Label;

public class Node {
    public static Image NONE;
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
    }

    public Node(Point2D center, Image image, GraphicsContext gc, boolean isGroup){
        this(center, image, gc);
        this.isGroup = isGroup;
        if(isGroup) subNodes = new ArrayList<>();
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

    public void newVertex(Point2D start, Point2D stop, Node toNode, ArrowType arrow, LineType line, boolean isDuplex, boolean isCurved, double width){
        VertexPoint startVP = new VertexPoint(start), stopVP = new VertexPoint(stop);
        startVP.setPointBounded(start, this);
        stopVP.setPointBounded(stop, toNode);
        vertices.add(
                new Vertex(this, toNode, startVP.getPoint(), stopVP.getPoint(), gc, arrow, line, isDuplex, isCurved, width)
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

    public Point2D getCenter() {
        return center;
    }

    public double getHeight() {
        return isCollapsed ? heightCollapsed : height;
    }

    public double getWidth() {
        return isCollapsed ? widthCollapsed : width;
    }
}

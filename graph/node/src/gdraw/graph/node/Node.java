package gdraw.graph.node;

import javafx.scene.image.Image;
import javafx.geometry.Point2D;
import java.util.ArrayList;

import gdraw.graph.vertex.Vertex;
import gdraw.graph.util.Label;

public class Node {
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

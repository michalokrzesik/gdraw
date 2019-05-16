package gdraw.graph.vertex;

import gdraw.graph.node.Node;
import javafx.geometry.Point2D;

import java.util.LinkedList;

public class Vertex {

    private ArrowType arrowType;
    private LineType lineType;
    private Node fromNode;
    private Node toNode;
    private LinkedList<VertexPoint> points;
    private boolean duplex;
    private boolean curved;

    public Vertex(Node from, Node to, ArrowType arrow, LineType line, Point2D fromPoint, Point2D toPoint, boolean isDuplex, boolean isCurved){
        fromNode = from;
        toNode = to;
        arrowType = arrow;
        lineType = line;
        points = new LinkedList<>();
        points.addFirst(new VertexPoint(fromPoint));
        points.addLast(new VertexPoint(toPoint));
        duplex = isDuplex;
        curved = isCurved;
    }




}

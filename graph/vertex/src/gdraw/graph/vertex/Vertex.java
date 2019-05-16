package gdraw.graph.vertex;

import gdraw.graph.node.Node;
import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;

import java.util.Iterator;
import java.util.LinkedList;

public class Vertex {

    private ArrowType arrowType;
    private LineType lineType;
    private Node fromNode;
    private Node toNode;
    private LinkedList<VertexPoint> points;
    private boolean duplex;
    private VertexType vertexType;
    private boolean selected;
    private GraphicsContext gc;
    private double width;
    private Path path;

    public Vertex(Node from, Node to, Point2D fromPoint, Point2D toPoint, GraphicsContext graphicsContext, ArrowType arrow, LineType line, boolean isDuplex, boolean isCurved, double w){
        fromNode = from;
        toNode = to;
        gc = graphicsContext;
        arrowType = arrow;
        lineType = line;
        selected = true;
        points = new LinkedList<>();
        points.addLast(new VertexPoint(toPoint));
        points.addFirst(new VertexPoint(fromPoint));
        duplex = isDuplex;
        vertexType = (isCurved ? VertexType.Curved : VertexType.Straight);
        width = w;
        path = new Path();
        draw();
    }

    private void drawSelect(VertexPoint point){
        Paint prev = gc.getFill();
        gc.setFill((point.isHardPoint() ? Color.AQUA : Color.BLUE));
        gc.fillOval(point.getX() - width/2, point.getY() - width/2, width, width);
        gc.setFill(prev);
    }


    public boolean isInPath(Point2D point){
        return path.contains(point);
    }


    public void draw() {
        path = new Path();

        Iterator<VertexPoint> it = points.listIterator();
        VertexPoint prev = null, now = null;
        if (it.hasNext()) prev = it.next();
        lineType.set(gc, width);
        path.getElements().add(new MoveTo(prev.getX(), prev.getY()));
        while (it.hasNext()) {
            now = it.next();
            path.getElements().add(vertexType.newElement(prev, now));
            vertexType.createMid(points, prev, now);

            it = points.listIterator(points.indexOf(prev));
            now = it.next();
            vertexType.draw(gc, prev, now);

            if (selected) drawSelect(prev);
        }
        if(selected) drawSelect(now);

        arrowType.draw(gc, prev, now);
        if (duplex) arrowType.draw(gc, points.get(1), points.getFirst());

    }

    private void center(VertexPoint mid){
        mid.setPoint(
                new Point2D(

                )
        );
    }

}

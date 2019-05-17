package gdraw.graph.vertex;

import gdraw.graph.node.Node;
import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;

import gdraw.graph.util.Label;

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
    private Label label;

    private double value;
    private Paint color;

    public Vertex(Node from, Node to, Point2D fromPoint, Point2D toPoint, GraphicsContext graphicsContext, ArrowType arrow, LineType line, boolean isDuplex, boolean isCurved, double w, Paint c){
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
        color = c;
        path = new Path();
        value = 1.0;
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

    public void draw(Node from){
        if(from == fromNode) draw();
    }

    public void draw() {
        path = new Path();

        Iterator<VertexPoint> it = points.listIterator();
        VertexPoint prev = null, now = null;
        if (it.hasNext()) prev = it.next();
        gc.setStroke(color);
        gc.setLineWidth(width);
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
        int midI = points.indexOf(mid);
        VertexPoint prev = points.get(midI - 1), next = points.get(midI + 1);
        vertexType.center(prev, mid, next);
    }

    private void decideOnCenter(VertexPoint point){
        switch(vertexType) {
            case Curved:
                if(point.getOrientation() != VertexPointOrientation.NONE) center(point);
                break;
            case Straight:
                if (!point.isHardPoint()) center(point);
                break;
        }
    }

    public void move(@NotNull VertexPoint point, Point2D newPoint){
        ListIterator it = points.listIterator(points.indexOf(point));
        if(!it.hasPrevious()) point.setPointBounded(newPoint, fromNode);
        else{
            it.next();
            if(!it.hasNext()) point.setPointBounded(newPoint, toNode);
            else point.setPoint(newPoint);
            it.previous();
        }
        if(it.hasPrevious()){
            decideOnCenter((VertexPoint) it.previous());
            it.next();
        }
        it.next();
        if(it.hasNext())
            decideOnCenter((VertexPoint) it.next());
        int centerPointI = points.size()/2, pointI = points.indexOf(point);
        if(pointI == centerPointI || pointI == centerPointI + 1)
            label.setUpperLeft(getCenterForLabel());
    }

    public void setLabel(String newLabel){
        newLabel += " (" + value + ")";
        if(label == null){
            label = new Label(
                    newLabel,
                    getCenterForLabel()
            );
        }
        else label.setLabel(newLabel);
    }

    private Point2D getCenterForLabel() {
        int size = points.size();
        ListIterator it = points.listIterator(size/2);
        VertexPoint point = (VertexPoint) it.next();
        double x = point.getX(), y = point.getY();
        if(size % 2 == 0){
            VertexPoint next = (VertexPoint) it.next();
            x += next.getX();
            x /= 2;
            y += next.getY();
            y /= 2;
        }
        x -= 10;
        y -= 10;
        return new Point2D(x,y);
    }
}

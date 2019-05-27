package gdraw.graph.vertex;

import gdraw.graph.node.Node;
import gdraw.graph.util.Selectable;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;

import gdraw.graph.util.Label;

public class Vertex implements Selectable {

    private ArrowType arrowType;
    private LineType lineType;
    private Node fromNode;
    private Node toNode;
    private LinkedList<VertexPoint> points;
    private boolean duplex;
    private VertexType vertexType;
    private boolean selected;
    private Group group;
    private Path path;
    private Label label;
    private ArrayList<Shape> arrows;

    private double value;

    public Vertex(Node from, Node to, Point2D fromPoint, Point2D toPoint, Group group, ArrowType arrow, LineType line, boolean isDuplex, boolean isCurved, double w, Paint c){
        fromNode = from;
        toNode = to;
        this.group = group;
        arrowType = arrow;
        lineType = line;
        selected = true;
        points = new LinkedList<>();
        points.addLast(new VertexPoint(toPoint, this));
        points.addFirst(new VertexPoint(fromPoint, this));
        duplex = isDuplex;
        vertexType = (isCurved ? VertexType.Curved : VertexType.Straight);
        path = new Path();
        path.setStroke(c);
        path.setStrokeWidth(w);
        path.setStrokeDashOffset(w);
        path.setOnMouseClicked(e -> {
            setSelected(true);
            draw();
        });
        arrows = new ArrayList<>();
        value = 1.0;
        draw();
    }

    public Vertex(Node from, Node to, Vertex copy){
        fromNode = from;
        toNode = to;
        toNode.addVertex(this);

        ListIterator<VertexPoint> it = copy.getPoints().listIterator(copy.getPoints().size() - 1);
        points.addLast(new VertexPoint(this, it.next()));
        it.previous();
        while(it.hasPrevious()){
            points.addFirst(new VertexPoint(this, it.previous()));
        }
    }

    private LinkedList<VertexPoint> getPoints() {
        return points;
    }

    public void setColor(Color color){
        path.setStroke(color);
    }

    public Paint getColor(){
        return path.getStroke();
    }

    public void setLineWidth(double width){
        path.setStrokeWidth(width);
    }

    public double getLineWidth(){
        return path.getStrokeWidth();
    }

    public void setValue(double v){ value = v;}
    public double getValue(){ return value; }



    public void setSelected(boolean selected){
        this.selected = selected;
        if(!selected) points.forEach(point -> group.getChildren().remove(point.getCircle()));
    }

    private void drawSelect(VertexPoint point){
        group.getChildren().add(point.getCircle());
    }

    public void draw(Node from){
        if(from == fromNode) draw();
    }

    public void draw() {
        group.getChildren().removeAll(arrows);
        group.getChildren().remove(path);
        path = new Path();
        arrows.clear();

        Iterator<VertexPoint> it = points.listIterator();
        VertexPoint prev = null, now = null;
        if (it.hasNext()) prev = it.next();
        lineType.set(path);
        path.getElements().add(new MoveTo(prev.getX(), prev.getY()));
        while (it.hasNext()) {
            now = it.next();
            vertexType.createMid(this, points, prev, now);

            it = points.listIterator(points.indexOf(prev));
            now = it.next();

            path.getElements().add(vertexType.newElement(prev, now));
            if (selected) drawSelect(prev);
        }
        if(selected) drawSelect(now);

        arrowType.draw(arrows, path.getStroke(), prev, now);
        if (duplex) arrowType.draw(arrows, path.getStroke(), points.get(1), points.getFirst());
        group.getChildren().addAll(arrows);

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

    public void translateNode(Node node, double dx, double dy) {
        VertexPoint point = (node == fromNode ? points.getFirst() : points.getLast());
        Point2D newPoint = new Point2D(point.getX() + dx, point.getY() + dy);
        move(point, newPoint);
    }

    public Node getToNode() {
        return toNode;
    }

    @Override
    public void checkSelect(Rectangle rectangle) {
        ListIterator<VertexPoint> it = points.listIterator();
        boolean all = it.hasNext();                                 //Zabezpieczenie przed pustym vertexem
        while(it.hasNext())
            all = all && rectangle.contains(it.next().getPoint());
        setSelected(all);
    }
}

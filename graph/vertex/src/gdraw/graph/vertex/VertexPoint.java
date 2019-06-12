package gdraw.graph.vertex;

import gdraw.graph.node.Node;
import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

public class VertexPoint implements Serializable {
    private Point2D point;
    private VertexPointOrientation orientation;
    private boolean hardPoint;
    private transient Circle circle;

    public VertexPoint(@NotNull Point2D point, Vertex vertex){ this(point.getX(), point.getY(), vertex); }

    public VertexPoint(@NotNull Point2D point, Vertex vertex, boolean hardPoint){
        this(point.getX(), point.getY(), vertex);
        setHardPoint(hardPoint);
    }

    public VertexPoint(double x, double y, Vertex vertex){ this(x, y, vertex, VertexPointOrientation.NONE); }

    public VertexPoint(double x, double y, Vertex vertex, VertexPointOrientation orientation) {
        this(x,y);
        this.orientation = orientation;
        setVertex(vertex);
    }

    private void setVertex(Vertex vertex) {
        circle.setOnMouseDragged(e -> vertex.move(this, new Point2D(e.getX(), e.getY())));
    }

    public VertexPoint(Vertex vertex, VertexPoint copy){
        this(copy.getX() + 5, copy.getY() + 5, vertex, copy.getOrientation());
        setHardPoint(copy.isHardPoint());
    }

    public VertexPoint(Point2D point2D) {
        this(point2D.getX(), point2D.getY());
    }

    public VertexPoint(double x, double y) {
        point = new Point2D(x, y);
        this.orientation = VertexPointOrientation.NONE;
        hardPoint = true;
        setCircle();
    }

    private void setCircle() {
        circle = new Circle();
        circle.setCenterX(point.getX());
        circle.setCenterY(point.getY());
        circle.setRadius(3);
        circle.setFill(Color.BLUE);
    }

    public void refresh(Vertex vertex){
        setCircle();
        setVertex(vertex);
    }

    public double getX(){
        return point.getX();
    }

    public double getY(){
        return point.getY();
    }

    public void setPoint(Point2D newPoint){
        circle.setCenterX(newPoint.getX());
        circle.setCenterY(newPoint.getY());
        point = newPoint;
    }

    public Point2D getPoint() {
        return point;
    }

    public VertexPoint setOrientation(VertexPointOrientation newOrientation){
        orientation = newOrientation;
        return this;
    }

    public VertexPointOrientation getOrientation(){
        return orientation;
    }

    public boolean isHardPoint(){
        return hardPoint;
    }

    public void setHardPoint(boolean hp){
        hardPoint = hp;
        circle.setFill((hardPoint ? Color.BLUE : Color.AQUA));
    }

    public Circle getCircle(){
        return circle;
    }


    public void setPointBounded(Point2D newPoint, Node node) {
        double x = newPoint.getX(), y = newPoint.getY();
        double xcenter = node.getCenter().getX(), ycenter = node.getCenter().getY();
        double halfWidth = node.getWidth()/2; double halfHeight = node.getHeight()/2;

        x = Math.max(x, xcenter - halfWidth);
        x = Math.min(x, xcenter + halfWidth);
        y = Math.max(y, ycenter - halfHeight);
        y = Math.min(y, ycenter + halfHeight);

        setPoint(new Point2D(x, y));
    }
}

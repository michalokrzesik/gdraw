package gdraw.graph.vertex;

import gdraw.graph.node.Node;
import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.io.Serializable;

public class VertexPoint implements Serializable {
    private Point2D point;
    private VertexPointOrientation orientation;
    private boolean hardPoint;
//    private transient Circle circle;

    public VertexPoint(Point2D point, Vertex vertex){ this(point.getX(), point.getY(), vertex); }

    public VertexPoint(Point2D point, Vertex vertex, boolean hardPoint){
        this(point.getX(), point.getY(), vertex);
        setHardPoint(hardPoint);
    }

    public VertexPoint(double x, double y, Vertex vertex){ this(x, y, vertex, VertexPointOrientation.NONE); }

    public VertexPoint(double x, double y, Vertex vertex, VertexPointOrientation orientation) {
        this(x,y);
        this.orientation = orientation;
//        setVertex(vertex);
    }

    public VertexPoint(Point2D point, Vertex vertex, VertexPointOrientation orientation) {
        this(point);
        this.orientation = orientation;
//        setVertex(vertex);
    }

//    private void setVertex(Vertex vertex) {
//        circle.setOnMouseDragged(e -> vertex.move(this, new Point2D(e.getX(), e.getY())));
//    }

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
//        setCircle();
    }

//    private void setCircle() {
//        circle = new Circle();
//        circle.setCenterX(point.getX());
//        circle.setCenterY(point.getY());
//        circle.setRadius(5);
//        circle.setFill(Color.BLUE);
//    }

    public void refresh(Vertex vertex){
//        setCircle();
//        setVertex(vertex);
    }

    public double getX(){
        return point.getX();
    }

    public double getY(){
        return point.getY();
    }

    public void setPoint(Point2D newPoint){
//        circle.setCenterX(newPoint.getX());
//        circle.setCenterY(newPoint.getY());
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
//        circle.setFill((hardPoint ? Color.BLUE : Color.AQUA));
    }

//    public Circle getCircle(){
//        return circle;
//    }


    public void setPointBounded(Point2D newPoint, Node node) {
        double x = newPoint.getX(), y = newPoint.getY(), newX, newY;
        double xcenter = node.getCenter().getX(), ycenter = node.getCenter().getY();
        double halfWidth = node.getWidth()/2; double halfHeight = node.getHeight()/2;

        x = Math.max(x, xcenter - halfWidth);
        x = Math.min(x, xcenter + halfWidth);
        y = Math.max(y, ycenter - halfHeight);
        y = Math.min(y, ycenter + halfHeight);

        newX = xcenter + (x - (xcenter - halfWidth) < (xcenter + halfWidth) - x ? -halfWidth : halfWidth);
        newY = ycenter + (y - (ycenter - halfHeight) < (ycenter + halfHeight) - y ? -halfHeight : halfHeight);

        if(Math.abs(x - newX) < Math.abs(y - newY))
            newY = y;
        else
            newX = x;

        setPoint(new Point2D(newX, newY));
    }

    public void draw(GraphicsContext gc, double width, boolean isSelected) {
            gc.setFill(isSelected ? (hardPoint ? Color.BLUE : Color.AQUA) : Color.GRAY);
            gc.fillOval(getX() - 2.5, getY() - 2.5, 5, 5);
    }

    public void setPointBounded(Node node) {
        setPointBounded(point, node);
    }

    public boolean contains(double x, double y) {
        double xc = point.getX(), yc = point.getY();
        return (xc - 3 <= x) && (xc + 3 >= x) && (yc - 3 <= y) && (yc + 3 >= y);
    }
}

package gdraw.graph.vertex;

import gdraw.graph.node.Node;
import javafx.geometry.Point2D;
import org.jetbrains.annotations.NotNull;

public class VertexPoint {
    private Point2D point;
    private VertexPointOrientation orientation;
    private boolean hardPoint;

    public VertexPoint(@NotNull Point2D point){ this(point.getX(), point.getY()); }

    public VertexPoint(@NotNull Point2D point, boolean hardPoint){
        this(point.getX(), point.getY());
        this.hardPoint = hardPoint;
    }

    public VertexPoint(double x, double y){ this(x, y, VertexPointOrientation.NONE); }

    public VertexPoint(double x, double y, VertexPointOrientation orientation) {
        point = new Point2D(x, y);
        this.orientation = orientation;
        hardPoint = true;
    }

    public double getX(){
        return point.getX();
    }

    public double getY(){
        return point.getY();
    }

    public void setPoint(Point2D newPoint){
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
    }


    public void setPointBounded(Point2D newPoint, Node node) {
        double x = newPoint.getX(), y = newPoint.getY();
        double xcenter = node.getCenter().getX(), ycenter = node.getCenter().getY();

        if(Math.abs(x - xcenter) > Math.abs(y - ycenter)){
            double height = node.getHeight()/2;
            y = ycenter + (y > ycenter ? height : -height);
        }
        else {
            double width = node.getWidth()/2;
            x = xcenter + (x > xcenter ? width : -width);
        }

        setPoint(new Point2D(x, y));
    }
}

package gdraw.graph.vertex;

import javafx.geometry.Point2D;
import org.jetbrains.annotations.NotNull;

public class VertexPoint {
    Point2D point;
    VertexPointOrientation orientation;

    public VertexPoint(@NotNull Point2D point){ this(point.getX(), point.getY()); }

    public VertexPoint(double x, double y){ this(x, y, VertexPointOrientation.NONE); }

    public VertexPoint(double x, double y, VertexPointOrientation orientation) {
        point = new Point2D(x, y);
        this.orientation = orientation;
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

    public void setOrientation(VertexPointOrientation newOrientation){
        orientation = newOrientation;
    }
}

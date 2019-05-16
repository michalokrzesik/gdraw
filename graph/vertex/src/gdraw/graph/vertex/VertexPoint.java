package gdraw.graph.vertex;

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

    public void setOrientation(VertexPointOrientation newOrientation){
        orientation = newOrientation;
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


}

package gdraw.graph.vertex;

import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.util.Pair;

public enum ArrowType {
    None{
        public void draw(GraphicsContext gc, Point2D source, Point2D destination){}
    },
    Opened{
        @Override
        public void draw(GraphicsContext gc, Point2D source, Point2D destination) {

        }
    }

    private Pair<Point2D, Point2D> halfPerpendicularPoints(Point2D source, Point2D destination, double distance){
        double A = (source.getY() - destination.getY())/(source.getX() - destination.getX());
        double A1 = 2*A;
        double A2 = 1/A1;


    }

    abstract public void draw(GraphicsContext gc, Point2D source, Point2D destination);
    }

package gdraw.graph.vertex;

import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.PathElement;
import javafx.scene.shape.QuadCurveTo;

import java.util.LinkedList;

public enum VertexType {
    Straight{
        @Override
        public void draw(GraphicsContext gc, VertexPoint a, VertexPoint b) {
            gc.strokeLine(a.getX(), a.getY(), b.getX(), b.getY());
        }

        @Override
        public PathElement newElement(VertexPoint a, VertexPoint b) {
            return new LineTo(b.getX(),b.getY());
        }

        @Override
        public void createMid(LinkedList<VertexPoint> points, VertexPoint prev, VertexPoint now) {
            if (prev.isHardPoint() && now.isHardPoint()) {
                VertexPoint mid = new VertexPoint(prev.getPoint().midpoint(now.getPoint()), false);
                points.add(points.indexOf(now), mid);
            }
        }
    },
    Curved{
        private Point2D controlPoint(VertexPoint a, VertexPoint b){
            if(a.getOrientation() == VertexPointOrientation.HORIZONTAL) return new Point2D(b.getX(), a.getY());
            return new Point2D(a.getX(), b.getY());
        }

        @Override
        public void draw(GraphicsContext gc, VertexPoint a, VertexPoint b) {
            Point2D c = controlPoint(a, b);
            gc.beginPath();
            gc.moveTo(a.getX(),a.getY());
            gc.quadraticCurveTo(c.getX(), c.getY(), b.getX(), b.getY());
            gc.stroke();
        }

        @Override
        public PathElement newElement(VertexPoint a, VertexPoint b) {
            Point2D c = controlPoint(a, b);
            return new QuadCurveTo(c.getX(), c.getY(), b.getX(), b.getY());
        }

        @Override
        public void createMid(LinkedList<VertexPoint> points, VertexPoint prev, VertexPoint now) {
            if(prev.getOrientation() == now.getOrientation()){
                boolean sameX = prev.getX() == now.getX(), sameY = prev.getY() == now.getY();
                if(sameX || sameY) {
                    if (prev.getOrientation() == VertexPointOrientation.NONE) {
                        points.removeFirst();
                        double distance = now.getX() - prev.getX() + now.getY() - prev.getY();
                        points.addFirst(
                                new VertexPoint(
                                        new Point2D(
                                                (sameX ? prev.getX() : now.getX() - distance/4),
                                                (sameY ? prev.getY() : now.getY() - distance/4)),
                                        false));
                        points.addFirst(
                                new VertexPoint(
                                        new Point2D(
                                                (sameX ? prev.getX() : now.getX() - distance/2),
                                                (sameY ? prev.getY() : now.getY() - distance/2)),
                                        false));
                        points.addFirst(
                                new VertexPoint(
                                        new Point2D(
                                                (sameX ? prev.getX() : prev.getX() + distance/4),
                                                (sameY ? prev.getY() : prev.getY() + distance/4)),
                                        false));
                        points.addFirst(prev);
                    }
                    else{
                        prev.setPoint(new Point2D((prev.getX() + now.getX())/2, (prev.getY() + now.getY())/2));
                        points.remove(now);
                    }
                }
            }
        }
    };


    abstract public void draw(GraphicsContext gc, VertexPoint a, VertexPoint b);
    abstract public PathElement newElement(VertexPoint a, VertexPoint b);

    public abstract void createMid(LinkedList<VertexPoint> points, VertexPoint prev, VertexPoint now);
}

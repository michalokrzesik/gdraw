package gdraw.graph.vertex;

import javafx.scene.canvas.GraphicsContext;

import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.LinkedList;

public enum VertexType implements Serializable {
    Straight{
        @Override
        public void newElement(GraphicsContext gc, VertexPoint a, VertexPoint b) {
            gc.lineTo(b.getX(),b.getY());
        }

        @Override
        public boolean createMid(Vertex vertex, LinkedList<VertexPoint> points, VertexPoint prev, VertexPoint now) {
            if (prev.isHardPoint() && now.isHardPoint()) {
                javafx.geometry.Point2D point = new javafx.geometry.Point2D(prev.getX(), prev.getY()).midpoint(now.getX(), now.getY());
                VertexPoint mid = new VertexPoint(new Point2D.Double(point.getX(), point.getY()), vertex, false);
                points.add(points.indexOf(now), mid);
                return true;
            }
            return false;
        }

        @Override
        public void center(VertexPoint prev, VertexPoint mid, VertexPoint next) {
            mid.setPoint(
                    new Point2D.Double(
                            (prev.getX() + next.getX())/2,
                            (prev.getY() + next.getY())/2
                    )
            );
        }
    },
    Curved{
        @Override
        public void newElement(GraphicsContext gc, VertexPoint a, VertexPoint b) {
            Point2D c = VertexType.controlPoint(a, b);
            gc.quadraticCurveTo(c.getX(), c.getY(), b.getX(), b.getY());
        }

        @Override
        public boolean createMid(Vertex vertex, LinkedList<VertexPoint> points, VertexPoint prev, VertexPoint now) {
            if(prev.getOrientation() == now.getOrientation()){
                boolean sameX = prev.getX() == now.getX(), sameY = prev.getY() == now.getY();
                if(sameX || sameY) {
                    if (prev.getOrientation() == VertexPointOrientation.NONE) {
                        points.removeFirst();
                        double distance = now.getX() - prev.getX() + now.getY() - prev.getY();
                        VertexPointOrientation orientation = (prev.getX() == now.getX() ? VertexPointOrientation.VERTICAL : VertexPointOrientation.HORIZONTAL);
                        points.addFirst(
                                new VertexPoint(
                                        new Point2D.Double(
                                                (sameX ? prev.getX() : now.getX() - distance/4),
                                                (sameY ? prev.getY() : now.getY() - distance/4)),
                                        vertex,
                                        false).setOrientation(orientation));
                        points.addFirst(
                                new VertexPoint(
                                        new Point2D.Double(
                                                (sameX ? prev.getX() : now.getX() - distance/2),
                                                (sameY ? prev.getY() : now.getY() - distance/2)),
                                        vertex,
                                        false).setOrientation(orientation));
                        points.addFirst(
                                new VertexPoint(
                                        new Point2D.Double(
                                                (sameX ? prev.getX() : prev.getX() + distance/4),
                                                (sameY ? prev.getY() : prev.getY() + distance/4)),
                                        vertex,
                                        false).setOrientation(orientation));
                        points.addFirst(prev);
                    }
                    else{
                        prev.setPoint(new Point2D.Double((prev.getX() + now.getX())/2, (prev.getY() + now.getY())/2));
                        points.remove(now);
                    }
                }
                else {
                    VertexPoint mid = new VertexPoint(
                            new Point2D.Double((prev.getX() + now.getX()) / 2, (prev.getY() + now.getY()) / 2), vertex);
                    mid.setOrientation((prev.getOrientation() == VertexPointOrientation.HORIZONTAL ? VertexPointOrientation.VERTICAL : VertexPointOrientation.HORIZONTAL));
                    points.add(points.indexOf(now), mid);
                }
                return true;
            }
            return false;
        }

        @Override
        public void center(VertexPoint prev, VertexPoint mid, VertexPoint next) {
            boolean midHorizon = mid.getOrientation() == VertexPointOrientation.HORIZONTAL;
            mid.setPoint(
                    new Point2D.Double(
                            (midHorizon ? (prev.getX() + next.getX())/2 : mid.getX()),
                            (midHorizon ? mid.getY() : (prev.getY() + next.getY())/2)
                    )
            );
        }
    };


    abstract public void newElement(GraphicsContext gc, VertexPoint a, VertexPoint b);

    public abstract boolean createMid(Vertex vertex, LinkedList<VertexPoint> points, VertexPoint prev, VertexPoint now);

    public abstract void center(VertexPoint prev, VertexPoint mid, VertexPoint next);

    public static Point2D.Double controlPoint(VertexPoint a, VertexPoint b) {
        switch (a.getOrientation()){
            case HORIZONTAL:
                return new Point2D.Double(b.getX(), a.getY());
            case VERTICAL:
                return new Point2D.Double(a.getX(), b.getY());
            default:
                if (b.getOrientation() == VertexPointOrientation.HORIZONTAL)
                    return new Point2D.Double(a.getX(), b.getY());
                else
                    return new Point2D.Double(b.getX(), a.getY());
        }
    }
}

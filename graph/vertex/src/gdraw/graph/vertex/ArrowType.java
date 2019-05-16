package gdraw.graph.vertex;

import javafx.scene.canvas.GraphicsContext;
import javafx.util.Pair;

public enum ArrowType {
    None{
        public void draw(GraphicsContext gc, VertexPoint source, VertexPoint destination){}
    },
    Opened{
        @Override
        public void draw(GraphicsContext gc, VertexPoint source, VertexPoint destination) {
            Pair<VertexPoint, VertexPoint> points = arrowPoints(source, destination, 5.0);
            gc.setLineDashes(null);
            gc.beginPath();
            gc.moveTo(points.getKey().getX(), points.getKey().getY());
            gc.lineTo(destination.getX(), destination.getY());
            gc.lineTo(points.getValue().getX(), points.getValue().getY());
            gc.stroke();
        }
    },
    Filled{
        @Override
        public void draw(GraphicsContext gc, VertexPoint source, VertexPoint destination) {
            Pair<VertexPoint, VertexPoint> points = arrowPoints(source, destination, 5.0);
            gc.setLineDashes(null);
            double[] xPoints = {points.getKey().getX(), points.getValue().getX(), destination.getX()},
                    yPoints = {points.getKey().getY(), points.getValue().getY(), destination.getY()};
            gc.fillPolygon(xPoints, yPoints, 3);
        }
    };

    private static Pair<VertexPoint, VertexPoint> arrowPoints(VertexPoint source, VertexPoint destination, double arrowHeadSize){
        //ArrowHead
        double angle = Math.atan2((destination.getY() - source.getY()), (destination.getX() - source.getX())) - Math.PI / 2.0;
        double sin = Math.sin(angle);
        double cos = Math.cos(angle);
        //point1
        double x1 = (- 1.0 / 2.0 * cos + Math.sqrt(3) / 2 * sin) * arrowHeadSize + destination.getX();
        double y1 = (- 1.0 / 2.0 * sin - Math.sqrt(3) / 2 * cos) * arrowHeadSize + destination.getY();
        //point2
        double x2 = (1.0 / 2.0 * cos + Math.sqrt(3) / 2 * sin) * arrowHeadSize + destination.getX();
        double y2 = (1.0 / 2.0 * sin - Math.sqrt(3) / 2 * cos) * arrowHeadSize + destination.getY();

        return new Pair<>(new VertexPoint(x1,y1), new VertexPoint(x2,y2));
    }

    abstract public void draw(GraphicsContext gc, VertexPoint source, VertexPoint destination);
    }

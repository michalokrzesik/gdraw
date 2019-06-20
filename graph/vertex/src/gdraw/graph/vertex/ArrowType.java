package gdraw.graph.vertex;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.Shape;

import java.io.Serializable;
import java.util.ArrayList;

public enum ArrowType implements Serializable {
    None{
        public void draw(GraphicsContext gc, Paint color, VertexPoint source, VertexPoint destination){ gc.setLineDashes(null);}

        @Override
        public String toString() {
            return "———";
        }
    },
    Opened{
        @Override
        public void draw(GraphicsContext gc, Paint color, VertexPoint source, VertexPoint destination) {
            double[] points = arrowPoints(source, destination, gc.getLineWidth() + 7);
            gc.setLineDashes(null);
            /*Polyline polyline = new Polyline();
            polyline*/gc.setStroke(color);
//            for(double coordinate : points)
//                polyline.getPoints().add(coordinate);
//            arrows.add(polyline);
            double[] xs = { points[0], points[2], points[4] }, ys = { points[1], points[3], points[5] };
            gc.strokePolyline(xs, ys, 3);
        }

        @Override
        public String toString() {
            return "———>";
        }
    },
    Filled{
        @Override
        public void draw(GraphicsContext gc, Paint color, VertexPoint source, VertexPoint destination) {
            double[] points = arrowPoints(source, destination, gc.getLineWidth() + 7);
//            double[] xPoints = {points.getKey().getX(), points.getValue().getX(), destination.getX()},
//                    yPoints = {points.getKey().getY(), points.getValue().getY(), destination.getY()};
//
//            Polygon polygon = new Polygon();
//            polygon.setFill(color);
//            polygon.setStroke(color);
//            for(double coordinate : points)
//                polygon.getPoints().add(coordinate);
//            arrows.add(polygon);
            gc.setLineDashes(null);
            double[] xs = { points[0], points[2], points[4] }, ys = { points[1], points[3], points[5] };
            gc.setFill(color);
            gc.setStroke(color);
            gc.fillPolygon(xs, ys, 3);


        }

        @Override
        public String toString() {
            return "———▶";
        }
    };



    public static double[] arrowPoints(VertexPoint source, VertexPoint destination, double arrowHeadSize){
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

        double[] points = {
                x1, y1,
                destination.getX(), destination.getY(),
                x2, y2
        };

        return points;
    }

    public static ArrowType getValueOf(String name) {
        switch (name){
            case "———▶": return Filled;
            case "———>": return Opened;
            default: return None;
        }
    }

    abstract public void draw(GraphicsContext gc, Paint color, VertexPoint source, VertexPoint destination);
    }

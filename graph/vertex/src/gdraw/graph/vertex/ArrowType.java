package gdraw.graph.vertex;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.Shape;
import javafx.util.Pair;

import java.io.Serializable;
import java.util.ArrayList;

public enum ArrowType implements Serializable {
    None{
        public void draw(ArrayList<Shape> arrows, Paint color, VertexPoint source, VertexPoint destination){}
    },
    Opened{
        @Override
        public void draw(ArrayList<Shape> arrows, Paint color, VertexPoint source, VertexPoint destination) {
            double[] points = arrowPoints(source, destination, 5.0);
            Polyline polyline = new Polyline();
            polyline.setStroke(color);
            for(double coordinate : points)
                polyline.getPoints().add(coordinate);
            arrows.add(polyline);
        }
    },
    Filled{
        @Override
        public void draw(ArrayList<Shape> arrows, Paint color, VertexPoint source, VertexPoint destination) {
            double[] points = arrowPoints(source, destination, 5.0);
 /*           double[] xPoints = {points.getKey().getX(), points.getValue().getX(), destination.getX()},
                    yPoints = {points.getKey().getY(), points.getValue().getY(), destination.getY()};
 */
            Polygon polygon = new Polygon();
            polygon.setFill(color);
            polygon.setStroke(color);
            for(double coordinate : points)
                polygon.getPoints().add(coordinate);
            arrows.add(polygon);
        }
    };



    private static double[] arrowPoints(VertexPoint source, VertexPoint destination, double arrowHeadSize){
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

    abstract public void draw(ArrayList<Shape> arrows, Paint color, VertexPoint source, VertexPoint destination);
    }

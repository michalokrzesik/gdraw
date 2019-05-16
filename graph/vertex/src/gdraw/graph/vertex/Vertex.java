package gdraw.graph.vertex;

import gdraw.graph.node.Node;
import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

import java.util.Iterator;
import java.util.LinkedList;

public class Vertex {

    private ArrowType arrowType;
    private LineType lineType;
    private Node fromNode;
    private Node toNode;
    private LinkedList<VertexPoint> points;
    private boolean duplex;
    private boolean curved;
    private boolean selected;
    private GraphicsContext gc;
    private double width;

    public Vertex(Node from, Node to, Point2D fromPoint, Point2D toPoint, GraphicsContext graphicsContext, ArrowType arrow, LineType line, boolean isDuplex, boolean isCurved, double w){
        fromNode = from;
        toNode = to;
        gc = graphicsContext;
        arrowType = arrow;
        lineType = line;
        selected = true;
        points = new LinkedList<>();
        points.addLast(new VertexPoint(toPoint));
        points.addFirst(new VertexPoint(fromPoint));
        duplex = isDuplex;
        curved = isCurved;
        width = w;
        draw();
    }

    private void drawSelect(Point2D point){
        Paint prev = gc.getFill();
        gc.setFill(Color.BLUE);
        gc.fillOval(point.getX() - width/2, point.getY() - width/2, width, width);
        gc.setFill(prev);
    }


    public void draw(){
        if(curved) drawCurved();
        else{
            Iterator<VertexPoint> it = points.iterator();
            VertexPoint prev = null, now = null;
            if(it.hasNext())  prev = it.next();
            lineType.set(gc, width);
            if(selected) drawSelect(prev.getPoint());
            while(it.hasNext()){
                now = it.next();
                gc.strokeLine(prev.getX(), prev.getY(), now.getX(), now.getY());
                if(prev.isHardPoint() && now.isHardPoint()){
                    VertexPoint mid = new VertexPoint(prev.getPoint().midpoint(now.getPoint()), false);
                    points.add(points.indexOf(now), mid);
                    if(selected) drawSelect(mid.getPoint());
                }
                if(selected)
                    drawSelect(prev.getPoint());
            }
            drawSelect(now.getPoint());
            arrowType.draw(gc, prev, now);
            if(duplex) arrowType.draw(gc, points.get(1), points.getFirst());
        }

    }


}

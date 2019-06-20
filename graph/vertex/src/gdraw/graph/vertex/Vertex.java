package gdraw.graph.vertex;

import gdraw.graph.node.Node;
import gdraw.graph.util.Selectable;
import gdraw.graph.util.action.MultiAction;
import gdraw.graph.util.action.VertexCreation;
import gdraw.main.MainController;
import gdraw.main.Project;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Path;
import javafx.scene.shape.Rectangle;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;

import gdraw.graph.util.Label;

public class Vertex extends Selectable {

    private ArrowType arrowType;
    private LineType lineType;
    private Node fromNode;
    private Node toNode;
    private LinkedList<VertexPoint> points;
    private boolean duplex;
    private VertexType vertexType;
    private transient Path path;
    private Label label;
    private Color color;
    private double width;

    private double xl, xr, yu, yd;

    private double value;

    public Vertex(Node from, Node to, Point2D fromPoint, Point2D toPoint,
                  Canvas canvas, ArrowType arrow, LineType line, boolean isDuplex, boolean isCurved,
                  double w, Color c, MainController mainController){
        controller = mainController;
        fromNode = from;
        toNode = to;
        toNode.addVertex(this);
        this.canvas = canvas;
        init(arrow, line, isDuplex, isCurved);
        points.addLast(new VertexPoint(toPoint, this));
        points.addFirst(new VertexPoint(fromPoint, this));
        color = c;
        width = w;
        makePath();
        value = 1.0;
        controller.addObject(this);

        double fx = fromPoint.getX(), fy = fromPoint.getY(), tx = toPoint.getX(), ty = toPoint.getY();
        xl = fx < tx ? fx : tx; xr = fx + tx - xl;
        yu = fy < ty ? fy : ty; yd = fy + ty - yu;
//        draw();
    }

    public static void draw(Node from, VertexPoint begin, VertexPoint end, Node to, Canvas canvas) {
        begin.setPointBounded(from);
        if(to != null) end.setPointBounded(to);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        double bx = begin.getX(), by = begin.getY(), ex = end.getX(), ey = end.getY();

        if(from == to){
            double x = (bx + ex)/2, y = (by + ey)/2, w = from.getWidth(), h = from.getHeight();
            Point2D center = from.getCenter();
            double cx = center.getX(), cy = center.getY();
            x = cx + (cx > x ? -w : w);
            y = cy + (cy > y ? -h : h);
            double[] points = ArrowType.arrowPoints(new VertexPoint(center), new VertexPoint(x, y), (w + h)/8);

            gc.beginPath();
            gc.moveTo(cx, cy);
            VertexType type = VertexType.Curved;
            VertexPoint a = new VertexPoint(cx, cy),
                    b = new VertexPoint(points[0], points[1]),
                    c = new VertexPoint(points[2], points[3]),
                    d = new VertexPoint(points[4], points[5]);
            if(Math.abs(points[1] - cy) < Math.abs(points[5] - cy)){
                b.setOrientation(VertexPointOrientation.VERTICAL);
                d.setOrientation(VertexPointOrientation.HORIZONTAL);
            }
            else {
                b.setOrientation(VertexPointOrientation.HORIZONTAL);
                d.setOrientation(VertexPointOrientation.VERTICAL);
            }
            type.newElement(gc, a, b);
            type.newElement(gc, b, d);
            type.newElement(gc, d, a);
//            gc.quadraticCurveTo(points[0], points[1], (cx + points[0])/2, (cy + points[1])/2);
//            gc.quadraticCurveTo(points[2], points[3], (points[2] + points[0])/2, (points[3] + points[1])/2);
//            gc.quadraticCurveTo(points[4], points[5], (points[2] + points[4])/2, (points[3] + points[5])/2);
//            gc.quadraticCurveTo(cx, cy, (cx + points[4])/2, (cy + points[5])/2);
            gc.stroke();
        }
        else gc.strokeLine(bx, by, ex, ey);
    }

    private void makePath(){
        path = new Path();
        path.setStroke(color);
        path.setStrokeWidth(width);
        path.setStrokeDashOffset(width);
        path.setOnMouseClicked(this::setSelected);
        path.setOnMousePressed(this::onMousePressed);
        path.setOnMouseDragged(this::onMouseDragged);
        path.setOnContextMenuRequested(controller::contextMenu);
    }

    private void init(ArrowType arrow, LineType line, boolean isDuplex, boolean isCurved){
        arrowType = arrow;
        lineType = line;
        selected = true;
        points = new LinkedList<>();
        duplex = isDuplex;
        vertexType = (isCurved ? VertexType.Curved : VertexType.Straight);
    }

    public Vertex(Node from, Node to, Vertex copy){
        this(copy);
        fromNode = from;
        toNode = to;
        toNode.addVertex(this);
        canvas = copy.canvas;
        draw();

        VertexPoint startVP = points.getFirst();
        startVP.setPointBounded(startVP.getPoint(), fromNode);
        VertexPoint stopVP = points.getLast();
        stopVP.setPointBounded(stopVP.getPoint(), toNode);
    }

    private void copyPoints(Vertex copy) {
        ListIterator<VertexPoint> it = copy.getPoints().listIterator(copy.getPoints().size() - 1);
        points.addLast(it.next());
        it.previous();
        while(it.hasPrevious()){
            points.addFirst(new VertexPoint(this, it.previous()));
        }
    }

    public Vertex(Vertex vertex) {
        controller = vertex.controller;
        init(vertex.arrowType, vertex.lineType, vertex.duplex, vertex.vertexType == VertexType.Curved);
        value = vertex.value;
        color = vertex.color;
        width = vertex.width;
//        makePath();

        copyPoints(vertex);
    }

    private LinkedList<VertexPoint> getPoints() {
        return points;
    }

    public void setColor(Color color){
        this.color = color;
        path.setStroke(color);
    }

    public Color getColor(){
        return color;
    }

    public void setLineWidth(double width){
        this.width = width;
        path.setStrokeWidth(width);
    }

    public double getLineWidth(){
        return width;
    }

    public void setValue(double v){ value = v;}
    public double getValue(){ return value; }


    @Override
    public void translate(double dx, double dy) {
        points.forEach(point -> point.setPoint(new Point2D(point.getX() + dx, point.getY() + dy)));
        VertexPoint startVP = points.getFirst(), stopVP = points.getLast();
        startVP.setPointBounded(startVP.getPoint(), fromNode);
        stopVP.setPointBounded(stopVP.getPoint(), toNode);
    }

    private void drawSelect(VertexPoint point){
//        if(!pane.getChildren().contains(point.getCircle())) pane.getChildren().add(point.getCircle());
//        point.getCircle().toFront();
        point.draw(canvas.getGraphicsContext2D(), width,true);
    }

    public void draw(Node from){
        if(from == fromNode) draw();
    }

    private void draw() {
//        pane.getChildren().removeAll(arrows);
//        pane.getChildren().remove(path);
//        makePath();
//        if(!pane.getChildren().contains(path))
//            pane.getChildren().add(path);
//        path.toFront();
//        arrows.clear();

        GraphicsContext gc = canvas.getGraphicsContext2D();
        Iterator<VertexPoint> it = points.listIterator();
        VertexPoint prev = null, now = null;
        if (it.hasNext()) now = it.next();
        lineType.set(gc, width);
        gc.setStroke(color);
        gc.beginPath();
        gc.moveTo(/*);
        path.getElements().add(new MoveTo(*/now.getX(), now.getY());
        while (it.hasNext()) {
            prev = now;
            now = it.next();
            if(vertexType.createMid(this, points, prev, now)) {
                it = points.listIterator(points.indexOf(prev));
                now = it.next();
            }

            /*path.getElements().add(*/vertexType.newElement(gc, prev, now);
            //if (selected) drawSelect(prev);
        }
        gc.stroke();
        if(selected) points.forEach(this::drawSelect);
        else if(!controller.isToSnapshot()) points.forEach(p -> p.draw(gc, width, false));

        arrowType.draw(gc, color, prev, now);
        if (duplex) arrowType.draw(gc, color, points.get(1), points.getFirst());
//        if(!arrows.isEmpty()) {
////            pane.getChildren().addAll(arrows);
////            arrows.forEach(javafx.scene.Node::toFront);
//        }
        if(label != null) label.draw();

    }

    private void center(VertexPoint mid){
        int midI = points.indexOf(mid);
        VertexPoint prev = points.get(midI - 1), next = points.get(midI + 1);
        vertexType.center(prev, mid, next);
    }

    private void decideOnCenter(VertexPoint point){
        switch(vertexType) {
            case Curved:
                if(point.getOrientation() != VertexPointOrientation.NONE) center(point);
                break;
            case Straight:
                if (!point.isHardPoint()) center(point);
                break;
        }
    }

    public void move(VertexPoint point, Point2D newPoint){
        ListIterator it = points.listIterator(points.indexOf(point));
        if(!it.hasPrevious())
            point.setPointBounded(newPoint, fromNode);
        else{
            it.next();
            if(!it.hasNext())
                point.setPointBounded(newPoint, toNode);
            else point.setPoint(newPoint);
            it.previous();
        }
        if(it.hasPrevious()){
            decideOnCenter((VertexPoint) it.previous());
            it.next();
        }
        it.next();
        if(it.hasNext())
            decideOnCenter((VertexPoint) it.next());
        int centerPointI = points.size()/2, pointI = points.indexOf(point);
        if(label != null && (pointI == centerPointI || pointI == centerPointI + 1))
            label.setUpperLeft(getCenterForLabel());

        point.setHardPoint(true);

        xl = xl < point.getX() ? xl : point.getX();
        xr = xr > point.getX() ? xr : point.getX();
        yu = yu < point.getY() ? yu : point.getY();
        yd = yd < point.getY() ? yd : point.getY();

        draw();
    }

    public void setLabel(String newLabel){
        newLabel += " (" + value + ")";
        if(label == null){
            label = new Label(
                    newLabel, canvas,
                    getCenterForLabel()
            );
        }
        else label.setLabel(newLabel);
    }

    @Override
    public void setProperties(ScrollPane properties, ArrayList<Selectable> selected) {
        javafx.scene.control.Label labelName = new javafx.scene.control.Label("Etykieta:"),
                lineName = new javafx.scene.control.Label("Typ linii:"),
                arrowName = new javafx.scene.control.Label("Typ strzałek:"),
                widthName = new javafx.scene.control.Label("Szerokość linii:"),
                valueName = new javafx.scene.control.Label("Wartość krawędzi:"),
                duplexName = new javafx.scene.control.Label("Dwustronny?");

        double width = getLineWidth();
        boolean isDuplex = duplex;
        String v = String.valueOf(value);
        String lt = lineType.toString(), at = arrowType.toString();

        for(Selectable s : selected){
            Vertex vertex = (Vertex) s;
            if(!v.equals(String.valueOf(vertex.getValue()))) v = "";
            if(!lt.equals(vertex.lineType.toString())) lt = "";
            if(!at.equals(vertex.arrowType.toString())) at = "";
            isDuplex = isDuplex && vertex.isDuplex();
            if(width != vertex.getLineWidth()) width = -1;
        }

        TextField labelField = new TextField(),
                valueField = new TextField(v),
                widthField = new TextField(width < 0 ? Double.toString(width) : "");

        ColorPicker colorPicker = new ColorPicker(); colorPicker.setValue(color);

        CheckBox duplexBox = new CheckBox(); duplexBox.selectedProperty().setValue(isDuplex);

        ChoiceBox<LineType> lineTypeChoiceBox = new ChoiceBox<>();
        lineTypeChoiceBox.getItems().addAll(LineType.values());
        lineTypeChoiceBox.getSelectionModel().select(LineType.getValueOf(lt));
        ChoiceBox<ArrowType> arrowTypeChoiceBox = new ChoiceBox<>();
        arrowTypeChoiceBox.getItems().addAll(ArrowType.values());
        arrowTypeChoiceBox.getSelectionModel().select(ArrowType.getValueOf(at));

        Button btn = new Button("Zatwierdź");

        properties.setContent(getPropertiesGridPane(labelName, lineName, arrowName, widthName, valueName, duplexName,
                colorPicker,
                labelField, lineTypeChoiceBox, arrowTypeChoiceBox, widthField, valueField, duplexBox,
                btn));

        duplexBox.setOnAction(e -> {
            if(!selected.isEmpty())
                selected.forEach(s -> ((Vertex) s).setDuplex(duplexBox.isSelected()));
        });

        btn.setOnAction(e -> {
            if(!selected.isEmpty() && !labelField.getText().isEmpty())
                selected.forEach(s -> s.setLabel(labelField.getText()));

            controller.getProject().getActionHolder().add(MultiAction.applyVertexPropertiesChange(controller.getProject(), lineTypeChoiceBox, arrowTypeChoiceBox, widthField, colorPicker, valueField));
            controller.getProject().draw();
        });


    }

    public void writeToFile(FileWriter writer, boolean json, int indent) throws IOException {
        String ind = indent(indent), ind1 = ind + "  ";
        super.writeToFile(writer, json, indent, "Vertex");
        int codeFrom = fromNode.hashCode(), codeTo = toNode.hashCode();

        writer.append(ind1 + (json ? "\"from-id\": \"" + codeFrom + "\"\n" +
                ind1 + "\"to-id\": " + codeTo + "\"\n" + ind1 + "\"is-duplex\": \"" + duplex + "\"\n" + ind + "}\n" :
                "from-id=\"" + codeFrom + "\" to-id=\"" + codeTo + "\" is-duplex=\"" + duplex + "\" />"
                ));
    }

    private void setDuplex(boolean isDuplex) {
        duplex = isDuplex;
        draw();
    }

    private Point2D getCenterForLabel() {
        int size = points.size();
        ListIterator it = points.listIterator(size/2);
        VertexPoint point = (VertexPoint) it.next();
        double x = point.getX(), y = point.getY();
        if(size % 2 == 0){
            VertexPoint next = (VertexPoint) it.next();
            x += next.getX();
            x /= 2;
            y += next.getY();
            y /= 2;
        }
        x -= 10;
        y -= 10;
        return new Point2D(x,y);
    }

    public void translateNode(Node node, double dx, double dy) {
        VertexPoint point = (node == fromNode ? points.getFirst() : points.getLast());
        Point2D newPoint = new Point2D(point.getX() + dx, point.getY() + dy);
        move(point, newPoint);
    }

    public Node getToNode() {
        return toNode;
    }

    @Override
    public void checkSelect(Rectangle rectangle) {
        setSelected(rectangle.contains(xl, yu) && rectangle.contains(xr, yd));
    }

    public VertexPoint interactedPoint(double x, double y){
        for(VertexPoint point : points)
            if(point.contains(x, y))
                return point;
        return null;
    }

    @Override
    public boolean checkSelect(double x, double y) {
        return interactedPoint(x, y) != null;
    }

    public boolean isSelected() {
        return selected;
    }

    public Node getFromNode() {
        return fromNode;
    }


    public Vertex copy(){
        Vertex ret = new Vertex(this);
        controller.request(true, fromNode, ret, this);
        controller.request(false, toNode, ret, this);
        return ret;
    }

    @Override
    public void delete() {
        Project project = controller.getProject();
        controller.getProject().getActionHolder().add(VertexCreation.applyDelete(project.getUndo(), this, project.getRedo()));
    }

    public void finishDelete(){
        fromNode.deleteVertex(this);
        fromNode = null;
        toNode.deleteVertex(this);
        toNode = null;
//        pane.getChildren().remove(path);
        controller.getProject().removeObject(this);
//        if(label != null) label.hide();
        setSelected(false);
    }

    @Override
    public boolean isNode() {
        return false;
    }

    @Override
    public void refresh(Project project) {
        super.refresh(project);
        points.forEach(p -> p.refresh(this));
        draw();
    }

    public void setFrom(Node node) {
        fromNode = node;
    }

    public void setTo(Node node) {
        toNode = node;
    }

    public Point2D getFromPoint() {
        return points.getFirst().getPoint();
    }

    public Point2D getToPoint() {
        return points.getLast().getPoint();
    }

    public ArrowType getArrowType() {
        return arrowType;
    }

    public LineType getLineType() {
        return lineType;
    }

    public VertexType getVertexType() {
        return vertexType;
    }

    public boolean isDuplex() {
        return duplex;
    }

    @Override
    public String toString(){
        return super.toString() + " (" + value + ")";
    }

    public void setLineType(LineType lineType) {
        this.lineType = lineType;
    }

    public void setArrowType(ArrowType arrowType) {
        this.arrowType = arrowType;
    }

    public void moveInteractedPoint(double x, double y, double nx, double ny) {
        move(interactedPoint(x, y), new Point2D(nx, ny));
        forceProjectDraw();
    }
}

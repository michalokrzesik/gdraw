package gdraw.graph.node;

import gdraw.graph.util.Selectable;
import gdraw.graph.vertex.ArrowType;
import gdraw.graph.vertex.LineType;
import gdraw.graph.vertex.VertexPoint;
import gdraw.main.MainController;
import gdraw.main.Project;
import javafx.scene.Group;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.geometry.Point2D;
import java.util.ArrayList;

import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

import gdraw.graph.vertex.Vertex;
import gdraw.graph.util.Label;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

public class Node extends Selectable {
    private ImageView imageView;
    protected Point2D center;
    protected double width;
    protected double height;
    protected Image image;
    protected boolean isGroupNodes;
    protected ArrayList<Node> subNodes;
    protected ArrayList<Vertex> vertices;
    protected Label label;
    protected Group group;
    protected boolean isCollapsed;
    protected double widthCollapsed;
    protected double heightCollapsed;
    protected boolean selected;
    private Circle[] circles = new Circle[8];
    private boolean hidden;

    protected TreeItem<Node> treeItem;

    public Node(Point2D center, Image image, Group group, TreeItem<Node> parent, MainController mainController){
        mainController.addObject(this);
        controller = mainController;
        this.center = center;
        this.image = image;
        hidden = true;
        width = image.getWidth();
        height = image.getHeight();
        widthCollapsed = width;
        heightCollapsed = height;
        isCollapsed = false;
        isGroupNodes = false;
        vertices = new ArrayList<>();
        this.group = group;
        makeImageView();
        setSelected();
        setCircles();

        if(parent != null) {
            treeItem = new TreeItem<>(this);
            parent.getChildren().add(treeItem);
            treeItem.setGraphic(imageView);
        }

    }

    private void makeImageView() {
        imageView = new ImageView(image);
        imageView.setOnMouseClicked(e -> setSelected(e));
        imageView.setOnContextMenuRequested(e -> contextMenu());
        imageView.setOnMousePressed(e -> onMousePressed(e));
        imageView.setOnMouseDragged(e -> onMouseDragged(e));

    }

    public Node(Node node) {
        controller = node.controller;
        center = node.center;
        image = node.image;
        makeImageView();
        hidden = node.hidden;
        width = node.width;
        widthCollapsed = node.widthCollapsed;
        height = node.height;
        heightCollapsed = node.heightCollapsed;
        isGroupNodes = node.isGroupNodes;
        isCollapsed = node.isCollapsed;

        setCircles();
    }

    private void setCircles() {
        double w = getWidth(), h = getHeight();
        double x = center.getX() - w/2, y = center.getY() - h/2;
        circles[0].setCenterX(x); circles[0].setCenterY(y);
        circles[1].setCenterX(x); circles[1].setCenterY(y + h/2);
        circles[2].setCenterX(x); circles[2].setCenterY(y + h);
        circles[3].setCenterX(x + w/2); circles[3].setCenterY(y + h);
        circles[4].setCenterX(x + w); circles[4].setCenterY(y + h);
        circles[5].setCenterX(x + w); circles[5].setCenterY(y + h/2);
        circles[6].setCenterX(x + w); circles[6].setCenterY(y);
        circles[7].setCenterX(x + w/2); circles[7].setCenterY(y);

        for(int i = 0; i < circles.length; i++){
            circles[i].setFill(Color.BLUE);
            circles[i].setStroke(Color.BLUE);
            circles[i].setRadius(3);
            int finalI = i;
            circles[i].setOnMouseDragged(e -> CircleHelper.move(this, finalI, e.getX() - circles[finalI].getCenterX(), e.getY() - circles[finalI].getCenterY()));
        }
    }

    public Node(Point2D center, Image image, Group group, boolean isGroupNodes, TreeItem<Node> parent, MainController mainController){
        this(center, image, group, parent, mainController);
        this.isGroupNodes = isGroupNodes;
        if(isGroupNodes) subNodes = new ArrayList<>();
    }

    public void setSelected(boolean selected) {
        controller.getProject().setDragModel(NodeDragModel.Standard);
        this.selected = selected;
        draw();
    }

    public void setLabel(String newLabel){
        if(label == null){
            label = new Label(
                    newLabel,
                    new Point2D(center.getX() - 10, center.getY() - 10)
            );
        }
        else label.setLabel(newLabel);
    }

    public Node copy(TreeItem<Node> parent){
        Node ret = new Node(new Point2D(center.getX() + 5, center.getY() + 5), image, group, isGroupNodes, parent, controller);
        if(this.isGroupNodes)
            for(Node node : subNodes)
                node.copy(ret.getTreeItem());
        vertices.forEach(vertex -> {
            vertex.copy();
            controller.request(vertex.getFromNode() == this, ret, vertex, this);
        });
        return ret;
    }

    public TreeItem<Node> getTreeItem() {
        return treeItem;
    }

    public void addVertex(Vertex vertex){
        if(!vertices.contains(vertex)) vertices.add(vertex);
    }

    public void removeVertex(Vertex vertex){
        vertices.remove(vertex);
    }

    public Vertex newVertex(Point2D start, Point2D stop, Node toNode, ArrowType arrow, LineType line, boolean isDuplex, boolean isCurved, double width, double value, Paint color){
        VertexPoint startVP = new VertexPoint(start), stopVP = new VertexPoint(stop);
        startVP.setPointBounded(start, this);
        stopVP.setPointBounded(stop, toNode);
        Vertex vertex = new Vertex(this, toNode, startVP.getPoint(), stopVP.getPoint(), group, arrow, line, isDuplex, isCurved, width, color, controller);
        vertex.setValue(value);
        vertices.add(vertex);
        return vertex;
    }

    public void groupNodes(){
        isGroupNodes = true;
        if(subNodes == null) subNodes = new ArrayList<>();
    }

    public void groupNodes(ArrayList<Node> nodes){
        groupNodes();
        for(Node node : nodes)
            groupNodes(node);
    }

    public void groupNodes(Node node){
        TreeItem<Node> nodeTI = node.getTreeItem();
        nodeTI.getParent().getChildren().remove(nodeTI);
        treeItem.getChildren().add(nodeTI);
        subNodes.add(node);
        expandIfNeeded();
        node.fitInGroup();
    }

    private void expandIfNeeded() {
        double neededW = 5, neededH = 5;
        double xmin = Double.MAX_VALUE, xmax = 0, ymin = xmin, ymax = 0;

        for (Node node : subNodes){
            double x = node.getCenter().getX() - node.getWidth()/2, y = node.getCenter().getY() - node.getHeight()/2;

            xmin = x < xmin ? x : xmin;
            xmax = x > xmax ? x : xmax;
            ymin = y < ymin ? y : ymin;
            ymax = y > ymax ? y : ymax;
        }

        neededW += xmax - xmin;
        neededH += ymax - ymin;

        if(getWidth() < neededW) setWidth(neededW);
        if(getHeight() < neededH) setHeight(neededH);
        fitInGroup();
    }

    private void fitInGroup() {
        Node parent = treeItem.getParent().getValue();
        double px = parent.getCenter().getX(), py = parent.getCenter().getY(), pw = parent.getWidth(), ph = parent.getHeight();
        double x = center.getX(), y = center.getY();

        if(getWidth() > parent.getWidth()) setWidth(parent.getWidth());
        if(getHeight() > parent.getHeight()) setHeight(parent.getHeight());
        double w = getWidth(), h = getHeight();

        double dx = (x - w/2) - (px - pw/2), dy = (y - h/2) - (py - ph/2);
        dx = (dx > 0 ? dx : (x + w/2) - (px + pw/2));
        dy = (dy > 0 ? dy : (y + h/2) - (py + ph/2));

        translate(dx, dy);
    }

    public void changeGroupToNode() {
        unGroup(subNodes);
    }

    public void unGroup(ArrayList<Node> nodes) {
        for (Node node : nodes)
            unGroup(node);
        if (subNodes.isEmpty()) {
            subNodes = null;
            isGroupNodes = false;
        }
    }

    public void unGroup(Node node) {
        TreeItem<Node> nodeTI = node.getTreeItem();
        treeItem.getChildren().remove(nodeTI);
        treeItem.getParent().getChildren().add(nodeTI);
    }


    public Point2D getCenter() {
        return center;
    }

    public double getHeight() {
        return isCollapsed ? heightCollapsed : height;
    }

    public void setHeight(double h){
        double dh;
        if(isCollapsed) {
            dh = h - heightCollapsed;
            heightCollapsed = h;
        }
        else {
            dh = h - height;
            height = h;
        }
        translate(0, dh);
    }

    public double getWidth() {
        return isCollapsed ? widthCollapsed : width;
    }

    public void setWidth(double w){
        double dw;
        if(isCollapsed) {
            dw = w - widthCollapsed;
            widthCollapsed = w;
        }
        else {
            dw = w - width;
            width = w;
        }
        translate(dw,0);
    }

    public void draw(){
        hide();
        vertices.forEach((Vertex v) -> v.draw(this));
        group.getChildren().add(imageView);
        hidden = false;

        double w = getWidth(), h = getHeight();
        double x = center.getX() - w/2, y = center.getY() - h/2;
        imageView.setFitWidth(w);
        imageView.setFitHeight(h);
        imageView.setLayoutX(x);
        imageView.setLayoutY(y);
        if(selected){
            setCircles();
            group.getChildren().addAll(circles);
        }
        else{
            group.getChildren().removeAll(circles);
        }

        if(isGroupNodes) subNodes.forEach((Node n) -> n.draw());
    }

    private void hide() {
        if(!hidden) {
            group.getChildren().remove(imageView);
            subNodes.forEach(node -> node.hide());
            hidden = true;
        }
    }

    public void translate(double dx, double dy){
        double x = center.getX() + dx, y = center.getY() + dy;
        center = new Point2D(x, y);
        fitInGroup();
        vertices.forEach((Vertex v) -> v.translateNode(this, dx, dy));
        if(isGroupNodes) subNodes.forEach((Node n) -> n.translate(dx, dy));
    }

    @Override
    public Selectable copy() {
        Node ret = new Node(this);
        ret.treeItem = new TreeItem<>(ret);
        vertices.forEach(vertex -> {
            if(vertex.isSelected()) {
                controller.request(vertex.getFromNode() == this, ret, vertex, this);
            }
        });
        subNodes.forEach(node -> node.copy(ret.treeItem));
        return ret;
    }

    @Override
    public void delete() {
        vertices.forEach(v -> v.delete());
        controller.getProject().removeObject(this);
        TreeItem<Node> parent = treeItem.getParent();
        parent.getValue().removeSubNode(this);
        parent.getChildren().remove(treeItem);
        setSelected(false);
        group.getChildren().remove(imageView);
    }

    @Override
    public boolean isNode() {
        return true;
    }

    @Override
    public void refresh(Project project) {
        group = project.getGroup();
        draw();
    }

    private void removeSubNode(Node node) {
        subNodes.remove(node);
    }

    @Override
    public void checkSelect(Rectangle rectangle) {
        double w = getWidth()/2, h = getHeight()/2;
        setSelected(rectangle.contains(center.getX() - w, center.getY() - h)
                && rectangle.contains(center.getX() + w, center.getY() + h));
    }

    public void contextMenu() {
        Project project = controller.getProject();
        project.setDragModel(NodeDragModel.Standard);
        //TODO odwołaj się do selected z projektu
        //Jeżeli nie ma w selected, to clear i dodaj
        //dla multiSelect
            // grupuj tworzące nowy node o center pośrodku wszystkich, width i height obejmującymi wszystkie +5 i biorącym wszystkie jako childreny w treeView
                //Przy różnych parentach wyszukaj nabliższego wspólnego i zrób grupę pod nim.
            // rozgrupuj, jeżeli mają wspólnego parenta
        //dla pojedynczego selecta (node'a)
            // zmień tryb pomiędzy node a group
            // zwiń/rozwiń, działające dla grupy (można zapisać ten MenuItem i zmieniać jego dostępność przy zmianie pomiędzy trybami)
    }

    public void deleteVertex(Vertex vertex) {
        vertices.remove(vertex);
    }
}

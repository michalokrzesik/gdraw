package gdraw.graph.node;

import gdraw.graph.util.DragModel;
import gdraw.graph.util.Selectable;
import gdraw.graph.util.action.MultiAction;
import gdraw.graph.vertex.ArrowType;
import gdraw.graph.vertex.LineType;
import gdraw.graph.vertex.VertexPoint;
import gdraw.main.MainController;
import gdraw.main.Project;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.effect.BlendMode;
import javafx.scene.image.Image;
import javafx.geometry.Point2D;
import java.util.ArrayList;

import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;

import gdraw.graph.vertex.Vertex;
import gdraw.graph.util.Label;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

public class Node extends Selectable {
//    protected transient Canvas imageView;
    protected Point2D center;
    protected double width;
    protected double height;
    protected Image image;
    protected boolean isGroupNodes;
    protected ArrayList<Node> subNodes;
    protected ArrayList<Vertex> vertices;
    protected boolean isCollapsed;
    protected double widthCollapsed;
    protected double heightCollapsed;
    private transient Circle[] circles;
    private transient boolean hidden;
    private Node parent;

    protected transient TreeItem<Node> treeItem;

    public Node(Point2D center, Image image, Canvas canvas, TreeItem<Node> parent, MainController mainController){
        this(center, image, canvas, parent, mainController, true);
    }

    public Node(Point2D center, Image image, Canvas canvas, TreeItem<Node> parent, MainController mainController, boolean isNode){
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
        subNodes = new ArrayList<>();
        //this.pane = pane;
        this.canvas = canvas;
        treeItem = new TreeItem<>(this);

        if(isNode) {
            mainController.addObject(this);
            //makeImageView();
            setSelected();
            setCircles();
            parent.getChildren().add(treeItem);
            ImageView graphic = new ImageView(image);
            graphic.setFitHeight(10);
            graphic.setFitWidth(10);
            treeItem.setGraphic(graphic);
            draw();
        }

    }

    public void setParent() {
        parent = treeItem.getParent().getValue();
    }

//    private void makeImageView() {
//        //imageView = new ImageView(image);
//        imageView = new Canvas(getWidth(), getHeight());
//        imageView.setOnMouseClicked(this::setSelected);
//        imageView.setOnContextMenuRequested(controller::contextMenu);
//        imageView.setOnMousePressed(this::onMousePressed);
//        imageView.setOnMouseDragged(this::onMouseDragged);
//        imageView.setOnMouseReleased(this::onMouseReleased);
//
//    }

    public Node(Node node) {
        controller = node.controller;
        center = node.center;
        image = node.image;
//        makeImageView();
        hidden = node.hidden;
        width = node.width;
        widthCollapsed = node.widthCollapsed;
        height = node.height;
        heightCollapsed = node.heightCollapsed;
        isGroupNodes = node.isGroupNodes;
        isCollapsed = node.isCollapsed;
        treeItem = new TreeItem<>(this);

        setCircles();
    }

    private void setCircles() {
        double w = getWidth(), h = getHeight();
        double x = center.getX() - w/2, y = center.getY() - h/2;
        circles = new Circle[8];

        circles[0] = new Circle(2); circles[0].setCenterX(x); circles[0].setCenterY(y);
        circles[1] = new Circle(2); circles[1].setCenterX(x); circles[1].setCenterY(y + h/2);
        circles[2] = new Circle(2); circles[2].setCenterX(x); circles[2].setCenterY(y + h);
        circles[3] = new Circle(2); circles[3].setCenterX(x + w/2); circles[3].setCenterY(y + h);
        circles[4] = new Circle(2); circles[4].setCenterX(x + w); circles[4].setCenterY(y + h);
        circles[5] = new Circle(2); circles[5].setCenterX(x + w); circles[5].setCenterY(y + h/2);
        circles[6] = new Circle(2); circles[6].setCenterX(x + w); circles[6].setCenterY(y);
        circles[7] = new Circle(2); circles[7].setCenterX(x + w/2); circles[7].setCenterY(y);

    }

    public Node(Point2D center, Image image, Canvas canvas, boolean isGroupNodes, TreeItem<Node> parent, MainController mainController){
        this(center, image, canvas, parent, mainController);
        this.isGroupNodes = isGroupNodes;
        subNodes = new ArrayList<>();
    }

    public void setSelected(boolean selected) {
        controller.getProject().setDragModel(DragModel.Standard);
        this.selected = selected;
        draw();
    }

    public void setLabel(String newLabel){
        if(label == null){
            label = new Label(
                    newLabel, canvas,
                    new Point2D(center.getX() - 10, center.getY() - 10)
            );
        }
        else label.setLabel(newLabel);
    }

    @Override
    public void setProperties(ScrollPane properties, ArrayList<Selectable> selected) {

        javafx.scene.control.Label labelName = new javafx.scene.control.Label("Etykieta:"),
                centerName = new javafx.scene.control.Label("Środek:"),
                xName = new javafx.scene.control.Label("x: "),
                yName = new javafx.scene.control.Label("y: "),
                widthName = new javafx.scene.control.Label("Szerokość:"),
                heightName = new javafx.scene.control.Label("Wysokość:"),
                groupName = new javafx.scene.control.Label("Jest grupą?");

        double xmin = Double.MAX_VALUE, xmax = 0, ymin = Double.MAX_VALUE, ymax = 0, width = getWidth(), height = getHeight();
        boolean isGroup = this.isGroupNodes;

        for(Selectable s : selected){
            Node node = (Node) s;
            double x = node.getCenter().getX(), y = node.getCenter().getY();
            xmin = x < xmin ? x : xmin; xmax = x > xmax ? x : xmax;
            ymin = y < ymin ? y : ymin; ymax = y > ymax ? y : ymax;
            isGroup = isGroup && node.isGroupNodes();
            if(width != node.getWidth()) width = -1;
            if(height != node.getHeight()) height = -1;
        }

        TextField labelField = new TextField(),
                xField = new TextField(Double.toString((xmax + xmin)/2)), yField = new TextField(Double.toString((ymax + ymin)/2)),
                widthField = new TextField(width < 0 ? Double.toString(width) : ""), heightField = new TextField(height < 0 ? Double.toString(height) : "");

        CheckBox groupBox = new CheckBox(); groupBox.selectedProperty().setValue(isGroup);

        Button btn = new Button("Zatwierdź");

        properties.setContent(
                getPropertiesGridPane(labelName, xName, yName, widthName, heightName, groupName,
                        centerName,
                        labelField, xField, yField, widthField, heightField, groupBox,
                        btn)
        );

        groupBox.setOnAction(e -> {
            if(groupBox.isSelected()) controller.nodesToGroups(e);
            else controller.groupsToNodes(e);
        });

        double finalXmax = xmax;
        double finalXmin = xmin;
        double finalYmax = ymax;
        double finalYmin = ymin;
        btn.setOnAction(e -> {
            if(!selected.isEmpty()) selected.forEach(s -> s.setLabel(labelField.getText()));
            double x = (finalXmax + finalXmin)/2, y = (finalYmax + finalYmin)/2, w, h;
            try{
                x = Double.parseDouble(xField.getText()) - x;
            } catch(Exception e1) { x = (finalXmax + finalXmin)/2; }
            try{
                y = Double.parseDouble(yField.getText()) - y;
            } catch(Exception e1) { y = (finalYmax + finalYmin)/2; }
            try{
                w = Double.parseDouble(widthField.getText());
            } catch(Exception e1) { w = -1; }
            try{
                h = Double.parseDouble(heightField.getText());
            } catch(Exception e1) { h = -1; }

            controller.getProject().getActionHolder().add(MultiAction.applyNodePropertiesChange(controller.getProject(), x, y, w, h));
            controller.getProject().draw();
        });
    }

    public Node copy(TreeItem<Node> parent){
        Node ret = new Node(new Point2D(center.getX() + 5, center.getY() + 5), image, canvas, isGroupNodes, parent, controller);
        if(this.isGroupNodes && !this.subNodes.isEmpty())
            for(Node node : subNodes)
                node.copy(ret.getTreeItem());
        if(!vertices.isEmpty()) vertices.forEach(vertex -> {
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

    public Vertex newVertex(Point2D start, Point2D stop, Node toNode, ArrowType arrow, LineType line, boolean isDuplex, boolean isCurved, double width, double value, Color color){
        VertexPoint startVP = new VertexPoint(start), stopVP = new VertexPoint(stop);
        startVP.setPointBounded(start, this);
        stopVP.setPointBounded(stop, toNode);
        Vertex vertex = new Vertex(this, toNode, startVP.getPoint(), stopVP.getPoint(), canvas, arrow, line, isDuplex, isCurved, width, color, controller);
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
        TreeItem<Node> nodeTI = node.getTreeItem(), parent = nodeTI.getParent();
        parent.getChildren().remove(nodeTI);
        parent.getValue().removeSubNode(node);
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
        TreeItem<Node> p = treeItem.getParent();
        if(p == null) return;
        Node parent = p.getValue();
        double px = parent.getCenter().getX(), py = parent.getCenter().getY(),
                pw = parent.getWidth(), ph = parent.getHeight(),
                x = center.getX(), y = center.getY();

        if(getWidth() > pw) setWidth(pw);
        if(getHeight() > ph) setHeight(ph);
        double w = getWidth(), h = getHeight();

        double dx = (x - w/2) - (px - pw/2), dy = (y - h/2) - (py - ph/2);
        dx = (dx > 0 ? dx : (x + w/2) - (px + pw/2));
        dy = (dy > 0 ? dy : (y + h/2) - (py + ph/2));

        translate(dx, dy, false);
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
        treeItem.getParent().getValue().groupNodes(node);
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

    public void draw(Canvas canvas){
        this.canvas = canvas;
        draw();
    }

    public void draw(){
        //hide();
        if(!vertices.isEmpty())
            vertices.forEach((Vertex v) -> v.draw(this));
//        if(!pane.getChildren().contains(imageView)) pane.getChildren().add(imageView);
//        imageView.toFront();
        //hidden = false;

        double w = getWidth(), h = getHeight();
        double x = center.getX() - w/2, y = center.getY() - h/2;
//        imageView.setFitWidth(w);
//        imageView.setFitHeight(h);
//        imageView.setX(x);
//        imageView.setY(y);
//        imageView.setLayoutX(x);
//        imageView.setLayoutY(y);
//        imageView.getGraphicsContext2D().drawImage(image, 0, 0, w, h);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setGlobalBlendMode(BlendMode.SRC_OVER);
        gc.drawImage(image, x, y, w, h);
        if(selected) {
            setCircles();
            gc.setStroke(Color.BLUE);
            gc.setFill(Color.BLUE);
            for (int i = 0; i < circles.length; i++)
                gc.fillOval(circles[i].getCenterX() - 1, circles[i].getCenterY() - 1, 2, 2);
        }
        if(!subNodes.isEmpty()) {
            if (!isCollapsed) subNodes.forEach(Node::draw);
        }
        if(label != null) label.draw();
    }

//    private void hide() {
//        if(!hidden) {
//            if(label != null) label.hide();
//            imageView.toBack();
//            if(!subNodes.isEmpty())
//                subNodes.forEach(Node::hide);
//            hidden = true;
//        }
//    }

    @Override
    public void translate(double dx, double dy){
        translate(dx, dy, true);
    }

    public void translate(double dx, double dy, boolean fit){
        double x = center.getX() + dx, y = center.getY() + dy;
        center = new Point2D(x, y);
//        imageView.setX(x);
//        imageView.setY(y);
//        imageView.setLayoutY(y);
//        imageView.setLayoutX(x);
        if(fit) fitInGroup();
        else {
            if (!vertices.isEmpty())
                vertices.forEach(v -> v.translateNode(this, dx, dy));
            if (isGroupNodes && !subNodes.isEmpty()) subNodes.forEach(n -> n.translate(dx, dy));
        }
    }

    @Override
    public Selectable copy() {
        Node ret = new Node(this);
        ret.treeItem = new TreeItem<>(ret);
        if(!vertices.isEmpty())
            vertices.forEach(vertex -> {
                if (vertex.isSelected()) {
                    controller.request(vertex.getFromNode() == this, ret, vertex, this);
                }
            });
        if(!subNodes.isEmpty())
            subNodes.forEach(node -> node.copy(ret.treeItem));
        return ret;
    }

    @Override
    public void delete() {
        controller.getProject().removeObject(this);
        TreeItem<Node> parent = treeItem.getParent();
        parent.getValue().removeSubNode(this);
        parent.getChildren().remove(treeItem);
        setSelected(false);
//        pane.getChildren().remove(imageView);
//        if(label != null) label.hide();
    }

    @Override
    public boolean isNode() {
        return true;
    }

    @Override
    public void refresh(Project project) {
        superRefresh(project);
//        makeImageView();
        selected = false;
        treeItem = new TreeItem<>(this);
        parent.getTreeItem().getChildren().add(treeItem);
        if(!subNodes.isEmpty())
            subNodes.forEach(n -> n.refresh(project));
        if(!vertices.isEmpty())
            vertices.forEach(v -> v.refresh(project));
        draw();
    }

    private void removeSubNode(Node node) {
        subNodes.remove(node);
    }

    @Override
    public void checkSelect(Rectangle rectangle) {
        double w = getWidth()/2, h = getHeight()/2, x = center.getX(), y = center.getY();
        setSelected(rectangle.contains(x - w, y - h)
                && rectangle.contains(x + w, y + h));
    }

    @Override
    public boolean checkSelect(double x, double y) {
        double xc = center.getX(), yc = center.getY(), w = getWidth(), h = getHeight();
        return (xc - w <= x) && (xc + w >= x) && (yc - h <= y) && (yc + h >= y);
    }

    public void deleteVertex(Vertex vertex) {
        vertices.remove(vertex);
    }

    public void setWH(double width, double height, double widthCollapsed, double heightCollapsed) {
        this.width = width; this.widthCollapsed = widthCollapsed;
        this.height = height; this.heightCollapsed = heightCollapsed;
    }

    public void setCollapsed(boolean isCollapsed) {
        this.isCollapsed = isCollapsed;
        draw();
    }

    public Image getImage() {
        return image;
    }

    public Canvas getProjectCanvas() {
        return canvas;
    }

    public boolean isGroupNodes() {
        return isGroupNodes;
    }

    public boolean isCollapsed() {
        return isCollapsed;
    }

    public MainController getController() {
        return controller;
    }

    public ArrayList<Vertex> getVertices() {
        return vertices;
    }

    public ArrayList<Node> getSubNodes() {
        return subNodes;
    }

    @Override
    public void contextMenu(ContextMenu contextMenu){
        super.contextMenu(contextMenu);

        SeparatorMenuItem separatorMenuItem = new SeparatorMenuItem();

        Menu grouping = new Menu();
        contextMenu.getItems().addAll(separatorMenuItem, grouping);

        MenuItem group = new MenuItem("Grupuj zaznaczone");
        group.setOnAction(controller::groupSelected);

        MenuItem ungroup = new MenuItem("Oddziel zaznaczone");
        ungroup.setOnAction(controller::ungroupSelected);

        MenuItem nodesToGroups = new MenuItem("Zamień zaznaczone w puste grupy");
        nodesToGroups.setOnAction(controller::nodesToGroups);

        MenuItem groupsToNodes = new MenuItem("Zamień zaznaczone w zwykłe węzły (oddziel zawartość)");
        groupsToNodes.setOnAction(controller::groupsToNodes);

        SeparatorMenuItem separatorMenuItem1 = new SeparatorMenuItem();

        MenuItem collapse = new MenuItem("Zwiń");
        collapse.setOnAction(controller::collapse);

        MenuItem extend = new MenuItem("Rozwiń");
        extend.setOnAction(controller::extend);

        grouping.getItems().addAll(group, ungroup, nodesToGroups, groupsToNodes, separatorMenuItem1, collapse, extend);
    }

    protected void superRefresh(Project project) {
        super.refresh(project);
    }
}

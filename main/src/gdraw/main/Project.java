package gdraw.main;

import gdraw.graph.node.Node;
import gdraw.graph.node.NodeDragModel;
import gdraw.graph.util.Background;
import gdraw.graph.util.MIandButtonPair;
import gdraw.graph.util.Selectable;
import gdraw.graph.util.action.*;
import gdraw.graph.vertex.ArrowType;
import gdraw.graph.vertex.LineType;
import javafx.collections.ObservableList;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.ListIterator;

public class Project implements Serializable {

    private File file;
    private TreeView<Node> nodes;
    private ArrayList<Selectable> graphObjects;
    private ArrayList<Selectable> selected;
    private Tab tab;
    private String name;
    private MainController controller;
    private Group group;
    private ScrollPane properties;
    private double x, y;
    private NodeDragModel dragModel;

    private ActionHelper undo;
    private ActionHelper redo;

    public Project(MainController mainController, String projectName, Group group, Canvas canvas, ScrollPane scrollPane, MIandButtonPair undoFXML, MIandButtonPair redoFXML) {
        name = projectName;
        properties = scrollPane;
        graphObjects = new ArrayList<>();
        selected = new ArrayList<>();
        dragModel = NodeDragModel.Standard;

        undo = new ActionHelper(undoFXML);
        redo = new ActionHelper(redoFXML);

        file = null;
        controller = mainController;
        nodes = new TreeView<>();
        this.group = new Group();
        Background background = new Background(controller, canvas, new Image("/white.png"), this.group, canvas.getWidth(), canvas.getHeight());
        this.group.getChildren().add(canvas);
        nodes.setEditable(true);
        nodes.setRoot(background.getTreeItem());
        nodes.getSelectionModel().selectedItemProperty().addListener((observableValue, oldTreeItem, newTreeItem) -> {
            oldTreeItem.getValue().setSelected(false);
            newTreeItem.getValue().setSelected(true);
        });
        nodes.setOnContextMenuRequested(e -> {
            ObservableList<TreeItem<Node>> selectedTI = nodes.getSelectionModel().getSelectedItems();
            if(selectedTI.size() > 0) {
                ContextMenu contextMenu = new ContextMenu();
                selectedTI.get(0).getValue().contextMenu(contextMenu);
                contextMenu.show(nodes, e.getScreenX(), e.getScreenY());
            };
        });
    }

    public NodeDragModel getDragModel() { return dragModel; }

    public void setDragModel(NodeDragModel model) { dragModel = model; }

    public void checkSelect(double x1, double y1, double x2, double y2) {
        Rectangle rectangle = new Rectangle(x1, y1, x2 - x1, y2 - y1);
        graphObjects.forEach(s -> s.checkSelect(rectangle));
    }

    public void clearSelected() {
        selected.forEach(s -> s.setSelected(false));
        selected.clear();
        setProperties();
    }

    public Tab getTab() {
        return tab;
    }

    public String getName() {
        return name;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public File getFile() {
        return file;
    }

    public void setTab(Tab tab) {
        this.tab = tab;
    }

    public Background getBackground() {
        return (Background) nodes.getRoot().getValue();
    }

    public void draw() {
        TreeItem<Node> root = nodes.getRoot();
        root.getValue().draw();
        root.getChildren().forEach(treeItem -> treeItem.getValue().draw());
    }

    public WritableImage snapshot() {
        return group.snapshot(new SnapshotParameters(), null);
    }

    public void select(Selectable item, boolean ctrlPressed) {
        if(!ctrlPressed) {
            clearSelected();
            selected.add(item);
        }
        else if(selected.contains(item)) {
            selected.remove(item);
            item.setSelected(false);
        }
        setProperties();
    }

    public void onMousePressed(MouseEvent e, Selectable item) {
        if(!item.isNode()) dragModel = NodeDragModel.Standard;
        dragModel.pressed(this, e, item);
    }

    public void onMouseDragged(MouseEvent e, Selectable item) {
        dragModel.dragged(this, e, item);
    }

    public TreeView<Node> getTreeView() {
        return nodes;
    }

    public void setProperties() {
        if(selected.isEmpty()) properties.setContent(null);
    }

    public void refresh(MainController mainController, Tab tab, Group group, Canvas canvas, ScrollPane scrollPane) {
        controller = mainController;
        this.tab = tab;
        //TODO

    }

    public Group getGroup() {
        return group;
    }

    public void newObject(Selectable object){
        graphObjects.add(object);
    }

    public void removeObject(Selectable object){
        graphObjects.remove(object);
    }

    public void undo(){
        undo.pop();
    }

    public void redo(){
        redo.pop();
    }

    public ArrayList<Selectable> getSelected() {
        return selected;
    }

    public void deleteSelected() {
        MultiAction.applyDelete(undo, selected, redo);
    }

    public void paste(ArrayList<Selectable> clipboard) {
        MultiAction.applyCreate(undo, clipboard, redo);
        /*
        clipboard.forEach(s -> {
            graphObjects.add(s);
            if(s.isNode()){
                nodes.getRoot().getChildren().add(((Node)s).getTreeItem());
            }
            s.refresh(this);
        });*/
    }

    public void addVertex(LineType lineType, ArrowType arrowType, Color color, boolean duplex, boolean curved, String width) {
            dragModel = NodeDragModel.Vertex;
            dragModel.set(lineType, arrowType, color, duplex, curved, width);
    }

    public void onMouseReleased(MouseEvent e, Selectable item) {
        dragModel.released(this, e, item);
        dragModel = NodeDragModel.Standard;
    }

    public void groupSelected() {
        if(selected.isEmpty()) return;
        TreeItem<Node> parent = nodes.getRoot();
        ArrayList<Double> minMaxs = new ArrayList<>();
        ArrayList<Node> nodes = new ArrayList<>();
        minMaxs.add(Double.MAX_VALUE); minMaxs.add(0.0); minMaxs.add(Double.MAX_VALUE); minMaxs.add(0.0);
        selected.forEach(s -> {
            if(s.isNode()){
                Node node = (Node) s;
                nodes.add(node);
                double x = node.getCenter().getX(), y = node.getCenter().getY();
                ListIterator<Double> it = minMaxs.listIterator();
                double xmin = it.next(); double xmax = it.next();
                double ymin = it.next(); double ymax = it.next();
                minMaxs.clear();

                xmin = x < xmin ? x : xmin;
                xmax = x > xmax ? x : xmax;
                ymin = y < ymin ? y : ymin;
                ymax = y > ymax ? y : ymax;

                minMaxs.add(xmin); minMaxs.add(xmax); minMaxs.add(ymin); minMaxs.add(ymax);
            }
        });


        Node groupNode = new Node(
                new Point2D((minMaxs.get(0) + minMaxs.get(1))/2, (minMaxs.get(2) + minMaxs.get(3))/2),
                new Image("standardGroup.png"),
                group, true, parent, controller);
        new SelectableCreationListener(groupNode);

        GroupManagement.applyGroup(undo, groupNode, parent, nodes, redo);

//        groupNode.groupNodes(nodes);

    }

    public void ungroupSelected() {
/*        boolean isGroup = true;
        Node parent = null;
        ArrayList<Node> sNodes = new ArrayList<>();
        for(Selectable item : selected){
            if(item.isNode()){
                Node node = (Node) item;
                sNodes.add(node);
                if(parent == null) parent = node.getTreeItem().getParent().getValue();
                else isGroup = node.getTreeItem().getParent().getValue() == parent;
            }
            if(!isGroup) break;
        }
        if(parent != null && isGroup)
            parent.unGroup(sNodes);
 */
        GroupManagement.applyUngroup(undo, selected, redo);
    }

    public void nodesToGroups() {
        GroupManagement.applyToGroup(undo, selected, redo);
/*        selected.forEach(s -> {
            if(s.isNode()) ((Node) s).groupNodes();
        });
  */  }

    public void groupsToNodes() {
        GroupManagement.applyToNode(undo, selected, redo);
    /*    selected.forEach(s -> {
            if(s.isNode()) ((Node) s).changeGroupToNode();
        });
   */ }

    public void moveToGroup() {
        dragModel = NodeDragModel.Grouping;
        dragModel.set(LineType.Straight, ArrowType.Opened, Color.BLACK, false, false, "1");
    }

    public ActionHelper getUndo() {
        return undo;
    }

    public ActionHelper getRedo(){
        return redo;
    }

    public void addNode(Image image) {
        NodeCreation.applyCreate(undo, this, image, redo);
    }

    public MainController getController() {
        return controller;
    }

    public void changeCollapsed(boolean collapsed) {
        selected.forEach(o -> {
            if(o.isNode() && ((Node) o).isGroupNodes()) ((Node) o).setCollapsed(collapsed);
        });
    }
}

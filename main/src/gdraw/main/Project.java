package gdraw.main;

import gdraw.graph.node.Node;
import gdraw.graph.util.*;
import gdraw.graph.util.action.*;
import gdraw.graph.vertex.ArrowType;
import gdraw.graph.vertex.LineType;
import javafx.collections.ObservableList;
import javafx.geometry.Point2D;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.ListIterator;

public class Project implements Serializable {

    private transient File file;
    private transient Hierarchy nodes;
    private transient ScrollPane hierarchy;
    private Background background;
    private ArrayList<Selectable> graphObjects;
    private transient ArrayList<Selectable> selected;
    private transient Tab tab;
    private String name;
    private transient MainController controller;
//    private transient Pane pane;
    private transient Canvas canvas;
    private transient ScrollPane properties;
    private DragModel dragModel;

    private transient ActionHelper undo;
    private transient ActionHelper redo;
    private transient ArrayList<Action> actionHolder;

    public Project(MainController mainController, String projectName,double w, double h,
                   ScrollPane hScrollPane, ScrollPane pScrollPane,
                   MIandButtonPair undoFXML, MIandButtonPair redoFXML) {
        name = projectName;
        actionHolder = new ArrayList<>();
        properties = pScrollPane;
        hierarchy = hScrollPane;
        graphObjects = new ArrayList<>();
        selected = new ArrayList<>();
        dragModel = DragModel.Standard;

        undo = new ActionHelper(undoFXML);
        redo = new ActionHelper(redoFXML);

        file = null;
        controller = mainController;
//        this.pane = new Pane();

        canvas = setCanvas(w, h);

        background = new Background(controller, new Image(new File("./libraries/default_bck.png").toURI().toString()), canvas, w, h);
        setNodes();
    }

    public Canvas setCanvas(double w, double h){
        canvas = new Canvas(w, h);
        canvas.setOnMouseClicked(e -> {
            Selectable found = getInteractedObject(e);
            if(found != background) select(found, e.isControlDown());
            else found.setSelected(true);
        });
        canvas.setOnMousePressed(e -> onMousePressed(e, getInteractedObject(e)));
        canvas.setOnMouseDragged(e -> onMouseDragged(e, getInteractedObject(e)));
        canvas.setOnMouseReleased(e -> onMouseReleased(e, getInteractedObject(e)));
        canvas.setOnContextMenuRequested(controller::contextMenu);
        return canvas;
    }

    private Selectable getInteractedObject(MouseEvent e) {
        Selectable found = background;
        if(!graphObjects.isEmpty())
            for(Selectable object : graphObjects)
                if(object.checkSelect(e.getX(), e.getY()) && object.isCloserThan(found))
                    found = object;
        return found;
    }

    public void refresh(File file, MainController mainController,
                        ScrollPane hScrollPane, ScrollPane pScrollPane,
                        MIandButtonPair undoFXML, MIandButtonPair redoFXML) {
        controller = mainController;
        undo = new ActionHelper(undoFXML);
        redo = new ActionHelper(redoFXML);
        properties = pScrollPane;
        hierarchy = hScrollPane;
        selected = new ArrayList<>();
        this.file = file;
        canvas = setCanvas(background.getWidth(), background.getHeight());
        background.refresh(this);

        setNodes();
    }

    private void setNodes() {
        nodes = new Hierarchy();
        nodes.setEditable(true);
        nodes.setRoot(background.getTreeItem());
        nodes.getSelectionModel().selectedItemProperty()
                .addListener((observableValue, oldTreeItem, newTreeItem) -> checkSelect());
        nodes.setOnContextMenuRequested(e -> {
            ObservableList<TreeItem<Node>> selectedTI = nodes.getSelectionModel().getSelectedItems();
            if(selectedTI.size() > 0) {
                ContextMenu contextMenu = new ContextMenu();
                selectedTI.get(0).getValue().contextMenu(contextMenu);
                contextMenu.show(nodes, e.getScreenX(), e.getScreenY());
            }
        });
        hierarchy.setContent(nodes);
    }

    private void checkSelect() {
        ObservableList<TreeItem<Node>> selectedTI = nodes.getSelectionModel().getSelectedItems();
        clearSelected(null);
        if(selectedTI.size() > 0){
            selectedTI.forEach(ti -> select(ti.getValue(), true));
        }
    }

    public DragModel getDragModel() { return dragModel; }

    public void setDragModel(DragModel model) { dragModel = model; }

    public void checkSelect(Rectangle selection) {
        if(!graphObjects.isEmpty()) graphObjects.forEach(s -> s.checkSelect(selection));
    }

    public void clearSelected(Selectable item) {
        if(!selected.isEmpty()) selected.forEach(s -> {
            if(s != item) s.setSelected(false);
        });
        selected.clear();
        if(item != null) selected.add(item);
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
        return background;
    }

    public void draw() {
        nodes.refresh();
        background.draw();
//        if(!graphObjects.isEmpty()) graphObjects.forEach(o -> {
//            if(o.isNode()) ((Node) o).draw();
//        });
    }

    public WritableImage snapshot() {
        return canvas.snapshot(new SnapshotParameters(), null);
    }

    public void select(Selectable item, boolean ctrlPressed) {
        if(item == background) return;
        if(!ctrlPressed)
            clearSelected(item);
        else {
            item.setSelected(!selected.contains(item));
            setProperties();
        }
    }

    public void onMousePressed(MouseEvent e, Selectable item) {
        if(item == background) dragModel = DragModel.Select;
        else if(!item.isNode()) dragModel = DragModel.Standard;
        dragModel.pressed(this, e, item);
    }

    public void onMouseDragged(MouseEvent e, Selectable item) {
        dragModel.dragged(this, e, item);
    }

    public void onMouseReleased(MouseEvent e, Selectable item) {
        dragModel.released(this, e, item);
        dragModel = DragModel.Standard;
    }

    public TreeView<Node> getTreeView() {
        return nodes;
    }

    public void setProperties() {
        if(selected.isEmpty())
            properties.setContent(null);
        else{
            boolean allNodes = true, allVertices = true;
            for(Selectable s : selected) {
                allNodes = allNodes && s.isNode();
                allVertices = allVertices && !s.isNode();
            }
            if(allNodes || allVertices)
                selected.get(0).setProperties(properties, selected);
            else{
                Label labelName = new Label("Etykieta: ");
                TextField labelField = new TextField();
                Button btn = new Button("Zatwierdź");
                btn.setOnAction(e -> {
                    if(!selected.isEmpty() && !labelField.getText().isEmpty())
                        selected.forEach(s -> s.setLabel(labelField.getText()));
                });

                GridPane pane = new GridPane();
                pane.getChildren().addAll(labelName, labelField, btn);
                GridPane.setConstraints(labelName, 0, 0);
                GridPane.setConstraints(labelField, 1, 0);
                GridPane.setConstraints(btn, 1, 2);

                properties.setContent(pane);
            }

        }
    }

    public Canvas getCanvas() {
        return canvas;
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
        actionHolder.add(MultiAction.applyDelete(undo, selected, redo));
    }

    public void paste(ArrayList<Selectable> clipboard) {
        actionHolder.add(MultiAction.applyCreate(undo, clipboard, redo));
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
            dragModel = DragModel.Vertex;
            dragModel.set(lineType, arrowType, color, duplex, curved, width);
    }

    public void groupSelected() {
        if(selected.isEmpty()) return;
        TreeItem<Node> parent = nodes.getRoot();
        ArrayList<Double> minMaxs = new ArrayList<>();
        ArrayList<Node> nodes = new ArrayList<>();
        minMaxs.add(Double.MAX_VALUE); minMaxs.add(0.0); minMaxs.add(Double.MAX_VALUE); minMaxs.add(0.0);
        if(!selected.isEmpty()) selected.forEach(s -> {
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
                canvas, true, controller);
        parent.getValue().groupNodes(groupNode);
        groupNode.setCreationListener(new SelectableCreationListener(groupNode));

        actionHolder.add(GroupManagement.applyGroup(undo, groupNode, parent, nodes, redo));

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
        actionHolder.add(GroupManagement.applyUngroup(undo, selected, redo));
    }

    public void nodesToGroups() {
        actionHolder.add(GroupManagement.applyToGroup(undo, selected, redo));
/*        selected.forEach(s -> {
            if(s.isNode()) ((Node) s).groupNodes();
        });
  */  }

    public void groupsToNodes() {
        actionHolder.add(GroupManagement.applyToNode(undo, selected, redo));
    /*    selected.forEach(s -> {
            if(s.isNode()) ((Node) s).changeGroupToNode();
        });
   */ }

    public void moveToGroup() {
        dragModel = DragModel.Grouping;
        dragModel.set(LineType.Straight, ArrowType.Opened, Color.BLACK, false, false, "1");
    }

    public ActionHelper getUndo() {
        return undo;
    }

    public ActionHelper getRedo(){
        return redo;
    }

    public void addNode(Image image) {
        actionHolder.add(NodeCreation.applyCreate(undo, this, image, redo));
    }

    public MainController getController() {
        return controller;
    }

    public void changeCollapsed(boolean collapsed) {
        if(!selected.isEmpty()) selected.forEach(o -> {
            if(o.isNode() && ((Node) o).isGroupNodes()) ((Node) o).setCollapsed(collapsed);
        });
    }

    public void setParents() {
        background.setParent();
    }

    public ArrayList<Action> getActionHolder() {
        return actionHolder;
    }
}

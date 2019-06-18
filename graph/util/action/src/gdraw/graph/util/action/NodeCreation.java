package gdraw.graph.util.action;

import gdraw.graph.node.Node;
import gdraw.graph.util.Selectable;
import gdraw.graph.vertex.Vertex;
import gdraw.main.MainController;
import gdraw.main.Project;
import javafx.geometry.Point2D;
import javafx.scene.image.Image;
import javafx.scene.canvas.Canvas;
import java.util.ArrayList;
import java.util.List;

public class NodeCreation extends MultiAction {


    private NodeCreation(ActionHelper from, Node object, ActionHelper to) {
        super(from, to);
        node = object.getCreationListener();
        type = ActionType.Delete;
    }

    private NodeCreation(ActionHelper from, Node object, MultiAction requestManager, ActionHelper to) {
        super(from, to);
        getInfoFromNode(object);
        type = ActionType.Create;
    }

    private NodeCreation(ActionHelper from, Project project, Image image, ActionHelper to) {
        super(from, to);
        width = image.getWidth(); widthCollapsed = width;
        height = image.getHeight(); heightCollapsed = height;
        center = new Point2D(width/2, height/2);
        this.image = image;
        canvas = project.getCanvas();
        isCollapsed = false; isGroupNodes = false;
        label = "";
        parent = project.getBackground().getCreationListener();
        controller = project.getController();
        type = ActionType.Create;
        node = null;
    }

    private enum ActionType{
        Create,
        Delete
    }

    private SelectableCreationListener node;
    private Point2D center;
    private Image image;
    private Canvas canvas;
    private boolean isGroupNodes, isCollapsed;
    private double width, height, widthCollapsed, heightCollapsed;
    private String label;
    private SelectableCreationListener parent;
    private MainController controller;

    private ActionType type;

    public static Action applyDelete(ActionHelper undo, Node o, ActionHelper redo) {
        Action action = new NodeCreation(redo, o, undo);
        action.action();
        return action;
    }

    public static Action applyCreate(ActionHelper undo, Project project, Image image, ActionHelper redo) {
        Action action = new NodeCreation(redo, project, image, undo);
        action.action();
        return action;
    }

    public static Action applyCreate(ActionHelper undo, Node node, ActionHelper redo) {
        NodeCreation creator = new NodeCreation(undo, node, redo);
        creator.action();
        creator.action();
        return creator;
    }

    public static Action applyCopy(ActionHelper undo, Node node, MultiAction requestManager, ActionHelper redo){
        NodeCreation creator = new NodeCreation(redo, node, requestManager, undo);
        creator.action();
        List<Vertex> vertices = node.getVertices();
        if(!vertices.isEmpty()) vertices.forEach(vertex -> requestManager.request(vertex.getFromNode() == node, creator.getNode(), vertex, node));
        return creator;
    }

    private Node getNode() {
        return (Node) node.getObject();
    }

    @Override
    public void action() {
        switch (type){
            case Create:
                Node object = new Node(center, image, canvas, isGroupNodes, controller);
                object.setWH(width, height, widthCollapsed, heightCollapsed);
                object.setCollapsed(isCollapsed);
                object.setLabel(label);
                ((Node) parent.getObject()).groupNodes(object);

                if(node == null) node = new SelectableCreationListener(object);
                else node.setObject(object);

                super.action();

                type = ActionType.Delete;
                return;
            case Delete:
                Node objectToDelete = (Node) node.getObject();

                ArrayList<Selectable> toDelete = new ArrayList<>();
                if(!objectToDelete.getVertices().isEmpty()) toDelete.addAll(objectToDelete.getVertices());
                if(!objectToDelete.getSubNodes().isEmpty()) toDelete.addAll(objectToDelete.getSubNodes());

                multiFrom.clear();
                multiTo.clear();

                actionHolder.add(applyDelete(multiFrom, toDelete, multiTo));

                getInfoFromNode(objectToDelete);

                node.getObject().delete();
                node.setObject(null);

                type = ActionType.Create;
                changeStacks();
                return;
        }
    }

    private void getInfoFromNode(Node object) {
        center = object.getCenter();
        image = object.getImage();
        canvas = object.getProjectCanvas();
        isGroupNodes = object.isGroupNodes(); isCollapsed = object.isCollapsed();
        object.setCollapsed(false); width = object.getWidth(); height = object.getHeight();
        object.setCollapsed(true); widthCollapsed = object.getWidth(); heightCollapsed = object.getHeight();
        object.setCollapsed(isCollapsed);
        label = object.getLabel();
        parent = object.getTreeItem().getParent().getValue().getCreationListener();
        controller = object.getController();
    }

}

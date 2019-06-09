package gdraw.graph.util.action;

import gdraw.graph.node.Node;
import gdraw.graph.util.Selectable;
import gdraw.graph.vertex.Vertex;
import gdraw.main.MainController;
import gdraw.main.Project;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;

import java.util.ArrayList;

//TODO
public class NodeCreation extends MultiAction {


    public NodeCreation(ActionHelper from, Node object, ActionHelper to) {
        super(from, to);
        node = object.getCreationListener();
    }

    public NodeCreation(ActionHelper from, Project project, Image image, ActionHelper to) {
        super(from, to);
        width = image.getWidth(); widthCollapsed = width;
        height = image.getHeight(); heightCollapsed = height;
        center = new Point2D(width/2, height/2);
        this.image = image;
        group = project.getGroup();
        isCollapsed = false; isGroupNodes = false;
        label = "";
        parent = project.getBackground().getCreationListener();
        controller = project.getController();
        type = ActionType.Create;
    }

    private enum ActionType{
        Create,
        Delete
    }

    private SelectableCreationListener node;
    private Point2D center;
    private Image image;
    private Group group;
    private boolean isGroupNodes, isCollapsed;
    private double width, height, widthCollapsed, heightCollapsed;
    private String label;
    private SelectableCreationListener parent;
    private MainController controller;

    private ActionType type;

    public static void applyDelete(ActionHelper undo, Node o, ActionHelper redo) {
        new NodeCreation(undo, o, redo).action();
    }

    public static void applyCreate(ActionHelper undo, Project project, Image image, ActionHelper redo) {
        new NodeCreation(undo, project, image, redo).action();
    }

    @Override
    public void action() {
        switch (type){
            case Create:
                Node object = new Node(center, image, group, isGroupNodes, ((Node) parent.getObject()).getTreeItem(), controller);
                object.setWH(width, height, widthCollapsed, heightCollapsed);
                object.setCollapsed(isCollapsed);
                object.setLabel(label);
                controller.getProject().newObject(object);
                ((Node) parent.getObject()).groupNodes(object);

                if(node == null) node = new SelectableCreationListener(object);
                else node.setObject(object);

                super.action();

                type = ActionType.Delete;
                break;
            case Delete:
                Node objectToDelete = (Node) node.getObject();

                ArrayList<Selectable> toDelete = new ArrayList<>();
                toDelete.addAll(objectToDelete.getVertices());
                toDelete.addAll(objectToDelete.getSubNodes());

                multiFrom.clear();
                multiTo.clear();

                applyDelete(multiFrom, toDelete, multiTo);

                center = objectToDelete.getCenter();
                image = objectToDelete.getImage();
                group = objectToDelete.getProjectGroup();
                isGroupNodes = objectToDelete.isGroupNodes(); isCollapsed = objectToDelete.isCollapsed();
                objectToDelete.setCollapsed(false); width = objectToDelete.getWidth(); height = objectToDelete.getHeight();
                objectToDelete.setCollapsed(true); widthCollapsed = objectToDelete.getWidth(); heightCollapsed = objectToDelete.getHeight();
                objectToDelete.setCollapsed(isCollapsed);
                label = objectToDelete.getLabel();
                parent = objectToDelete.getTreeItem().getParent().getValue().getCreationListener();
                controller = objectToDelete.getController();

                node.getObject().delete();
                node.setObject(null);

                type = ActionType.Create;
                break;
        }
        changeStacks();
    }

}

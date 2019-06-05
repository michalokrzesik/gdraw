package gdraw.main;

import gdraw.graph.node.Node;
import gdraw.graph.util.Background;
import gdraw.graph.util.Selectable;
import javafx.collections.ObservableList;
import javafx.scene.Group;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Rectangle;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;

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

    public Project(MainController mainController, String projectName, Tab tab, Group group, Canvas canvas, ScrollPane scrollPane) {
        name = projectName;
        properties = scrollPane;
        tab.setOnSelectionChanged(e -> {
            if(tab.isSelected()) controller.setProject(this);
        });
        file = null;
        this.tab = tab;
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
            if(selectedTI.size() > 0) selectedTI.get(0).getValue().contextMenu();
        });
    }


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
        if(selected.contains(item)){
            x = e.getX();
            y = e.getY();
        }
    }

    public void onMouseDragged(MouseEvent e, Selectable item) {
        if(selected.contains(item)){
            double nx = e.getX(), ny = e.getY();
            for (Selectable selectedItem : selected) {
                selectedItem.translate(nx - x, ny - y);
            }
            x = nx;
            y = ny;
        }
    }

    public TreeView<Node> getTreeView() {
        return nodes;
    }

    public void setProperties() {
        if(selected.isEmpty()) properties.setContent(null);
    }

    public void refresh(MainController mainController, Tab tab, Group group, Canvas canvas, ScrollPane scrollPane) {
        //TODO
        this.controller = mainController;
        this.tab = tab;
        tab.setOnSelectionChanged(e -> );
    }
}

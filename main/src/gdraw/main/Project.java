package gdraw.main;

import gdraw.graph.node.Node;
import gdraw.graph.util.Background;
import gdraw.graph.util.Selectable;
import javafx.collections.ObservableList;
import javafx.scene.Group;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Tab;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
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

    public Project(MainController mainController, String projectName, Tab tab, Canvas canvas) {
        name = projectName;
        tab.setOnSelectionChanged(e -> {
            if(tab.isSelected()) controller.setProject(this);
        });
        file = null;
        this.tab = tab;
        controller = mainController;
        nodes = new TreeView<>();
        group = new Group();
        Background background = new Background(controller, canvas, new Image("/white.png"), group, canvas.getWidth(), canvas.getHeight());
        nodes.setEditable(true);
        nodes.setRoot(new TreeItem<>(background));
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

    public Background getBackgorund() {
        return (Background) nodes.getRoot().getValue();
    }
}

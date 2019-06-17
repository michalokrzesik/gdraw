package gdraw.graph.util;

import gdraw.graph.util.action.SelectableCreationListener;
import gdraw.main.MainController;
import gdraw.main.Project;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.shape.Rectangle;

import java.io.Serializable;
import java.util.ArrayList;

public abstract class Selectable implements Serializable {
    protected transient MainController controller;
    private SelectableCreationListener creationListener;
    protected transient Canvas canvas;
    protected transient boolean selected;
    protected Label label;

    public abstract void checkSelect(Rectangle rectangle);

    public abstract void setSelected(boolean b);

    public abstract void translate(double dx, double dy);

    protected void setSelected() {
        controller.select(this, false);
    }

    protected void setSelected(MouseEvent e) {
        controller.select(this, e.isControlDown());
    }

    protected void onMousePressed(MouseEvent e){
        controller.onMousePressed(e, this);
    }

    protected void onMouseReleased(MouseEvent e){
        controller.onMouseReleased(e, this);
    }

    protected void onMouseDragged(MouseEvent e){
        controller.onMouseDragged(e, this);
    }

    public abstract Selectable copy();

    public abstract void delete();

    public abstract boolean isNode();

    public void refresh(Project project){
        canvas = project.getCanvas();
        controller = project.getController();
    }

    public void setCreationListener(SelectableCreationListener listener){
        creationListener = listener;
    }

    public SelectableCreationListener getCreationListener(){
        if(creationListener == null) creationListener = new SelectableCreationListener(this);
        return creationListener;
    }

    public String getLabel(){
        if(label != null) return label.getLabel();
        else return "";
    }

    public void contextMenu(ContextMenu contextMenu){
        MenuItem cut = new MenuItem("Wytnij zaznaczone");
        cut.setOnAction(controller::cutSelected);

        MenuItem copy = new MenuItem("Kopiuj zaznaczone");
        copy.setOnAction(controller::copySelected);

        MenuItem paste = new MenuItem("Wklej");
        paste.setOnAction(controller::paste);

        SeparatorMenuItem separatorMenuItem = new SeparatorMenuItem();

        MenuItem duplicate = new MenuItem("Duplikuj zaznaczone");
        duplicate.setOnAction(controller::duplicateSelected);

        MenuItem delete = new MenuItem("Usuń zaznaczone");
        delete.setOnAction(controller::deleteSelected);

        contextMenu.getItems().addAll(cut, copy, paste, separatorMenuItem, duplicate, delete);
    }

    @Override
    public String toString(){
        if(label != null && label.getLabel().matches("[^\\s]+")) return label.getLabel();
        return "<bez_nazwy>";
    }

    public abstract void setLabel(String text);

    public abstract void setProperties(ScrollPane properties, ArrayList<Selectable> selected);

    public GridPane getPropertiesGridPane(
            javafx.scene.control.Label l0, javafx.scene.control.Label l1, javafx.scene.control.Label l2,
            javafx.scene.control.Label l3, javafx.scene.control.Label l4, javafx.scene.control.Label l5,
            Control separate,
            Control c0, Control c1, Control c2, Control c3, Control c4, Control c5,
            Button btn){
        GridPane pane = new GridPane();

        pane.setStyle("-fx-background-color: #FDF5E6;");

        pane.getChildren().addAll(l0, l1, l2, l3, l4, l5, separate,
                c0, c1, c2, c3, c4, c5, btn);

        GridPane.setConstraints(l0, 0, 0); GridPane.setConstraints(c0, 1, 0);
        GridPane.setConstraints(separate, 0, 1);
        GridPane.setConstraints(l1, 0, 2); GridPane.setConstraints(c1, 1, 2);
        GridPane.setConstraints(l2, 0, 3); GridPane.setConstraints(c2, 1, 3);
        GridPane.setConstraints(l3, 0, 4); GridPane.setConstraints(c3, 1, 4);
        GridPane.setConstraints(l4, 0, 5); GridPane.setConstraints(c4, 1, 5);
        GridPane.setConstraints(l5, 0, 6); GridPane.setConstraints(c5, 1, 6);
        GridPane.setConstraints(btn, 1, 8);
        pane.setHgap(2);
        pane.setVgap(2);
        pane.setLayoutX(2);
        pane.setLayoutY(2);

        return pane;
    }
}

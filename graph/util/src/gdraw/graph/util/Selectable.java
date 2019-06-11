package gdraw.graph.util;

import gdraw.graph.util.action.SelectableCreationListener;
import gdraw.main.MainController;
import gdraw.main.Project;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Rectangle;

import java.io.Serializable;
import java.util.ArrayList;

public abstract class Selectable implements Serializable {
    protected transient MainController controller;
    protected SelectableCreationListener creationListener;
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

    public abstract void refresh(Project project);

    public void setCreationListener(SelectableCreationListener listener){
        creationListener = listener;
    }

    public SelectableCreationListener getCreationListener(){
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
        if(label != null) return label.getLabel();
        return "<bez_nazwy>";
    }

    public abstract void setLabel(String text);

    public abstract void setProperties(ScrollPane properties, ArrayList<Selectable> selected);
}

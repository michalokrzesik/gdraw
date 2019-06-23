package gdraw.graph.util;

import gdraw.graph.node.Node;
import gdraw.graph.util.action.SelectableReference;
import gdraw.graph.vertex.Vertex;
import gdraw.main.MainController;
import gdraw.main.Project;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.shape.Rectangle;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

public abstract class Selectable implements Serializable {
    protected transient MainController controller;
    private transient SelectableReference reference;
    protected transient Canvas canvas;
    protected transient boolean selected;
    protected Label label;

    public abstract void checkSelect(Rectangle rectangle);

    public abstract boolean checkSelect(double x, double y);

    public void setSelected(boolean selected) {
        Project project = controller.getProject();
        ArrayList<Selectable> selectables = project.getSelected();
        boolean contains = selectables.contains(this);
        if(selected) {
            if (!contains) selectables.add(this);
        }
        else if(contains) selectables.remove(this);
        this.selected = selected;
    }

    public abstract void translate(double dx, double dy);

    protected void setSelected() {
        controller.select(this, false);
    }

//    protected void setSelected(MouseEvent e) {  controller.select(this, e.isControlDown()); }

//    protected void onMousePressed(MouseEvent e){  controller.onMousePressed(e, this); }

//    protected void onMouseReleased(MouseEvent e){  controller.onMouseReleased(e, this);}

//    protected void onMouseDragged(MouseEvent e){ controller.onMouseDragged(e, this); }

    public abstract Selectable copy();

    public abstract void delete();

    public abstract boolean isNode();

    public void refresh(Project project){
        canvas = project.getCanvas();
        controller = project.getController();
        if(label != null) label.refresh(project);
    }

    public void setReference(SelectableReference listener){
        reference = listener;
    }

    public SelectableReference getReference(){
        if(reference == null) reference = new SelectableReference(this);
        return reference;
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

    protected Node parentForIsCloserThan(){
        if(!isNode()){
            Vertex vThis = (Vertex) this;
            Node vThisFrom = vThis.getFromNode(), vThisTo = vThis.getToNode();
            return vThisFrom.isCloserThan(vThisTo) ? vThisFrom : vThisTo;
        }
        return (Node) this;
    }

    public boolean isCloserThan(Selectable found){
        return depth(parentForIsCloserThan().getTreeItem())
                >=
                depth(found.parentForIsCloserThan().getTreeItem());
    }

    private int depth(TreeItem<Node> treeItem) {
        int res;
        for(res = 0; treeItem != null; treeItem = treeItem.getParent(), res++);
        return res;
    }

    public void forceProjectDraw() {
        controller.forceDraw();
    }

//    public void setController(MainController controller) { this.controller = controller; }

    public void writeToFile(FileWriter writer, boolean json, int indent) throws IOException {
        int code = this.hashCode();
        String objectName = isNode() ? "Node" : "Vertex",
                ind = indent(indent), ind1 = ind + "  ";
        String toAppend = ind + (json ?
                "{\n" + ind1 + "\"id\": \"" + code + "\",\n" + ind1 + "\"label\": \"" + getLabel() + "\"" :
                "< " + objectName + " " + "id=\"" + code + "\" " + "label=\"" + getLabel() + "\" " );
        writer.append(toAppend);
    }

    public String indent(int indent){
        String res = "";
        for(int i = 0; i < indent; i++)
            res = res.concat("  ");
        return res;
    }
}

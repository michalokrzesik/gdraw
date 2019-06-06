package gdraw.graph.util;

import gdraw.main.MainController;
import gdraw.main.Project;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Rectangle;

public abstract class Selectable {
    protected MainController controller;

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
}

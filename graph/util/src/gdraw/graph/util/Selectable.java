package gdraw.graph.util;

import javafx.scene.shape.Rectangle;

public interface Selectable {
    void checkSelect(Rectangle rectangle);

    void setSelected(boolean b);
}

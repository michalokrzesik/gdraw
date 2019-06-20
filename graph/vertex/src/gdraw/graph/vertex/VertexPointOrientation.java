package gdraw.graph.vertex;

import java.io.Serializable;

public enum VertexPointOrientation implements Serializable {
    NONE,
    HORIZONTAL,
    VERTICAL;

    public VertexPointOrientation otherType() {
        switch (this){
            case HORIZONTAL: return VERTICAL;
            case VERTICAL: return HORIZONTAL;
            default: return NONE;
        }
    }
}

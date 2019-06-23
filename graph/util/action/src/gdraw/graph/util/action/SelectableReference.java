package gdraw.graph.util.action;

import gdraw.graph.util.Selectable;

public class SelectableReference {
    private Selectable object;

    public SelectableReference(Selectable selectable) {
        setObject(selectable);
    }

    public void setObject(Selectable selectable){
        object = selectable;
        if(object != null) object.setReference(this);
    }

    public Selectable getObject(){
        return object;
    }

}

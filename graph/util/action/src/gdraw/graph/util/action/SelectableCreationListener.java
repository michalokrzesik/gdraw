package gdraw.graph.util.action;

import gdraw.graph.util.Selectable;

public class SelectableCreationListener {
    Selectable object;

    public SelectableCreationListener(Selectable selectable) {
        setObject(selectable);
    }

    public void setObject(Selectable selectable){
        object = selectable;
        if(object != null) object.setCreationListener(this);
    }

    public Selectable getObject(){
        return object;
    }

}

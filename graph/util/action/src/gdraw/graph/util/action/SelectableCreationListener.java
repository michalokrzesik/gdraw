package gdraw.graph.util.action;

import gdraw.graph.util.Selectable;

public class SelectableCreationListener {
    private Selectable object;
    private int count;                      //Hopefully blocks object deletion

    public SelectableCreationListener(Selectable selectable) {
        count = 0;
        setObject(selectable);
    }

    public void setObject(Selectable selectable){
        object = selectable;
        if(object != null) object.setCreationListener(this);
    }

    public Selectable getObject(){
        count ++;
        return object;
    }

    public int getCount(){
        return count;
    }

}

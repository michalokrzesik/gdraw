package gdraw.main.gui;

import gdraw.graph.node.Node;
import javafx.scene.control.TreeView;

public class Hierarchy extends TreeView<Node> {
    public Hierarchy(){
        super();
        setStyle("-fx-background-color:  #FDF5E6; " +
                "-fx-color:  #D2B48C; ");
    }
}

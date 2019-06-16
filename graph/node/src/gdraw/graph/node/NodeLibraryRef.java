package gdraw.graph.node;

public class NodeLibraryRef {
    private NodeLibrary library;

    public NodeLibraryRef(NodeLibrary nodeLibrary){
        library = nodeLibrary;
    }

    @Override
    public String toString(){
        return library.getName();
    }

    public NodeLibrary getLibrary() {
        return library;
    }
}

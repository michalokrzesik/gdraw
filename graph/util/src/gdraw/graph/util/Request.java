package gdraw.graph.util;

import gdraw.graph.node.Node;
import gdraw.graph.vertex.Vertex;
import gdraw.main.Project;

public class Request {
    private boolean isFrom;
    private Node node;
    private Vertex vertex;
    private Project project;

    public Request(Project project, boolean isFrom, Node node, Vertex vertex){
        this.project = project;
        this.isFrom = isFrom;
        this.node = node;
        this.vertex = vertex;
    }

//    public void request(){
//        request(node, vertex);
//    }

    public void request(Node node){
        request(node, vertex);
    }

    public void request(Vertex vertex){
        request(node, vertex);
    }

    private void request(Node node, Vertex vertex){
        if(isFrom) vertex.setFrom(node);
        else vertex.setTo(node);

        node.addVertex(vertex);
    }

    public boolean checkIsFrom(boolean requested) { return isFrom == requested; }

    public boolean checkNode(Node oldNode) {
        return node == oldNode;
    }

    public boolean checkVertex(Vertex oldVertex) {
        return vertex == oldVertex;
    }

    public boolean checkProject(Project project) {
        return this.project == project;
    }

    public Node getNode() {
        return node;
    }
}

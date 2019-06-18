package gdraw.graph.util.action;

import gdraw.graph.vertex.ArrowType;
import gdraw.graph.vertex.LineType;
import gdraw.graph.vertex.Vertex;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;

public class VertexEdit extends Action {
    private SelectableCreationListener vertex;
    private LineType lineType;
    private ArrowType arrowType;
    private double width, value;
    private Color color;

    private VertexEdit(ActionHelper from, Vertex vertex, LineType lineType, ArrowType arrowType, double w, Color c, double v, ActionHelper to) {
        this.from = from;
        this.to = to;
        this.vertex = vertex.getCreationListener();
        this.lineType = lineType;
        this.arrowType = arrowType;
        width = w;
        color = c;
        value = v;
    }

    public static Action apply(ActionHelper undo,
                             Vertex vertex, ChoiceBox<LineType> lineTypeChoiceBox, ChoiceBox<ArrowType> arrowTypeChoiceBox,
                             TextField widthField, ColorPicker colorPicker, TextField valueField,
                             ActionHelper redo) {
        LineType lineType; ArrowType arrowType;
        if(lineTypeChoiceBox.isShowing()) lineType = lineTypeChoiceBox.getValue();
        else lineType = vertex.getLineType();
        if(arrowTypeChoiceBox.isShowing()) arrowType = arrowTypeChoiceBox.getValue();
        else arrowType = vertex.getArrowType();

        double w, v;
        try{
            w = Double.parseDouble(widthField.getText());
        } catch(Exception e1) { w = vertex.getLineWidth(); }
        try{
            v = Double.parseDouble(valueField.getText());
        } catch(Exception e1) { v = vertex.getValue(); }

        Action action = new VertexEdit(redo, vertex, lineType, arrowType, w, colorPicker.getValue(), v, undo);
        action.action();
        return action;
    }

    @Override
    public void action() {
        Vertex v = (Vertex) vertex.getObject();
        LineType oldLineType = v.getLineType();
        ArrowType oldArrowType = v.getArrowType();
        double oldWidth = v.getLineWidth(), oldValue = v.getValue();
        Color oldColor = v.getColor();

        v.setLineType(lineType);
        v.setArrowType(arrowType);
        v.setLineWidth(width);
        v.setColor(color);
        v.setValue(value);

        lineType = oldLineType;
        arrowType = oldArrowType;
        width = oldWidth;
        value = oldValue;
        color = oldColor;

        changeStacks();
    }
}

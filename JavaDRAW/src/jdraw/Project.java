package jdraw;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.Tab;
import javafx.scene.effect.BlendMode;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.ArrayList;

public class Project implements Serializable
{
    private String name;
    private double canvas_width;
    private double canvas_height;
    private ArrayList<DrawObject> drawObjects;
    private ArrayList<Integer> selectedObjects;
    private transient Tab tab;
    private transient Canvas canvas;
    private transient Controller.LineMode activeLineMode;
    private transient Controller.SelectMode activeSelectMode;
    private transient boolean toolSelect;
    private transient boolean freeSelect;
    private transient boolean curveModify;
    private Point2D.Double selectStart;
    private double scale;
    private transient Slider slider;
    private transient Label sliderLabel;

    public Project(String name, double canvas_width, double canvas_height, Tab tab, Canvas canvas, Slider slider, Label sliderLabel) {
        this.name = name;
        this.canvas_width = canvas_width;
        this.canvas_height = canvas_height;
        this.tab = tab;
        freeSelect = false;
        selectStart = new Point2D.Double(0,0);
        drawObjects = new ArrayList<>();
        selectedObjects = new ArrayList<>();
        this.canvas = canvas;
        activeLineMode = null;
        activeSelectMode = Controller.SelectMode.RECT;
        toolSelect = true;
        curveModify = false;
        this.slider = slider;
        this.sliderLabel = sliderLabel;
        slider.adjustValue(100);
        scale = 1;

        this.tab.setOnSelectionChanged(event -> {
            slider.adjustValue(100 * scale);
            sliderLabel.setText(String.format("%.0f%%", slider.getValue()));
        });

        this.canvas.setOnMouseReleased(e -> {
            if (toolSelect)
                activeSelectMode.END(e.getX(), e.getY(), this);
            else if(activeLineMode.lastPoint != null)
                activeLineMode.setLastPoint(e.getX(), e.getY(), this);
        });

        this.canvas.setOnMouseDragged(e -> {
            if (toolSelect)
                activeSelectMode.SELECT(e.getX(), e.getY(), this);

            else {
                activeLineMode.drawLine(e.getX(), e.getY(), this);
            }
        });

        this.canvas.setOnMousePressed(e -> {
            if (toolSelect) {
                if (e.isSecondaryButtonDown()) selectedObjects.clear();
                else
                    activeSelectMode.START(e.getX(), e.getY(), this);
            }
            else if (activeLineMode == Controller.LineMode.LINE ||
                    activeLineMode == Controller.LineMode.BEZIER ||
                    activeLineMode == Controller.LineMode.OVAL) {
                activeLineMode.setStartPoint(e.getX(), e.getY(), this);
            } else {
                if (e.isSecondaryButtonDown() == true) //użyto ppm
                {
                    activeLineMode.lastPoint = null;
                } else //użyto lpm
                {
                    activeLineMode.setStartPoint(e.getX(), e.getY(), this);
                }
            }
        });
    }

    /**
     * Ustawianie slidera przybliżenia
     * @param slider    obiekt slidera
     * @param sliderLabel   obiekt etykiety powiązanej
     */
    public void setSlider(Slider slider, Label sliderLabel){
        this.slider = slider;
        this.sliderLabel = sliderLabel;
    }

    /**
     * Uzyskanie listy zaznaczonych obiektów
     * @return
     */
    public ArrayList<Integer> getSelectedObjects()
    {
        return selectedObjects;
    }

    /**
     * Uzyskanie listy obiektów
     * @return
     */
    public ArrayList<DrawObject> getDrawObjects()
    {
        return drawObjects;
    }

    /**
     * Uzyskanie nazwy projektu
     * @return
     */
    public String getName()
    {
        return name;
    }

    /**
     * Uzyskanie szerokości canvas
     * @return
     */
    public double getCanvas_width()
    {
        return canvas_width;
    }

    /**
     * Uzyskanie wysokości canvas
     * @return
     */
    public double getCanvas_height()
    {
        return canvas_height;
    }

    /**
     * Uzyskanie zakładki powiązanej z projektem
     * @return
     */
    public Tab getTab()
    {
        return tab;
    }

    /**
     * Ustawianie zakładki i obsługi jej przełączania
     * @param tab   zakładka
     */
    public void setTab(Tab tab)
    {
        this.tab = tab;
        this.tab.setOnSelectionChanged(event -> {
            slider.adjustValue(100 * scale);
            sliderLabel.setText(String.format("%.0f%%", slider.getValue()));
        });
    }

    /**
     * Uzyskanie canvas
     * @return
     */
    public Canvas getCanvas()
    {
        return canvas;
    }

    /**
     * Ustawienie canvas i obsługi myszy
     * @param canvas canvas
     */
    public void setCanvas(Canvas canvas)
    {
        this.canvas = canvas;
        this.canvas.setOnMouseReleased(e -> {
            if (toolSelect)
                activeSelectMode.END(e.getX(), e.getY(), this);
            else if(activeLineMode.lastPoint != null)
                activeLineMode.setLastPoint(e.getX(), e.getY(), this);
        });

        this.canvas.setOnMouseDragged(e -> {
            if (toolSelect)
                activeSelectMode.SELECT(e.getX(), e.getY(), this);

            else {
                activeLineMode.drawLine(e.getX(), e.getY(), this);
            }
        });

        this.canvas.setOnMousePressed(e -> {
            if (toolSelect) {
                if (e.isSecondaryButtonDown()) selectedObjects.clear();
                else
                    activeSelectMode.START(e.getX(), e.getY(), this);
            }
            else if (activeLineMode == Controller.LineMode.LINE ||
                    activeLineMode == Controller.LineMode.BEZIER ||
                    activeLineMode == Controller.LineMode.OVAL) {
                activeLineMode.setStartPoint(e.getX(), e.getY(), this);
            } else {
                if (e.isSecondaryButtonDown() == true) //użyto ppm
                {
                    activeLineMode.lastPoint = null;
                } else //użyto lpm
                {
                    activeLineMode.setStartPoint(e.getX(), e.getY(), this);
                }
            }
        });
    }

    /**
     * Ustawienie aktywnego trybu linii
     * @param activeLineMode tryb linii
     */
    public void setActiveLineMode(Controller.LineMode activeLineMode)
    {
        this.activeLineMode = activeLineMode;
    }

    /**
     * Rysuje ponownie obiekty.
     */
    public void redraw()
    {
//        if(selectedObjects.size() < 1) selectedObjects.add(0);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        gc.setFill(Color.WHITE);
        gc.setGlobalBlendMode(BlendMode.SRC_OVER);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        drawObjects.iterator().forEachRemaining(e -> {

            e.draw(canvas.getGraphicsContext2D());
        });

        for(Integer i : selectedObjects)
            drawObjects.get(i).drawSelection(gc);
    }

    /**
     * Ustawienie flagi określającej, czy zaznaczamy, czy rysujemy
     * @param toolSelect flaga
     */
    public void setToolSelect(boolean toolSelect) {
        this.toolSelect = toolSelect;
    }

    /**
     * Ustawienie aktywnego trybu zaznaczania
     * @param activeSelectMode tryb zaznaczania
     */
    public void setActiveSelectMode(Controller.SelectMode activeSelectMode) {
        this.activeSelectMode = activeSelectMode;
    }

    /**
     * Rysowanie zaznaczenia
     * @param gc kontekst graficzny
     * @param drawObject obiekt zaznaczony
     */
    public static void drawSelection(GraphicsContext gc, DrawObject drawObject) {
        ArrayList<Point2D.Double> points = drawObject.getPoints();
        ArrayList<Integer> selectedPoints = drawObject.getSelectedPoints();
        gc.setLineWidth(1);
        Paint c = (drawObject.getColor() == Color.BLUE ? Color.DARKVIOLET : Color.BLUE);
        for(int i = 0; i < points.size(); i++){
            Point2D point = points.get(i);
            gc.setFill(selectedPoints.contains(i) ? c : Color.WHITE);
            gc.fillOval(point.getX() - 4, point.getY() - 4, 8, 8);
            gc.setStroke(c);
            gc.strokeOval(point.getX() - 4, point.getY() - 4, 8, 8);
        }
    }

    /**
     * Uzyskanie punktu początkowego zaznaczenia
     * @return
     */
    public Point2D.Double getSelectStart() {
        return selectStart;
    }

    /**
     * Uzyskanie flagi określającej, czy aktualnie wykonywane jest wolne zaznaczanie
     * @return
     */
    public boolean getFreeSelect() {
        return freeSelect;
    }

    /**
     * Ustawienie flagi freeSelect
     * @param freeSelect flaga
     */
    public void setFreeSelect(boolean freeSelect) {
        this.freeSelect = freeSelect;
    }

    /**
     * Uzyskanie flagi określającej, czy krzywa ma mieć nadawane zakrzywienie
     * @return
     */
    public boolean getCurveModify() {
        return curveModify;
    }

    /**
     * Ustawienie flagi curveModify
     * @param curveModify flaga
     */
    public void setCurveModify(boolean curveModify) {
        this.curveModify = curveModify;
    }

    /**
     * Ustawianie skali
     * @param scale skala
     */
    public void setScale(Double scale) {
        this.scale = scale;
    }

    /**
     * Przeskalowywanie do standardowej wielkości
     */
    public void reScale() {
        canvas_height /= scale;
        canvas_width /= scale;
        for(DrawObject drawObject : drawObjects) {
            drawObject.resize(drawObject.getWidth() / scale, drawObject.getHeight() / scale, drawObject.getClose());
            drawObject.translate(drawObject.getArea().getX() * (1 - scale) / scale, drawObject.getArea().getY() * (1 - scale) / scale);
        }
    }


    /**
     * Skalowanie do nowej wielkości
     */
    public void Scale() {
        canvas_height *= scale;
        canvas_width *= scale;
        for(DrawObject drawObject : drawObjects) {
            drawObject.resize(drawObject.getWidth() * scale, drawObject.getHeight() * scale, drawObject.getClose());
            drawObject.translate(drawObject.getArea().getX() * (scale - 1), drawObject.getArea().getY() * (scale - 1));
        }
        canvas.setWidth(canvas_width);
        canvas.setHeight(canvas_height);

    }
}

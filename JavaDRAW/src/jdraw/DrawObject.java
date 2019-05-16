package jdraw;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

import java.awt.geom.Point2D;
import java.util.ArrayList;

public interface DrawObject
{
    /**
     * Uzyskanie flagi określającej, czy krzywa jest zamknięta
     * @return
     */
    boolean getClose();

    /**
     * Uzyskanie grubości linii
     * @return
     */
    double getLine();

    /**
     * Uzyskanie szerokości obiektu
     * @return
     */
    double getWidth();

    /**
     * Uzyskanie wysokości obiektu
     * @return
     */
    double getHeight();

    /**
     * Ustawienie flagi zamknięcia obiektu
     * @param close flaga
     */
    void setClose(boolean close);

    /**
     * Ustawienie grubości konturu
     * @param width grubość
     */
    void setLine(double width);

    /**
     * Ustawienie szerokości obiektu
     * @param width szerokość
     */
    void setWidth(double width);

    /**
     * Ustawienie wysokości obiektu
     * @param height wysokość
     */
    void setHeight(double height);

    /**
     * Dodawanie punktu do obiektu
     * @param point punkt
     */
    void addPoint(Point2D.Double point);

    /**
     * Zmiana ostatniego punktu
     * @param point nowy ostatni punkt
     */
    void setLastPoint(Point2D.Double point);

    /**
     * Rysowanie obiektu
     * @param gc kontekst graficzny
     */
    void draw(GraphicsContext gc);

    /**
     * Rysowanie zaznaczenia obiektu
     * @param gc kontekst graficzny
     */
    void drawSelection(GraphicsContext gc);

    /**
     * Naprawianie punktu "zaznaczenia" całego obiektu, jego wysokości i szerokości
     */
    void setWHA();

    /**
     * Zmiana rozmiarów obiektu
     * @param width szerokość
     * @param height    wysokość
     * @param close flaga domknięcia obiektu
     */
    void resize(double width, double height, boolean close);

    /**
     * Przesuwanie obiektu
     * @param deltax różnica współrzędnych x
     * @param deltay różnica współrzędnych y
     */
    void translate(double deltax, double deltay);

    /**
     * Uzyskanie listy punktów
     * @return
     */
    ArrayList<Point2D.Double> getPoints();

    /**
     * Uzyskanie listy zaznaczonych punktów
     * @return
     */
    ArrayList<Integer> getSelectedPoints();

    /**
     * Uzyskanie punktu "zaznaczenia" całego obiektu
     * @return
     */
    Point2D.Double getArea();

    /**
     * Uzyskanie koloru konturu
     * @return
     */
    Paint getColor();

    /**
     * Ustawienie koloru konturu
     * @param color kolor
     */
    void setColor(Color color);

    /**
     * Metoda naprawiająca kolor po wczytaniu obiektu z pliku
     */
    void refresh();

    /**
     * Przesunięcie punktów zaznaczonych
     * @param deltax różnica współrzędnych x
     * @param deltay różnica współrzędnych y
     */
    void translateSelected(double deltax, double deltay);
}

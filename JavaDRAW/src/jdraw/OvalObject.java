package jdraw;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.ArrayList;

public class OvalObject implements DrawObject, Serializable
{
    private ArrayList<Point2D.Double> points;
    private ArrayList<Integer> selectedPoints;
    private boolean isClosed;
    private boolean controlPoint;
    private double lineWidth;
    private double width;
    private double height;
    private Point2D.Double area;
    private transient Paint color;
    private String sColor;                          //Zmienna do zapisu koloru (Paint nie jest Serializable

    public OvalObject(double x, double y){
        points = new ArrayList<>();
        selectedPoints = new ArrayList<>();
        area = new Point2D.Double(x, y);
        color = Color.BLACK;
        sColor = color.toString();
        lineWidth = 1;
        isClosed = false;
        controlPoint = false;
    }

    @Override
    public boolean getClose()
    {
        return isClosed;
    }

    @Override
    public double getLine()
    {
        return lineWidth;
    }

    @Override
    public double getWidth()
    {
        return width;
    }

    @Override
    public double getHeight()
    {
        return height;
    }

    @Override
    public void setClose(boolean close)
    {
        this.isClosed = close;
    }

    @Override
    public void setLine(double width)
    {
        lineWidth = width;
    }

    @Override
    public void setWidth(double width)
    {
        this.width = width;
    }

    @Override
    public void setHeight(double height)
    {
        this.height = height;
    }

    @Override
    public void addPoint(Point2D.Double point)
    {
        points.add(point);
    }

    @Override
    public void setLastPoint(Point2D.Double point)
    {
        points.remove(points.size() - 1); //usuwa ostatni element
        points.add(point); //dodaje element z argumentu
    }

    @Override
    public void draw(GraphicsContext gc){
        gc.setLineWidth(lineWidth);
        gc.setStroke(color);
        gc.strokeOval(area.x, area.y, width, height);
    }

    @Override
    public void drawSelection(GraphicsContext gc) {
        Project.drawSelection(gc, this);
    }

    @Override
    public void setWHA()
    {
        double xmin = points.get(0).getX();
        double ymin = points.get(0).getY();
        double xmax = points.get(0).getX();
        double ymax = points.get(0).getY();

        for(Point2D point : points)
        {
            if(point.getX() < xmin)
            {
                xmin = point.getX();
            }

            if(point.getX() > xmax)
            {
                xmax = point.getX();
            }

            if(point.getY() < ymin)
            {
                ymin = point.getY();
            }

            if(point.getY() > ymax)
            {
                ymax = point.getY();
            }
        }

        area.setLocation((xmin - lineWidth/2 > 0 ? xmin - lineWidth/2 : 0), (ymin - lineWidth/2 > 0 ? ymin - lineWidth/2 : 0));
        width = xmax - area.getX() + lineWidth/2;
        height = ymax - area.getY() + lineWidth/2;
    }

    @Override
    public void resize(double width, double height, boolean close) {
        double scaleX = width/this.width;
        double scaleY = height/this.height;

        for(Point2D.Double point : points){
            point.setLocation(area.getX() + (point.getX() - area.getX()) * scaleX,area.getY() + (point.getY() - area.getY()) * scaleY);
        }

        setHeight(height);
        setWidth(width);
        setClose(close);
    }

    @Override
    public void translate(double deltax, double deltay){
        for(Point2D.Double point: points)
            point.setLocation(point.getX() + deltax, point.getY() + deltay);
        area.setLocation(area.getX() + deltax, area.getY() + deltay);
    }

    @Override
    public ArrayList<Point2D.Double> getPoints() {
        return points;
    }

    @Override
    public ArrayList<Integer> getSelectedPoints() {
        return selectedPoints;
    }

    @Override
    public Point2D.Double getArea() {
        return area;
    }

    @Override
    public Paint getColor() {
        return color;
    }

    @Override
    public void setColor(Color color) {
        this.color = color;
        sColor = color.toString();
    }

    @Override
    public void refresh() {
        color = Color.valueOf(sColor);
    }

    @Override
    public void translateSelected(double deltax, double deltay) {
        for(Integer i : selectedPoints)
            points.get(i).setLocation(points.get(i).getX() + deltax, points.get(i).getY() + deltay);
        setWHA();
    }

    /**
     * Naprawianie punktów elipsy
     * @param x współrzędna kursora myszy
     * @param y współrzędna kursora myszy
     */
    public void setOval(double x, double y) {
        area.setLocation(points.get(3).getX(), points.get(0).getY());
        double X = area.getX(), Y = area.getY();
        points.get(0).setLocation((X + x)/2, Y);
        points.get(1).setLocation(x, (Y + y)/2);
        points.get(2).setLocation((X + x)/2, y);
        points.get(3).setLocation(X, (Y + y)/2);
    }

}
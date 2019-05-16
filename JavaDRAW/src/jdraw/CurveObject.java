package jdraw;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.ArrayList;

public class CurveObject implements DrawObject, Serializable
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
    private String sColor;

    public CurveObject(double x, double y){
        points = new ArrayList<>();
        selectedPoints = new ArrayList<>();
        area = new Point2D.Double(x, y);
        lineWidth = 1;
        color = Color.BLACK;
        sColor = color.toString();
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
    public void draw(GraphicsContext gc)
    {
        gc.beginPath();

        //rysowanie od punktu do punktu
        gc.moveTo(points.get(0).getX(), points.get(0).getY());
        for(int i = 1; i < points.size(); i++)
        {
            gc.quadraticCurveTo(points.get(i).getX(), points.get(i++).getY(), points.get(i).getX(), points.get(i).getY());
        }

        //zamknięcie obiektu
        if(isClosed == true && points.size() > 3)
        {
            gc.lineTo(points.get(0).getX(), points.get(0).getY());
        }

        gc.setLineWidth(lineWidth);
        gc.setStroke(color);
        gc.stroke();
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

    public void translate(double deltax, double deltay){
        for(Point2D.Double point: points)
            point.setLocation(point.getX() + deltax, point.getY() + deltay);
        area.setLocation(area.getX() + deltax, area.getY() + deltay);
    }

    @Override
    public void translateSelected(double deltax, double deltay) {
        for(Integer i : selectedPoints)
            points.get(i).setLocation(points.get(i).getX() + deltax, points.get(i).getY() + deltay);
        setWHA();
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
        sColor = color.toString();
        this.color = color;
    }

    @Override
    public void refresh() {
        color = Color.valueOf(sColor);
    }

    public void changeControlPoint() {
        controlPoint = !controlPoint;
    }

    public boolean getControlPoint() {
        return controlPoint;
    }

    public void setControl(double x, double y) {
        points.get(points.size() - 2).setLocation(x, y);
    }

    /**
     * Tworzenie udawanej elipsy z krzywych beziera drugiego stopnia
     * @param x współrzędna kursora myszy
     * @param y współrzędna kursora myszy
     */
    public void setOval(double x, double y) {
        double prevX = area.getX(), prevY = area.getY();
        double newX, newY;
        if(prevX < x) newX = prevX;
        else{
            newX = x;
            x = prevX;
        }
        if(prevY < y) newY = prevY;
        else{
            newY = y;
            y = prevY;
        }
        area.setLocation(newX, newY);
        points.get(0).setLocation((newX + x)/2, newY);  //zawarty
        points.get(1).setLocation(x, newY);                 //kontrolny
        points.get(2).setLocation(x, (newY + y)/2);     //zawarty
        points.get(3).setLocation(x, y);                    //kontrolny
        points.get(4).setLocation((newX + x)/2, y);     //zawarty
        points.get(5).setLocation(newX, y);                 //kontrolny
        points.get(6).setLocation(newX, (newY + y)/2);  //zawarty
        points.get(7).setLocation(newX, newY);
        points.get(8).setLocation(points.get(0));          //domknięcie
    }
}
package com.rongill.rsg.sinprojecttest.navigation;

import java.io.Serializable;
import java.util.Objects;

public class Point implements Serializable {
    private int x;
    private int y;

    public Point(){}

    public Point(int x, int y){
        this.x = x;
        this.y = y;
    }
    public Point(Point otherPoint){
        this.x = otherPoint.getX();
        this.y = otherPoint.getY();
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getY() {
        return y;
    }

    public int getX() {
        return x;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Point)) return false;
        Point point = (Point) o;
        return getX() == point.getX() &&
                getY() == point.getY();
    }

    @Override
    public int hashCode() {

        return Objects.hash(getX(), getY());
    }

    @Override
    public String toString() {
        return "(" + x + "," + y + ')';
    }

    public float distanceToOtherPoint(Point p){
        return (float)Math.sqrt(Math.pow(p.getX()-this.x,2)+Math.pow(p.getY()-this.y,2));
    }

    public int relativePosition(Point p){
        int result;
        if(p.getX() >= this.x && p.getY() >= this.y) result = 1;
        else if(p.getX() >= this.x && p.getY() <= this.y) result = 2;
        else if(p.getX() <= this.x && p.getY() >= this.y) result = 3;
        else result = 4;

        return result;

    }
}

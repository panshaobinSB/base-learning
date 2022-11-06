package com.panshao.model.data;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

public class LineWithPoint {
    public static final int UP = 0;
    public static final int RIGHT = 1;
    public static final int DOWN = 1;
    public static final int LEFT = 1;

    private Line2D line2D;
    private Point2D point2D;
    private int direction;

    public Line2D getLine2D() {
        return line2D;
    }

    public void setLine2D(Line2D line2D) {
        this.line2D = line2D;
    }

    public Point2D getPoint2D() {
        return point2D;
    }

    public void setPoint2D(Point2D point2D) {
        this.point2D = point2D;
    }

    public int getDirection() {
        return direction;
    }

    public void setDirection(int direction) {
        this.direction = direction;
    }
}

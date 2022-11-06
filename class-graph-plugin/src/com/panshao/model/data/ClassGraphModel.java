package com.panshao.model.data;

import com.intellij.psi.PsiClass;
import com.panshao.model.graph.GraphNode;

import java.awt.*;
import java.util.List;

public class ClassGraphModel {
    private GraphNode<PsiClass> graphNode;
    private Rectangle retangle;
    private java.util.List<LineWithPoint> lineWithPointList;
    private StringPoint stringPoint;

    public GraphNode<PsiClass> getGraphNode() {
        return graphNode;
    }

    public void setGraphNode(GraphNode<PsiClass> graphNode) {
        this.graphNode = graphNode;
    }

    public Rectangle getRetangle() {
        return retangle;
    }

    public void setRetangle(Rectangle retangle) {
        this.retangle = retangle;
    }

    public List<LineWithPoint> getLineWithPointList() {
        return lineWithPointList;
    }

    public void setLineWithPointList(List<LineWithPoint> lineWithPointList) {
        this.lineWithPointList = lineWithPointList;
    }

    public StringPoint getStringPoint() {
        return stringPoint;
    }

    public void setStringPoint(StringPoint stringPoint) {
        this.stringPoint = stringPoint;
    }
}

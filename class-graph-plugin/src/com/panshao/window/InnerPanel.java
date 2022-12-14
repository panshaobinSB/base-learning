package com.panshao.window;

import com.intellij.ide.projectView.ProjectView;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.search.GlobalSearchScope;
import com.panshao.model.graph.Edge;
import com.panshao.model.graph.GraphNode;
import com.panshao.model.data.ClassGraphModel;
import com.panshao.model.data.LineWithPoint;
import com.panshao.model.data.StringPoint;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InnerPanel<V> extends JPanel implements MouseMotionListener {

    private Map<PsiClass, ClassGraphModel> map = new HashMap<>();
    private ClassGraphModel currentClassGraphModel;
    private List<List<GraphNode<PsiClass>>> dataList;
    private int count;
    private int width;
    private int height;
    private Color paleTurquoise4 = new Color(102, 139, 139);
    private Color paleGreen3 = new Color(124, 205, 124);
    private Color khaki1 = new Color(255, 246, 143);

    public InnerPanel(Project project, ToolWindow toolWindow, List<List<GraphNode<PsiClass>>> dataList){
        this.count = 0;
        this.dataList = dataList;

        addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                int x = e.getX();
                int y = e.getY();
                currentClassGraphModel = getModel(x, y);
                if(currentClassGraphModel == null){
                    return;
                }

                StringPoint stringPoint = currentClassGraphModel.getStringPoint();
                String className = stringPoint.getText();
                GlobalSearchScope searchScope = GlobalSearchScope.allScope(project);
                VirtualFile virtualFile = JavaPsiFacade.getInstance(project).
                        findClass(className, searchScope).
                        getContainingFile().
                        getVirtualFile();

                ProjectView.getInstance(project).
                        getCurrentProjectViewPane().
                        select(null, virtualFile, true);

                FileEditorManager instance = FileEditorManager.getInstance(project);
                instance.openFile(virtualFile, true);
            }

            @Override
            public void mousePressed(MouseEvent e) {
                int x = e.getX();
                int y = e.getY();
                currentClassGraphModel = getModel(x, y);
            }
        });

        addMouseMotionListener(this);
    }


    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        if(count == 0) {
            initModel(g);
            // calculate the size
            this.setSize(width, height);
            this.setPreferredSize(new Dimension(width, height));
        }
        count++;
        repaintModel(g);
    }

    private void repaintCurrentModel(Graphics g){

        g.setColor(paleTurquoise4);
        ((Graphics2D)g).draw(currentClassGraphModel.getRetangle());

        StringPoint stringPoint = currentClassGraphModel.getStringPoint();
        g.setColor(khaki1);
        g.drawString(stringPoint.getText(), stringPoint.getX(), stringPoint.getY());

        g.setColor(paleGreen3);
        for (LineWithPoint lineWithPoint : currentClassGraphModel.getLineWithPointList()) {
            ((Graphics2D)g).draw(lineWithPoint.getLine2D());
        }
    }

    private void repaintModel(Graphics g){
        for (PsiClass aClass : map.keySet()) {
            g.setColor(paleTurquoise4);
            ((Graphics2D)g).draw(map.get(aClass).getRetangle());

            StringPoint stringPoint = map.get(aClass).getStringPoint();
            g.setColor(khaki1);
            g.drawString(stringPoint.getText(), stringPoint.getX(), stringPoint.getY());

            g.setColor(paleGreen3);
            for (LineWithPoint lineWithPoint : map.get(aClass).getLineWithPointList()) {
                ((Graphics2D)g).draw(lineWithPoint.getLine2D());
            }
        }
    }

    private void initModel(Graphics g){
        int stringHeight = g.getFontMetrics().getHeight();
        int rectHeight = stringHeight + 10;
        int baseX = 0;
        int maxWidth = 0;

        for (int i = 0; i < dataList.size(); i++) {
            int baseY = 50;
            baseX += maxWidth + 50;
            if (baseX >= width) {
                width = baseX + maxWidth + 50;
            }

            maxWidth = 0;
            for (int j = 0; j < dataList.get(i).size(); j++) {

                // build model
                GraphNode<PsiClass> classGraphNode = dataList.get(i).get(j);
                String className = classGraphNode.getVertex().getQualifiedName();
                int stringWidth = g.getFontMetrics().stringWidth(className);

                if(stringWidth > maxWidth){
                    maxWidth = stringWidth;
                }

                if (baseY >= height) {
                    height = baseY + rectHeight + 50;
                }

                ClassGraphModel classGraphModel = new ClassGraphModel();
                classGraphModel.setGraphNode(classGraphNode);

                Rectangle retangle = new Rectangle(baseX , baseY, stringWidth + 10 , rectHeight);
                classGraphModel.setRetangle(retangle);

                StringPoint stringPoint = new StringPoint();
                stringPoint.setText(className);
                stringPoint.setHeight(stringHeight);
                stringPoint.setWidth(stringWidth);
                stringPoint.setX(baseX + 5);
                stringPoint.setY(baseY + 17);
                classGraphModel.setStringPoint(stringPoint);

                classGraphModel.setLineWithPointList(new ArrayList<>());

                baseY += rectHeight + 50 ;

                map.put(classGraphNode.getVertex(), classGraphModel);
            }
        }

        for (int i = 0; i < dataList.size(); i++) {
            for (int j = 0; j < dataList.get(i).size(); j++) {
                GraphNode<PsiClass> classGraphNode = dataList.get(i).get(j);
                PsiClass vertex = classGraphNode.getVertex();
                ClassGraphModel classGraphModel = map.get(vertex);
                Rectangle retangle = classGraphModel.getRetangle();
                double x1 = retangle.getX() + retangle.getWidth();
                double y1 = retangle.getY() + retangle.getHeight()/2;

                for (Edge<PsiClass> classEdge : classGraphNode.getEdgeSet()) {
                    ClassGraphModel toClassGraphModel = map.get(classEdge.getTo());
                    Rectangle toRectangle = toClassGraphModel.getRetangle();
                    double x2 = toRectangle.getX();
                    double y2 = toRectangle.getY() + toRectangle.getHeight()/2;

                    LineWithPoint lineWithPoint = new LineWithPoint();
                    Line2D line2D = new Line2D.Double(x1, y1, x2, y2);
                    lineWithPoint.setLine2D(line2D);
                    lineWithPoint.setPoint2D(line2D.getP1());
                    lineWithPoint.setDirection(LineWithPoint.RIGHT);

                    classGraphModel.getLineWithPointList().add(lineWithPoint);

                    LineWithPoint lineWithPoint2 = new LineWithPoint();
                    lineWithPoint2.setLine2D(line2D);
                    lineWithPoint2.setPoint2D(line2D.getP2());
                    lineWithPoint2.setDirection(LineWithPoint.LEFT);

                    toClassGraphModel.getLineWithPointList().add(lineWithPoint2);
                }
            }
        }
    }

    private void updateModel(int x, int y){

        PsiClass aClass = currentClassGraphModel.getGraphNode().getVertex();
        Rectangle retangle = map.get(aClass).getRetangle();
        retangle.x = x;
        retangle.y = y;

        StringPoint stringPoint = map.get(aClass).getStringPoint();
        stringPoint.setX(x + 5);
        stringPoint.setY(y + 17);

        for (LineWithPoint lineWithPoint : map.get(aClass).getLineWithPointList()) {
            Line2D line2D = lineWithPoint.getLine2D();
            if (lineWithPoint.getDirection() == LineWithPoint.LEFT) {
                line2D.setLine(line2D.getP1(), new Point2D.Double(x, y + retangle.getHeight()/2));

            }else if (lineWithPoint.getDirection() == LineWithPoint.RIGHT) {
                line2D.setLine(new Point2D.Double(x + retangle.getWidth(), y + retangle.getHeight()/2), line2D.getP2());

            }
        }

        if (x + retangle.getWidth() + 50 > width) {
            width = x + Double.valueOf(retangle.getWidth()).intValue() + 50;
        }
        if(y + retangle.getHeight() + 50 > height){
            height = y + Double.valueOf(retangle.getHeight()).intValue() + 50;
        }
        this.setSize(width, height);
        this.setPreferredSize(new Dimension(width, height));
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();
        if(currentClassGraphModel == null){
            return;
        }

        Graphics graphics = getGraphics();

        graphics.setXORMode(getBackground());

        repaintCurrentModel(graphics);

        if(x < 0){
            x = 0;
        }

        if(y < 0){
            y = 0;
        }

        updateModel(x, y);

        repaintCurrentModel(graphics);

        graphics.dispose();

        repaint();
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();
        if (getModel(x, y) != null) {
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        } else {
            setCursor(Cursor.getDefaultCursor());
        }
    }

    public ClassGraphModel getModel(int x , int y){
        for (PsiClass aClass : map.keySet()) {
            if(map.get(aClass).getRetangle().contains(x, y)){
                return map.get(aClass);
            }
        }
        return null;
    }
}

package com.panshao.window;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ex.ToolWindowEx;
import com.intellij.psi.PsiClass;
import com.panshao.model.graph.GraphNode;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class DrawPanel extends JPanel {
    private JPanel innerPanel;
    private JScrollPane jScrollPane;
    private ToolWindow toolWindow;
    private Project project;

    public DrawPanel(Project project, ToolWindow toolWindow, List<List<GraphNode<PsiClass>>> dataList){
        this.project = project;
        this.toolWindow = toolWindow;

        ToolWindowEx tw = (ToolWindowEx) toolWindow;
        int width = tw.getComponent().getWidth();
        int height = tw.getComponent().getHeight();

        jScrollPane = new JScrollPane();

        jScrollPane.setBounds(0, 0, width, height);


        innerPanel = new InnerPanel(project, toolWindow, dataList);

       // setPanelSize(dataList, innerPanel, jScrollPane.getGraphics());
        innerPanel.setSize(1000, 1000);
        innerPanel.setPreferredSize(new Dimension(innerPanel.getWidth(), innerPanel.getHeight()));
        innerPanel.setVisible(true);

        jScrollPane.getViewport().add(innerPanel);
        jScrollPane.validate();

        this.add(jScrollPane);
    }

    public void setPanelSize(List<List<GraphNode<Class>>> dataList, JPanel innerPanel, Graphics graphics){
        int width = 0;
        int height = 0;
        int stringHeight = graphics.getFontMetrics().getHeight();
        int rectHeight = stringHeight + 10;
        int baseY = 50;
        for (int i = 0; i < dataList.size(); i++) {
            int baseX = 50;
            for (int j = 0; j < dataList.get(i).size(); j++) {

                GraphNode<Class> classGraphNode = dataList.get(i).get(j);
                String className = classGraphNode.getVertex().getName();
                int stringWidth = graphics.getFontMetrics().stringWidth(className);

                if (baseX >= width) {
                    width = baseX + stringWidth + 10 + 50;
                }
                if (baseY >= height) {
                    height = baseY + rectHeight + 50;
                }
            }
        }

        innerPanel.setSize(width, height);
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);

        ToolWindowEx tw = (ToolWindowEx) toolWindow;
        int width = tw.getComponent().getWidth();
        int height = tw.getComponent().getHeight();

        if(width != jScrollPane.getWidth() || height != jScrollPane.getHeight()){
            jScrollPane.setBounds(0, 0, width, height);
            jScrollPane.revalidate();
        }
    }
}

package com.panshao.window;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.psi.PsiClass;
import com.panshao.model.graph.GraphNode;

import javax.swing.*;
import java.util.List;

public class ClassGraphWindow {
    private JPanel jcontent;

    public ClassGraphWindow(Project project, ToolWindow toolWindow, List<List<GraphNode<PsiClass>>> dataList){
        jcontent = new DrawPanel(project, toolWindow, dataList);
        jcontent.setLayout(null);
        jcontent.setVisible(true);
    }

    public JPanel getJcontent(){
        return jcontent;
    }
}

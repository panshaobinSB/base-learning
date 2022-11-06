package com.panshao.action;

import com.intellij.ide.projectView.ProjectView;
import com.intellij.ide.projectView.impl.AbstractProjectViewPane;
import com.intellij.ide.projectView.impl.ProjectRootsUtil;
import com.intellij.ide.projectView.impl.nodes.PsiDirectoryNode;
import com.intellij.ide.util.treeView.NodeDescriptor;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.psi.*;
import com.intellij.psi.impl.file.PsiDirectoryImpl;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.panshao.model.graph.Edge;
import com.panshao.model.graph.GraphNode;
import com.panshao.model.graph.IDirectGraph;
import com.panshao.utils.GraphUtil;
import com.panshao.window.ClassGraphWindow;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class GenerateClassGraphAction extends AnAction {

    private List<PsiClass> getClassName(PsiPackage psiPackage, List<PsiClass> classNames){
        List<PsiClass> myClassName = new ArrayList<>();
        List<PsiClass> nameList = Arrays.stream(psiPackage.getClasses()).collect(Collectors.toList());
        myClassName.addAll(nameList);
        if(psiPackage.getSubPackages() != null && psiPackage.getSubPackages().length > 0){
            for (PsiPackage subPackage : psiPackage.getSubPackages()) {
                myClassName.addAll(getClassName(subPackage, myClassName));
            }
        }
        return myClassName;
    }

    @Override
    public void actionPerformed(AnActionEvent e) {

        Project project = e.getProject();
        // get the project packages
        String[] packageNames = getPackageNames(project);

        // generate the class graph data List<List<GraphNode<V>>> by packages
        List<PsiClass> classNames = new ArrayList<>();
        for (String packageName : packageNames) {
            PsiPackage psiPackage = JavaPsiFacade.getInstance(project).findPackage(packageName);
            List<PsiClass> tempList = getClassName(psiPackage, null);
            classNames.addAll(tempList);
        }

        IDirectGraph<PsiClass> graph = null;
        try {
            graph = GraphUtil.getGraphBy(classNames);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        List<List<GraphNode<PsiClass>>> dataList = graph.topoSortByKahn();
//        List<List<GraphNode<Class>>> dataList = new ArrayList<>();
//        dataList.add(new ArrayList<>());
//        dataList.add(new ArrayList<>());
//        dataList.add(new ArrayList<>());
//
//        Class<?> objectClass = null;
//        Class<?> listClass = null;
//        Class<?> stringClass = null;
//        Class<?> arrayListClass = null;
//        try {
//            objectClass = Class.forName("java.lang.Object");
//            listClass = Class.forName("java.util.List");
//            stringClass = Class.forName("java.lang.String");
//            arrayListClass = Class.forName("java.util.ArrayList");
//        } catch (ClassNotFoundException classNotFoundException) {
//            classNotFoundException.printStackTrace();
//        }
//        GraphNode<Class> graphNode = new GraphNode<>(objectClass);
//        graphNode.add(new Edge<>(objectClass, listClass));
//        graphNode.add(new Edge<>(objectClass, stringClass));
//        dataList.get(0).add(graphNode);
//
//        GraphNode<Class> graphNodeList = new GraphNode<>(listClass);
//        graphNodeList.add(new Edge<>(listClass, arrayListClass));
//        dataList.get(1).add(graphNodeList);
//
//        GraphNode<Class> graphNodeString = new GraphNode<>(stringClass);
//        dataList.get(1).add(graphNodeString);
//
//        GraphNode<Class> graphNodeArrayList = new GraphNode<>(arrayListClass);
//        dataList.get(2).add(graphNodeArrayList);

        // past data to init the toolWindow

        ToolWindowManager windowManager = ToolWindowManager.getInstance(project);
        windowManager.unregisterToolWindow("com.panshao.class.graph");
        ToolWindow toolWindow = windowManager.registerToolWindow("com.panshao.class.graph", false, ToolWindowAnchor.RIGHT, project, true);

        ClassGraphWindow classGraphicsWindow = new ClassGraphWindow(project, toolWindow, dataList);

        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();

        Content content = contentFactory.createContent(classGraphicsWindow.getJcontent(), "", false);

        toolWindow.getContentManager().addContent(content);

        toolWindow.show();
    }

    private String[] getPackageNames(Project project){
        AbstractProjectViewPane currentProjectViewPane = ProjectView.getInstance(project).getCurrentProjectViewPane();

        NodeDescriptor selectedDescriptor = currentProjectViewPane.getSelectedDescriptor();
        String[] packagePath = null;
        if (selectedDescriptor instanceof PsiDirectoryNode) {
            PsiDirectoryNode psiDirectoryNode = (PsiDirectoryNode) selectedDescriptor;
            PsiDirectory psiDirectory = psiDirectoryNode.getValue();

            if (ProjectRootsUtil.isSourceRoot(psiDirectory)) {
                packagePath = getPackageNames(psiDirectory.getChildren());

            }else if (ProjectRootsUtil.isProjectHome(psiDirectory)) {
                if(psiDirectory.getChildren() != null && psiDirectory.getChildren().length > 0){
                    for(int i = 0; i < psiDirectory.getChildren().length; i++){
                        if (psiDirectory.getChildren()[i] instanceof PsiDirectoryImpl &&
                            "src".equals(((PsiDirectoryImpl) psiDirectory.getChildren()[i]).getName())) {
                            packagePath = getPackageNames(psiDirectory.getChildren()[i].getChildren());
                        }
                    }
                }

            }else if (ProjectRootsUtil.isInSource(psiDirectory)) {
                boolean addFlag = false;
                String tempPackagePath = "";
                for (Object path : currentProjectViewPane.getSelectedPath().getPath()) {
                    String pathStr = path.toString();
                    if(addFlag){
                        tempPackagePath += pathStr + ".";
                    }

                    if("src".equals(pathStr)){
                        addFlag = true;
                    }
                }
                packagePath = new String[1];
                packagePath[0] = tempPackagePath.substring(0, tempPackagePath.length() - 1);
            }
        }

        return packagePath;
    }

    private String[] getPackageNames(PsiElement[] children){
        String[] packagePath = null;
        if(children == null || children.length == 0){
            return null;
        }

        packagePath = new String[children.length];
        int i = 0;
        for(PsiElement child : children){
            if(child instanceof PsiDirectoryImpl){
                String tempPackagePath = "";
                tempPackagePath = ((PsiDirectoryImpl)child).getName() + ".";

                while(child.getChildren() != null &&
                    child.getChildren().length == 1 &&
                    child.getFirstChild() instanceof PsiDirectoryImpl){

                    tempPackagePath +=  ((PsiDirectoryImpl)child.getFirstChild()).getName() + ".";

                    child = child.getFirstChild();
                }

                packagePath[i] = tempPackagePath.substring(0, tempPackagePath.length() - 1);
                i++;
            }
        }

        return packagePath;
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        boolean showAction = false;
        Project project = e.getProject();
        NodeDescriptor selectedDescriptor = ProjectView.getInstance(project).getCurrentProjectViewPane().getSelectedDescriptor();

        if(selectedDescriptor instanceof  PsiDirectoryNode){
            PsiDirectoryNode psiDirectoryNode = (PsiDirectoryNode) selectedDescriptor;
            if(ProjectRootsUtil.isInSource(psiDirectoryNode.getValue()) ||
                    ProjectRootsUtil.isSourceRoot(psiDirectoryNode.getValue()) ||
                      ProjectRootsUtil.isProjectHome(psiDirectoryNode.getValue())){
                showAction = true;
            }
        }

        e.getPresentation().setVisible(showAction);
    }
}

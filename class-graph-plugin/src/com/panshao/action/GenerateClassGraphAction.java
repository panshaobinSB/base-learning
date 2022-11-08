package com.panshao.action;

import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.ide.projectView.ProjectView;
import com.intellij.ide.projectView.impl.AbstractProjectViewPane;
import com.intellij.ide.projectView.impl.ProjectRootsUtil;
import com.intellij.ide.projectView.impl.nodes.PsiDirectoryNode;
import com.intellij.ide.util.treeView.NodeDescriptor;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.SourceFolder;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.psi.*;
import com.intellij.psi.impl.file.PsiDirectoryImpl;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.GlobalSearchScopes;
import com.intellij.psi.search.searches.AllClassesSearch;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.util.indexing.FileBasedIndex;
import com.panshao.model.graph.GraphNode;
import com.panshao.utils.GraphUtil;
import com.panshao.model.graph.IDirectGraph;
import com.panshao.window.ClassGraphWindow;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class GenerateClassGraphAction extends AnAction {

    private List<PsiClass> getClasses(PsiPackage psiPackage, List<PsiClass> classList){
        List<PsiClass> myClassList = new ArrayList<>();
        List<PsiClass> nameList = Arrays.stream(psiPackage.getClasses()).collect(Collectors.toList());
        myClassList.addAll(nameList);
        if(psiPackage.getSubPackages() != null && psiPackage.getSubPackages().length > 0){
            for (PsiPackage subPackage : psiPackage.getSubPackages()) {
                myClassList.addAll(getClasses(subPackage, myClassList));
            }
        }
        return myClassList;
    }

    @Override
    public void actionPerformed(AnActionEvent e) {

        Project project = e.getProject();
        AbstractProjectViewPane currentProjectViewPane = ProjectView.getInstance(project).getCurrentProjectViewPane();
        NodeDescriptor selectedDescriptor = currentProjectViewPane.getSelectedDescriptor();
        PsiDirectoryNode psiDirectoryNode = (PsiDirectoryNode) selectedDescriptor;
        PsiDirectory psiDirectory = psiDirectoryNode.getValue();
        GlobalSearchScope globalSearchScope = GlobalSearchScopes.directoryScope(psiDirectory, true);

        // Collection<PsiClass> allClass = AllClassesSearch.search(globalSearchScope, project).findAll();

        List<PsiClass> classes = new ArrayList<>();
        Collection<VirtualFile> containingFiles = FileBasedIndex.getInstance()
                .getContainingFiles(
                        FileTypeIndex.NAME,
                        JavaFileType.INSTANCE,
                        globalSearchScope);

        for (VirtualFile virtualFile : containingFiles) {
            PsiFile psiFile = PsiManager.getInstance(project).findFile(virtualFile);
            if (psiFile instanceof PsiJavaFile) {
                PsiJavaFile psiJavaFile = (PsiJavaFile) psiFile;
                PsiClass[] javaFileClasses = psiJavaFile.getClasses();

                for (PsiClass javaFileClass : javaFileClasses) {
                    classes.add(javaFileClass);
                }
            }
        }

        IDirectGraph<PsiClass> graph = null;
        try {
            graph = GraphUtil.getGraphBy(classes);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        List<List<GraphNode<PsiClass>>> dataList = graph.topoSortByKahn();

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

            } else if (ProjectRootsUtil.isModuleContentRoot(psiDirectory)) {
                // TODO


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
                    ProjectRootsUtil.isProjectHome(psiDirectoryNode.getValue()) ||
                    ProjectRootsUtil.isModuleContentRoot(psiDirectoryNode.getValue())
            ){
                showAction = true;
            }
        }

        e.getPresentation().setVisible(showAction);
    }
}

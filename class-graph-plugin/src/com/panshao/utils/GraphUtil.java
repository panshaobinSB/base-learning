package com.panshao.utils;

import com.intellij.psi.PsiClass;
import com.panshao.model.graph.Edge;
import com.panshao.model.graph.IDirectGraph;
import com.panshao.model.graph.ListDirectGraph;

import java.util.List;

public class GraphUtil {

    public static IDirectGraph<PsiClass> getGraphBy(List<PsiClass> psiClasses) throws Exception{

        IDirectGraph<PsiClass> directGraph = new ListDirectGraph<>();

        for (PsiClass psiClass : psiClasses) {
            directGraph.addVertex(psiClass);

            if(psiClass.getSuperClass() != null){
                directGraph.addVertex(psiClass.getSuperClass());
                directGraph.addEdge(new Edge<>(psiClass.getSuperClass(), psiClass, 0.0));
            }

            if(psiClass.getInterfaces() != null && psiClass.getInterfaces().length > 0){
                for (PsiClass anInterface : psiClass.getInterfaces()) {
                    directGraph.addVertex(anInterface);
                    directGraph.addEdge(new Edge<>(anInterface, psiClass, 1.0));
                }
            }

            // TODO fields
        }

        return directGraph;
    }

}

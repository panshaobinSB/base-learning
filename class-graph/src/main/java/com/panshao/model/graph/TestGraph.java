package com.panshao.model.graph;

import com.panshao.PackageUtil;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class TestGraph {
    public static void main(String[] args) throws Exception {
        // String packageName = "org.reflections";
        // String packageName = "com.panshao";
        String packageName = "org.junit";
        testClassGraph(packageName);
        // test2();
    }

    public static void testClassGraph(String packageName) throws Exception{
        // String packageName = "org.reflections";
        // String packageName = "com.panshao";
        List<String> classNames = PackageUtil.getClassName(packageName);
        IDirectGraph<Class> directGraph = new ListDirectGraph<>();

        List<String> classNameWithoutAnonymousInnerClass = classNames.stream()
                .filter(v -> !Pattern.matches(".*(\\$)(\\d+)", v))
                .collect(Collectors.toList());

        if (classNameWithoutAnonymousInnerClass != null) {
            for (String className : classNameWithoutAnonymousInnerClass) {

                // 去除匿名内部类 xxx$xxx.class
                String pattern = ".*(\\$)(\\d+)";
                boolean isMatch = Pattern.matches(pattern, className);
                if (isMatch) {
                    continue;
                }

                Class myClass = Class.forName(className);
                String parentClassName = (myClass.getGenericSuperclass() == null? "" : myClass.getGenericSuperclass().getTypeName());

                directGraph.addVertex(myClass);

                if(myClass.getGenericSuperclass() != null && !"java.lang.Object".equals(parentClassName)){

                    directGraph.addVertex(myClass.getGenericSuperclass().getClass());

                    directGraph.addEdge(new Edge<>(myClass.getGenericSuperclass().getClass(), myClass, 0.0));
                }

                if(myClass.getInterfaces() != null && myClass.getInterfaces().length > 0){
                    Arrays.stream(myClass.getInterfaces()).forEach(v -> {
                        directGraph.addVertex(v);

                        directGraph.addEdge(new Edge<>(v, myClass, 1.0));
                    });
                }

                if(myClass.getDeclaredFields() != null && myClass.getDeclaredFields().length > 0){
                    Arrays.stream(myClass.getDeclaredFields()).forEach(v -> {

                        directGraph.addVertex(v.getType());

                        directGraph.addEdge(new Edge<>(v.getType(), myClass, 2.0));
                    });
                }

            }

            directGraph.topoSortByKahn();
            System.out.println();
        }
    }

    public static void test1(){
        IDirectGraph<String> directGraph = new ListDirectGraph<>();
//1. 初始化顶点
        directGraph.addVertex("1");
        directGraph.addVertex("2");
        directGraph.addVertex("3");
        directGraph.addVertex("4");
        directGraph.addVertex("5");
        directGraph.addVertex("6");
        directGraph.addVertex("7");
        directGraph.addVertex("8");

//2. 初始化边
        directGraph.addEdge(new Edge<>("1", "2"));
        directGraph.addEdge(new Edge<>("1", "3"));
        directGraph.addEdge(new Edge<>("2", "4"));
        directGraph.addEdge(new Edge<>("2", "5"));
        directGraph.addEdge(new Edge<>("3", "6"));
        directGraph.addEdge(new Edge<>("3", "7"));
        directGraph.addEdge(new Edge<>("4", "8"));
        directGraph.addEdge(new Edge<>("8", "5"));
        directGraph.addEdge(new Edge<>("6", "7"));
//3. BFS 遍历
        List<String> bfsList = directGraph.bfs("1");
        System.out.println(bfsList);

//4. DFS 遍历
        List<String> dfsList = directGraph.dfs("1");
        System.out.println(dfsList);
    }

    public static void test2(){
        IDirectGraph<String> directGraph = new ListDirectGraph<>();
//1. 初始化顶点
        directGraph.addVertex("1");
        directGraph.addVertex("2");
        directGraph.addVertex("3");
        directGraph.addVertex("4");
        directGraph.addVertex("5");
        directGraph.addVertex("6");
        directGraph.addVertex("7");
        directGraph.addVertex("8");
        directGraph.addVertex("9");

//2. 初始化边
        directGraph.addEdge(new Edge<>("2", "5"));
        directGraph.addEdge(new Edge<>("3", "5"));
        directGraph.addEdge(new Edge<>("4", "5"));
        directGraph.addEdge(new Edge<>("6", "7"));
        directGraph.addEdge(new Edge<>("6", "9"));
        directGraph.addEdge(new Edge<>("7", "9"));
//3. BFS 遍历
        directGraph.topoSortByKahn();
    }
}

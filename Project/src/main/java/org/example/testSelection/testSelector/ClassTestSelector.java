package org.example.testSelection.testSelector;

import com.ibm.wala.classLoader.ShrikeBTMethod;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import org.example.testSelection.Utils.MethodType;
import org.example.testSelection.Utils.Utils;

import java.util.HashSet;
import java.util.Iterator;

public class ClassTestSelector extends TestSelector {
    @Override
    protected void initiateCallMatrix() {
        CallGraph callGraph = basicData.getCallGraph();
        // 先填充key
        for (CGNode node : callGraph) {
            if (node.getMethod() instanceof ShrikeBTMethod) {
                ShrikeBTMethod curMethod = (ShrikeBTMethod) node.getMethod();
                MethodType curMethodType = Utils.getMethodType(curMethod);
                if (curMethodType != MethodType.Other) {
                    String className = curMethod.getDeclaringClass().getName().toString();  // key只填充类名
                    if (!callMatrix.containsKey(className)) {
                        callMatrix.put(className, new HashSet<>());
                    }
                    // 找调用关系
                    Iterator<CGNode> callerNodes = callGraph.getPredNodes(node);
                    while (callerNodes.hasNext()) {
                        CGNode curCallerNode = callerNodes.next();
                        if (curCallerNode.getMethod() instanceof ShrikeBTMethod) {
                            ShrikeBTMethod curCallerMethod = (ShrikeBTMethod) curCallerNode.getMethod();
                            if (Utils.getMethodType(curCallerMethod) != MethodType.Other) {
                                String callerClassName = curCallerMethod.getDeclaringClass().getName().toString();
                                callMatrix.get(className).add(callerClassName);
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void selectTestCases() {
        initiateCallMatrix();

        for (String change : basicData.getChanges()) {
            String changedClassName = change.split(" ")[0];
            dfs(new HashSet<>(), changedClassName);
        }

        StringBuilder sb = new StringBuilder();
        for (String selectedCase : selectedTestCases) {
            sb.append(selectedCase).append("\n");
        }
        String dotFileContent = Utils.toDot(basicData.getProjectName(), callMatrix);
        Utils.write2File("class-" + basicData.getProjectName() + ".dot", dotFileContent);
        Utils.write2File("selection-class.txt", sb.toString());
    }


    protected void dfs(HashSet<String> visited, String callee) {
        if (visited.contains(callee))
            return;
        visited.add(callee);
        for (String caller : callMatrix.get(callee)) {
            // 如果是@Test方法，就把方法签名加入结果列表
            if (basicData.getAllTestMethods().containsKey(caller)) {
                HashSet<String> callerMethods = basicData.getAllTestMethods().get(caller);
                for (String method : callerMethods) {
                    selectedTestCases.add(caller + ' ' + method);
                }
            }
            dfs(visited, caller);
        }
    }
}

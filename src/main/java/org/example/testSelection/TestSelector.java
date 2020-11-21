package org.example.testSelection;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class TestSelector {
    // 图的所有顶点名称，在class级别里面名称就是class， 在method级别，名称是类 + 方法签名
    // protected ArrayList<String> allVertex = new ArrayList<>();
    // 调用关系的邻接表, 行号和上面的顶点相对应，记录某个顶点的调用者（caller）
    protected HashMap<String, HashSet<String>> callMatrix = new HashMap<>();
    // 基本数据
    protected BasicData basicData = BasicData.getInstance();
    // 选中的测试用例
    HashSet<String> selectedTestCases = new HashSet<>();

    /**
     * 初始化邻接表
     */
    protected void initiateCallMatrix(){}

    /**
     * 选择到受变更影响的测试用例
     */
    protected void selectTestCases(){}

    /**
     * dfs过程中，对于一个类名或者完整的方法签名，判断其是不是@Test方法
     * 由于继承，可能是类名或类名 + 方法签名
     */
    // protected void dfs(HashSet<String> visited, String callee){}


}

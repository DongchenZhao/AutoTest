package org.example.testSelection;

import com.ibm.wala.classLoader.Language;
import com.ibm.wala.classLoader.ShrikeBTMethod;
import com.ibm.wala.ipa.callgraph.*;
import com.ibm.wala.ipa.callgraph.impl.AllApplicationEntrypoints;
import com.ibm.wala.ipa.callgraph.impl.Util;
import com.ibm.wala.ipa.callgraph.propagation.SSAPropagationCallGraphBuilder;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.ipa.cha.ClassHierarchyFactory;
import com.ibm.wala.shrikeCT.InvalidClassFileException;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.config.AnalysisScopeReader;
import org.example.testSelection.Utils.MethodType;
import org.example.testSelection.Utils.Utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;

public class BasicData {
    private static BasicData basicData;

    // 项目名称
    private String projectName;
    // 调用图
    private CallGraph callGraph;
    // 从文件中读取的变更信息
    private HashSet<String> changes = new HashSet<>();
    // 测试类的测试方法<类名，方法签名>
    private HashMap<String, HashSet<String>> allTestMethods = new HashMap<>();

    public String getProjectName() {
        return projectName;
    }

    public CallGraph getCallGraph() {
        return callGraph;
    }

    public HashSet<String> getChanges() {
        return changes;
    }

    public HashMap<String, HashSet<String>> getAllTestMethods() {
        return allTestMethods;
    }

    private BasicData() {}

    /**
     * 在取得单例之前必须初始化
     */
    public static BasicData getInstance(){
        if(BasicData.basicData == null){
            System.out.println("please initiate instance");
            return null;
        }
        return basicData;
    }

    /**
     * 初始化单例
     */
    public static void initiateInstance(String projectTarget, String changeInfo) throws IOException, InvalidClassFileException, ClassHierarchyException, CancelException {

        if (basicData != null)
            return;
        // 创建实例
        basicData = new BasicData();
        String[] targetDir = projectTarget.split("/|\\\\");
        try {
            basicData.projectName = targetDir[targetDir.length - 2].split("-")[1];
        } catch (Exception e) {
            // 否则，项目名称设置为文件夹名
            basicData.projectName = targetDir[targetDir.length - 2];
        }

        // 读取变更信息文件
        try {
            Scanner in = new Scanner(new FileReader(changeInfo));
            while (in.hasNextLine()) {
                basicData.changes.add(in.nextLine());
            }
        } catch (FileNotFoundException e) {
            System.out.println("file not found");
            e.printStackTrace();
        }
        if(!projectTarget.endsWith(File.separator))
            projectTarget += File.separator;

        // 以下行为构建callGraph
        AnalysisScope analysisScope;
        ClassHierarchy classHierarchy;   // 层次关系对象
        String testClassDir = projectTarget + "test-classes";  // 测试类根目录
        String normalClassDir = projectTarget + "classes"; // 生产代码的路径
        analysisScope = AnalysisScopeReader.readJavaScope("scope.txt", new File("exclusion.txt"), BasicData.class.getClassLoader());
        // 将测试类添加进scope
        addClassToScope(analysisScope, new File(testClassDir));
        classHierarchy = ClassHierarchyFactory.makeWithRoot(analysisScope);  // 生成层次关系对象
        AllApplicationEntrypoints entryPoints = new AllApplicationEntrypoints(analysisScope, classHierarchy); // 生成进入点
        AnalysisOptions option = new AnalysisOptions(analysisScope, entryPoints);
        SSAPropagationCallGraphBuilder builder = Util.makeZeroCFABuilder(Language.JAVA, option, new AnalysisCacheImpl(), classHierarchy, analysisScope);
        basicData.callGraph = builder.makeCallGraph(option);
        // 遍历callGraph，找到所有的@Test方法
        for(CGNode node: basicData.callGraph){
            if(node.getMethod() instanceof ShrikeBTMethod){
                ShrikeBTMethod curMethod = (ShrikeBTMethod) node.getMethod();
                MethodType curMethodType = Utils.getMethodType(curMethod);
                if(curMethodType == MethodType.Test){ // 如果是@Test方法，放入单件的allTestMethods字段中
                    // TODO DIFF
                    String className = curMethod.getDeclaringClass().getName().toString();
                    String methodSignature = curMethod.getSignature();
                    if(!basicData.allTestMethods.containsKey(className)) {
                        basicData.allTestMethods.put(className, new HashSet<>());
                    }
                    basicData.allTestMethods.get(className).add(methodSignature);
                }
            }
        }
        // 进一步对生产代码生成callGraph
        addClassToScope(analysisScope, new File(normalClassDir));
        classHierarchy = ClassHierarchyFactory.makeWithRoot(analysisScope);
        entryPoints = new AllApplicationEntrypoints(analysisScope, classHierarchy);
        option = new AnalysisOptions(analysisScope, entryPoints);
        builder = Util.makeZeroCFABuilder(Language.JAVA, option, new AnalysisCacheImpl(), classHierarchy, analysisScope);
        basicData.callGraph = builder.makeCallGraph(option);
    }

    /**
     * 递归地找到当前目录中地字节码文件，并加入scope中
     * @param analysisScope 范围
     * @param rootDir 当前目录
     * @throws InvalidClassFileException
     */
    private static void addClassToScope(AnalysisScope analysisScope, File rootDir) throws InvalidClassFileException {
        File[] files = rootDir.listFiles();
        for(File file: files){
            if(file.isDirectory()){
                addClassToScope(analysisScope, file); // 是目录，递归进行搜索
            }
            else if(file.isFile() && file.getName().endsWith(".class")){ // 是字节码文件
                analysisScope.addClassFileToScope(ClassLoaderReference.Application, file);
            }
        }
    }
}

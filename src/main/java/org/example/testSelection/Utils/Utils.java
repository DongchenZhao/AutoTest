package org.example.testSelection.Utils;

import com.ibm.wala.classLoader.ShrikeBTMethod;
import com.ibm.wala.types.annotations.Annotation;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

public class Utils {
    public static MethodType getMethodType(ShrikeBTMethod method) {
        if ("Application".equals(method.getDeclaringClass().getClassLoader().toString())) {
            Collection<Annotation> annotations = method.getAnnotations();
            for (Annotation annotation : annotations) {
                if (annotation.toString().contains("Lorg/junit/Test")) {
                    return MethodType.Test;
                }
            }
            return MethodType.Normal;
        }
        return MethodType.Other;
    }

    public static void write2File(String path, String content) {
        try {
            Writer writer = new FileWriter(path);
            writer.write(content);
            writer.close();
        } catch (IOException e) {
            System.out.println("error while output");
            e.printStackTrace();
        }
    }

    public static String toDot(String projectName, HashMap<String, HashSet<String>> callerMatrix) {
        StringBuilder sb = new StringBuilder();
        sb.append("digraph ").append(projectName).append(" {\n");
        for(String callee : callerMatrix.keySet()){
            for(String caller: callerMatrix.get(callee)){
                sb.append("\t\"").append(callee).append("\" -> \"").append(caller).append("\";\n");
            }
        }
        String res = sb.toString();
        res.trim();
        res += "}";
        return res;
    }
}

package org.example.testSelection;

import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.shrikeCT.InvalidClassFileException;
import com.ibm.wala.util.CancelException;

import java.io.IOException;

public class Main {
    private static BasicData basicData;
    private static TestSelector testSelector;

    public static void main(String[] args) throws CancelException, ClassHierarchyException, InvalidClassFileException, IOException {
        preprocess(args);
        String option = args[0];
        if(option.equals("-c")){
            testSelector = new ClassTestSelector();
        }
        else if(option.equals("-m")){
            testSelector = new MethodTestSelector();
        }
        else{
            System.out.println("error option parameter");
            return;
        }
        testSelector.selectTestCases();
    }


    public static void preprocess(String[] args) throws CancelException, ClassHierarchyException, InvalidClassFileException, IOException {
        String option = args[0];
        String projectTarget = args[1];
        String changeInfo = args[2];
        BasicData.initiateInstance(projectTarget, changeInfo);
        basicData = BasicData.getInstance();
    }
}

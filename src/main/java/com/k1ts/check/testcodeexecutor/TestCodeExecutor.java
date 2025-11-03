package com.k1ts.check.testcodeexecutor;

import com.k1ts.check.codeinspector.CodeInspector;
import com.k1ts.check.javacodecontainer.TestJavaCodeContainer;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;

import java.util.List;
import java.util.Map;

@Builder
public class TestCodeExecutor {
    private final String className;
    private final String testCode;

    @Singular
    private final List<TestCase> testCases;

    private final static String PATTERN = """
            import java.util.*;
            import java.io.*;
            import java.lang.reflect.*;
                        
            public class ${className} {
                public static void main(String[] args) {
                    ${testCode}
                }
            }
            """;

    private String generateTestCode(String className, String input) {
        return PATTERN
                .replace("${className}", className)
                .replace("${testCode}", input == null ?
                        testCode
                        : testCode.replace("${input}", input));
    }

    public void apply(CodeInspector codeInspector) {
        for (int i = 0; i < testCases.size(); i++) {
            TestCase testCase = testCases.get(i);

            codeInspector.addJavaContainer(TestJavaCodeContainer
                    .builder()
                    .className(className + i)
                    .javaCode(generateTestCode(className + i, testCase.getInput()))
                    .consoleInput(testCase.getConsoleInput())
                    .build());

            final int iterator = i;

            codeInspector.addPostProcessor((checkResult, codeResult) -> {
                Map<String, String> classConsoleOutputMap = codeResult.getClassConsoleOutputMap();

                if (testCase.getExpectedOutput().equals(classConsoleOutputMap.get(className + iterator).replace("\r", "").strip())) {
                    checkResult.getSuccessResult().add(testCase.getSuccessMessageOnExpectedOutputEqualsActualOutput()
                            .replace("${input}", testCase.getInput() == null ? "" : testCase.getInput()));
                } else {
                    checkResult.getFailedResult().add(testCase.getFailedMessageOnExpectedOutputNotEqualsActualOutput()
                            .replace("${input}", testCase.getInput() == null ? "" : testCase.getInput())
                            .replace("${expectedOutput}", testCase.getExpectedOutput())
                            .replace("${actualOutput}", classConsoleOutputMap.get(className + iterator)));
                }
            });
        }
    }

    @Getter
    @Builder
    public static class TestCase {
        private final String input;
        private final String consoleInput;
        private final String expectedOutput;
        private final String successMessageOnExpectedOutputEqualsActualOutput;
        private final String failedMessageOnExpectedOutputNotEqualsActualOutput;
    }
}

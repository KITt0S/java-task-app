package com.k1ts.check.taskcheckservice.year_2024.course_3.cpp.practice_1;

import com.k1ts.check.CheckResult;
import com.k1ts.check.codeinspector.CodeInspector;
import com.k1ts.check.codevalidator.CodeValidator;
import com.k1ts.check.javacodecontainer.TestJavaCodeContainer;
import com.k1ts.check.taskcheckservice.DefaultAbstractTaskCheckService;
import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service("year-2024-course-3-cpp-practice-1-task-1")
public class Year2024Course3CppPractice1Task1CheckService extends DefaultAbstractTaskCheckService {

    @Lookup
    public CodeInspector getCodeInspector() {
        return null;
    }


    @Override
    public CheckResult doCheck(String code) {
        CodeInspector codeInspector = getCodeInspector();
        codeInspector.setClassName("MainThreadApp");

        codeInspector.addCodeValidator(checkResult -> CodeValidator
                .builder()
                .className("MainThreadApp")
                .code(code)
                .methodValidator(CodeValidator.MethodValidator
                        .builder()
                        .methodName("public static void main(String[] args)")
                        .line(CodeValidator.LineValidator
                                .builder()
                                .line(".*?.getName\\(\\).*?")
                                .successfulMassageIfLinePresent("Ти використав метод getName")
                                .failedMessageIfLineNotPresent("Ти не використав метод getName")
                                .build())
                        .line(CodeValidator.LineValidator
                                .builder()
                                .line(".*?.getPriority\\(\\).*?")
                                .successfulMassageIfLinePresent("Ти використав метод getPriority")
                                .failedMessageIfLineNotPresent("Ти не використав метод getPriority")
                                .build())
                        .line(CodeValidator.LineValidator
                                .builder()
                                .line(".*?.getThreadGroup\\(\\)\\.getName\\(\\).*?")
                                .successfulMassageIfLinePresent("Ти використав метод getPriority")
                                .failedMessageIfLineNotPresent("Ти не використав метод getPriority")
                                .build())
                        .build())
                .build()
                .validate(checkResult));

        codeInspector.addJavaContainer(TestJavaCodeContainer
                .builder()
                .className("TestCase")
                .javaCode("""
                        public class TestCase {
                            public static void main(String[] args) {
                                MainThreadApp.main(args);
                            }
                        }
                        """)
                .build());

        codeInspector.addPostProcessor((checkResult, codeResult) -> {
            Map<String, String> classConsoleOutputMap = codeResult.getClassConsoleOutputMap();

            String actualOutput = classConsoleOutputMap.get("TestCase").strip();

            String expectedOutput = "main, 5, main";

            if (expectedOutput.equals(actualOutput)) {
                checkResult.getSuccessResult().add("Твій код працює вірно");
            } else {
                checkResult.getFailedResult().add("Твій код працює невірно, повинен виводити " + expectedOutput + ", а виводить " + actualOutput);
            }
        });

        return codeInspector.inspect(code);
    }

    private static class MainThreadApp {
        public static void main(String[] args) {
            System.out.println(Thread.currentThread().getName() + ", " +
                    Thread.currentThread().getPriority() + ", " +
                    Thread.currentThread().getThreadGroup().getName());
        }
    }
}

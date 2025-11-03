package com.k1ts.check.taskcheckservice.year_2024.course_4.java.practice_1;

import com.k1ts.check.CheckResult;
import com.k1ts.check.codeinspector.CodeInspector;
import com.k1ts.check.codevalidator.CodeValidator;
import com.k1ts.check.javacodecontainer.TestJavaCodeContainer;
import com.k1ts.check.taskcheckservice.DefaultAbstractTaskCheckService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

@RequiredArgsConstructor
@Service("year-2024-course-4-java-practice-1-task-1")
public class Year2024Course4JavaPractice1Task1CheckService extends DefaultAbstractTaskCheckService {

    @Lookup
    public CodeInspector getCodeInspector() {
        return null;
    }

    @Override
    public CheckResult doCheck(String code) {
        CodeInspector codeInspector = getCodeInspector();

        codeInspector.setClassName("DateApp");

        codeInspector.addCodeValidator((checkResult) -> CodeValidator
                .builder()
                .className("DateApp")
                .code(code)
                .methodValidator(CodeValidator.MethodValidator
                        .builder()
                        .methodName("public static void main(String[] args)")
                        .line(CodeValidator.LineValidator
                                .builder()
                                .line(".*?new Date\\(\\).*?")
                                .successfulMassageIfLinePresent("Ти створив об'єкт класу Date")
                                .failedMessageIfLineNotPresent("Ти не створив об'єкт класу Date")
                                .build())
                        .line(CodeValidator.LineValidator
                                .builder()
                                .line(".*?new SimpleDateFormat\\(\"yyyy\\/MM\\/dd\"\\).*?")
                                .successfulMassageIfLinePresent("Ти створив об'єкт класу SimpleDateFormat")
                                .failedMessageIfLineNotPresent("Ти не створив об'єкт класу SimpleDateFormat")
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
                                DateApp.main(args);
                            }
                        }
                        """)
                .build());

        codeInspector.addPostProcessor((checkResult, codeResult) -> {
            Map<String, String> classConsoleOutputMap = codeResult.getClassConsoleOutputMap();

            String actualOutput = classConsoleOutputMap.get("TestCase").strip();

            String expectedOutput = new SimpleDateFormat("yyyy/MM/dd").format(new Date());

            if (expectedOutput.equals(actualOutput)) {
                checkResult.getSuccessResult().add("Твій код працює вірно");
            } else {
                checkResult.getFailedResult().add("Твій код працює невірно, повинен виводити " + expectedOutput + ", а виводить " + actualOutput);
            }
        });

        return codeInspector.inspect(code);
    }

    private static class DateApp {
        public static void main(String[] args) {
            Date date = new Date();
            System.out.println(new SimpleDateFormat("yyyy/MM/dd").format(date));
        }
    }
}

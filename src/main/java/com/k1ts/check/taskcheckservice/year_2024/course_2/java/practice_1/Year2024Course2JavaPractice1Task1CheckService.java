package com.k1ts.check.taskcheckservice.year_2024.course_2.java.practice_1;

import com.k1ts.check.CheckResult;
import com.k1ts.check.codeinspector.CodeInspector;
import com.k1ts.check.codevalidator.CodeValidator;
import com.k1ts.check.taskcheckservice.DefaultAbstractTaskCheckService;
import com.k1ts.check.testcodeexecutor.TestCodeExecutor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service("year-2024-course-2-java-practice-1-task-1")
public class Year2024Course2JavaPractice1Task1CheckService extends DefaultAbstractTaskCheckService {

    @Lookup
    public CodeInspector getCodeInspector() {
        return null;
    }

    @Override
    public CheckResult doCheck(String code) {
        CodeInspector codeInspector = getCodeInspector();

        codeInspector.setClassName("Program");

        codeInspector.addCodeValidator((checkResult) -> CodeValidator
                .builder()
                .className("Program")
                .code(code)
                .methodValidator(CodeValidator.MethodValidator
                        .builder()
                        .methodName("public static void main(String[] args)")
                        .line(CodeValidator.LineValidator
                                .builder()
                                .line("System\\.out\\.println\\(.*?\\);")
                                .successfulMassageIfLinePresent("Ти використав метод println")
                                .failedMessageIfLineNotPresent("Ти не використав метод println")
                                .build())
                        .build())
                .build()
                .validate(checkResult));

        TestCodeExecutor
                .builder()
                .className("TestCaseProgram")
                .testCode("Program.main(null);")
                .testCase(TestCodeExecutor.TestCase
                        .builder()
                        .expectedOutput("Java – одна з найсучасніших мов програмування у світі")
                        .successMessageOnExpectedOutputEqualsActualOutput("Метод main класу Program працює вірно")
                        .failedMessageOnExpectedOutputNotEqualsActualOutput("Метод main класу Program працює невірно, повинен виводити ${expectedOutput}, а виводить ${actualOutput}")
                        .build())
                .build()
                .apply(codeInspector);

        return codeInspector.inspect(code);
    }

    @SuppressWarnings("unused")
    private static class Program {

        public static void main(String[] args) {
            System.out.println("Java – одна з найсучасніших мов програмування у світі");
        }
    }
}

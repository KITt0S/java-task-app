package com.k1ts.check.taskcheckservice.year_2024.course_2.oop_2.practice_1;

import com.k1ts.check.CheckResult;
import com.k1ts.check.codeinspector.CodeInspector;
import com.k1ts.check.codevalidator.CodeValidator;
import com.k1ts.check.taskcheckservice.DefaultAbstractTaskCheckService;
import com.k1ts.check.testcodeexecutor.TestCodeExecutor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service("year-2024-course-2-oop_2-practice-1-task-1")
public class Year2024Course2Oop2Practice1Task1CheckService extends DefaultAbstractTaskCheckService {

    @Lookup
    public CodeInspector getCodeInspector() {
        return null;
    }

    @Override
    public CheckResult doCheck(String code) {
        CodeInspector codeInspector = getCodeInspector();

        codeInspector.setClassName("Program");

        codeInspector.addCodeValidator((checkResult) -> {
            CodeValidator
                    .builder()
                    .className("Printable")
                    .code(code)
                    .methodValidator(CodeValidator.MethodValidator
                            .builder()
                            .methodName(" abstract void print()")
                            .build())
                    .build()
                    .validate(checkResult);

            CodeValidator
                    .builder()
                    .className("Document")
                    .implementedInterfaceName("Printable")
                    .code(code)
                    .methodValidator(CodeValidator.MethodValidator
                            .builder()
                            .methodName("public void print()")
                            .build())
                    .build()
                    .validate(checkResult);

            CodeValidator
                    .builder()
                    .className("Program")
                    .code(code)
                    .methodValidator(CodeValidator.MethodValidator
                            .builder()
                            .methodName("public static void main(String[] args)")
                            .line(CodeValidator.LineValidator
                                    .builder()
                                    .line("Printable document = new Document\\(\\);")
                                    .successfulMassageIfLinePresent("Ти створив змінну document типу Printable у методі main класу Program")
                                    .failedMessageIfLineNotPresent("Ти не створив змінну document типу Printable у методі main класу Program")
                                    .build())
                            .line(CodeValidator.LineValidator
                                    .builder()
                                    .line("document.print\\(\\);")
                                    .successfulMassageIfLinePresent("Ти викликав метод print у змінної document")
                                    .failedMessageIfLineNotPresent("Ти не викликав метод print у змінної document")
                                    .build())
                            .build())
                    .build()
                    .validate(checkResult);
        });

        TestCodeExecutor
                .builder()
                .className("TestCase")
                .testCode("""
                        Program program = new Program();
                        program.main(null);
                        """)
                .testCase(TestCodeExecutor.TestCase
                        .builder()
                        .input("")
                        .expectedOutput("Документ виведено на друк")
                        .successMessageOnExpectedOutputEqualsActualOutput("Метод main класу Program працює вірно")
                        .failedMessageOnExpectedOutputNotEqualsActualOutput("Метод main класу Program працює невірно, повинен виводити ${expectedOutput}, а виводить ${actualOutput}")
                        .build())
                .build()
                .apply(codeInspector);

        return codeInspector.inspect(code);
    }

    private interface Printable {

        void print();
    }

    private static class Document implements Printable {

        @Override
        public void print() {
            System.out.println("Документ виведено на друк");
        }
    }

    private static class Program {

        public static void main(String[] args) {
            Printable document = new Document();
            document.print();
        }
    }
}

package com.k1ts.check.codeinspector;

import com.k1ts.check.CheckResult;
import com.k1ts.check.CodeResult;
import com.k1ts.check.codeprocessor.CodeProcessor;
import com.k1ts.check.javacodecontainer.ClassNameIsAbsentException;
import com.k1ts.check.javacodecontainer.JavaCodeContainer;
import com.k1ts.check.javacodecontainer.SourceJavaCodeContainer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.*;

@Scope("prototype")
@RequiredArgsConstructor
@Service
public class CodeInspector {
    private final CodeProcessor codeProcessor;

    @Getter
    private final CheckResult checkResult = new CheckResult();
    private final Set<CodeValidator> codeValidatorSet = new HashSet<>();
    private final List<JavaCodeContainer> javaCodeContainerList = new ArrayList<>();
    private final Set<PostProcessor> postProcessorSet = new HashSet<>();

    @Setter
    private String className;

    public CheckResult inspect(String code) {
        boolean isCompilationSuccessful = compile(code);

        if (!isCompilationSuccessful) {
            return checkResult;
        }

        for (CodeValidator codeValidator : codeValidatorSet) {
            codeValidator.validate(checkResult);
        }

        try {
            javaCodeContainerList.add(0, new SourceJavaCodeContainer(code));
        } catch (ClassNameIsAbsentException e) {
            checkResult.getFailedResult().add(e.getMessage());
            return checkResult;
        }

        CodeResult codeResult = codeProcessor.compileAndRun(javaCodeContainerList);

        if (!codeResult.getClassCompileErrorMap().isEmpty()) {
            codeResult.getClassCompileErrorMap().forEach((className, compileError) -> checkResult.getFailedResult().add(compileError));
            return checkResult;
        }

        if (!codeResult.getClassRuntimeExceptionMap().isEmpty()) {
            codeResult.getClassRuntimeExceptionMap().forEach((className, runtimeException) -> checkResult.getFailedResult().add(runtimeException));
            return checkResult;
        }

        for (PostProcessor postProcessor : postProcessorSet) {
            postProcessor.postProcess(checkResult, codeResult);
        }

        return checkResult;
    }

    private boolean compile(String code) {
        CodeResult compileResult;
        try {
            compileResult = codeProcessor.compileAndRun(Collections.singletonList(new SourceJavaCodeContainer(code)));
        } catch (ClassNameIsAbsentException e) {
            checkResult.getFailedResult().add(e.getMessage());
            return false;
        }

        if (compileResult.getClassCompileErrorMap().get(className) != null &&
                !compileResult.getClassCompileErrorMap().get(className).isEmpty()) {
            checkResult.getFailedResult().add(compileResult.getClassCompileErrorMap().get(className));
            return false;
        }

        Map<String, String> classConsoleOutputMap = compileResult.getClassConsoleOutputMap();

        checkResult.setConsoleOutput(classConsoleOutputMap.get(className));

        return true;
    }

    public void addCodeValidator(CodeValidator codeValidator) {
        codeValidatorSet.add(codeValidator);
    }

    public void addJavaContainer(JavaCodeContainer javaCodeContainer) {
        javaCodeContainerList.add(javaCodeContainer);
    }

    public void addPostProcessor(PostProcessor postProcessor) {
        postProcessorSet.add(postProcessor);
    }

    public interface CodeValidator {
        void validate(CheckResult checkResult);
    }

    public interface PostProcessor {
        void postProcess(CheckResult checkResult, CodeResult codeResult);
    }
}

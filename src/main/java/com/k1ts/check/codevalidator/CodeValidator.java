package com.k1ts.check.codevalidator;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.nodeTypes.NodeWithSimpleName;
import com.github.javaparser.ast.nodeTypes.modifiers.NodeWithAbstractModifier;
import com.github.javaparser.ast.nodeTypes.modifiers.NodeWithFinalModifier;
import com.k1ts.check.CheckResult;
import io.sentry.Sentry;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Builder
public class CodeValidator {
    private final String code;
    private final String className;
    private final String parentClassName;
    private final String implementedInterfaceName;
    private final Boolean isAbstract;
    private final Boolean isFinal;

    @Singular
    private final List<TypeParameterValidator> typeParameterValidators;

    @Singular
    private final List<LineValidator> lineValidators;

    @Singular
    private final List<MethodValidator> methodValidators;

    public void validate(CheckResult result) {
        try {
            List<String> successResult = new ArrayList<>();
            List<String> failedResult = new ArrayList<>();

            JavaParser javaParser = new JavaParser();
            CompilationUnit compilationUnit = javaParser.parse(code).getResult().orElse(null);

            if (compilationUnit == null) {
                throw new RuntimeException();
            }

            boolean classFound = compilationUnit
                    .findAll(ClassOrInterfaceDeclaration.class)
                    .stream()
                    .anyMatch(type -> type.getNameAsString().equals(className));

            if (classFound) {
                successResult.add("Програма містить клас " + className);
            } else {
                failedResult.add("Програма не містить клас " + className);
            }

            if (isAbstract != null) {
                boolean isAbstractClass = compilationUnit
                        .findAll(ClassOrInterfaceDeclaration.class)
                        .stream()
                        .filter(classOrInterfaceDeclaration -> classOrInterfaceDeclaration.getNameAsString().equals(className))
                        .anyMatch(NodeWithAbstractModifier::isAbstract);

                if (isAbstract) {
                    if (isAbstract == isAbstractClass) {
                        successResult.add("Клас " + className + " абстрактний");
                    } else {
                        failedResult.add("Клас " + className + " повинен бути абстрактним");
                    }
                }
            }

            if (isFinal != null) {
                boolean isFinalClass = compilationUnit
                        .findAll(ClassOrInterfaceDeclaration.class)
                        .stream()
                        .filter(classOrInterfaceDeclaration -> classOrInterfaceDeclaration.getNameAsString().equals(className))
                        .anyMatch(NodeWithFinalModifier::isFinal);

                if (isFinal) {
                    if (isFinal == isFinalClass) {
                        successResult.add("Клас " + className + " фінальний");
                    } else {
                        failedResult.add("Клас " + className + " повинен бути фінальним");
                    }
                }
            }

            if (parentClassName != null) {
                boolean extendedTypeFound = compilationUnit
                        .findAll(ClassOrInterfaceDeclaration.class)
                        .stream()
                        .filter(classOrInterfaceDeclaration -> classOrInterfaceDeclaration.getNameAsString().equals(className))
                        .flatMap(classOrInterfaceDeclaration -> classOrInterfaceDeclaration.getExtendedTypes().stream())
                        .anyMatch(classOrInterfaceType -> classOrInterfaceType.getNameAsString().equals(parentClassName));

                if (extendedTypeFound) {
                    successResult.add("Клас " + className + " розширює клас " + parentClassName);
                } else {
                    failedResult.add("Клас " + className + " не розширює клас " + parentClassName);
                }
            }

            if (implementedInterfaceName != null) {
                boolean implementedTypeFound = compilationUnit
                        .findAll(ClassOrInterfaceDeclaration.class)
                        .stream()
                        .filter(classOrInterfaceDeclaration -> classOrInterfaceDeclaration.getNameAsString().equals(className))
                        .flatMap(classOrInterfaceDeclaration -> classOrInterfaceDeclaration.getImplementedTypes().stream())
                        .anyMatch(classOrInterfaceType -> classOrInterfaceType.getNameAsString().equals(implementedInterfaceName));

                if (implementedTypeFound) {
                    successResult.add("Клас " + className + " імплементує інтерфейс " + implementedInterfaceName);
                } else {
                    failedResult.add("Клас " + className + " не імплементує інтерфейс " + implementedInterfaceName);
                }
            }

            for (TypeParameterValidator entry : typeParameterValidators) {
                boolean typeParameterFound = compilationUnit
                        .findAll(ClassOrInterfaceDeclaration.class)
                        .stream()
                        .filter(classOrInterfaceDeclaration -> classOrInterfaceDeclaration.getNameAsString().equals(className))
                        .anyMatch(classOrInterfaceDeclaration -> classOrInterfaceDeclaration
                                .getTypeParameters()
                                .stream()
                                .anyMatch(secondEntry -> secondEntry.getNameAsString().equals(entry.getTypeParameterName()) &&
                                        secondEntry
                                                .getTypeBound()
                                                .stream()
                                                .map(NodeWithSimpleName::getNameAsString)
                                                .collect(Collectors.toSet())
                                                .containsAll(entry.getTypeParameterBounds()) &&
                                        new HashSet<>(entry.getTypeParameterBounds()).containsAll(secondEntry
                                                .getTypeBound()
                                                .stream()
                                                .map(NodeWithSimpleName::getNameAsString).collect(Collectors.toSet()))));

                if (typeParameterFound) {
                    successResult.add("Клас " + className + " містить параметризований тип " + entry.getTypeParameterName());
                } else {
                    failedResult.add("Клас " + className + " не містить параметризований тип " + entry.getTypeParameterName());
                }
            }

            for (LineValidator lineValidator : lineValidators) {
                int expectedLineCount = lineValidator.getCount();

                if (expectedLineCount == 1) {
                    boolean lineFound = compilationUnit
                            .findAll(ClassOrInterfaceDeclaration.class)
                            .stream()
                            .filter(classOrInterfaceDeclaration -> classOrInterfaceDeclaration.getNameAsString().equals(className))
                            .map(Node::toString)
                            .flatMap(body -> Arrays.stream(body.replace("\r", "").split("\n")))
                            .map(String::trim)
                            .anyMatch(s -> Pattern.compile(lineValidator.getLine()).matcher(s).matches());

                    if (lineFound) {
                        successResult.add(lineValidator.getSuccessfulMassageIfLinePresent());
                    } else {
                        failedResult.add(lineValidator.getFailedMessageIfLineNotPresent());
                    }

                    continue;
                }

                int actualLineCount = (int) compilationUnit
                        .findAll(ClassOrInterfaceDeclaration.class)
                        .stream()
                        .filter(classOrInterfaceDeclaration -> classOrInterfaceDeclaration.getNameAsString().equals(className))
                        .map(Node::toString)
                        .flatMap(body -> Arrays.stream(body.replace("\r", "").split("\n")))
                        .map(String::trim)
                        .filter(s -> Pattern.compile(lineValidator.getLine()).matcher(s).matches())
                        .count();

                if (expectedLineCount == actualLineCount) {
                    successResult.add(lineValidator.getSuccessfulMassageIfLinePresent());
                } else {
                    failedResult.add(lineValidator.getFailedMessageIfLineNotPresent());
                }
            }

            for (MethodValidator methodValidator : methodValidators) {
                boolean methodFound = compilationUnit
                        .findAll(ClassOrInterfaceDeclaration.class)
                        .stream()
                        .filter(classOrInterfaceDeclaration -> classOrInterfaceDeclaration.getNameAsString().equals(className))
                        .flatMap(classOrInterfaceDeclaration -> classOrInterfaceDeclaration.getMethods().stream())
                        .anyMatch(method -> method.getDeclarationAsString().equals(methodValidator.getMethodName()));

                if (!methodValidator.isAbsent()) {
                    if (methodFound) {
                        successResult.add("Програма містить метод " + methodValidator.getMethodName());
                    } else {
                        failedResult.add("Програма не містить метод " + methodValidator.getMethodName());
                    }
                } else {
                    if (methodFound) {
                        failedResult.add("Програма містить метод " + methodValidator.getMethodName());
                    } else {
                        successResult.add("Програма не містить метод " + methodValidator.getMethodName());
                    }
                }

                if (methodValidator.isAbsent()) {
                    break;
                }

                for (TypeParameterValidator entry : methodValidator.getTypeParameterValidators()) {
                    boolean typeParameterFound = compilationUnit
                            .findAll(ClassOrInterfaceDeclaration.class)
                            .stream()
                            .filter(classOrInterfaceDeclaration -> classOrInterfaceDeclaration.getNameAsString().equals(className))
                            .flatMap(classOrInterfaceDeclaration -> classOrInterfaceDeclaration.getMethods().stream())
                            .filter(methodDeclaration -> methodDeclaration.getDeclarationAsString().equals(methodValidator.getMethodName()))
                            .flatMap(methodDeclaration -> methodDeclaration.getTypeParameters().stream())
                            .anyMatch(secondEntry -> secondEntry.getNameAsString().equals(entry.getTypeParameterName()) &&
                                    secondEntry
                                            .getTypeBound()
                                            .stream()
                                            .map(NodeWithSimpleName::getNameAsString)
                                            .collect(Collectors.toSet())
                                            .containsAll(entry.getTypeParameterBounds()) &&
                                    new HashSet<>(entry.getTypeParameterBounds())
                                            .containsAll(secondEntry
                                                    .getTypeBound()
                                                    .stream()
                                                    .map(NodeWithSimpleName::getNameAsString)
                                                    .collect(Collectors.toSet())));

                    if (typeParameterFound) {
                        successResult.add("Метод " + methodValidator.getMethodName() + " містить параметризований тип " + entry.getTypeParameterName());
                    } else {
                        failedResult.add("Метод " + methodValidator.getMethodName() + " не містить параметризований тип " + entry.getTypeParameterName());
                    }
                }

                List<CodeBlockValidator> codeBlockValidators = methodValidator.getCodeBlocks();

                for (CodeBlockValidator codeBlockValidator : codeBlockValidators) {
                    Pattern pattern = Pattern.compile(normalizeCode(codeBlockValidator.getCodeBlock()));

                    boolean codeBlockFound = compilationUnit
                            .findAll(ClassOrInterfaceDeclaration.class)
                            .stream()
                            .filter(classOrInterfaceDeclaration -> classOrInterfaceDeclaration.getNameAsString().equals(className))
                            .flatMap(classOrInterfaceDeclaration -> classOrInterfaceDeclaration.getMethods().stream())
                            .filter(methodDeclaration -> methodDeclaration.getDeclarationAsString().equals(methodValidator.getMethodName()))
                            .map(methodDeclaration -> Objects.requireNonNull(methodDeclaration.getBody().orElse(null)).toString())
                            .map(this::normalizeCode)
                            .anyMatch(methodBody -> pattern.matcher(methodBody).find());

                    if (codeBlockFound) {
                        successResult.add(codeBlockValidator.getSuccessfulMassageIfCodeBlockPresent());
                    } else {
                        failedResult.add(codeBlockValidator.getFailedMassageIfCodeBlockNotPresent());
                    }
                }

                List<LineValidator> lineValidators = methodValidator.getLines();

                for (LineValidator lineValidator : lineValidators) {

                    int expectedLineCount = lineValidator.getCount();

                    if (expectedLineCount == 1) {
                        boolean lineFound = compilationUnit
                                .findAll(ClassOrInterfaceDeclaration.class)
                                .stream()
                                .filter(classOrInterfaceDeclaration -> classOrInterfaceDeclaration.getNameAsString().equals(className))
                                .flatMap(classOrInterfaceDeclaration -> classOrInterfaceDeclaration.getMethods().stream())
                                .filter(methodDeclaration -> methodDeclaration.getDeclarationAsString().equals(methodValidator.getMethodName()))
                                .map(methodDeclaration -> Objects.requireNonNull(methodDeclaration.getBody().orElse(null)).toString())
                                .flatMap(methodBody -> Arrays.stream(methodBody.replace("\r", "").split("\n")))
                                .map(String::trim)
                                .anyMatch(line -> Pattern.compile(lineValidator.getLine()).matcher(line).matches());

                        if (lineFound) {
                            if (lineValidator.getSuccessfulMassageIfLinePresent() != null) {
                                successResult.add(lineValidator.getSuccessfulMassageIfLinePresent());
                            }

                            if (lineValidator.getFailedMassageIfLinePresent() != null) {
                                failedResult.add(lineValidator.getFailedMassageIfLinePresent());
                            }
                        } else {
                            if (lineValidator.getFailedMessageIfLineNotPresent() != null) {
                                failedResult.add(lineValidator.getFailedMessageIfLineNotPresent());
                            }

                            if (lineValidator.getSuccessfulMessageIfLineNotPresent() != null) {
                                successResult.add(lineValidator.getSuccessfulMessageIfLineNotPresent());
                            }
                        }

                        continue;
                    }

                    int lineCount = (int) compilationUnit
                            .findAll(ClassOrInterfaceDeclaration.class)
                            .stream()
                            .filter(classOrInterfaceDeclaration -> classOrInterfaceDeclaration.getNameAsString().equals(className))
                            .flatMap(classOrInterfaceDeclaration -> classOrInterfaceDeclaration.getMethods().stream())
                            .filter(methodDeclaration -> methodDeclaration.getDeclarationAsString().equals(methodValidator.getMethodName()))
                            .map(methodDeclaration -> Objects.requireNonNull(methodDeclaration.getBody().orElse(null)).toString())
                            .flatMap(methodBody -> Arrays.stream(methodBody.replace("\r", "").split("\n")))
                            .map(String::trim)
                            .filter(line -> Pattern.compile(lineValidator.getLine()).matcher(line).matches())
                            .count();

                    if (expectedLineCount == lineCount) {
                        if (lineValidator.getSuccessfulMassageIfLinePresent() != null) {
                            successResult.add(lineValidator.getSuccessfulMassageIfLinePresent());
                        }

                        if (lineValidator.getFailedMassageIfLinePresent() != null) {
                            failedResult.add(lineValidator.getFailedMassageIfLinePresent());
                        }
                    } else {
                        if (lineValidator.getFailedMessageIfLineNotPresent() != null) {
                            failedResult.add(lineValidator.getFailedMessageIfLineNotPresent());
                        }

                        if (lineValidator.getSuccessfulMessageIfLineNotPresent() != null) {
                            successResult.add(lineValidator.getSuccessfulMessageIfLineNotPresent());
                        }
                    }
                }
            }

            result.getSuccessResult().addAll(successResult);
            result.getFailedResult().addAll(failedResult);
        } catch (RuntimeException e) {
            Sentry.captureException(e);
            throw e;
        }
    }

    private String normalizeCode(String code) {
        return Pattern.compile("\\s+").matcher(code).replaceAll(" ").trim();
    }

    @Getter
    @Builder
    public static class TypeParameterValidator {
        private final String typeParameterName;
        @Singular
        private final List<String> typeParameterBounds;
    }

    @Getter
    @Builder
    public static class MethodValidator {
        private final String methodName;
        private final boolean absent;
        @Singular
        private final List<TypeParameterValidator> typeParameterValidators;
        @Singular
        private final List<CodeBlockValidator> codeBlocks;
        @Singular
        private final List<LineValidator> lines;
    }

    @Getter
    @Builder
    public static class LineValidator {
        private final String line;

        @Builder.Default
        private final int count = 1;

        private final String successfulMassageIfLinePresent;
        private final String failedMessageIfLineNotPresent;
        private final String failedMassageIfLinePresent;
        private final String successfulMessageIfLineNotPresent;
    }

    @Getter
    @Builder
    public static class CodeBlockValidator {
        private final String codeBlock;
        private final String successfulMassageIfCodeBlockPresent;
        private final String failedMassageIfCodeBlockNotPresent;
    }
}

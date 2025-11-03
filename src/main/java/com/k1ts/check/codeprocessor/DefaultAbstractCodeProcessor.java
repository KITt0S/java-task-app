package com.k1ts.check.codeprocessor;

import com.k1ts.check.CodeResult;
import com.k1ts.check.javacodecontainer.JavaCodeContainer;
import io.sentry.Sentry;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public abstract class DefaultAbstractCodeProcessor implements CodeProcessor {

    private static final String JAVA_TOOL_OPTIONS = "Picked up JAVA_TOOL_OPTIONS: -Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8";

    @Override
    public CodeResult compileAndRun(List<JavaCodeContainer> containers) {
        Path tempDirectoryPath;

        tempDirectoryPath = createTempDirectory();

        writeJavaCodeFiles(tempDirectoryPath, containers);

        writeConsoleInputFiles(tempDirectoryPath, containers);

        Path runFilePath = writeRunFile(tempDirectoryPath, containers);

        createProcess(tempDirectoryPath, runFilePath);

        Map<String, String> classCompileErrorMap = getClassCompileErrorMap(tempDirectoryPath, containers);

        Iterator<Map.Entry<String, String>> iterator = classCompileErrorMap.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<String, String> entry = iterator.next();

            if (entry.getValue().equals("Picked up JAVA_TOOL_OPTIONS: -Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8\n")) {
                iterator.remove(); // Правильний спосіб видалення під час ітерації
            }
        }

        Map<String, String> classRuntimeExceptionMap = getClassRuntimeExceptionMap(tempDirectoryPath, containers);

        Iterator<Map.Entry<String, String>> anotherIterator = classRuntimeExceptionMap.entrySet().iterator();

        while (anotherIterator.hasNext()) {
            Map.Entry<String, String> entry = anotherIterator.next();

            if (entry.getValue().equals("Picked up JAVA_TOOL_OPTIONS: -Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8\n")) {
                anotherIterator.remove(); // Правильний спосіб видалення під час ітерації
            }
        }

        Map<String, String> consoleOutputMap = getConsoleOutput(tempDirectoryPath, containers);

        removeTempDirectory(tempDirectoryPath);

        return new CodeResult(classCompileErrorMap, classRuntimeExceptionMap, consoleOutputMap);
    }

    abstract Path writeRunFile(Path tempDirectoryPath, List<JavaCodeContainer> containers);

    abstract void createProcess(Path tempDirectoryPath, Path runFilePath);

    abstract Map<String, String> getConsoleOutput(Path tempDirectoryPath, List<JavaCodeContainer> containers);

    private Path createTempDirectory() {
        Path tempDirectoryPath;
        try {
            tempDirectoryPath = Files.createDirectory(Path.of("temp_" +
                    LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME).replace(":", "_")));
        } catch (IOException e) {
            Sentry.captureException(e);
            throw new RuntimeException(e);
        }
        return tempDirectoryPath;
    }

    private void writeJavaCodeFiles(Path tempDirectoryPath, List<JavaCodeContainer> containers) {
        for (JavaCodeContainer container : containers) {
            Path filePath;

            filePath = createFile(tempDirectoryPath, container);

            writeJavaCodeToFile(filePath, container);
        }
    }

    private Path createFile(Path tempDirectoryPath, JavaCodeContainer container) {
        Path filePath;
        try {
            filePath = Files.createFile(Path.of(tempDirectoryPath + "/" + container.getClassName() + ".java"));
        } catch (IOException e) {
            Sentry.captureException(e);
            throw new RuntimeException(e);
        }
        return filePath;
    }

    private void writeJavaCodeToFile(Path filePath, JavaCodeContainer container) {
        try (FileWriter writer = new FileWriter(filePath.toString())) {
            writer.write(container.getJavaCode());
            writer.flush();
        } catch (IOException e) {
            Sentry.captureException(e);
            throw new RuntimeException(e);
        }
    }

    private void writeConsoleInputFiles(Path dirPath, List<JavaCodeContainer> containers) {
        for (JavaCodeContainer container : containers) {
            if (container.isExecutable()) {
                Path consoleInputFilePath;

                consoleInputFilePath = createConsoleInputFile(dirPath, container);

                writeConsoleInput(consoleInputFilePath, container);
            }
        }
    }

    private Path createConsoleInputFile(Path dirPath, JavaCodeContainer container) {
        Path consoleInputFilePath;
        try {
            consoleInputFilePath = Files.createFile(Path.of(dirPath.toString() + "/" + container.getClassName() + "-console-input.txt"));
        } catch (IOException e) {
            Sentry.captureException(e);
            throw new RuntimeException(e);
        }
        return consoleInputFilePath;
    }

    private void writeConsoleInput(Path consoleInputFilePath, JavaCodeContainer container) {
        try (FileWriter writer = new FileWriter(consoleInputFilePath.toString())) {
            writer.write(container.getConsoleInput() == null ? "Some data" : container.getConsoleInput());
            writer.flush();
        } catch (IOException e) {
            Sentry.captureException(e);
            throw new RuntimeException(e);
        }
    }

    private Map<String, String> getClassCompileErrorMap(Path tempDirectoryPath, List<JavaCodeContainer> containers) {
        Map<String, String> result = new HashMap<>();

        for (JavaCodeContainer container : containers) {

            StringJoiner compileErrorJoiner = new StringJoiner("\n");

            try (BufferedReader reader = new BufferedReader(new FileReader(tempDirectoryPath + "/" + container.getClassName() + "-class-javac.txt"))) {
                reader.lines().forEach(line -> {
                    if (line.equals(JAVA_TOOL_OPTIONS)) {
                        return;
                    }

                    if (line.isBlank()) {
                        return;
                    }

                    compileErrorJoiner.add(line);
                });
            } catch (IOException e) {
                Sentry.captureException(e);
                throw new RuntimeException(e);
            }

            if (!compileErrorJoiner.toString().isEmpty()) {
                result.put(container.getClassName(), compileErrorJoiner.toString());
            }
        }

        return result;
    }

    Map<String, String> getClassRuntimeExceptionMap(Path tempDirectoryPath, List<JavaCodeContainer> containers) {
        Map<String, String> result = new HashMap<>();

        for (JavaCodeContainer container : containers) {

            if (container.isExecutable()) {
                StringJoiner runtimeExceptionJoiner = new StringJoiner("\n");

                try (BufferedReader reader = new BufferedReader(new FileReader(tempDirectoryPath + "/" + container.getClassName() + "-class-runtime-exception.txt"))) {
                    reader.lines().forEach(line -> {
                        if (line.equals(JAVA_TOOL_OPTIONS)) {
                            return;
                        }

                        if (line.isBlank()) {
                            return;
                        }

                        runtimeExceptionJoiner.add(line);
                    });
                } catch (IOException e) {
                    Sentry.captureException(e);
                    throw new RuntimeException(e);
                }

                if (!runtimeExceptionJoiner.toString().isEmpty()) {
                    result.put(container.getClassName(), runtimeExceptionJoiner.toString());
                }
            }
        }

        return result;
    }

    Map<String, String> getConsoleOutput(Path tempDirectoryPath, List<JavaCodeContainer> containers, Charset charset) {
        Map<String, String> result = new HashMap<>();

        for (JavaCodeContainer container : containers) {

            if (container.isExecutable()) {
                StringJoiner consoleResultJoiner = new StringJoiner("\n");

                try (FileReader fileReader = new FileReader(tempDirectoryPath + "/" + container.getClassName() + "-class-output.txt", charset)) {

                    int charsNum;
                    char[] buffer = new char[1024];

                    while ((charsNum = fileReader.read(buffer)) != -1) {
                        consoleResultJoiner.add(String.valueOf(buffer, 0, charsNum));
                    }
                } catch (IOException e) {
                    Sentry.captureException(e);
                    throw new RuntimeException(e);
                }

                result.put(container.getClassName(), consoleResultJoiner.toString());
            }
        }
        return result;
    }

    private void removeTempDirectory(Path tempDirectoryPath) {
        try {
            Files.walkFileTree(tempDirectoryPath, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });

        } catch (IOException e) {
            Sentry.captureException(e);
            throw new RuntimeException(e);
        }
    }
}

package com.k1ts.check.codeprocessor;

import com.k1ts.check.javacodecontainer.JavaCodeContainer;
import io.sentry.Sentry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

@ConditionalOnProperty(value = "code.processor.linux", havingValue = "true")
@Scope("prototype")
@Component
public class LinuxCodeProcessor extends DefaultAbstractCodeProcessor {

    Path writeRunFile(Path tempDirectoryPath, List<JavaCodeContainer> containers) {
        StringJoiner runTextJoiner = new StringJoiner("\n");
        for (JavaCodeContainer container : containers) {
            runTextJoiner.add("javac " + container.getClassName() + ".java > " + container.getClassName() + "-class-javac.txt 2>&1");
        }

        int timeoutSeconds = 2;

        for (JavaCodeContainer container : containers) {

            if (container.isExecutable()) {
                // Run Java process and capture PID
                runTextJoiner.add(
                        "timeout -s 9 " + timeoutSeconds + " java " + container.getClassName() +
                                " < " + container.getClassName() + "-console-input.txt > " +
                                container.getClassName() + "-class-output.txt" +
                                " 2>" + container.getClassName() + "-class-runtime-exception.txt");
            }
        }

        Path runFilePath;

        try {
            runFilePath = Files.createFile(Path.of(tempDirectoryPath + "/run.sh"));
        } catch (IOException e) {
            Sentry.captureException(e);
            throw new RuntimeException(e);
        }

        try (FileWriter fileWriter = new FileWriter(runFilePath.toString())) {
            fileWriter.write(runTextJoiner.toString());
            fileWriter.flush();
        } catch (IOException e) {
            Sentry.captureException(e);
            throw new RuntimeException(e);
        }
        return runFilePath;
    }

    void createProcess(Path tempDirectoryPath, Path runFilePath) {
        Path outputFilePath;
        Path errorFilePath;

        try {
            outputFilePath = Files.createFile(Path.of(tempDirectoryPath + "/output.txt"));
            errorFilePath = Files.createFile(Path.of(tempDirectoryPath + "/error.txt"));
        } catch (IOException e) {
            Sentry.captureException(e);
            throw new RuntimeException(e);
        }

        ProcessBuilder processBuilder = new ProcessBuilder()
                .directory(tempDirectoryPath.toFile())
                .command("/bin/bash", "./" + runFilePath.getFileName().toString());

        processBuilder.redirectOutput(outputFilePath.toFile());
        processBuilder.redirectError(errorFilePath.toFile());

        Process process;
        try {
            process = processBuilder.start();
        } catch (IOException e) {
            Sentry.captureException(e);
            throw new RuntimeException(e);
        }

        int exitCode;
        try {
            exitCode = process.waitFor();
        } catch (InterruptedException e) {
            Sentry.captureException(e);
            throw new RuntimeException(e);
        }

        System.out.println("Процес завершився з кодом: " + exitCode);
    }

    @Override
    Map<String, String> getConsoleOutput(Path tempDirectoryPath, List<JavaCodeContainer> containers) {
        return super.getConsoleOutput(tempDirectoryPath, containers, StandardCharsets.UTF_8);
    }
}

package com.k1ts.check.codeprocessor;

import com.k1ts.check.javacodecontainer.JavaCodeContainer;
import io.sentry.Sentry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

@ConditionalOnProperty(value = "code.processor.linux", havingValue = "false", matchIfMissing = true)
@Scope("prototype")
@Component
public class WinCodeProcessor extends DefaultAbstractCodeProcessor {

    Path writeRunFile(Path tempDirectoryPath, List<JavaCodeContainer> containers) {
        StringJoiner runTextJoiner = new StringJoiner("\n");
        for (JavaCodeContainer container : containers) {
            runTextJoiner.add("javac " + container.getClassName() + ".java > " + container.getClassName() + "-class-javac.txt 2>&1");
        }

        for (JavaCodeContainer container : containers) {
            if (container.isExecutable()) {
                // Run Java process and capture PID
                runTextJoiner.add(
                        "start /b java " + container.getClassName() +
                                " < " + container.getClassName() + "-console-input.txt > " +
                                container.getClassName() + "-class-output.txt" +
                                " 2>" + container.getClassName() + "-class-runtime-exception.txt");
            }
        }

        int timeoutSeconds = 2;
        runTextJoiner.add("powershell -noprofile -command \"& {[system.threading.thread]::sleep(" + timeoutSeconds * 1000 + ")}\"");

        // Kill Java processes using PIDs
        for (JavaCodeContainer container : containers) {
            if (container.isExecutable()) {
                runTextJoiner.add("for /f \"tokens=1\" %%i in ('jps -l ^| findstr /i \"" + container.getClassName() + "\"') do taskkill /f /pid %%i");
            }
        }

        Path runFilePath;

        try {
            runFilePath = Files.createFile(Path.of(tempDirectoryPath + "/run.bat"));
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
                .command("cmd", "/c", runFilePath.getFileName().toString());

        processBuilder.redirectOutput(outputFilePath.toFile());
        processBuilder.redirectError(errorFilePath.toFile());

        Process process;
        StopWatch stopWatch = new StopWatch("run.bat process");
        try {
            stopWatch.start();
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

        stopWatch.stop();
        System.out.println(stopWatch.prettyPrint());
        System.out.println("Процес завершився з кодом: " + exitCode);
    }

    @Override
    Map<String, String> getConsoleOutput(Path tempDirectoryPath, List<JavaCodeContainer> containers) {
        return super.getConsoleOutput(tempDirectoryPath, containers, StandardCharsets.UTF_8);
    }
}

package com.k1ts.check.javacodecontainer;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RequiredArgsConstructor
@Getter
public class SourceJavaCodeContainer implements JavaCodeContainer {
    private final String className;
    private final String javaCode;
    private final String consoleInput;
    private final boolean executable;

    public SourceJavaCodeContainer(String code) throws ClassNameIsAbsentException {
        this(code, null);
    }

    public SourceJavaCodeContainer(String code, String consoleInput) throws ClassNameIsAbsentException {
        Pattern classPattern = Pattern.compile("public class (([a-zA-Z0-9])+)(\\<.*?\\>)? \\s*\\{");

        Matcher matcher = classPattern.matcher(code);

        if (!matcher.find()) {
            throw new ClassNameIsAbsentException("Class is absent");
        }

        this.className = matcher.group(1);
        this.javaCode = code;
        this.executable = code.contains("public static void main(String[] args)");
        this.consoleInput = consoleInput;
    }
}

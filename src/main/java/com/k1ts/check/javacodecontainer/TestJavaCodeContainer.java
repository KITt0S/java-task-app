package com.k1ts.check.javacodecontainer;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class TestJavaCodeContainer implements JavaCodeContainer {
    private final String className;
    private final String javaCode;
    private final String consoleInput;
    private final boolean executable = true;
}

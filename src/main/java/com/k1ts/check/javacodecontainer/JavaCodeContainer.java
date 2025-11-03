package com.k1ts.check.javacodecontainer;

public interface JavaCodeContainer {
    String getClassName();

    String getJavaCode();

    String getConsoleInput();

    boolean isExecutable();
}

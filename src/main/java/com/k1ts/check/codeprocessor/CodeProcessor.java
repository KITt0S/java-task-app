package com.k1ts.check.codeprocessor;

import com.k1ts.check.CodeResult;
import com.k1ts.check.javacodecontainer.JavaCodeContainer;

import java.util.List;

public interface CodeProcessor {

    CodeResult compileAndRun(List<JavaCodeContainer> containers);
}

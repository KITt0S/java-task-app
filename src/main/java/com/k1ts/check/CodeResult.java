package com.k1ts.check;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

@Getter
@AllArgsConstructor
public class CodeResult {
    private final Map<String, String> classCompileErrorMap;
    private final Map<String, String> classRuntimeExceptionMap;
    private final Map<String, String> classConsoleOutputMap;
}

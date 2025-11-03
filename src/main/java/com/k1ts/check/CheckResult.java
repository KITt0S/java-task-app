package com.k1ts.check;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class CheckResult {
    private final List<String> successResult = new ArrayList<>();
    private final List<String> failedResult = new ArrayList<>();
    private String consoleOutput;
}

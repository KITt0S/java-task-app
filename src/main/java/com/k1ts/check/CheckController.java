package com.k1ts.check;

import com.k1ts.check.request.CheckRequest;
import com.k1ts.check.request.CheckResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class CheckController {
    private final CheckService checkService;

    @PostMapping("/task/check")
    public CheckResponse check(@RequestBody CheckRequest request) {
        return checkService.check(request);
    }
}

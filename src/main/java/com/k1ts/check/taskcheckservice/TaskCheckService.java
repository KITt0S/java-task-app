package com.k1ts.check.taskcheckservice;

import com.k1ts.check.request.CheckResponse;

public interface TaskCheckService {
    CheckResponse check(String code);
}

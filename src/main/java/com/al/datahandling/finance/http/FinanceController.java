package com.al.datahandling.finance.http;

import com.al.datahandling.finance.domain.FinanceEnriched;
import com.al.datahandling.finance.usecase.TransformFinanceData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FinanceController {

    @Autowired
    private TransformFinanceData transformFinanceData;

    @GetMapping("/")
    public FinanceEnriched index() {
        return transformFinanceData.transform();
    }

}

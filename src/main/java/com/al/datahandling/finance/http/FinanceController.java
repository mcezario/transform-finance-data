package com.al.datahandling.finance.http;

import com.al.datahandling.finance.domain.FinanceEnriched;
import com.al.datahandling.finance.gateway.FinanceObjectApi;
import com.al.datahandling.finance.usecase.TransformFinanceData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
public class FinanceController {

    @Autowired
    private TransformFinanceData transformFinanceData;

    @Autowired
    private FinanceObjectApi financeObjectApi;

    @GetMapping("/")
    public FinanceEnriched index() {
        return transformFinanceData.transform();
    }

    @GetMapping("/upload1")
    public String upload1() {
        FinanceEnriched e1 = new FinanceEnriched(BigDecimal.valueOf(1234));
        List<FinanceEnriched> e11 = List.of(e1);
        financeObjectApi.upload(e11);
        return "ok";
    }

    @GetMapping("/upload2")
    public String upload2() {
        FinanceEnriched e1 = new FinanceEnriched(BigDecimal.valueOf(1234));
        List<FinanceEnriched> e11 = List.of(e1);
        financeObjectApi.upload2(e11);
        return "ok";
    }

    @GetMapping("/upload3")
    public String upload3() {
        FinanceEnriched e1 = new FinanceEnriched(BigDecimal.valueOf(1234));
        List<FinanceEnriched> e11 = List.of(e1);
        financeObjectApi.upload3(e11);
        return "ok";
    }

}

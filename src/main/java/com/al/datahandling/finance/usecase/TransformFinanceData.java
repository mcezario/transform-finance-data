package com.al.datahandling.finance.usecase;

import com.al.datahandling.finance.domain.FinanceData;
import com.al.datahandling.finance.domain.FinanceEnriched;
import com.al.datahandling.finance.gateway.FinanceObjectApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class TransformFinanceData {

    private static final String DATA_SOURCE = "as-findata-tech-challenge";

    @Autowired
    private FinanceObjectApi financeObjectApi;

    public FinanceEnriched transform() {
        return transformSource(financeObjectApi.get(DATA_SOURCE));
    }

    private FinanceEnriched transformSource(List<FinanceData> input) {
        BigDecimal total = BigDecimal.ZERO;
        for (FinanceData finance : input) {
            total = total.add(new BigDecimal(finance.id()));
        }
        return new FinanceEnriched(total);
    }

}

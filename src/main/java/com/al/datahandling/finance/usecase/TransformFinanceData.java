package com.al.datahandling.finance.usecase;

import com.al.datahandling.finance.domain.FinanceData;
import com.al.datahandling.finance.domain.FinanceEnriched;
import com.al.datahandling.finance.gateway.FinanceObjectApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TransformFinanceData {

    private static final String DATA_SOURCE = "as-findata-tech-challenge";

    @Autowired
    private FinanceObjectApi financeObjectApi;

    public FinanceEnriched transform() {
        return transformSource(financeObjectApi.get(DATA_SOURCE));
    }

    private FinanceEnriched transformSource(List<FinanceData> rawData) {
        Map<String, String> transformedData = new HashMap<>();

        Optional<FinanceData> moBsInv = rawData.stream().filter(rd -> "MO_BS_INV".equals(rd.id())).findFirst();
        if (moBsInv.isPresent()) {
            FinanceData financeData = moBsInv.get();
            transformedData.put(financeData.id(), null);
        }

        return null;
    }

}

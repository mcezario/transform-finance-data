package com.al.datahandling.finance.gateway;

import com.al.datahandling.finance.domain.FinanceData;

import java.util.List;

public interface FinanceObjectApi {

    List<FinanceData> get(String location);

}

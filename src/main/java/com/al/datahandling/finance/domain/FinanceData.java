package com.al.datahandling.finance.domain;

import java.io.Serializable;
import java.util.Map;

public record FinanceData(String id, String scale, Map<String, String> values) implements Serializable {

}

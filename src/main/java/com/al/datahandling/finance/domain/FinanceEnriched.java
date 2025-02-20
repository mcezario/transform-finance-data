package com.al.datahandling.finance.domain;


import com.al.datahandling.finance.commons.serializer.MoneySerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.io.Serializable;
import java.math.BigDecimal;

public record FinanceEnriched(
        @JsonSerialize( using = MoneySerializer.class ) BigDecimal total) implements Serializable {

}

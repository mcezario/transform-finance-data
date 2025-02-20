package com.al.datahandling.commons.serializer;

import com.al.datahandling.finance.commons.serializer.MoneySerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;


record MockMoneyClass(@JsonSerialize(using = MoneySerializer.class) BigDecimal amount) {

}

public class MoneySerializerTest {

    @Test
    public void jsonSerializationTest() throws Exception {
        MockMoneyClass money = new MockMoneyClass(new BigDecimal("20.3"));

        ObjectMapper mapper = new ObjectMapper();
        assertThat("{\"amount\":\"20.30\"}", is(mapper.writeValueAsString(money)));
    }

}

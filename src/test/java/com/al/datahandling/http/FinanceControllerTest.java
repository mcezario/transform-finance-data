package com.al.datahandling.http;

import com.al.datahandling.finance.domain.FinanceEnriched;
import com.al.datahandling.finance.gateway.FinanceObjectApi;
import com.al.datahandling.finance.http.FinanceController;
import com.al.datahandling.finance.usecase.TransformFinanceData;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@RunWith( SpringRunner.class)
@WebMvcTest(FinanceController.class)
public class FinanceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TransformFinanceData transformFinanceData;

    @MockitoBean
    private FinanceObjectApi financeObjectApi;

    @Test
    public void shouldTransformContentSuccessfully() throws Exception {
        final FinanceEnriched financeEnriched = new FinanceEnriched(BigDecimal.valueOf(1.20));
        when(transformFinanceData.transform()).thenReturn(financeEnriched);

        final String expectedValue = financeEnriched.total().setScale(2, RoundingMode.HALF_UP).toString();

        mockMvc.perform(get("/")).andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total", is(expectedValue)));
    }

}

package com.al.datahandling.usecase;

import com.al.datahandling.finance.domain.FinanceData;
import com.al.datahandling.finance.domain.FinanceEnriched;
import com.al.datahandling.finance.gateway.FinanceObjectApi;
import com.al.datahandling.finance.usecase.TransformFinanceData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith( MockitoExtension.class )
public class TransformFinanceDataTest {

    @InjectMocks
    private TransformFinanceData transformFinanceData;

    @Mock
    private FinanceObjectApi financeObjectApi;

    @Test
    public void shouldTransformDataSuccessfully() {
        List<FinanceData> finances = List.of(
                new FinanceData("1", "a", null),
                new FinanceData("2", "b", null),
                new FinanceData("3", "c", null)
        );
        when(financeObjectApi.get(any())).thenReturn(finances);

        FinanceEnriched transform = transformFinanceData.transform();

        assertThat(transform.total(), equalTo(new BigDecimal("6")));
    }

}

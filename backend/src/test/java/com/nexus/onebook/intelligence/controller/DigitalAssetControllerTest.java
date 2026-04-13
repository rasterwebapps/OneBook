package com.nexus.onebook.intelligence.controller;

import com.nexus.onebook.intelligence.dto.MarketValuation;
import com.nexus.onebook.exception.GlobalExceptionHandler;
import com.nexus.onebook.intelligence.model.DigitalAsset;
import com.nexus.onebook.intelligence.model.DigitalAssetType;
import com.nexus.onebook.intelligence.service.DigitalAssetService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DigitalAssetController.class)
@Import(GlobalExceptionHandler.class)
class DigitalAssetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DigitalAssetService digitalAssetService;

    @Test
    void getAssets_returnsOk() throws Exception {
        DigitalAsset asset = new DigitalAsset(
                "t1", "BTC", "Bitcoin",
                DigitalAssetType.CRYPTOCURRENCY,
                new BigDecimal("1.5"), new BigDecimal("30000.00")
        );
        asset.setId(1L);

        when(digitalAssetService.getAssetsByTenant("t1")).thenReturn(List.of(asset));

        mockMvc.perform(get("/api/digital-assets").param("tenantId", "t1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].symbol").value("BTC"))
                .andExpect(jsonPath("$[0].assetName").value("Bitcoin"));
    }

    @Test
    void getAsset_returnsOk() throws Exception {
        DigitalAsset asset = new DigitalAsset(
                "t1", "BTC", "Bitcoin",
                DigitalAssetType.CRYPTOCURRENCY,
                new BigDecimal("1.5"), new BigDecimal("30000.00")
        );
        asset.setId(1L);

        when(digitalAssetService.getAsset(1L)).thenReturn(asset);

        mockMvc.perform(get("/api/digital-assets/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.symbol").value("BTC"));
    }

    @Test
    void getValuation_returnsOk() throws Exception {
        MarketValuation valuation = new MarketValuation(
                "t1",
                1,
                new BigDecimal("30000.00"),
                new BigDecimal("35000.00"),
                new BigDecimal("5000.00"),
                new BigDecimal("16.67"),
                LocalDate.now(),
                List.of()
        );

        when(digitalAssetService.getPortfolioValuation("t1")).thenReturn(valuation);

        mockMvc.perform(get("/api/digital-assets/valuation").param("tenantId", "t1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tenantId").value("t1"))
                .andExpect(jsonPath("$.totalHoldings").value(1));
    }
}

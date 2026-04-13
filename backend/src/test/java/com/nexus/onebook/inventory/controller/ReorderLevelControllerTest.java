package com.nexus.onebook.inventory.controller;

import com.nexus.onebook.inventory.dto.ReorderAlert;
import com.nexus.onebook.exception.GlobalExceptionHandler;
import com.nexus.onebook.inventory.model.ReorderLevel;
import com.nexus.onebook.inventory.service.ReorderLevelService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReorderLevelController.class)
@Import(GlobalExceptionHandler.class)
class ReorderLevelControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReorderLevelService reorderLevelService;

    @Test
    void setReorderLevel_validRequest_returns201() throws Exception {
        ReorderLevel level = new ReorderLevel();
        level.setId(1L);
        level.setTenantId("t1");
        level.setReorderLevel(new BigDecimal("50"));
        level.setReorderQuantity(new BigDecimal("100"));

        when(reorderLevelService.setReorderLevel(any())).thenReturn(level);

        mockMvc.perform(post("/api/reorder-levels")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "tenantId": "t1",
                                    "stockItemId": 1,
                                    "minimumLevel": 10,
                                    "reorderLevel": 50,
                                    "maximumLevel": 200,
                                    "reorderQuantity": 100
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.reorderLevel").value(50))
                .andExpect(jsonPath("$.reorderQuantity").value(100));
    }

    @Test
    void getLevels_returnsList() throws Exception {
        ReorderLevel level = new ReorderLevel();
        level.setId(1L);
        level.setTenantId("t1");
        level.setReorderLevel(new BigDecimal("50"));

        when(reorderLevelService.getReorderLevels("t1")).thenReturn(List.of(level));

        mockMvc.perform(get("/api/reorder-levels")
                        .param("tenantId", "t1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].reorderLevel").value(50));
    }

    @Test
    void getAlerts_returnsList() throws Exception {
        ReorderAlert alert = new ReorderAlert(
                1L, "ITEM001", "Widget", new BigDecimal("5"),
                new BigDecimal("50"), new BigDecimal("100"), "Main Warehouse");

        when(reorderLevelService.checkReorderAlerts("t1")).thenReturn(List.of(alert));

        mockMvc.perform(get("/api/reorder-levels/alerts")
                        .param("tenantId", "t1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].itemCode").value("ITEM001"))
                .andExpect(jsonPath("$[0].itemName").value("Widget"))
                .andExpect(jsonPath("$[0].godownName").value("Main Warehouse"));
    }
}

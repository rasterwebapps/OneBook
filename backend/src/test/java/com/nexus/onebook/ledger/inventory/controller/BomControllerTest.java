package com.nexus.onebook.ledger.inventory.controller;

import com.nexus.onebook.ledger.exception.GlobalExceptionHandler;
import com.nexus.onebook.ledger.inventory.model.BillOfMaterials;
import com.nexus.onebook.ledger.inventory.model.BomComponent;
import com.nexus.onebook.ledger.inventory.service.BomService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BomController.class)
@Import(GlobalExceptionHandler.class)
class BomControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BomService bomService;

    @Test
    void createBom_validRequest_returns201() throws Exception {
        BillOfMaterials bom = new BillOfMaterials();
        bom.setId(1L);
        bom.setTenantId("t1");
        bom.setBomCode("BOM001");

        when(bomService.createBom("t1", "BOM001", 1L, new BigDecimal("1")))
                .thenReturn(bom);

        mockMvc.perform(post("/api/bom")
                        .param("tenantId", "t1")
                        .param("bomCode", "BOM001")
                        .param("finishedItemId", "1"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.bomCode").value("BOM001"));
    }

    @Test
    void getBoms_returnsList() throws Exception {
        BillOfMaterials bom = new BillOfMaterials();
        bom.setId(1L);
        bom.setTenantId("t1");
        bom.setBomCode("BOM001");

        when(bomService.getBoms("t1")).thenReturn(List.of(bom));

        mockMvc.perform(get("/api/bom")
                        .param("tenantId", "t1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].bomCode").value("BOM001"));
    }

    @Test
    void getBom_returnsOk() throws Exception {
        BillOfMaterials bom = new BillOfMaterials();
        bom.setId(1L);
        bom.setTenantId("t1");
        bom.setBomCode("BOM001");

        when(bomService.getBom(1L)).thenReturn(bom);

        mockMvc.perform(get("/api/bom/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bomCode").value("BOM001"));
    }

    @Test
    void addComponent_validRequest_returns201() throws Exception {
        BomComponent component = new BomComponent();
        component.setId(1L);
        component.setTenantId("t1");
        component.setQuantityRequired(new BigDecimal("5"));

        when(bomService.addComponent("t1", 1L, 2L, new BigDecimal("5"), 1L))
                .thenReturn(component);

        mockMvc.perform(post("/api/bom/1/components")
                        .param("tenantId", "t1")
                        .param("componentItemId", "2")
                        .param("quantityRequired", "5")
                        .param("uomId", "1"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.quantityRequired").value(5));
    }
}

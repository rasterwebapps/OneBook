package com.nexus.onebook.inventory.controller;

import com.nexus.onebook.exception.GlobalExceptionHandler;
import com.nexus.onebook.inventory.model.Godown;
import com.nexus.onebook.inventory.model.StockGroup;
import com.nexus.onebook.inventory.model.StockItem;
import com.nexus.onebook.inventory.model.UnitOfMeasure;
import com.nexus.onebook.inventory.service.StockManagementService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(InventoryController.class)
@Import(GlobalExceptionHandler.class)
class InventoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private StockManagementService stockManagementService;

    @Test
    void createItem_validRequest_returns201() throws Exception {
        StockItem item = new StockItem();
        item.setId(1L);
        item.setTenantId("t1");
        item.setItemCode("ITEM001");
        item.setItemName("Widget");

        when(stockManagementService.createStockItem(any())).thenReturn(item);

        mockMvc.perform(post("/api/inventory/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "tenantId": "t1",
                                    "itemCode": "ITEM001",
                                    "itemName": "Widget",
                                    "primaryUomId": 1
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.itemCode").value("ITEM001"))
                .andExpect(jsonPath("$.itemName").value("Widget"));
    }

    @Test
    void getItems_returnsList() throws Exception {
        StockItem item = new StockItem();
        item.setId(1L);
        item.setTenantId("t1");
        item.setItemCode("ITEM001");
        item.setItemName("Widget");

        when(stockManagementService.getStockItems("t1")).thenReturn(List.of(item));

        mockMvc.perform(get("/api/inventory/items")
                        .param("tenantId", "t1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].itemCode").value("ITEM001"));
    }

    @Test
    void createGroup_validRequest_returns201() throws Exception {
        StockGroup group = new StockGroup("t1", "GRP001", "Raw Materials");
        group.setId(1L);

        when(stockManagementService.createStockGroup("t1", "GRP001", "Raw Materials", null))
                .thenReturn(group);

        mockMvc.perform(post("/api/inventory/groups")
                        .param("tenantId", "t1")
                        .param("groupCode", "GRP001")
                        .param("groupName", "Raw Materials"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.groupCode").value("GRP001"))
                .andExpect(jsonPath("$.groupName").value("Raw Materials"));
    }

    @Test
    void createUom_validRequest_returns201() throws Exception {
        UnitOfMeasure uom = new UnitOfMeasure("t1", "KG", "Kilogram");
        uom.setId(1L);

        when(stockManagementService.createUom("t1", "KG", "Kilogram", 2))
                .thenReturn(uom);

        mockMvc.perform(post("/api/inventory/uoms")
                        .param("tenantId", "t1")
                        .param("uomCode", "KG")
                        .param("uomName", "Kilogram"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.uomCode").value("KG"))
                .andExpect(jsonPath("$.uomName").value("Kilogram"));
    }

    @Test
    void createGodown_validRequest_returns201() throws Exception {
        Godown godown = new Godown("t1", "GDN001", "Main Warehouse");
        godown.setId(1L);

        when(stockManagementService.createGodown("t1", "GDN001", "Main Warehouse", null))
                .thenReturn(godown);

        mockMvc.perform(post("/api/inventory/godowns")
                        .param("tenantId", "t1")
                        .param("godownCode", "GDN001")
                        .param("godownName", "Main Warehouse"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.godownCode").value("GDN001"))
                .andExpect(jsonPath("$.godownName").value("Main Warehouse"));
    }
}

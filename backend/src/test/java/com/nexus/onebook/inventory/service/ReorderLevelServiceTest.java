package com.nexus.onebook.inventory.service;

import com.nexus.onebook.inventory.dto.ReorderAlert;
import com.nexus.onebook.inventory.dto.ReorderLevelRequest;
import com.nexus.onebook.inventory.model.Godown;
import com.nexus.onebook.inventory.model.ReorderLevel;
import com.nexus.onebook.inventory.model.StockItem;
import com.nexus.onebook.inventory.repository.GodownRepository;
import com.nexus.onebook.inventory.repository.ReorderLevelRepository;
import com.nexus.onebook.inventory.repository.StockItemRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReorderLevelServiceTest {

    @Mock
    private ReorderLevelRepository reorderLevelRepository;
    @Mock
    private StockItemRepository stockItemRepository;
    @Mock
    private GodownRepository godownRepository;

    @InjectMocks
    private ReorderLevelService reorderLevelService;

    @Test
    void setReorderLevel_newEntry_createsSuccessfully() {
        StockItem stockItem = mock(StockItem.class);

        Godown godown = mock(Godown.class);

        ReorderLevelRequest request = new ReorderLevelRequest(
                "tenant-1", 1L, 2L,
                new BigDecimal("10"), new BigDecimal("20"),
                new BigDecimal("100"), new BigDecimal("50"));

        when(stockItemRepository.findById(1L)).thenReturn(Optional.of(stockItem));
        when(godownRepository.findById(2L)).thenReturn(Optional.of(godown));
        when(reorderLevelRepository.findByTenantIdAndStockItemIdAndGodownId("tenant-1", 1L, 2L))
                .thenReturn(Optional.empty());
        when(reorderLevelRepository.save(any(ReorderLevel.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        ReorderLevel result = reorderLevelService.setReorderLevel(request);

        assertNotNull(result);
        verify(reorderLevelRepository).save(any(ReorderLevel.class));
    }

    @Test
    void setReorderLevel_stockItemNotFound_throws() {
        ReorderLevelRequest request = new ReorderLevelRequest(
                "tenant-1", 99L, 2L,
                new BigDecimal("10"), new BigDecimal("20"),
                new BigDecimal("100"), new BigDecimal("50"));

        when(stockItemRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                reorderLevelService.setReorderLevel(request));
    }

    @Test
    void checkReorderAlerts_belowLevel_returnsAlert() {
        StockItem stockItem = mock(StockItem.class);
        when(stockItem.getId()).thenReturn(1L);
        when(stockItem.getItemCode()).thenReturn("ITM-001");
        when(stockItem.getItemName()).thenReturn("Widget A");
        when(stockItem.getCurrentBalance()).thenReturn(new BigDecimal("5"));

        Godown godown = mock(Godown.class);
        when(godown.getGodownName()).thenReturn("Main Warehouse");

        ReorderLevel level = new ReorderLevel(
                "tenant-1", stockItem,
                new BigDecimal("10"), new BigDecimal("20"),
                new BigDecimal("100"), new BigDecimal("50"));
        level.setGodown(godown);

        when(reorderLevelRepository.findByTenantIdAndActiveTrue("tenant-1"))
                .thenReturn(List.of(level));

        List<ReorderAlert> alerts = reorderLevelService.checkReorderAlerts("tenant-1");

        assertEquals(1, alerts.size());
        assertEquals("ITM-001", alerts.get(0).itemCode());
        assertEquals(new BigDecimal("5"), alerts.get(0).currentBalance());
    }

    @Test
    void checkReorderAlerts_aboveLevel_returnsEmpty() {
        StockItem stockItem = mock(StockItem.class);
        when(stockItem.getCurrentBalance()).thenReturn(new BigDecimal("50"));

        ReorderLevel level = new ReorderLevel(
                "tenant-1", stockItem,
                new BigDecimal("10"), new BigDecimal("20"),
                new BigDecimal("100"), new BigDecimal("50"));

        when(reorderLevelRepository.findByTenantIdAndActiveTrue("tenant-1"))
                .thenReturn(List.of(level));

        List<ReorderAlert> alerts = reorderLevelService.checkReorderAlerts("tenant-1");

        assertTrue(alerts.isEmpty());
    }
}

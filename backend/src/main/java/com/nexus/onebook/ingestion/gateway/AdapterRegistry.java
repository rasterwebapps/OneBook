package com.nexus.onebook.ingestion.gateway;

import com.nexus.onebook.ingestion.model.AdapterType;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Registry that holds all available {@link FinancialEventAdapter} implementations.
 * Spring auto-discovers adapters via constructor injection of the adapter list.
 */
@Service
public class AdapterRegistry {

    private final Map<AdapterType, FinancialEventAdapter> adapters;

    public AdapterRegistry(List<FinancialEventAdapter> adapterList) {
        this.adapters = adapterList.stream()
                .collect(Collectors.toMap(FinancialEventAdapter::getAdapterType, Function.identity()));
    }

    /**
     * Looks up an adapter by type.
     *
     * @param type the adapter type to find
     * @return the adapter implementation
     * @throws IllegalArgumentException if no adapter is registered for the type
     */
    public FinancialEventAdapter getAdapter(AdapterType type) {
        FinancialEventAdapter adapter = adapters.get(type);
        if (adapter == null) {
            throw new IllegalArgumentException("No adapter registered for type: " + type);
        }
        return adapter;
    }

    /**
     * Returns all registered adapter types.
     */
    public List<AdapterType> getRegisteredTypes() {
        return List.copyOf(adapters.keySet());
    }
}

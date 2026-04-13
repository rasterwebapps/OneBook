package com.nexus.onebook.intelligence.service;

import com.nexus.onebook.intelligence.dto.TransactionAnomaly;
import com.nexus.onebook.accounts.model.JournalEntry;
import com.nexus.onebook.accounts.repository.JournalEntryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AnomalyDetectionService {

    private final JournalEntryRepository entryRepository;

    public AnomalyDetectionService(JournalEntryRepository entryRepository) {
        this.entryRepository = entryRepository;
    }

    @Transactional(readOnly = true)
    public List<TransactionAnomaly> detectAnomalies(String tenantId) {
        List<JournalEntry> entries = entryRepository.findPostedEntriesByTenantId(tenantId);

        if (entries.isEmpty()) {
            return List.of();
        }

        List<TransactionAnomaly> anomalies = new ArrayList<>();

        detectDuplicates(entries, tenantId, anomalies);
        detectUnusualAmounts(entries, tenantId, anomalies);
        detectRoundNumbers(entries, tenantId, anomalies);

        anomalies.sort(Comparator.comparingDouble(TransactionAnomaly::confidenceScore).reversed());

        return anomalies;
    }

    private void detectDuplicates(List<JournalEntry> entries, String tenantId,
                                  List<TransactionAnomaly> anomalies) {
        Map<String, List<JournalEntry>> grouped = entries.stream()
                .collect(Collectors.groupingBy(e ->
                        e.getAccount().getId() + "|" + e.getAmount().toPlainString()
                                + "|" + (e.getDescription() != null ? e.getDescription() : "")));

        for (Map.Entry<String, List<JournalEntry>> group : grouped.entrySet()) {
            if (group.getValue().size() > 1) {
                for (JournalEntry entry : group.getValue()) {
                    anomalies.add(new TransactionAnomaly(
                            entry.getId(),
                            tenantId,
                            "DUPLICATE",
                            "Possible duplicate entry: amount " + entry.getAmount().toPlainString()
                                    + " for account " + entry.getAccount().getId(),
                            entry.getAmount(),
                            entry.getAmount(),
                            0.9,
                            "HIGH"));
                }
            }
        }
    }

    private void detectUnusualAmounts(List<JournalEntry> entries, String tenantId,
                                      List<TransactionAnomaly> anomalies) {
        BigDecimal sum = entries.stream()
                .map(JournalEntry::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal count = BigDecimal.valueOf(entries.size());
        BigDecimal mean = sum.divide(count, 10, RoundingMode.HALF_UP);

        BigDecimal varianceSum = entries.stream()
                .map(e -> e.getAmount().subtract(mean).pow(2))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal variance = varianceSum.divide(count, 10, RoundingMode.HALF_UP);
        BigDecimal stdDev = variance.sqrt(new MathContext(10));

        if (stdDev.compareTo(BigDecimal.ZERO) == 0) {
            return;
        }

        BigDecimal threshold3 = mean.add(stdDev.multiply(BigDecimal.valueOf(3)));
        BigDecimal threshold5 = mean.add(stdDev.multiply(BigDecimal.valueOf(5)));

        for (JournalEntry entry : entries) {
            if (entry.getAmount().compareTo(threshold3) > 0) {
                String severity = entry.getAmount().compareTo(threshold5) > 0 ? "CRITICAL" : "HIGH";

                anomalies.add(new TransactionAnomaly(
                        entry.getId(),
                        tenantId,
                        "UNUSUAL_AMOUNT",
                        "Amount " + entry.getAmount().toPlainString()
                                + " exceeds statistical threshold (mean: "
                                + mean.setScale(2, RoundingMode.HALF_UP).toPlainString() + ")",
                        entry.getAmount(),
                        mean,
                        0.8,
                        severity));
            }
        }
    }

    private void detectRoundNumbers(List<JournalEntry> entries, String tenantId,
                                    List<TransactionAnomaly> anomalies) {
        BigDecimal roundThreshold = BigDecimal.valueOf(10000);
        BigDecimal roundDivisor = BigDecimal.valueOf(1000);

        for (JournalEntry entry : entries) {
            if (entry.getAmount().compareTo(roundThreshold) > 0
                    && entry.getAmount().remainder(roundDivisor).compareTo(BigDecimal.ZERO) == 0) {
                anomalies.add(new TransactionAnomaly(
                        entry.getId(),
                        tenantId,
                        "ROUND_NUMBER",
                        "Suspiciously round amount: " + entry.getAmount().toPlainString(),
                        entry.getAmount(),
                        BigDecimal.ZERO,
                        0.5,
                        "LOW"));
            }
        }
    }
}

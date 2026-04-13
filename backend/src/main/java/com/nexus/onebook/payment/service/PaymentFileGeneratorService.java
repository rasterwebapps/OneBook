package com.nexus.onebook.payment.service;

import com.nexus.onebook.payment.model.PaymentBatch;
import com.nexus.onebook.payment.model.PaymentBatchItem;
import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public class PaymentFileGeneratorService {

    public byte[] generateCsv(PaymentBatch batch, List<PaymentBatchItem> items) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (PrintWriter writer = new PrintWriter(baos, true, StandardCharsets.UTF_8)) {
            writer.println("Sr No,Vendor Name,Bank Account,IFSC Code,Bank Name,Payment Amount,Payment Reference,Payment Mode");
            int srNo = 1;
            for (PaymentBatchItem item : items) {
                var entry = item.getRegisterEntry();
                writer.printf("%d,%s,%s,%s,%s,%s,%s,%s%n",
                    srNo++,
                    csvEscape(entry.getVendorName()),
                    csvEscape(entry.getBankAccountNumber()),
                    csvEscape(entry.getBankIfscCode()),
                    csvEscape(entry.getBankName()),
                    item.getAmount().toPlainString(),
                    csvEscape(batch.getBatchNumber()),
                    csvEscape(batch.getPaymentMode())
                );
            }
        }
        return baos.toByteArray();
    }

    private String csvEscape(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}

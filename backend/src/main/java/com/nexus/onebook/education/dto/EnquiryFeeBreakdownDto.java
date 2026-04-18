package com.nexus.onebook.education.dto;

import java.math.BigDecimal;

/**
 * DTO for the fee breakdown returned when calculating enquiry fees.
 * Breaks down fees into GENERIC, hostel, and transportation components.
 */
public record EnquiryFeeBreakdownDto(
        BigDecimal genericTotal,
        BigDecimal hostelFee,
        BigDecimal transportationFee
) {}

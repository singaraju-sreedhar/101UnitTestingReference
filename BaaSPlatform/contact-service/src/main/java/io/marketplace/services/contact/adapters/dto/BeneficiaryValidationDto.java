package io.marketplace.services.contact.adapters.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BeneficiaryValidationDto {
	private String qrRawData;
	private String debtorName;
	private String debtorAccount;
}

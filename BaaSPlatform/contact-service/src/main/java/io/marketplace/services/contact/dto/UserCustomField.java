package io.marketplace.services.contact.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class UserCustomField {
	private String customKey;
	private String customValue;
}

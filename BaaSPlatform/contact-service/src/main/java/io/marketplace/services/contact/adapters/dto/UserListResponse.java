package io.marketplace.services.contact.adapters.dto;

import io.marketplace.services.membership.model.dto.UserProfileDto;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class UserListResponse {
    private List<UserProfileDto> data;
}

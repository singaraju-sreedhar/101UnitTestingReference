package io.marketplace.services.contact.adapters.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

import javax.validation.constraints.Pattern;

@Data
@Builder
public class UserProfileResponse {
    private UserProfileData data;

    @Data
    @Builder
    public static class UserProfileData {

        private String userId;

        private String userName;

        private String firstName;

        private String lastName;

        private String gender;

        @Pattern(
                message = "must have the following format yyyy-MM-dd",
                regexp =
                        "^([1-2][0-9][0-9][0-9])-(0[1-9]||1[0-9]||2[0-9]||3[0-1])-(0[1-9]||1[0-2])$")
        private String dateOfBirth;

        @Pattern(message = "must have between 1 and 12 digits", regexp = "^[0-9]{1,12}$")
        private String mobileNumber;

        private Boolean isUSCitizen;

        private String status;

        private LocalDateTime lastLoginAt;

        private KycDetails kycDetails;

        private List<String> listRoles;

        private LocalDateTime createdAt;
    }

    @Data
    @Builder
    public static class KycDetails {
        private String idType;

        private String idNumber;

        private String idIssuingCountry;

        private String idExpiredDate;
    }
}

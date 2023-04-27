package io.marketplace.services.contact.adapters.dto;
import javax.validation.constraints.Size;

import com.google.gson.annotations.SerializedName;

import io.marketplace.commons.validation.CustomNotBlank;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BankAccountDto {
    @ApiModelProperty(value = "Code of a specific bank. This is a fixed value and will be given by the API provider", required = true, dataType = "String", example = "SAN-UK", position = 1)
    @CustomNotBlank
    @Size(max = 100)
    @SerializedName("bankCode")
    private String bankCode;

    @ApiModelProperty(value = "The logo of a specific bank. This is a fixed value and will be given by the API provider", required = true, dataType = "String", example = "", position = 1)
    @SerializedName("bankLogo")
    private String bankLogo;

    @ApiModelProperty(value = "Bank account's number. Typically a 8 digits long number", required = true, dataType = "String", example = "1236123645", position = 2)
    @SerializedName("accountNumber")
    private String accountNumber;

    @ApiModelProperty(value = "Bank account's type.", required = true, dataType = "String", example = "SavingsAccount", position = 2)
    @SerializedName("accountType")
    private String accountType;

    @ApiModelProperty(value = "Bank account's sort code (or bank-branch identifier). Typically a 6 digits long number", required = true, dataType = "String", example = "123123", position = 3)
    @SerializedName("bankBranchId")
    private String bankBranchId;

    @ApiModelProperty(value = "Bank account holder's name", required = true, dataType = "String", example = "John Well Anton John", position = 4)
    @SerializedName("accountHolderName")
    private String accountHolderName;

    @ApiModelProperty(value = "The consent identity to link bank account", required = true, dataType = "String", example = "", position = 6)
    @SerializedName("consentId")
    private String consentId;

    @ApiModelProperty(value = "Country code of the bank", required = true, dataType = "String", example = "UK", position = 7)
    @SerializedName("countryCode")
    private String countryCode;

    @ApiModelProperty(value = "The bank branch name", required = true, dataType = "String", example = "", position = 8)
    @SerializedName("bankBranchName")
    private String bankBranchName;

    @ApiModelProperty(value = "The account Id", required = true, dataType = "String", example = "", position = 9)
    @SerializedName("accountId")
    private String accountId;

    @ApiModelProperty(value = "Bank account's sub type.", required = true, dataType = "String", example = "", position = 10)
    @SerializedName("accountSubType")
    private String accountSubType;

    @ApiModelProperty(value = "Bank account's Credit Debit Indicator.", required = true, dataType = "String", example = "", position = 11)
    @SerializedName("creditDebitIndicator")
    private String creditDebitIndicator;

    @ApiModelProperty(value = "Bank account's Balance Type.", required = true, dataType = "String", example = "", position = 12)
    @SerializedName("balanceType")
    private String balanceType;

}
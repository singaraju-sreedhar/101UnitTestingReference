package io.marketplace.services.contact.utils;

public final class ErrorCode {

    public static final String ERROR_UNKNOWN = "092.01.400.00";
    public static final String ERROR_UNKNOWN_MSG = "Unhandled error "
        + "occurred";

    public static final String CONTACT_CREATION_DB_ERROR_CODE = "092.01.500.01";
    public static final String CONTACT_CREATION_DB_ERROR_MESSAGE =
            "Error occurred when creating new contact record";

    public static final String WALLET_NOT_FOUND_ERROR_CODE = "092.01.401.02";

    public static final String USER_GET_VIA_MOBILE_ERROR_CODE = "092.01.500.03";
    public static final String USER_GET_VIA_MOBILE_ERROR_CODE_MESSAGE =
            "Error occurred when getting user information using mobile number";

    public static final String WALLET_SEARCH_VIA_USER_ERROR_CODE = "092.01.500.04";
    public static final String WALLET_SEARCH_VIA_USER_ERROR_MESSAGE =
            "Error occurred when getting wallet information using userId";

    public static final String WALLET_SEARCH_VIA_ACCOUNT_ERROR_CODE = "092.01.404.05";
    public static final String WALLET_SEARCH_VIA_ACCOUNT_ERROR_MESSAGE =
            "Error occurred when getting wallet information using account number";

    public static final String KAFKA_CONSUMER_ERROR_MESSAGE =
            "Error in consuming kafka message for the topic :";
    public static final String KAFKA_CONSUMER_ERROR_CODE = "092.01.500.06";

    public static final String CONTACT_CREATION_DUP_MESSAGE =
            "Contact creation failed due to paymentReference/accountNumber already exist";
    public static final String CONTACT_CREATION_DUP_ERROR_CODE = "092.01.409.07";

    public static final String CONTACT_CREATION_ERROR =
            "Contact creation failed due to invalid request";
    public static final String CONTACT_CREATION_ERROR_CODE = "092.01.400.01";

    public static final String CONTACT_DELETE_MESSAGE =
            "Contact id not found in the system for the user";
    public static final String CONTACT_DELETE_ERROR_CODE = "092.01.409.08";

    public static final String CONTACT_LOOKUP_LIMIT_ERROR_MESSAGE =
            "Too Many Requests to lookup the beneficiary.";
    public static final String CONTACT_LOOKUP_LIMIT_ERROR_CODE = "092.01.500.07";

    public static final String CONTACT_LOOKUP_INTERNAL_ERROR_MESSAGE = "092.01.500.08";
    public static final String CONTACT_LOOKUP_INTERNAL_ERROR_CODE =
            "Error occurred when looking up the beneficiary.";

    public static final String LOOKUP_BENEFICIARY_ACCOUNT_ERROR_CODE = "092.01.500.09";
    public static final String LOOKUP_BENEFICIARY_ACCOUNT_ERROR_MESSAGE =
            "Error occurred when looking up the beneficiary account.";

    public static final String LOOKUP_BENEFICIARY_FAILED_ERROR_CODE = "092.01.500.12";
    public static final String LOOKUP_BENEFICIARY_FAILED_ERROR_MESSAGE =
            "Error occurred when connecting to the interbank service.";

    public static final String LOOKUP_BENEFICIARY_VIA_IDENTIFICATION_ERROR_CODE = "092.01.404.06";
    public static final String LOOKUP_BENEFICIARY_VIA_IDENTIFICATION_ERROR_MESSAGE =
            "Error occurred when getting beneficiary using identification";

    public static final String INVALID_MAPPING_CONFIG = "092.01.500.10";
    public static final String INVALID_MAPPING_MESSAGE =
            "Can not to find the adapter mapping for entityId: %s, appId: %s";

    public static final String IDENTIFICATION_TYPE_ERROR = "Invalid identification type";
    public static final String IDENTIFICATION_TYPE_ERROR_CODE = "092.01.400.02";

    public static final String WALLET_NOT_FOUND_ERROR = "Wallet not found for the user";
    public static final String WALLET_NOT_FOUND_FOR_USER_ERROR_CODE = "092.01.404.07";

    public static final String REMOVE_BENEFICIARY_ACCOUNT_ERROR_CODE = "092.01.400.03";
    public static final String REMOVE_BENEFICIARY_ACCOUNT_ERROR_MESSAGE =
            "Cannot remove beneficiary account due to invalid data.";

    public static final String REMOVE_BENEFICIARY_ACCOUNT_SERVER_ERROR_CODE = "092.01.500.11";
    public static final String REMOVE_BENEFICIARY_ACCOUNT_SERVER_ERROR_MESSAGE =
            "Error occurred at adapter server while removing the beneficiary account.";

    public static final String UPDATE_BENEFICIARY_VIA_IDENTIFICATION_ERROR_CODE = "092.01.404.08";
    public static final String UPDATE_BENEFICIARY_VIA_IDENTIFICATION_ERROR_MESSAGE =
            "Error occurred when updating beneficiary using identification";

    public static final String ERROR_CANNOT_GET_USER = "092.01.400.09";
    public static final String ERROR_CANNOT_GET_USER_MSG = "Cannot get account.";

    public static final String ERROR_USER_DONOT_HAS_KYCDETAIL = "092.01.400.10";
    public static final String ERROR_USER_DONOT_HAS_KYCDETAIL_MSG =
            "The account does not has kyc information.";

    public static final String ERROR_CANNOT_FIND_WALLET = "092.01.400.11";
    public static final String ERROR_CANNOT_FIND_WALLET_MSG = "Cannot find wallet.";

    public static final String ERROR_WALLET_WITHOUT_BANK_INFO = "092.01.400.12";
    public static final String ERROR_WALLET_WITHOUT_BANK_INFO_MSG =
            "The wallet is invalid, it has no bank information.";

    public static final String ERROR_DONOT_SUPPORT_SECONDARY_TYPE = "092.01.400.13";
    public static final String ERROR_DONOT_SUPPORT_SECONDARY_TYPE_MSG =
            "Do not support secondary type '%s'.";

    public static final String ERROR_CANNOT_FIND_IDENTIFICATION = "092.01.400.14";
    public static final String ERROR_CANNOT_FIND_IDENTIFICATION_MSG = "Cannot find identification.";

    public static final String ERROR_DONOT_SUPPORT_TYPE = "092.01.400.15";
    public static final String ERROR_DONOT_SUPPORT_TYPE_MSG = "Do not support type '%s'.";

    public static final String ERROR_CREATE_BENEFICIARY_ACCOUNT = "092.01.500.13";
    public static final String ERROR_CREATE_BENEFICIARY_ACCOUNT_MSG =
            "Error occurred at adapter while create the beneficiary account.";

    public static final String ERROR_CREATE_BENEFICIARY_ACCOUNT_DETAIL = "092.01.500.14";
    public static final String ERROR_CREATE_BENEFICIARY_ACCOUNT_DETAIL_MSG =
            "Failed to create beneficiary account.";

    public static final String ERROR_REMOVE_BENEFICIARY_ACCOUNT_DETAIL = "092.01.500.16";
    public static final String ERROR_REMOVE_BENEFICIARY_ACCOUNT_DETAIL_MSG =
            "Failed to remove beneficiary account.";

    public static final String ERROR_REMOVE_BENEFICIARY_ACCOUNT = "092.01.500.17";
    public static final String ERROR_REMOVE_BENEFICIARY_ACCOUNT_MSG =
            "Error occurred at adapter while remove the beneficiary account.";

    public static final String GET_USER_PROFILE_ERROR_CODE = "092.01.500.12";
    public static final String GET_USER_PROFILE_ERROR_MESSAGE =
            "Error occurred when getting user profile information";

    public static final String GET_WALLET_VIA_ACCOUNT_TYPE_ERROR_CODE = "092.01.500.15";
    public static final String GET_WALLET_VIA_ACCOUNT_TYPE_ERROR_MESSAGE =
            "Error occurred when getting wallet information using account type";

    public static final String UPDATE_BENEFICIARY_ACCOUNT_ERROR_CODE = "092.01.500.11";
    public static final String UPDATE_BENEFICIARY_ACCOUNT_BAD_REQUEST_ERROR_CODE = "092.01.400.16";
    public static final String UPDATE_BENEFICIARY_ACCOUNT_ERROR_MESSAGE =
            "Error occurred when updating  the beneficiary account.";

    public static final String LOOKUP_BENEFICIARY_VIA_QR_ERROR_CODE = "092.01.404.09";
    public static final String LOOKUP_BENEFICIARY_VIA_QR_ERROR_MESSAGE =
            "Error occurred when getting beneficiary using QR code";

    public static final String LOOKUP_BENEFICIARY_QR_ERROR_CODE = "092.01.500.12";
    public static final String LOOKUP_BENEFICIARY_QR_ERROR_MESSAGE =
            "Error occurred when looking up the beneficiary by QR code.";

  public static final String LIST_OF_BENEFICIARY_ERROR_CODE = "092.01.500.16";
  public static final String LIST_OF_BENEFICIARY_ERROR_MESSAGE =
      "Error occurred when getting List of Beneficiaries.";
  
  public static final String QR_INVALID_ERROR_CODE = "092.01.400.17";
  public static final String QR_INVALID_ERROR_MESSAGE =
          "Invalid QR rawData found in request";
  
  public static final String EMPTY_WALLET_ERROR = "Wallet Id cannot be empty";
  public static final String EMPTY_WALLET_ERROR_CODE = "092.01.400.18";
  
  public static final String INVALID_IDENTIFICATION_ERROR_CODE = "092.01.400.18";
  public static final String INVALID__IDENTIFICATION_ERROR_MESSAGE =
          "Error occurred when getting beneficiary using invalid identification";

  public static final String BENEFICIARY_ACCOUNT_TYPE_MISSING_ERROR_CODE = "092.01.400.19";
  public static final String BENEFICIARY_ACCOUNT_TYPE_MISSING_ERROR_MESSAGE = "Missing beneficiaryAccountType mandatory field";

  public static final String INVALID_USER_PROFILE_ERROR_CODE = "092.01.500.18";
  public static final String INVALID_USER_PROFILE_ERROR_CODE_ERROR_MESSAGE = "Invalid user profile to get the idType of the user";

}

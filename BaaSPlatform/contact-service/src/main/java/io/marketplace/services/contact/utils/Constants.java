package io.marketplace.services.contact.utils;

public class Constants {

    public static final String RECEIVING_USER_INFORMATION_GET = "User information get response";

    public static final String RECEIVING_THE_REQUEST_TO_GET_BENEFICIARY_ACTIVITY =
            "Get Beneficiaries";
    public static final String RECEIVING_THE_REQUEST_TO_GET_BENEFICIARY =
            "Received request to search beneficiaries";
    public static final String RECV_GET_BEN_REQUEST = "GET.BENEFICIARIES.REQUEST";

    public static final String RECV_GET_BEN = "GET.BENEFICIARIES";

    public static final String RECEIVING_THE_REQUEST_TO_GET_ACTIVITY = "Get Contact";
    public static final String RECEIVING_THE_REQUEST_TO_GET = "Received request to search contact";
    public static final String RECV_GET_REQUEST = "GET.CONTACT.REQUEST";

    public static final String RECEIVING_THE_REQUEST_TO_SAVE_ACTIVITY = "Create Contact";
    public static final String RECEIVING_THE_REQUEST_TO_DELETE_ACTIVITY = "Delete Contact";
    public static final String DELETE_ACTIVITY = "Contact delete done";

    public static final String RECEIVING_THE_REQUEST_TO_SAVE_CONTACT =
            "Received request to create new contact";
    public static final String RECV_SAVE_REQUEST = "CREATE.CONTACT.REQUEST";
    public static final String RECV_DELETE_REQUEST = "DELETE.CONTACT.REQUEST";

    public static final String RECEIVING_THE_REQUEST_TO_LOOKUP_BENEFICIARY_ACCOUNT_ACTIVITY =
            "Lookup beneficiary account";
    public static final String EVENT_GET_BENEFICIARY_ACCOUNT = "GET.BENEFICIARY.ACCOUNT";
    public static final String RECEIVING_THE_REQUEST_TO_LOOKUP_BENEFICIARY_ACCOUNT =
            "Received request to Lookup beneficiary account";
    public static final String RESPONSE_THE_REQUEST_TO_LOOKUP_BENEFICIARY_ACCOUNT =
            "Received successful response for Lookup beneficiary account";
    
    public static final String RECEIVING_THE_REQUEST_TO_LOOKUP_BENEFICIARY_QR_ACTIVITY =
            "Lookup beneficiary QR";
    public static final String EVENT_GET_BENEFICIARY_QR = "POST.BENEFICIARY";
    public static final String RECEIVING_THE_REQUEST_TO_LOOKUP_BENEFICIARY_QR =
            "Received request to Lookup beneficiary QR code";
    public static final String PROCESS_THE_REQUEST_TO_LOOKUP_BENEFICIARY_QR =
            "Process request to Lookup beneficiary QR code";
    public static final String RESPONSE_THE_REQUEST_TO_LOOKUP_BENEFICIARY_QR =
            "Received successful response for Lookup beneficiary QR code";

    public static final String REMOVE_BENEFICIARY_ACCOUNT_ACTIVITY = "Remove beneficiary account";
    public static final String EVENT_REMOVE_BENEFICIARY_ACCOUNT = "REMOVE.BENEFICIARY.ACCOUNT";
    public static final String RECEIVING_THE_REQUEST_TO_REMOVE_BENEFICIARY_ACCOUNT =
            "Received request to remove beneficiary account";

    public static final String CREATE_BENEFICIARY_ACCOUNT_ACTIVITY = "Create beneficiary account";
    public static final String CREATE_BENEFICIARY_ACCOUNT_EVENT = "POST.BENEFICIARY.ACCOUNT";
    public static final String CREATE_BENEFICIARY_ACCOUNT_REQUEST_MSG =
            "Received request to create beneficiary account";
    public static final String CREATE_BENEFICIARY_ACCOUNT_RESPONSE_MSG =
            "Response for create beneficiary account";
    public static final String CALL_ADAPTER_TO_CREATE_BENEFICIARY_ACCOUNT =
            "Call adapter to create beneficiary account";
    public static final String CALL_ADAPTER_TO_CREATE_BENEFICIARY_ACCOUNT_EVENT =
            "POST.CREATE.BENEFICIARY.ACCOUNT.FROM.FNX";

    public static final String CALL_ADAPTER_TO_REMOVE_BENEFICIARY_ACCOUNT =
            "Call adapter to remove beneficiary account";
    public static final String CALL_ADAPTER_TO_REMOVE_BENEFICIARY_ACCOUNT_EVENT =
            "POST.REMOVE.BENEFICIARY.ACCOUNT.FROM.FNX";

    public static final String SEQUENCE_REQUEST = ".REQUEST";
    public static final String SEQUENCE_RESPONSE = ".RESPONSE";
    public static final String SEQUENCE_INVOKE = ".INVOKE";

    public static final String SUCCESS_REQUEST_TO_SAVE_CONTACT = "Create new contact successful";

    public static final String RECEIVING_THE_REQUEST_TO_GET_WALLETS = "Get Wallet details";
    public static final String RECEIVING_THE_REQUEST_TO_SEARCH_USER = "Get User information";

    public static final String SEARCH_REQUEST_BUSINESS_DATA = "userId: %s searchText: %s";
    public static final String SEARCH_REQUEST_BUSINESS_DATA_BENEFICIARY =
            "mobileNumber: %s accountNumber: %s accountType: %s";
    public static final String SEARCH_REQUEST_BUSINESS_DATA_BENEFICIARY_BY_IDENTIFICATION =
            "type: %s identification: %s bankCode : %s";

    public static final String SAVE_REQUEST_BUSINESS_DATA = "request: %s";
    public static final String DELETE_REQUEST_BUSINESS_DATA = "contact id : %s";

    public static final String SUPER_ROLE = "SuperAdmin";
    public static final String PERMISSION_GET_CONTACT_LIST = "GET_CONTACT_LIST";

    public static final String SERVICE_CODE = "UD";

    public static final int DEFAULT_PAGE_NUMBER = 1;
    public static final int DEFAULT_PAGE_SIZE = 10;

    public static final String CREATED_AT_COLUMN = "createdAt";
    public static final String DISPLAY_NAME = "displayName";
    public static final String PAYMENT_REFERENCE = "paymentReference";
    public static final String ACCOUNT_COLUMN = "accountNumber";
    public static final String ORDER_DESCENT = "DESC";
    public static final String ORDER_ASCENT = "ASC";

    public static final String EVENT_CODE_UPDATE_USER_CUSTOM_FIELD = "PUT.USER.CUSTOM_FIELD";
    public static final String EVENT_TRACKING_UPDATE_USER_PROFILE = "Update User Profile";
    public static final String USER_ENTITY = "User";

    public static final String USER_ATTACK_LOOKUP_BENEFICIARY_KEY =
            "TwoManyRequestLookupBeneficiary";
    public static final String USER_ATTACK_LOOKUP_BENEFICIARY_VALUE = "Yes";

    public static final String X_JWT_ASSERTION = "X-JWT-Assertion";

    public static final String BENEFICIARY_ACCOUNT_UPDATE_ACTIVITY = "Beneficiary account update";
    public static final String EVENT_UPDATE_BENEFICIARY_ACCOUNT = "PUT.BENEFICIARY.ACCOUNT";
    public static final String RECEIVING_THE_REQUEST_TO_UPDATE_BENEFICIARY_ACCOUNT =
            "Received request to update beneficiary account";
    public static final String PAYMENT_ADAPTER_REQUEST_TO_UPDATE_BENEFICIARY_ACCOUNT =
            "Calling Interbank Payment Service to update beneficiary account";
    public static final String PAYMENT_ADAPTER_RESPONSE_TO_UPDATE_BENEFICIARY_ACCOUNT =
            "Received Interbank Payment Service response to update beneficiary account";
    public static final String RESPONSE_THE_REQUEST_TO_UPDATE_BENEFICIARY_ACCOUNT =
            "Received successful response to update beneficiary account";
    public static final String UPDATE_REQUEST_BUSINESS_DATA_BENEFICIARY_BY_IDENTIFICATION =
            "type: %s and identification: %s";
    public static final String RECEIVING_USER_PROFILE_INFORMATION_GET =
            "Receiving User profile information response";
    public static final String RECEIVING_WALLET_INFORMATION_GET =
            "Receiving Wallet information response";

    public static final String RESPONSE_LIST_OF_BENEFICIARIES =
            "Received successful response for List of beneficiaries";
    public static final String ADAPTER_ROUT = "Found the adapter: %s";
    public static final String WALLET_ID = "Wallet id: %s, ";
    public static final String SAVINGS_ACCOUNT = "SavingsAccount" ;


    public static final String GENERATE_QR_ACTIVITY = "QR Generate";
}

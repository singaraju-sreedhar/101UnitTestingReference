package io.marketplace.services.contact.service;

import static io.marketplace.services.contact.utils.Constants.UPDATE_REQUEST_BUSINESS_DATA_BENEFICIARY_BY_IDENTIFICATION;
import static io.marketplace.services.contact.utils.ErrorCode.*;

import io.marketplace.commons.exception.ApiResponseException;
import io.marketplace.commons.exception.BadRequestException;
import io.marketplace.commons.exception.InternalServerErrorException;
import io.marketplace.commons.logging.Error;
import io.marketplace.commons.logging.Logger;
import io.marketplace.commons.logging.LoggerFactory;
import io.marketplace.commons.model.event.EventMessage;
import io.marketplace.commons.model.event.EventStatus;
import io.marketplace.commons.utils.MembershipUtils;
import io.marketplace.commons.utils.StringUtils;
import io.marketplace.commons.utils.ThreadContextUtils;
import io.marketplace.services.contact.adapters.MembershipAdapter;
import io.marketplace.services.contact.adapters.PaymentServiceAdapter;
import io.marketplace.services.contact.adapters.WalletServiceAdapter;
import io.marketplace.services.contact.adapters.dto.UserProfileResponse;
import io.marketplace.services.contact.adapters.dto.UserProfileResponse.KycDetails;
import io.marketplace.services.contact.adapters.dto.UserProfileResponse.UserProfileData;
import io.marketplace.services.contact.adapters.dto.WalletListResponse;
import io.marketplace.services.contact.config.AdapterDefinition;
import io.marketplace.services.contact.model.BankAccount;
import io.marketplace.services.contact.model.Wallet;
import io.marketplace.services.contact.model.LookupBeneficiaryResponse;
import io.marketplace.services.contact.model.BeneficiaryAccountDetails;
import io.marketplace.services.contact.model.BeneficiaryDeleteResponse;
import io.marketplace.services.contact.model.BeneficiaryValidation;
import io.marketplace.services.contact.model.BeneficiaryCreatedData;
import io.marketplace.services.contact.model.UpdateBeneficiaryRecord;
import io.marketplace.services.contact.model.CreateBeneficiaryRecord;
import io.marketplace.services.contact.utils.AdapterUtils;
import io.marketplace.services.contact.utils.Constants;
import io.marketplace.services.contact.utils.ErrorCode;
import io.marketplace.services.contact.utils.IdentificationType;
import io.marketplace.services.pxchange.client.service.PXChangeServiceClient;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class BeneficiaryService {
    private static final Logger log = LoggerFactory.getLogger(BeneficiaryService.class);

    private RestTemplate restTemplate;

    @Value("${payment-adapter-finexus.base-url}")
    public String baseUrl;

    @Autowired private AdapterUtils adapterUtils;

    @Autowired private PaymentServiceAdapter paymentServiceAdapter;

    @Autowired private PXChangeServiceClient pxClient;

    @Autowired private WalletServiceAdapter walletServiceAdapter;

    @Autowired private MembershipAdapter membershipAdapter;

    @Value("${debtorAccountType:SVGS}")
    public String debtorAccountTypeValue;

    @Value("${nad-walletAccountType}")
    public String walletAccountType;

    @Autowired
    public BeneficiaryService(@Qualifier("gson-rest-template") final RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public BeneficiaryAccountDetails updateBeneficiary(
            String type, String identification, UpdateBeneficiaryRecord updateBeneficiaryRecord) {
        log.info("updateBeneficiary for type : {} and identification : {}", type, identification);

        if (IdentificationType.getValueByType(type) == null) {
            pxClient.addEvent(
                    EventMessage.builder()
                            .activityName(Constants.BENEFICIARY_ACCOUNT_UPDATE_ACTIVITY)
                            .eventTitle(ErrorCode.IDENTIFICATION_TYPE_ERROR)
                            .errorCode(ErrorCode.IDENTIFICATION_TYPE_ERROR_CODE)
                            .eventCode(Constants.EVENT_UPDATE_BENEFICIARY_ACCOUNT)
                            .businessData(String.format("Identification type: %s, ", type))
                            .eventStatus(EventStatus.FAILED)
                            .build());

            throw new BadRequestException(
                    ErrorCode.IDENTIFICATION_TYPE_ERROR_CODE,
                    ErrorCode.IDENTIFICATION_TYPE_ERROR,
                    type);
        }
        AdapterDefinition adapterDefinition = adapterUtils.getAdapterDefinition();
        String businessId =
                adapterUtils.constructAdapterKey(
                        ThreadContextUtils.getCustomRequest().getEntityId(),
                        ThreadContextUtils.getCustomRequest().getAppId());

        if (adapterDefinition != null) {
            log.info("Calling for Get User profile information");
            UserProfileResponse userProfileResponse;
            try {
                userProfileResponse = membershipAdapter.getUserProfileInfo();
                if (userProfileResponse != null) {
                    pxClient.addEvent(
                            EventMessage.builder()
                                    .activityName(Constants.BENEFICIARY_ACCOUNT_UPDATE_ACTIVITY)
                                    .eventTitle(Constants.RECEIVING_USER_PROFILE_INFORMATION_GET)
                                    .eventCode(
                                            Constants.EVENT_UPDATE_BENEFICIARY_ACCOUNT
                                                    + Constants.SEQUENCE_INVOKE)
                                    .eventBusinessId(
                                            String.format(
                                                    UPDATE_REQUEST_BUSINESS_DATA_BENEFICIARY_BY_IDENTIFICATION,
                                                    type,
                                                    identification))
                                    .businessData(userProfileResponse)
                                    .build());

                    UserProfileResponse.KycDetails kycDetails =
                            userProfileResponse.getData().getKycDetails();
                    if (kycDetails != null) {
                        if (adapterDefinition.getSecondaryTypeMapping() == null
                                || !adapterDefinition
                                        .getSecondaryTypeMapping()
                                        .containsKey(kycDetails.getIdType())) {
                            throw ApiResponseException.builder()
                                    .code(ErrorCode.ERROR_DONOT_SUPPORT_SECONDARY_TYPE)
                                    .message(
                                            String.format(
                                                    ErrorCode
                                                            .ERROR_DONOT_SUPPORT_SECONDARY_TYPE_MSG,
                                                    kycDetails.getIdType()))
                                    .build();
                        }

                        String secondaryType =
                                adapterDefinition
                                        .getSecondaryTypeMapping()
                                        .get(kycDetails.getIdType());
                        updateBeneficiaryRecord.setSecondaryType(secondaryType);
                        updateBeneficiaryRecord.setSecondaryIdentification(
                                kycDetails.getIdNumber());
                    }
                }

            } catch (Exception ex) {
                throw new InternalServerErrorException(
                        GET_USER_PROFILE_ERROR_CODE, ex.getMessage(), null);
            }

            log.info("Calling for Get Wallet information");
            WalletListResponse walletListResponse;
            try {
                walletListResponse = walletServiceAdapter.getWalletInfo(walletAccountType);
                if (walletListResponse != null) {
                    pxClient.addEvent(
                            EventMessage.builder()
                                    .activityName(Constants.BENEFICIARY_ACCOUNT_UPDATE_ACTIVITY)
                                    .eventTitle(Constants.RECEIVING_WALLET_INFORMATION_GET)
                                    .eventCode(
                                            Constants.EVENT_UPDATE_BENEFICIARY_ACCOUNT
                                                    + Constants.SEQUENCE_INVOKE)
                                    .eventBusinessId(
                                            String.format(
                                                    UPDATE_REQUEST_BUSINESS_DATA_BENEFICIARY_BY_IDENTIFICATION,
                                                    type,
                                                    identification))
                                    .businessData(walletListResponse)
                                    .build());
                    if (!walletListResponse.getData().isEmpty()) {
                        BankAccount bankAccount =
                                walletListResponse.getData().get(0).getBankAccount();
                        if (bankAccount != null) {
                            updateBeneficiaryRecord.setPaymentReference(
                                    bankAccount.getAccountNumber());
                            updateBeneficiaryRecord.setDisplayName(
                                    bankAccount.getAccountHolderName());
                        }
                    }
                }
            } catch (Exception ex) {
                throw new InternalServerErrorException(
                        GET_WALLET_VIA_ACCOUNT_TYPE_ERROR_CODE, ex.getMessage(), null);
            }

            String baseURl = adapterDefinition.getBaseEndpoint();
            BeneficiaryAccountDetails response =
                    paymentServiceAdapter.updateBeneficiary(
                            baseURl, type, identification, updateBeneficiaryRecord);

            if (response == null) {
                pxClient.addEvent(
                        EventMessage.builder()
                                .activityName(Constants.BENEFICIARY_ACCOUNT_UPDATE_ACTIVITY)
                                .eventTitle(ErrorCode.UPDATE_BENEFICIARY_ACCOUNT_ERROR_MESSAGE)
                                .errorCode(ErrorCode.UPDATE_BENEFICIARY_ACCOUNT_ERROR_CODE)
                                .eventCode(Constants.EVENT_UPDATE_BENEFICIARY_ACCOUNT)
                                .eventStatus(EventStatus.FAILED)
                                .build());

                throw new InternalServerErrorException(
                        ErrorCode.UPDATE_BENEFICIARY_ACCOUNT_ERROR_CODE,
                        UPDATE_BENEFICIARY_ACCOUNT_ERROR_MESSAGE,
                        null);
            }

            pxClient.addEvent(
                    EventMessage.builder()
                            .activityName(Constants.BENEFICIARY_ACCOUNT_UPDATE_ACTIVITY)
                            .eventTitle(
                                    Constants.RESPONSE_THE_REQUEST_TO_UPDATE_BENEFICIARY_ACCOUNT)
                            .eventStatus(EventStatus.SUCCESS)
                            .eventCode(
                                    Constants.EVENT_UPDATE_BENEFICIARY_ACCOUNT
                                            + Constants.SEQUENCE_RESPONSE)
                            .eventBusinessId(
                                    String.format(
                                            UPDATE_REQUEST_BUSINESS_DATA_BENEFICIARY_BY_IDENTIFICATION,
                                            type,
                                            identification))
                            .businessData(response)
                            .build());
            return response;
        }

        throw new InternalServerErrorException(
                ErrorCode.INVALID_MAPPING_CONFIG,
                String.format(
                        ErrorCode.INVALID_MAPPING_MESSAGE,
                        ThreadContextUtils.getCustomRequest().getEntityId(),
                        ThreadContextUtils.getCustomRequest().getAppId()),
                businessId);
    }

    public LookupBeneficiaryResponse lookupBeneficiaryByIdentification(
        String type, String identification, String bankCode, String walletId,
        String beneficiaryAccountType) {
        log.info(
                "lookupBeneficiaryByIdentification for type : {}, identification : {} and bankCode"
                        + " : {}",
                type,
                identification,
                bankCode);

        String debtorIdNumber = null;

        if(IdentificationType.IDCARD.getType().equals(type) || IdentificationType.PASSPORT.getType().equals(type)){
            UserProfileResponse userProfileResponse = membershipAdapter.getUserProfileInfo();

            debtorIdNumber = Optional.ofNullable(userProfileResponse)
                .map(UserProfileResponse::getData)
                .map(UserProfileData::getKycDetails)
                .map(KycDetails::getIdNumber)
                .orElseThrow(() -> {
                    log.error(
                        ErrorCode.INVALID_USER_PROFILE_ERROR_CODE,
                        Error.of(ErrorCode.INVALID_USER_PROFILE_ERROR_CODE_ERROR_MESSAGE));

                    pxClient.addEvent(
                        EventMessage.builder()
                            .activityName(
                                Constants.RECEIVING_THE_REQUEST_TO_LOOKUP_BENEFICIARY_ACCOUNT_ACTIVITY)
                            .eventTitle(ErrorCode.INVALID_USER_PROFILE_ERROR_CODE_ERROR_MESSAGE)
                            .errorCode(ErrorCode.INVALID_USER_PROFILE_ERROR_CODE)
                            .eventCode(Constants.RECV_GET_BEN)
                            .eventStatus(EventStatus.FAILED)
                            .build());

                    throw ApiResponseException.builder()
                        .httpStatus(HttpStatus.BAD_REQUEST.value())
                        .code(ErrorCode.INVALID_USER_PROFILE_ERROR_CODE)
                        .message(ErrorCode.INVALID_USER_PROFILE_ERROR_CODE_ERROR_MESSAGE)
                        .build();
                });
        }

        if(IdentificationType.ACCOUNT.getType().equals(type) && StringUtils.isEmpty(beneficiaryAccountType)){
            log.error(
                ErrorCode.BENEFICIARY_ACCOUNT_TYPE_MISSING_ERROR_MESSAGE,
                Error.of(ErrorCode.BENEFICIARY_ACCOUNT_TYPE_MISSING_ERROR_CODE));

            pxClient.addEvent(
                EventMessage.builder()
                    .activityName(
                        Constants.RECEIVING_THE_REQUEST_TO_LOOKUP_BENEFICIARY_ACCOUNT_ACTIVITY)
                    .eventTitle(ErrorCode.BENEFICIARY_ACCOUNT_TYPE_MISSING_ERROR_MESSAGE)
                    .errorCode(ErrorCode.BENEFICIARY_ACCOUNT_TYPE_MISSING_ERROR_CODE)
                    .eventCode(Constants.RECV_GET_BEN)
                    .eventStatus(EventStatus.FAILED)
                    .build());

            throw ApiResponseException.builder()
                .httpStatus(HttpStatus.BAD_REQUEST.value())
                .code(ErrorCode.BENEFICIARY_ACCOUNT_TYPE_MISSING_ERROR_CODE)
                .message(ErrorCode.BENEFICIARY_ACCOUNT_TYPE_MISSING_ERROR_MESSAGE)
                .build();
        }

        String debtorName = null;
        String debtorAccountType = null;
        String debtorAccountNumber = null;

        if (IdentificationType.getValueByType(type) == null) {
            pxClient.addEvent(
                    EventMessage.builder()
                            .activityName(
                                    Constants
                                            .RECEIVING_THE_REQUEST_TO_LOOKUP_BENEFICIARY_ACCOUNT_ACTIVITY)
                            .eventTitle(ErrorCode.IDENTIFICATION_TYPE_ERROR)
                            .errorCode(ErrorCode.IDENTIFICATION_TYPE_ERROR_CODE)
                            .eventCode(Constants.EVENT_GET_BENEFICIARY_ACCOUNT)
                            .businessData(String.format("Identification type: %s, ", type))
                            .eventStatus(EventStatus.FAILED)
                            .build());

            throw new BadRequestException(
                    ErrorCode.IDENTIFICATION_TYPE_ERROR_CODE,
                    ErrorCode.IDENTIFICATION_TYPE_ERROR,
                    type);
        }

        if (IdentificationType.getValueByType(type) == IdentificationType.QR){

            LookupBeneficiaryResponse lookupBeneficiaryResponse = generateQR(type,identification,bankCode);
            return lookupBeneficiaryResponse;

        }


        if (!StringUtils.isEmpty(walletId)) {
            String userId = MembershipUtils.getUserId();
            log.info(
                    "lookupBeneficiaryByIdentification for user : {} walletId : {}",
                    userId,
                    walletId);
            WalletListResponse walletListResponse;
            try {
                walletListResponse = walletServiceAdapter.getWalletInformation(userId, "ALL");
            } catch (Exception ex) {
                throw new InternalServerErrorException(
                        WALLET_SEARCH_VIA_USER_ERROR_CODE, ex.getMessage(), null);
            }
            boolean userWalletExists = false;
            for (Wallet e : walletListResponse.getData()) {
                if (walletId.equals(e.getWalletId())) {
                    log.info("wallet {} belongs to the current user {}", walletId, userId);
                    userWalletExists = true;
                    debtorName = e.getBankAccount().getAccountHolderName();
                    debtorAccountNumber = e.getBankAccount().getAccountNumber();
                    debtorAccountType = debtorAccountTypeValue;
                }
            }

            if (!userWalletExists) {
                // In case of wallet not belongs for the logged-in user
                pxClient.addEvent(
                        EventMessage.builder()
                                .activityName(
                                        Constants
                                                .RECEIVING_THE_REQUEST_TO_LOOKUP_BENEFICIARY_ACCOUNT_ACTIVITY)
                                .eventTitle(ErrorCode.WALLET_NOT_FOUND_ERROR)
                                .errorCode(ErrorCode.WALLET_NOT_FOUND_FOR_USER_ERROR_CODE)
                                .eventCode(Constants.EVENT_GET_BENEFICIARY_ACCOUNT)
                                .businessData(String.format(Constants.WALLET_ID, walletId))
                                .eventStatus(EventStatus.FAILED)
                                .build());

                throw new BadRequestException(
                        ErrorCode.WALLET_NOT_FOUND_FOR_USER_ERROR_CODE,
                        ErrorCode.WALLET_NOT_FOUND_ERROR,
                        walletId);
            }
        }

        AdapterDefinition adapterDefinition = adapterUtils.getAdapterDefinition();
        if (adapterDefinition != null) {
            String baseURl = adapterDefinition.getBaseEndpoint();
            return paymentServiceAdapter.getBeneficiaryByIdentification(
                    baseURl,
                    type,
                    identification,
                    bankCode,
                    debtorName,
                    debtorAccountType,
                    debtorAccountNumber,
                    beneficiaryAccountType,
                    debtorIdNumber
                );
        }
        String businessId =
                adapterUtils.constructAdapterKey(
                        ThreadContextUtils.getCustomRequest().getEntityId(),
                        ThreadContextUtils.getCustomRequest().getAppId());
        throw new InternalServerErrorException(
                ErrorCode.INVALID_MAPPING_CONFIG,
                String.format(
                        ErrorCode.INVALID_MAPPING_MESSAGE,
                        ThreadContextUtils.getCustomRequest().getEntityId(),
                        ThreadContextUtils.getCustomRequest().getAppId()),
                businessId);
    }

    private LookupBeneficiaryResponse generateQR(String type,String identification,String bankCode) {

        String debtorName = null;
        String debtorAccountType = null;
        String debtorAccountNumber = null;

        WalletListResponse walletListResponse;
        try {
            walletListResponse = walletServiceAdapter.getWalletInformationByAccountNumber(identification, "ALL");
        } catch (Exception ex) {
            throw new InternalServerErrorException(
                    WALLET_SEARCH_VIA_ACCOUNT_ERROR_CODE, ex.getMessage(), null);
        }

        if (!walletListResponse.getData().isEmpty()){

                    Wallet wallet = walletListResponse.getData().get(0);
                    debtorName = wallet.getBankAccount().getAccountHolderName();
                    debtorAccountNumber = wallet.getBankAccount().getAccountNumber();
                    debtorAccountType = Constants.SAVINGS_ACCOUNT;
                    if(org.apache.commons.lang3.StringUtils.isEmpty(bankCode)){
                        bankCode = wallet.getBankAccount().getBankCode();
        }

        }else {

            pxClient.addEvent(
                    EventMessage.builder()
                            .activityName(
                                    Constants
                                            .GENERATE_QR_ACTIVITY)
                            .eventTitle(ErrorCode.WALLET_NOT_FOUND_ERROR)
                            .errorCode(ErrorCode.WALLET_NOT_FOUND_FOR_USER_ERROR_CODE)
                            .eventCode(Constants.EVENT_GET_BENEFICIARY_ACCOUNT)
                            .businessData(String.format("AccountId:", identification))
                            .eventStatus(EventStatus.FAILED)
                            .build());


            throw ApiResponseException.builder()
                    .httpStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .code(WALLET_NOT_FOUND_ERROR_CODE)
                    .message(WALLET_NOT_FOUND_ERROR)
                    .build();
        }

        AdapterDefinition adapterDefinition = adapterUtils.getAdapterDefinition();
        if (adapterDefinition != null && debtorAccountNumber!= null && debtorName!= null && bankCode!= null) {
            String baseURl = adapterDefinition.getBaseEndpoint();
            return paymentServiceAdapter.getBeneficiaryByIdentification(
                    baseURl,
                    type,
                    identification,
                    bankCode,
                    debtorName,
                    debtorAccountType,
                    debtorAccountNumber, null, null);
        }else {
            throw new BadRequestException(
                    ErrorCode.WALLET_NOT_FOUND_FOR_USER_ERROR_CODE,
                    ErrorCode.WALLET_NOT_FOUND_ERROR,
                    identification);
        }

    }

    public BeneficiaryDeleteResponse deleteBeneficiary(String type, String identification) {
        log.info("DeleteBeneficiary for type : {}, identification : {}", type, identification);

        AdapterDefinition adapterDefinition = adapterUtils.getAdapterDefinition();
        if (adapterDefinition != null) {
            log.info(String.format(Constants.ADAPTER_ROUT, adapterDefinition.getAdapterId()));
            return paymentServiceAdapter.removeBeneficiary(adapterDefinition, type, identification);
        }

        String businessId =
                adapterUtils.constructAdapterKey(
                        ThreadContextUtils.getCustomRequest().getEntityId(),
                        ThreadContextUtils.getCustomRequest().getAppId());
        log.error(String.format("Cannot determine adapter with information: %s", businessId));
        throw new InternalServerErrorException(
                ErrorCode.INVALID_MAPPING_CONFIG,
                String.format(
                        ErrorCode.INVALID_MAPPING_MESSAGE,
                        ThreadContextUtils.getCustomRequest().getEntityId(),
                        ThreadContextUtils.getCustomRequest().getAppId()),
                businessId);
    }

    public BeneficiaryCreatedData createBeneficiary(
            CreateBeneficiaryRecord createBeneficiaryRecord) {
        AdapterDefinition adapterDefinition = adapterUtils.getAdapterDefinition();
        if (adapterDefinition != null) {
            log.info(String.format(Constants.ADAPTER_ROUT, adapterDefinition.getAdapterId()));

            String baseURl = adapterDefinition.getBaseEndpoint();
            return paymentServiceAdapter.createBeneficiary(
                    adapterDefinition, createBeneficiaryRecord);
        }

        String businessId =
                adapterUtils.constructAdapterKey(
                        ThreadContextUtils.getCustomRequest().getEntityId(),
                        ThreadContextUtils.getCustomRequest().getAppId());
        log.error(String.format("Cannot determine adapter with information: %s", businessId));
        throw new InternalServerErrorException(
                ErrorCode.INVALID_MAPPING_CONFIG,
                String.format(
                        ErrorCode.INVALID_MAPPING_MESSAGE,
                        ThreadContextUtils.getCustomRequest().getEntityId(),
                        ThreadContextUtils.getCustomRequest().getAppId()),
                businessId);
    }

	public LookupBeneficiaryResponse lookupBeneficiaryByQrCode(BeneficiaryValidation request) {
		log.info("lookupBeneficiaryByQrCode for qrRawData : {}",request.getQrRawData());
		String debtorName = null;
        String debtorAccountNumber = null;
		if (!StringUtils.isEmpty(request.getWalletId())) {
            String userId = MembershipUtils.getUserId();
            log.info(
                    "lookupBeneficiaryByQrCode for user : {} walletId : {}",
                    userId,
                    request.getWalletId());
            String businessId = String.format("walletId: %s", request.getWalletId());
            pxClient.addEvent(
                    EventMessage.builder()
                            .activityName(
                                    Constants.RECEIVING_THE_REQUEST_TO_LOOKUP_BENEFICIARY_QR_ACTIVITY)
                            .eventTitle(Constants.RECEIVING_THE_REQUEST_TO_LOOKUP_BENEFICIARY_QR)
                            .eventCode(Constants.EVENT_GET_BENEFICIARY_QR + Constants.SEQUENCE_REQUEST)
                            .eventStatus(EventStatus.SUCCESS)
                            .eventBusinessId(businessId)
                            .build());
            
            WalletListResponse walletListResponse;
            try {
                walletListResponse = walletServiceAdapter.getWalletInformation(userId, "ALL");
            } catch (Exception ex) {
                throw new InternalServerErrorException(
                        WALLET_SEARCH_VIA_USER_ERROR_CODE, ex.getMessage(), null);
            }
            boolean userWalletExists = false;
            for (Wallet e : walletListResponse.getData()) {
                if (request.getWalletId().equals(e.getWalletId())) {
                    log.info("wallet {} belongs to the current user {}", request.getWalletId(), userId);
                    userWalletExists = true;
                    debtorName = e.getBankAccount().getAccountHolderName();
                    debtorAccountNumber = e.getBankAccount().getAccountNumber();
                    log.info("debtorName: {} debtorAccountNumber: {}", debtorName, debtorAccountNumber);
                }
            }

            if (!userWalletExists) {
                // In case of wallet not belongs for the logged-in user
                pxClient.addEvent(
                        EventMessage.builder()
                                .activityName(
                                        Constants
                                                .RECEIVING_THE_REQUEST_TO_LOOKUP_BENEFICIARY_QR_ACTIVITY)
                                .eventTitle(ErrorCode.WALLET_NOT_FOUND_ERROR)
                                .errorCode(ErrorCode.WALLET_NOT_FOUND_FOR_USER_ERROR_CODE)
                                .eventCode(Constants.EVENT_GET_BENEFICIARY_QR)
                                .businessData(String.format(Constants.WALLET_ID, request.getWalletId()))
                                .eventStatus(EventStatus.FAILED)
                                .build());

                throw new BadRequestException(
                        ErrorCode.WALLET_NOT_FOUND_FOR_USER_ERROR_CODE,
                        ErrorCode.WALLET_NOT_FOUND_ERROR,
                        request.getWalletId());
            }
        }else {
        	pxClient.addEvent(
                    EventMessage.builder()
                            .activityName(
                                    Constants
                                            .RECEIVING_THE_REQUEST_TO_LOOKUP_BENEFICIARY_QR_ACTIVITY)
                            .eventTitle(ErrorCode.EMPTY_WALLET_ERROR)
                            .errorCode(ErrorCode.EMPTY_WALLET_ERROR_CODE)
                            .eventCode(Constants.EVENT_GET_BENEFICIARY_QR)
                            .businessData(String.format(Constants.WALLET_ID, request.getWalletId()))
                            .eventStatus(EventStatus.FAILED)
                            .build());

            throw new BadRequestException(
                    ErrorCode.EMPTY_WALLET_ERROR_CODE,
                    ErrorCode.EMPTY_WALLET_ERROR,
                    request.getWalletId());
        }
		 AdapterDefinition adapterDefinition = adapterUtils.getAdapterDefinition();
		 if (adapterDefinition != null) {
	            String baseURl = adapterDefinition.getBaseEndpoint();
	            return paymentServiceAdapter.lookupBeneficiaryByQrCode(
	                    baseURl,
	                    request,
	                    debtorName,
	                    debtorAccountNumber);
	        }
	        String businessId =
	                adapterUtils.constructAdapterKey(
	                        ThreadContextUtils.getCustomRequest().getEntityId(),
	                        ThreadContextUtils.getCustomRequest().getAppId());
	        throw new InternalServerErrorException(
	                ErrorCode.INVALID_MAPPING_CONFIG,
	                String.format(
	                        ErrorCode.INVALID_MAPPING_MESSAGE,
	                        ThreadContextUtils.getCustomRequest().getEntityId(),
	                        ThreadContextUtils.getCustomRequest().getAppId()),
	                businessId);
	}
}

package io.marketplace.services.contact.adapters;

import static io.marketplace.services.contact.utils.ErrorCode.GET_USER_PROFILE_ERROR_CODE;
import static io.marketplace.services.contact.utils.ErrorCode.WALLET_SEARCH_VIA_USER_ERROR_CODE;
import static java.util.stream.Collectors.toList;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import io.marketplace.commons.exception.ApiResponseException;
import io.marketplace.commons.exception.BadRequestException;
import io.marketplace.commons.exception.GenericException;
import io.marketplace.commons.exception.InternalServerErrorException;
import io.marketplace.commons.logging.Error;
import io.marketplace.commons.logging.Logger;
import io.marketplace.commons.logging.LoggerFactory;
import io.marketplace.commons.model.dto.ErrorDto;
import io.marketplace.commons.model.dto.ErrorResponseDto;
import io.marketplace.commons.model.event.EventMessage;
import io.marketplace.commons.model.event.EventStatus;
import io.marketplace.commons.utils.MembershipUtils;
import io.marketplace.services.contact.adapters.dto.BeneficiaryListResponse;
import io.marketplace.services.contact.adapters.dto.BeneficiaryValidationDto;
import io.marketplace.services.contact.adapters.dto.WalletListResponse;
import io.marketplace.services.contact.config.AdapterDefinition;
import io.marketplace.services.contact.dto.BeneficiaryUpdateResponse;
import io.marketplace.services.contact.dto.UserProfile;
import io.marketplace.services.contact.dto.UserProfileResponse;
import io.marketplace.services.contact.mapper.BeneficiaryAccountDetailsMapper;
import io.marketplace.services.contact.mapper.BeneficiaryDataMapper;
import io.marketplace.services.contact.model.BankAccountSchema;
import io.marketplace.services.contact.model.Beneficiary;
import io.marketplace.services.contact.model.BeneficiaryAccountDetails;
import io.marketplace.services.contact.model.BeneficiaryCreatedData;
import io.marketplace.services.contact.model.BeneficiaryCreatedResponse;
import io.marketplace.services.contact.model.BeneficiaryDeleteRequest;
import io.marketplace.services.contact.model.BeneficiaryDeleteResponse;
import io.marketplace.services.contact.model.BeneficiaryValidation;
import io.marketplace.services.contact.model.CreateBeneficiaryRecord;
import io.marketplace.services.contact.model.ErrorResponse;
import io.marketplace.services.contact.model.LookupBeneficiaryResponse;
import io.marketplace.services.contact.model.ResponseStatus;
import io.marketplace.services.contact.model.UpdateBeneficiaryRecord;
import io.marketplace.services.contact.model.Wallet;
import io.marketplace.services.contact.utils.Constants;
import io.marketplace.services.contact.utils.ErrorCode;
import io.marketplace.services.contact.utils.RestUtils;
import io.marketplace.services.pxchange.client.service.PXChangeServiceClient;

import java.util.Collections;
import org.apache.commons.lang.StringUtils;
import org.apache.kafka.common.errors.ApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Component
public class PaymentServiceAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(PaymentServiceAdapter.class);
    public static final String ALL = "ALL";
    public static final String ACCOUNT_DEACTIVATE_STATUS = "Deactivate";
    private final RestTemplate restTemplate;

    @Value("${membership.server.base-url}")
    public String membershipUrl;

    @Value("${wallet.server.base-url}")
    public String walletUrl;

    @Value("${nad-walletAccountType}")
    public String walletAccountType;

    @Autowired private Gson gsonInstance;

    @Autowired private RestUtils restUtils;

    @Autowired private PXChangeServiceClient pxClient;

    @Autowired private BeneficiaryAccountDetailsMapper beneficiaryAccountDetailsMapper;

    @Autowired private MembershipAdapter membershipAdapter;

    @Autowired private BeneficiaryDataMapper beneficiaryDataMapper;

    @Autowired
    public PaymentServiceAdapter(@Qualifier("gson-rest-template") final RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Retryable(
            value = {InternalServerErrorException.class, ResourceAccessException.class},
            maxAttempts = 2,
            backoff = @Backoff(delay = 5000))
    public List<Beneficiary> getBeneficiaryInformation(
            String baseUrl, AdapterDefinition adapterDefinition) {

        LOG.info("Calling for Get User profile information");
        io.marketplace.services.contact.adapters.dto.UserProfileResponse userProfileResponse;
        String secondaryType = "";
        String secondaryIdentification = "";
        try {
            userProfileResponse = membershipAdapter.getUserProfileInfo();
            if (userProfileResponse != null) {
                pxClient.addEvent(
                        EventMessage.builder()
                                .activityName(
                                        Constants.RECEIVING_THE_REQUEST_TO_GET_BENEFICIARY_ACTIVITY)
                                .eventTitle(Constants.RECEIVING_USER_PROFILE_INFORMATION_GET)
                                .eventCode(Constants.RECV_GET_BEN + Constants.SEQUENCE_INVOKE)
                                .businessData(userProfileResponse)
                                .build());

                io.marketplace.services.contact.adapters.dto.UserProfileResponse.KycDetails
                        kycDetails = userProfileResponse.getData().getKycDetails();
                if (kycDetails != null) {
                    if (adapterDefinition.getSecondaryTypeMapping() == null
                            || !adapterDefinition
                                    .getSecondaryTypeMapping()
                                    .containsKey(kycDetails.getIdType())) {
                        throw ApiResponseException.builder()
                                .code(ErrorCode.ERROR_DONOT_SUPPORT_SECONDARY_TYPE)
                                .message(
                                        String.format(
                                                ErrorCode.ERROR_DONOT_SUPPORT_SECONDARY_TYPE_MSG,
                                                kycDetails.getIdType()))
                                .build();
                    }

                    String secType =
                            adapterDefinition.getSecondaryTypeMapping().get(kycDetails.getIdType());
                    secondaryType = secType;
                    secondaryIdentification = kycDetails.getIdNumber();
                }
            }

        } catch (Exception ex) {
            throw new InternalServerErrorException(
                    GET_USER_PROFILE_ERROR_CODE, ex.getMessage(), null);
        }
        try {

            URI uri = URI.create(baseUrl + "/beneficiaries");

            if (secondaryType != null && !secondaryType.isEmpty()) {
                uri = RestUtils.appendUri(uri, "secondaryType=" + secondaryType);
            }

            if (secondaryIdentification != null && !secondaryIdentification.isEmpty()) {
                uri =
                        RestUtils.appendUri(
                                uri, "secondaryIdentification=" + secondaryIdentification);
            }

            ResponseEntity<BeneficiaryListResponse> response =
                    restTemplate.exchange(
                            uri,
                            HttpMethod.GET,
                            restUtils.getHttpEntity(),
                            BeneficiaryListResponse.class);

            LOG.info("Wallet information GET response {}", gsonInstance.toJson(response));
            final var sndType = secondaryType;
            final var sndId = secondaryIdentification;

            return Optional.ofNullable(response.getBody())
                    .map(beneficiaryDataMapper::getBeneficiaryData)
                    .orElseThrow(
                            () -> {
                                pxClient.addEvent(
                                        EventMessage.builder()
                                                .activityName(
                                                        Constants
                                                                .RECEIVING_THE_REQUEST_TO_GET_BENEFICIARY_ACTIVITY)
                                                .eventTitle(
                                                        Constants.RESPONSE_LIST_OF_BENEFICIARIES)
                                                .eventCode(
                                                        Constants.RECV_GET_BEN
                                                                + Constants.SEQUENCE_RESPONSE)
                                                .eventBusinessId(
                                                        String.format(
                                                                "secondaryType: %s and secondaryIdentification: %s",
                                                                sndType, sndId))
                                                .businessData(response.getBody())
                                                .build());
                                return ApiResponseException.builder()
                                        .httpStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())
                                        .code(ErrorCode.LOOKUP_BENEFICIARY_FAILED_ERROR_CODE)
                                        .message(ErrorCode.LOOKUP_BENEFICIARY_FAILED_ERROR_MESSAGE)
                                        .build();
                            });

        } catch (HttpStatusCodeException ex) {
            if (ex.getStatusCode().equals(HttpStatus.NOT_FOUND)) {
                return new ArrayList<>();
            }

            try {
                ErrorResponse errorResponse =
                        gsonInstance.fromJson(ex.getResponseBodyAsString(), ErrorResponse.class);

                var pspErrors =
                        Optional.ofNullable(errorResponse.getErrors())
                                .orElse(Collections.emptyList()).stream()
                                .filter(e -> e.getCode().startsWith("RJCT"))
                                .map(
                                        e ->
                                                ErrorDto.builder()
                                                        .code(e.getCode())
                                                        .message(e.getMessage())
                                                        .build())
                                .collect(toList());

                LOG.error(
                        ErrorCode.LIST_OF_BENEFICIARY_ERROR_MESSAGE + ex.getMessage(),
                        Error.of(ErrorCode.LIST_OF_BENEFICIARY_ERROR_CODE),
                        ex);

                pxClient.addEvent(
                        EventMessage.builder()
                                .activityName(
                                        Constants.RECEIVING_THE_REQUEST_TO_GET_BENEFICIARY_ACTIVITY)
                                .eventTitle(ErrorCode.LIST_OF_BENEFICIARY_ERROR_MESSAGE)
                                .errorCode(ErrorCode.LIST_OF_BENEFICIARY_ERROR_CODE)
                                .eventCode(Constants.RECV_GET_BEN)
                                .eventStatus(EventStatus.FAILED)
                                .build());

                throw ApiResponseException.builder()
                        .httpStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .code(ErrorCode.LIST_OF_BENEFICIARY_ERROR_CODE)
                        .message(ErrorCode.LIST_OF_BENEFICIARY_ERROR_MESSAGE)
                        .moreErrors(pspErrors)
                        .businessId(
                                String.format(
                                        "secondaryType: %s and secondaryIdentification: %s",
                                        secondaryType, secondaryIdentification))
                        .build();
            } catch (JsonSyntaxException e) {
                LOG.error(ErrorCode.LOOKUP_BENEFICIARY_FAILED_ERROR_MESSAGE);
                throw new InternalServerErrorException(
                        ErrorCode.LOOKUP_BENEFICIARY_FAILED_ERROR_CODE,
                        ErrorCode.LOOKUP_BENEFICIARY_FAILED_ERROR_MESSAGE,
                        null);
            }

        } catch (Exception e) {
            LOG.error(
                    ErrorCode.WALLET_SEARCH_VIA_USER_ERROR_MESSAGE + e.getMessage(),
                    Error.of(WALLET_SEARCH_VIA_USER_ERROR_CODE),
                    e);
            throw new ApiException(ErrorCode.WALLET_SEARCH_VIA_USER_ERROR_MESSAGE, e);
        }
    }

    public LookupBeneficiaryResponse getBeneficiaryByIdentification(
        String baseURl,
        String type,
        String identification,
        String bankCode,
        String debtorName,
        String debtorAccountType,
        String debtorAccountNumber,
        String beneficiaryAccountType,
        String debtorIdNumber) {
        String businessId =
                String.format(
                        Constants.SEARCH_REQUEST_BUSINESS_DATA_BENEFICIARY_BY_IDENTIFICATION,
                        type,
                        identification,
                        bankCode);

        pxClient.addEvent(
                EventMessage.builder()
                        .activityName(
                                Constants
                                        .RECEIVING_THE_REQUEST_TO_LOOKUP_BENEFICIARY_ACCOUNT_ACTIVITY)
                        .eventTitle(Constants.RECEIVING_THE_REQUEST_TO_LOOKUP_BENEFICIARY_ACCOUNT)
                        .eventCode(
                                Constants.EVENT_GET_BENEFICIARY_ACCOUNT
                                        + Constants.SEQUENCE_REQUEST)
                        .eventStatus(EventStatus.SUCCESS)
                        .eventBusinessId(businessId)
                        .build());

        try {
            String url =
                    String.format("%s/%s/%s/%s", baseURl, "beneficiaries", type, identification);
            URI uri = URI.create(url);

            if (bankCode != null && !bankCode.isEmpty()) {
                uri = RestUtils.appendUri(uri, "bankCode=" + bankCode);
            }

            if (StringUtils.isNotEmpty(debtorName) && StringUtils.isNotEmpty(debtorAccountNumber)) {
                uri = RestUtils.appendUri(uri, "debtorName=" + debtorName);
                uri = RestUtils.appendUri(uri, "debtorAccountNumber=" + debtorAccountNumber);
                uri = RestUtils.appendUri(uri, "debtorAccountType=" + debtorAccountType);
            }

            if(StringUtils.isNotEmpty(beneficiaryAccountType)){
                uri = RestUtils.appendUri(uri, "beneficiaryAccountType=" + beneficiaryAccountType);
            }

            if(StringUtils.isNotEmpty(debtorIdNumber)){
                uri = RestUtils.appendUri(uri, "debtorIdNumber=" + debtorIdNumber);
            }

            LOG.info("Calling get beneficiary by identification request url : {} ", url);
            ResponseEntity<LookupBeneficiaryResponse> response =
                    restTemplate.exchange(
                        
                            uri,
                            HttpMethod.GET,
                            restUtils.getHttpEntity(),
                            LookupBeneficiaryResponse.class);

            LOG.info("Beneficiary Account GET response {}", gsonInstance.toJson(response));

            if (response.getBody() != null) {
                pxClient.addEvent(
                        EventMessage.builder()
                                .activityName(
                                        Constants
                                                .RECEIVING_THE_REQUEST_TO_LOOKUP_BENEFICIARY_ACCOUNT_ACTIVITY)
                                .eventTitle(
                                        Constants
                                                .RESPONSE_THE_REQUEST_TO_LOOKUP_BENEFICIARY_ACCOUNT)
                                .eventStatus(EventStatus.SUCCESS)
                                .eventCode(
                                        Constants.EVENT_GET_BENEFICIARY_ACCOUNT
                                                + Constants.SEQUENCE_RESPONSE)
                                .eventBusinessId(businessId)
                                .build());

                return response.getBody();
            }

        } catch (HttpStatusCodeException ex) {
            List<ErrorDto> errorDtos = new ArrayList<>();
            try {
                ErrorResponse errorResponse =
                        gsonInstance.fromJson(ex.getResponseBodyAsString(), ErrorResponse.class);

                for (ResponseStatus er : errorResponse.getErrors()) {
                    errorDtos.add(
                            ErrorDto.builder().code(er.getCode()).message(er.getMessage()).build());
                }
                if (ex.getStatusCode().equals(HttpStatus.BAD_REQUEST)) {
                    LOG.error(
                            errorResponse.getErrors().get(0).getMessage(),
                            Error.of(ErrorCode.INVALID_IDENTIFICATION_ERROR_CODE),
                            ex);

                    pxClient.addEvent(
                            EventMessage.builder()
                                    .activityName(
                                            Constants
                                                    .RECEIVING_THE_REQUEST_TO_LOOKUP_BENEFICIARY_ACCOUNT_ACTIVITY)
                                    .eventTitle(
                                            ErrorCode
                                                    .INVALID__IDENTIFICATION_ERROR_MESSAGE)
                                    .errorCode(
                                            ErrorCode
                                                    .INVALID_IDENTIFICATION_ERROR_CODE)
                                    .eventCode(Constants.EVENT_GET_BENEFICIARY_ACCOUNT)
                                    .eventStatus(EventStatus.FAILED)
                                    .build());

                    throw ApiResponseException.builder()
                            .httpStatus(HttpStatus.BAD_REQUEST.value())
                            .moreErrors(errorDtos)
                            .businessId(businessId)
                            .throwable(null)
                            .build();
                }
                else if (ex.getStatusCode().equals(HttpStatus.NOT_FOUND)) {
                    LOG.error(
                            errorResponse.getErrors().get(0).getMessage(),
                            Error.of(ErrorCode.LOOKUP_BENEFICIARY_VIA_IDENTIFICATION_ERROR_CODE),
                            ex);

                    pxClient.addEvent(
                            EventMessage.builder()
                                    .activityName(
                                            Constants
                                                    .RECEIVING_THE_REQUEST_TO_LOOKUP_BENEFICIARY_ACCOUNT_ACTIVITY)
                                    .eventTitle(
                                            ErrorCode
                                                    .LOOKUP_BENEFICIARY_VIA_IDENTIFICATION_ERROR_MESSAGE)
                                    .errorCode(
                                            ErrorCode
                                                    .LOOKUP_BENEFICIARY_VIA_IDENTIFICATION_ERROR_CODE)
                                    .eventCode(Constants.EVENT_GET_BENEFICIARY_ACCOUNT)
                                    .eventStatus(EventStatus.FAILED)
                                    .build());

                    throw ApiResponseException.builder()
                            .httpStatus(HttpStatus.NOT_FOUND.value())
                            .moreErrors(errorDtos)
                            .businessId(businessId)
                            .throwable(null)
                            .build();
                } else if (ex.getStatusCode().equals(HttpStatus.INTERNAL_SERVER_ERROR)) {
                    LOG.error(
                            ErrorCode.LOOKUP_BENEFICIARY_ACCOUNT_ERROR_MESSAGE + ex.getMessage(),
                            Error.of(ErrorCode.LOOKUP_BENEFICIARY_ACCOUNT_ERROR_CODE),
                            ex);

                    pxClient.addEvent(
                            EventMessage.builder()
                                    .activityName(
                                            Constants
                                                    .RECEIVING_THE_REQUEST_TO_LOOKUP_BENEFICIARY_ACCOUNT_ACTIVITY)
                                    .eventTitle(ErrorCode.LOOKUP_BENEFICIARY_ACCOUNT_ERROR_MESSAGE)
                                    .errorCode(ErrorCode.LOOKUP_BENEFICIARY_ACCOUNT_ERROR_CODE)
                                    .eventCode(Constants.EVENT_GET_BENEFICIARY_ACCOUNT)
                                    .eventStatus(EventStatus.FAILED)
                                    .build());

                    throw ApiResponseException.builder()
                            .httpStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .moreErrors(errorDtos)
                            .businessId(businessId)
                            .throwable(null)
                            .build();
                }

            } catch (JsonSyntaxException e) {
                LOG.error(ErrorCode.LOOKUP_BENEFICIARY_FAILED_ERROR_MESSAGE);
                throw new InternalServerErrorException(
                        ErrorCode.LOOKUP_BENEFICIARY_FAILED_ERROR_CODE,
                        ErrorCode.LOOKUP_BENEFICIARY_FAILED_ERROR_MESSAGE,
                        identification);
            }
        } catch (Exception ex) {
            try {
                ErrorResponse errorResponse =
                        gsonInstance.fromJson(ex.getMessage(), ErrorResponse.class);
                LOG.error(
                        errorResponse.getErrors().get(0).getMessage(),
                        Error.of(ErrorCode.LOOKUP_BENEFICIARY_ACCOUNT_ERROR_CODE),
                        ex);

                pxClient.addEvent(
                        EventMessage.builder()
                                .activityName(
                                        Constants
                                                .RECEIVING_THE_REQUEST_TO_LOOKUP_BENEFICIARY_ACCOUNT_ACTIVITY)
                                .eventTitle(errorResponse.getErrors().get(0).getMessage())
                                .errorCode(ErrorCode.LOOKUP_BENEFICIARY_ACCOUNT_ERROR_CODE)
                                .eventCode(Constants.EVENT_GET_BENEFICIARY_ACCOUNT)
                                .eventStatus(EventStatus.FAILED)
                                .build());

                throw new InternalServerErrorException(
                        ErrorCode.LOOKUP_BENEFICIARY_ACCOUNT_ERROR_CODE,
                        errorResponse.getErrors().get(0).getMessage(),
                        null);
            } catch (JsonSyntaxException e) {
                LOG.error(
                        ErrorCode.LOOKUP_BENEFICIARY_FAILED_ERROR_MESSAGE,
                        Error.of(ErrorCode.LOOKUP_BENEFICIARY_FAILED_ERROR_CODE),
                        e);

                throw new InternalServerErrorException(
                        ErrorCode.LOOKUP_BENEFICIARY_FAILED_ERROR_CODE,
                        ErrorCode.LOOKUP_BENEFICIARY_FAILED_ERROR_MESSAGE,
                        identification);
            }
        }
        return null;
    }

    public BeneficiaryAccountDetails updateBeneficiary(
            String baseURl,
            String type,
            String identification,
            UpdateBeneficiaryRecord updateBeneficiaryRecord) {
        String businessId =
                String.format(
                        Constants.UPDATE_REQUEST_BUSINESS_DATA_BENEFICIARY_BY_IDENTIFICATION,
                        type,
                        identification);

        pxClient.addEvent(
                EventMessage.builder()
                        .activityName(Constants.BENEFICIARY_ACCOUNT_UPDATE_ACTIVITY)
                        .eventTitle(Constants.PAYMENT_ADAPTER_REQUEST_TO_UPDATE_BENEFICIARY_ACCOUNT)
                        .eventCode(
                                Constants.EVENT_UPDATE_BENEFICIARY_ACCOUNT
                                        + Constants.SEQUENCE_INVOKE)
                        .eventStatus(EventStatus.SUCCESS)
                        .eventBusinessId(businessId)
                        .businessData(updateBeneficiaryRecord)
                        .build());

        try {
            String url =
                    String.format("%s/%s/%s/%s", baseURl, "beneficiaries", type, identification);
            URI uri = URI.create(url);

            HttpEntity<UpdateBeneficiaryRecord> reEntity =
                    new HttpEntity<>(
                            updateBeneficiaryRecord, restUtils.getHttpEntity().getHeaders());

            LOG.info("Calling Update beneficiary by identification request url : {} ", url);
            ResponseEntity<BeneficiaryUpdateResponse> response =
                    restTemplate.exchange(
                            uri, HttpMethod.PUT, reEntity, BeneficiaryUpdateResponse.class);

            LOG.info("Beneficiary Account Update response {}", gsonInstance.toJson(response));

            if (response != null) {
                var body = response.getBody();
                if (body != null) {
                    pxClient.addEvent(
                            EventMessage.builder()
                                    .activityName(Constants.BENEFICIARY_ACCOUNT_UPDATE_ACTIVITY)
                                    .eventTitle(
                                            Constants
                                                    .PAYMENT_ADAPTER_RESPONSE_TO_UPDATE_BENEFICIARY_ACCOUNT)
                                    .eventStatus(EventStatus.SUCCESS)
                                    .eventCode(
                                            Constants.EVENT_UPDATE_BENEFICIARY_ACCOUNT
                                                    + Constants.SEQUENCE_INVOKE)
                                    .eventBusinessId(businessId)
                                    .businessData(response.getBody())
                                    .build());

                    return beneficiaryAccountDetailsMapper.responseMapper(body.getData());
                }
            }

        } catch (HttpStatusCodeException ex) {
            List<ErrorDto> errorDtos = new ArrayList<>();
            try {
                ErrorResponse errorResponse =
                        gsonInstance.fromJson(ex.getResponseBodyAsString(), ErrorResponse.class);

                for (ResponseStatus er : errorResponse.getErrors()) {
                    errorDtos.add(
                            ErrorDto.builder().code(er.getCode()).message(er.getMessage()).build());
                }

                if (ex.getStatusCode().equals(HttpStatus.NOT_FOUND)) {
                    LOG.error(
                            errorResponse.getErrors().get(0).getMessage(),
                            Error.of(ErrorCode.UPDATE_BENEFICIARY_VIA_IDENTIFICATION_ERROR_CODE),
                            ex);

                    pxClient.addEvent(
                            EventMessage.builder()
                                    .activityName(Constants.BENEFICIARY_ACCOUNT_UPDATE_ACTIVITY)
                                    .eventTitle(
                                            ErrorCode
                                                    .UPDATE_BENEFICIARY_VIA_IDENTIFICATION_ERROR_MESSAGE)
                                    .errorCode(
                                            ErrorCode
                                                    .UPDATE_BENEFICIARY_VIA_IDENTIFICATION_ERROR_CODE)
                                    .eventCode(Constants.EVENT_UPDATE_BENEFICIARY_ACCOUNT)
                                    .eventStatus(EventStatus.FAILED)
                                    .build());

                    throw ApiResponseException.builder()
                            .httpStatus(HttpStatus.NOT_FOUND.value())
                            .moreErrors(errorDtos)
                            .businessId(businessId)
                            .throwable(null)
                            .build();

                } else if (ex.getStatusCode().equals(HttpStatus.INTERNAL_SERVER_ERROR)) {
                    LOG.error(
                            ErrorCode.UPDATE_BENEFICIARY_ACCOUNT_ERROR_MESSAGE + ex.getMessage(),
                            Error.of(ErrorCode.UPDATE_BENEFICIARY_ACCOUNT_ERROR_CODE),
                            ex);

                    pxClient.addEvent(
                            EventMessage.builder()
                                    .activityName(Constants.BENEFICIARY_ACCOUNT_UPDATE_ACTIVITY)
                                    .eventTitle(ErrorCode.UPDATE_BENEFICIARY_ACCOUNT_ERROR_MESSAGE)
                                    .errorCode(ErrorCode.UPDATE_BENEFICIARY_ACCOUNT_ERROR_CODE)
                                    .eventCode(Constants.EVENT_UPDATE_BENEFICIARY_ACCOUNT)
                                    .eventStatus(EventStatus.FAILED)
                                    .build());

                    throw ApiResponseException.builder()
                            .httpStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .moreErrors(errorDtos)
                            .businessId(businessId)
                            .throwable(null)
                            .build();

                } else if (ex.getStatusCode().equals(HttpStatus.BAD_REQUEST)) {
                    LOG.error(
                            ErrorCode.UPDATE_BENEFICIARY_ACCOUNT_ERROR_MESSAGE + ex.getMessage(),
                            Error.of(ErrorCode.UPDATE_BENEFICIARY_ACCOUNT_ERROR_CODE),
                            ex);

                    pxClient.addEvent(
                            EventMessage.builder()
                                    .activityName(Constants.BENEFICIARY_ACCOUNT_UPDATE_ACTIVITY)
                                    .eventTitle(ErrorCode.UPDATE_BENEFICIARY_ACCOUNT_ERROR_MESSAGE)
                                    .errorCode(
                                            ErrorCode
                                                    .UPDATE_BENEFICIARY_ACCOUNT_BAD_REQUEST_ERROR_CODE)
                                    .eventCode(Constants.EVENT_UPDATE_BENEFICIARY_ACCOUNT)
                                    .eventStatus(EventStatus.FAILED)
                                    .build());

                    throw ApiResponseException.builder()
                            .httpStatus(HttpStatus.BAD_REQUEST.value())
                            .moreErrors(errorDtos)
                            .businessId(businessId)
                            .throwable(null)
                            .build();
                }

            } catch (JsonSyntaxException e) {
                LOG.error("Issue in json payload received");
            }
        } catch (Exception ex) {
            try {
                ErrorResponse errorResponse =
                        gsonInstance.fromJson(ex.getMessage(), ErrorResponse.class);
                LOG.error(
                        errorResponse.getErrors().get(0).getMessage(),
                        Error.of(ErrorCode.UPDATE_BENEFICIARY_ACCOUNT_ERROR_CODE),
                        ex);

                pxClient.addEvent(
                        EventMessage.builder()
                                .activityName(Constants.BENEFICIARY_ACCOUNT_UPDATE_ACTIVITY)
                                .eventTitle(errorResponse.getErrors().get(0).getMessage())
                                .errorCode(ErrorCode.UPDATE_BENEFICIARY_ACCOUNT_ERROR_CODE)
                                .eventCode(Constants.EVENT_UPDATE_BENEFICIARY_ACCOUNT)
                                .eventStatus(EventStatus.FAILED)
                                .build());

                throw new InternalServerErrorException(
                        ErrorCode.UPDATE_BENEFICIARY_ACCOUNT_ERROR_CODE,
                        errorResponse.getErrors().get(0).getMessage(),
                        null);
            } catch (JsonSyntaxException e) {
                LOG.error("Issue in json payload received");
            }
        }

        return null;
    }

    public BeneficiaryDeleteResponse removeBeneficiary(
            AdapterDefinition adapterDefinition, String type, String identification) {
        String businessId = String.format("%s:%s", type, identification);

        BeneficiaryDeleteRequest beneficiaryDeleteRequest =
                BeneficiaryDeleteRequest.builder().status(ACCOUNT_DEACTIVATE_STATUS).build();

        // Validate type
        if (!adapterDefinition.getMainTypeMapping().containsKey(type)) {
            throw new BadRequestException(
                    ErrorCode.ERROR_DONOT_SUPPORT_TYPE,
                    String.format(ErrorCode.ERROR_DONOT_SUPPORT_TYPE_MSG, type),
                    MembershipUtils.getUserId());
        }
        String mainType = adapterDefinition.getMainTypeMapping().get(type);

        // Get secondary information
        UserProfile userProfile = getCurrentUserProfile();
        if (userProfile != null && userProfile.getKycDetails() != null) {
            if (adapterDefinition.getSecondaryTypeMapping() == null
                    || !adapterDefinition
                            .getSecondaryTypeMapping()
                            .containsKey(userProfile.getKycDetails().getIdType())) {
                throw new BadRequestException(
                        ErrorCode.ERROR_DONOT_SUPPORT_SECONDARY_TYPE,
                        String.format(
                                ErrorCode.ERROR_DONOT_SUPPORT_SECONDARY_TYPE_MSG,
                                userProfile.getKycDetails().getIdType()),
                        MembershipUtils.getUserId());
            }

            String secondaryType =
                    adapterDefinition
                            .getSecondaryTypeMapping()
                            .get(userProfile.getKycDetails().getIdType());
            beneficiaryDeleteRequest.setSecondaryType(secondaryType);
            beneficiaryDeleteRequest.setSecondaryIdentification(
                    userProfile.getKycDetails().getIdNumber());
        } else {
            if (userProfile == null) {
                throw ApiResponseException.builder()
                        .code(ErrorCode.ERROR_CANNOT_GET_USER)
                        .message(ErrorCode.ERROR_CANNOT_GET_USER_MSG)
                        .build();
            }
            throw ApiResponseException.builder()
                    .code(ErrorCode.ERROR_USER_DONOT_HAS_KYCDETAIL)
                    .message(ErrorCode.ERROR_USER_DONOT_HAS_KYCDETAIL_MSG)
                    .build();
        }

        // Get payment reference & display name
        Wallet wallet = getWallet();
        if (wallet != null && wallet.getBankAccount() != null) {
            BankAccountSchema bankAccountSchema =
                    Optional.ofNullable(wallet.getBankAccount().getAccount())
                            .orElse(Arrays.asList())
                            .stream()
                            .filter(
                                    x ->
                                            adapterDefinition
                                                    .getPaymentReferenceScheme()
                                                    .equalsIgnoreCase(x.getSchemeName()))
                            .findFirst()
                            .orElse(null);

            if (bankAccountSchema == null
                    || StringUtils.isEmpty(bankAccountSchema.getIdentification())) {
                throw ApiResponseException.builder()
                        .code(ErrorCode.ERROR_CANNOT_FIND_IDENTIFICATION)
                        .message(ErrorCode.ERROR_CANNOT_FIND_IDENTIFICATION_MSG)
                        .build();
            }

            beneficiaryDeleteRequest.setDisplayName(wallet.getBankAccount().getAccountHolderName());
            beneficiaryDeleteRequest.setPaymentReference(bankAccountSchema.getIdentification());
        } else {
            if (wallet == null) {
                throw ApiResponseException.builder()
                        .code(ErrorCode.ERROR_CANNOT_FIND_WALLET)
                        .message(ErrorCode.ERROR_CANNOT_FIND_WALLET_MSG)
                        .build();
            }
            throw ApiResponseException.builder()
                    .code(ErrorCode.ERROR_WALLET_WITHOUT_BANK_INFO)
                    .message(ErrorCode.ERROR_WALLET_WITHOUT_BANK_INFO_MSG)
                    .build();
        }

        // Call delete
        pxClient.addEvent(
                EventMessage.builder()
                        .activityName(Constants.CALL_ADAPTER_TO_REMOVE_BENEFICIARY_ACCOUNT)
                        .eventTitle(Constants.CALL_ADAPTER_TO_REMOVE_BENEFICIARY_ACCOUNT)
                        .eventCode(
                                Constants.CALL_ADAPTER_TO_REMOVE_BENEFICIARY_ACCOUNT_EVENT
                                        + Constants.SEQUENCE_REQUEST)
                        .eventStatus(EventStatus.SUCCESS)
                        .eventBusinessId(businessId)
                        .businessData(beneficiaryDeleteRequest)
                        .build());
        try {
            String adapterApiUrl =
                    String.format(
                            "%s/beneficiaries/%s/%s",
                            adapterDefinition.getBaseEndpoint(), mainType, identification);

            LOG.info("Calling to create new beneficiary, url : {} ", adapterApiUrl);
            HttpEntity<?> rqEntity =
                    new HttpEntity<>(beneficiaryDeleteRequest, generateHttpHeaders());
            ResponseEntity<BeneficiaryDeleteResponse> response =
                    restTemplate.exchange(
                            adapterApiUrl,
                            HttpMethod.PUT,
                            rqEntity,
                            BeneficiaryDeleteResponse.class);

            pxClient.addEvent(
                    EventMessage.builder()
                            .activityName(Constants.CALL_ADAPTER_TO_REMOVE_BENEFICIARY_ACCOUNT)
                            .eventTitle(Constants.CALL_ADAPTER_TO_REMOVE_BENEFICIARY_ACCOUNT)
                            .eventCode(
                                    Constants.CALL_ADAPTER_TO_REMOVE_BENEFICIARY_ACCOUNT_EVENT
                                            + Constants.SEQUENCE_RESPONSE)
                            .eventStatus(EventStatus.SUCCESS)
                            .eventBusinessId(businessId)
                            .businessData(gsonInstance.toJson(response.getBody()))
                            .build());

            return response.getBody();

        } catch (Exception oEx) {
            throw handleException(
                    oEx,
                    ErrorCode.ERROR_REMOVE_BENEFICIARY_ACCOUNT_DETAIL,
                    ErrorCode.ERROR_REMOVE_BENEFICIARY_ACCOUNT_DETAIL_MSG,
                    ErrorCode.ERROR_REMOVE_BENEFICIARY_ACCOUNT,
                    ErrorCode.ERROR_REMOVE_BENEFICIARY_ACCOUNT_MSG);
        }
    }

    public BeneficiaryCreatedData createBeneficiary(
            AdapterDefinition adapterDefinition, CreateBeneficiaryRecord createBeneficiaryRecord) {

        // Validate type
        String type = createBeneficiaryRecord.getType();
        if (!adapterDefinition.getMainTypeMapping().containsKey(type)) {
            throw new BadRequestException(
                    ErrorCode.ERROR_DONOT_SUPPORT_TYPE,
                    String.format(ErrorCode.ERROR_DONOT_SUPPORT_TYPE_MSG, type),
                    MembershipUtils.getUserId());
        }
        createBeneficiaryRecord.setType(adapterDefinition.getMainTypeMapping().get(type));

        // Get secondary information
        UserProfile userProfile = getCurrentUserProfile();
        if (userProfile != null && userProfile.getKycDetails() != null) {
            if (adapterDefinition.getSecondaryTypeMapping() == null
                    || !adapterDefinition
                            .getSecondaryTypeMapping()
                            .containsKey(userProfile.getKycDetails().getIdType())) {
                throw new BadRequestException(
                        ErrorCode.ERROR_DONOT_SUPPORT_SECONDARY_TYPE,
                        String.format(
                                ErrorCode.ERROR_DONOT_SUPPORT_SECONDARY_TYPE_MSG,
                                userProfile.getKycDetails().getIdType()),
                        MembershipUtils.getUserId());
            }

            String secondaryType =
                    adapterDefinition
                            .getSecondaryTypeMapping()
                            .get(userProfile.getKycDetails().getIdType());
            createBeneficiaryRecord.setSecondaryType(secondaryType);
            createBeneficiaryRecord.setSecondaryIdentification(
                    userProfile.getKycDetails().getIdNumber());
        } else {
            if (userProfile == null) {
                throw ApiResponseException.builder()
                        .code(ErrorCode.ERROR_CANNOT_GET_USER)
                        .message(ErrorCode.ERROR_CANNOT_GET_USER_MSG)
                        .build();
            }
            throw ApiResponseException.builder()
                    .code(ErrorCode.ERROR_USER_DONOT_HAS_KYCDETAIL)
                    .message(ErrorCode.ERROR_USER_DONOT_HAS_KYCDETAIL_MSG)
                    .build();
        }

        // Get bank code
        Wallet wallet = getWallet();
        if (wallet != null && wallet.getBankAccount() != null) {
            createBeneficiaryRecord.setBankCode(wallet.getBankAccount().getBankCode());
        } else {
            if (wallet == null) {
                throw ApiResponseException.builder()
                        .code(ErrorCode.ERROR_CANNOT_FIND_WALLET)
                        .message(ErrorCode.ERROR_CANNOT_FIND_WALLET_MSG)
                        .build();
            }
            throw ApiResponseException.builder()
                    .code(ErrorCode.ERROR_WALLET_WITHOUT_BANK_INFO)
                    .message(ErrorCode.ERROR_WALLET_WITHOUT_BANK_INFO_MSG)
                    .build();
        }

        // Call
        String businessId = String.format("%s:%s",
                createBeneficiaryRecord.getType(),
                createBeneficiaryRecord.getIdentification());
        pxClient.addEvent(
                EventMessage.builder()
                        .activityName(Constants.CALL_ADAPTER_TO_CREATE_BENEFICIARY_ACCOUNT)
                        .eventTitle(Constants.CALL_ADAPTER_TO_CREATE_BENEFICIARY_ACCOUNT)
                        .eventCode(
                                Constants.CALL_ADAPTER_TO_CREATE_BENEFICIARY_ACCOUNT_EVENT
                                        + Constants.SEQUENCE_INVOKE)
                        .eventStatus(EventStatus.SUCCESS)
                        .eventBusinessId(businessId)
                        .businessData(gsonInstance.toJson(createBeneficiaryRecord))
                        .build());

        try {
            String url = String.format("%s/beneficiaries", adapterDefinition.getBaseEndpoint());
            URI uri = URI.create(url);

            LOG.info("Calling to create new beneficiary, url : {} ", url);
            HttpEntity<?> rqEntity =
                    new HttpEntity<>(createBeneficiaryRecord, generateHttpHeaders());
            ResponseEntity<BeneficiaryCreatedResponse> response =
                    restTemplate.exchange(
                            uri, HttpMethod.POST, rqEntity, BeneficiaryCreatedResponse.class);

            if (response != null) {
                var body = response.getBody();
                if (body != null) {
                    pxClient.addEvent(
                            EventMessage.builder()
                                    .activityName(
                                            Constants.CALL_ADAPTER_TO_CREATE_BENEFICIARY_ACCOUNT)
                                    .eventTitle(
                                            Constants.CALL_ADAPTER_TO_CREATE_BENEFICIARY_ACCOUNT)
                                    .eventCode(
                                            Constants
                                                            .CALL_ADAPTER_TO_CREATE_BENEFICIARY_ACCOUNT_EVENT
                                                    + Constants.SEQUENCE_RESPONSE)
                                    .eventStatus(EventStatus.SUCCESS)
                                    .eventBusinessId(businessId)
                                    .businessData(gsonInstance.toJson(response.getBody()))
                                    .build());
                    return body.getData();
                }
            }

        } catch (Exception oEx) {
            throw handleException(
                    oEx,
                    ErrorCode.ERROR_CREATE_BENEFICIARY_ACCOUNT_DETAIL,
                    ErrorCode.ERROR_CREATE_BENEFICIARY_ACCOUNT_DETAIL_MSG,
                    ErrorCode.ERROR_CREATE_BENEFICIARY_ACCOUNT,
                    ErrorCode.ERROR_CREATE_BENEFICIARY_ACCOUNT_MSG);
        }
        return null;
    }

    private UserProfile getCurrentUserProfile() {
        String url = String.format("%s/users/me", membershipUrl);
        URI uri = URI.create(url);

        LOG.info("Calling to get user profile, url : {} ", url);
        HttpEntity<?> rqEntity = new HttpEntity<>(generateHttpHeaders());
        ResponseEntity<UserProfileResponse> response =
                restTemplate.exchange(uri, HttpMethod.GET, rqEntity, UserProfileResponse.class);

        LOG.info("User profile response {}", gsonInstance.toJson(response));
        if (response != null) {
            var body = response.getBody();
            if (body != null) {
                return body.getData();
            }
        }
        return null;
    }

    private Wallet getWallet() {
        String url = String.format("%s/wallets?type=%s", walletUrl, walletAccountType);
        URI uri = URI.create(url);

        LOG.info("Calling to get wallet, url : {} ", url);
        HttpEntity<?> rqEntity = new HttpEntity<>(generateHttpHeaders());
        ResponseEntity<WalletListResponse> response =
                restTemplate.exchange(uri, HttpMethod.GET, rqEntity, WalletListResponse.class);

        LOG.info("Wallet response {}", gsonInstance.toJson(response));
        if (response != null) {
            var body = response.getBody();
            if (body != null) {
                return body.getData().stream().findFirst().orElse(null);
            }
        }
        return null;
    }

    private HttpHeaders generateHttpHeaders() {
        final HttpHeaders headers = new HttpHeaders();
        final String internalServiceJWT = MembershipUtils.getJwtToken();
        headers.add("X-JWT-Assertion", internalServiceJWT);
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        return headers;
    }

    private GenericException handleException(
            Exception oEx,
            String detailErrorCode,
            String detailErrorMessage,
            String generalErrorCode,
            String generalErrorMessage) {
        LOG.error("Error occurred at interbank", oEx);

        if (oEx instanceof GenericException) {
            return (GenericException) oEx;
        }

        if (oEx instanceof HttpStatusCodeException) {
            HttpStatusCodeException ex = (HttpStatusCodeException) oEx;
            String body = ex.getResponseBodyAsString() + "";
            LOG.debug("Error Response: {}", body);

            List<ErrorDto> errorDtos = new ArrayList<>();
            try {
                ErrorResponseDto responseDto = gsonInstance.fromJson(body, ErrorResponseDto.class);

                for (ErrorDto er : responseDto.getErrors()) {
                    errorDtos.add(er);
                }

                if (ex.getStatusCode().equals(HttpStatus.NOT_FOUND)) {
                    LOG.error(
                            responseDto.getErrors().get(0).getMessage(),
                            Error.of(detailErrorCode),
                            ex);

                    return ApiResponseException.builder()
                            .httpStatus(HttpStatus.NOT_FOUND.value())
                            .moreErrors(errorDtos)
                            .build();

                } else if (ex.getStatusCode().equals(HttpStatus.INTERNAL_SERVER_ERROR)) {
                    LOG.error(detailErrorMessage + ex.getMessage(), Error.of(detailErrorCode), ex);

                    return ApiResponseException.builder()
                            .httpStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .moreErrors(errorDtos)
                            .build();

                } else if (ex.getStatusCode().equals(HttpStatus.BAD_REQUEST)) {
                    LOG.error(detailErrorMessage + ex.getMessage(), Error.of(detailErrorCode), ex);

                    return ApiResponseException.builder()
                            .httpStatus(HttpStatus.BAD_REQUEST.value())
                            .moreErrors(errorDtos)
                            .build();
                }
            } catch (JsonSyntaxException e) {
                LOG.trace("Response body is not json {}", body, e);
                return new InternalServerErrorException(
                        generalErrorCode, generalErrorMessage, null);
            }
        }
        return new BadRequestException(
                ErrorCode.ERROR_UNKNOWN,
                ErrorCode.ERROR_UNKNOWN_MSG + ":" + oEx.getMessage(),
                MembershipUtils.getUserId());
    }

    public LookupBeneficiaryResponse lookupBeneficiaryByQrCode(
            String baseURl,
            BeneficiaryValidation request,
            String debtorName,
            String debtorAccountNumber) {
        String businessId = String.format("QR Code: %s", request.getQrRawData());
        pxClient.addEvent(
                EventMessage.builder()
                        .activityName(
                                Constants.RECEIVING_THE_REQUEST_TO_LOOKUP_BENEFICIARY_QR_ACTIVITY)
                        .eventTitle(Constants.PROCESS_THE_REQUEST_TO_LOOKUP_BENEFICIARY_QR)
                        .eventCode(Constants.EVENT_GET_BENEFICIARY_QR + Constants.SEQUENCE_INVOKE)
                        .eventStatus(EventStatus.SUCCESS)
                        .eventBusinessId(businessId)
                        .build());
        BeneficiaryValidationDto beneficiaryValidationDto =
                BeneficiaryValidationDto.builder()
                        .qrRawData(request.getQrRawData())
                        .debtorName(debtorName)
                        .debtorAccount(debtorAccountNumber)
                        .build();
        try {
            String url = String.format("%s/beneficiaries/QR/validations", baseURl);
            URI uri = URI.create(url);

            LOG.info("Calling to lookup beneficiary by QR code, url : {} ", url);
            HttpEntity<?> rqEntity =
                    new HttpEntity<>(beneficiaryValidationDto, generateHttpHeaders());
            ResponseEntity<LookupBeneficiaryResponse> response =
                    restTemplate.exchange(
                            uri, HttpMethod.POST, rqEntity, LookupBeneficiaryResponse.class);

            LOG.info("Beneficiary QR POST response {}", gsonInstance.toJson(response));

            if (response.getBody() != null) {
                pxClient.addEvent(
                        EventMessage.builder()
                                .activityName(
                                        Constants
                                                .RECEIVING_THE_REQUEST_TO_LOOKUP_BENEFICIARY_QR_ACTIVITY)
                                .eventTitle(Constants.RESPONSE_THE_REQUEST_TO_LOOKUP_BENEFICIARY_QR)
                                .eventStatus(EventStatus.SUCCESS)
                                .eventCode(
                                        Constants.EVENT_GET_BENEFICIARY_QR
                                                + Constants.SEQUENCE_RESPONSE)
                                .eventBusinessId(businessId)
                                .build());

                return response.getBody();
            }
        } catch (HttpStatusCodeException ex) {
            List<ErrorDto> errorDtos = new ArrayList<>();
            try {
                ErrorResponse errorResponse =
                        gsonInstance.fromJson(ex.getResponseBodyAsString(), ErrorResponse.class);

                if (ex.getStatusCode().equals(HttpStatus.NOT_FOUND)) {
                    LOG.error(
                            errorResponse.getErrors().get(0).getMessage(),
                            Error.of(ErrorCode.LOOKUP_BENEFICIARY_VIA_QR_ERROR_CODE),
                            ex);

                    pxClient.addEvent(
                            EventMessage.builder()
                                    .activityName(
                                            Constants
                                                    .RECEIVING_THE_REQUEST_TO_LOOKUP_BENEFICIARY_QR_ACTIVITY)
                                    .eventTitle(ErrorCode.LOOKUP_BENEFICIARY_VIA_QR_ERROR_MESSAGE)
                                    .errorCode(ErrorCode.LOOKUP_BENEFICIARY_VIA_QR_ERROR_CODE)
                                    .eventCode(Constants.EVENT_GET_BENEFICIARY_QR)
                                    .eventStatus(EventStatus.FAILED)
                                    .build());

                    errorDtos.add(
                            ErrorDto.builder()
                                    .code(ErrorCode.LOOKUP_BENEFICIARY_VIA_QR_ERROR_CODE)
                                    .message(ErrorCode.LOOKUP_BENEFICIARY_VIA_QR_ERROR_MESSAGE)
                                    .build());

                    throw ApiResponseException.builder()
                            .httpStatus(HttpStatus.NOT_FOUND.value())
                            .moreErrors(addRppReturnCodes(errorResponse,errorDtos))
                            .businessId(businessId)
                            .build();
                } else if (ex.getStatusCode().equals(HttpStatus.BAD_REQUEST)) {
                    LOG.error(
                            ErrorCode.QR_INVALID_ERROR_MESSAGE + ex.getMessage(),
                            Error.of(ErrorCode.QR_INVALID_ERROR_CODE),
                            ex);

                    pxClient.addEvent(
                            EventMessage.builder()
                                    .activityName(
                                            Constants
                                                    .RECEIVING_THE_REQUEST_TO_LOOKUP_BENEFICIARY_QR_ACTIVITY)
                                    .eventTitle(ErrorCode.QR_INVALID_ERROR_MESSAGE)
                                    .errorCode(ErrorCode.QR_INVALID_ERROR_CODE)
                                    .eventCode(Constants.EVENT_GET_BENEFICIARY_QR)
                                    .eventStatus(EventStatus.FAILED)
                                    .build());

                    errorDtos.add(
                            ErrorDto.builder()
                                    .code(ErrorCode.QR_INVALID_ERROR_CODE)
                                    .message(ErrorCode.QR_INVALID_ERROR_MESSAGE)
                                    .build());

                    throw ApiResponseException.builder()
                            .httpStatus(HttpStatus.BAD_REQUEST.value())
                            .moreErrors(addRppReturnCodes(errorResponse,errorDtos))
                            .businessId(businessId)
                            .build();
                }else if (ex.getStatusCode().equals(HttpStatus.INTERNAL_SERVER_ERROR)) {
                    LOG.error(
                            ErrorCode.LOOKUP_BENEFICIARY_QR_ERROR_MESSAGE + ex.getMessage(),
                            Error.of(ErrorCode.LOOKUP_BENEFICIARY_QR_ERROR_CODE),
                            ex);

                    pxClient.addEvent(
                            EventMessage.builder()
                                    .activityName(
                                            Constants
                                                    .RECEIVING_THE_REQUEST_TO_LOOKUP_BENEFICIARY_QR_ACTIVITY)
                                    .eventTitle(ErrorCode.LOOKUP_BENEFICIARY_QR_ERROR_MESSAGE)
                                    .errorCode(ErrorCode.LOOKUP_BENEFICIARY_QR_ERROR_CODE)
                                    .eventCode(Constants.EVENT_GET_BENEFICIARY_QR)
                                    .eventStatus(EventStatus.FAILED)
                                    .build());

                    errorDtos.add(
                            ErrorDto.builder()
                                    .code(ErrorCode.LOOKUP_BENEFICIARY_QR_ERROR_CODE)
                                    .message(ErrorCode.LOOKUP_BENEFICIARY_QR_ERROR_MESSAGE)
                                    .build());

                    throw ApiResponseException.builder()
                            .httpStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .moreErrors(addRppReturnCodes(errorResponse,errorDtos))
                            .businessId(businessId)
                            .build();
                }

            } catch (JsonSyntaxException e) {
                LOG.error(ErrorCode.LOOKUP_BENEFICIARY_FAILED_ERROR_MESSAGE);
                throw new InternalServerErrorException(
                        ErrorCode.LOOKUP_BENEFICIARY_FAILED_ERROR_CODE,
                        ErrorCode.LOOKUP_BENEFICIARY_FAILED_ERROR_MESSAGE,
                        request.getQrRawData());
            }
        } catch (Exception ex) {
            try {
                ErrorResponse errorResponse =
                        gsonInstance.fromJson(ex.getMessage(), ErrorResponse.class);
                LOG.error(
                        errorResponse.getErrors().get(0).getMessage(),
                        Error.of(ErrorCode.LOOKUP_BENEFICIARY_QR_ERROR_CODE),
                        ex);

                pxClient.addEvent(
                        EventMessage.builder()
                                .activityName(
                                        Constants
                                                .RECEIVING_THE_REQUEST_TO_LOOKUP_BENEFICIARY_QR_ACTIVITY)
                                .eventTitle(errorResponse.getErrors().get(0).getMessage())
                                .errorCode(ErrorCode.LOOKUP_BENEFICIARY_QR_ERROR_CODE)
                                .eventCode(Constants.EVENT_GET_BENEFICIARY_QR)
                                .eventStatus(EventStatus.FAILED)
                                .build());

                throw new InternalServerErrorException(
                        ErrorCode.LOOKUP_BENEFICIARY_QR_ERROR_CODE,
                        errorResponse.getErrors().get(0).getMessage(),
                        null);
            } catch (JsonSyntaxException e) {
                LOG.error(
                        ErrorCode.LOOKUP_BENEFICIARY_FAILED_ERROR_MESSAGE,
                        Error.of(ErrorCode.LOOKUP_BENEFICIARY_FAILED_ERROR_CODE),
                        e);

                throw new InternalServerErrorException(
                        ErrorCode.LOOKUP_BENEFICIARY_FAILED_ERROR_CODE,
                        ErrorCode.LOOKUP_BENEFICIARY_FAILED_ERROR_MESSAGE,
                        request.getQrRawData());
            }
        }
        return null;
    }
    
    private List<ErrorDto> addRppReturnCodes(ErrorResponse errorResponse,List<ErrorDto> errorList){
    	if(errorResponse.getErrors().size() > 1) {
    		ResponseStatus responseStatus = errorResponse.getErrors().get(1);
			errorList.add(
                    ErrorDto.builder().code(responseStatus.getCode()).message(responseStatus.getMessage()).build());
    		
    	}
    	return errorList;
    }
}

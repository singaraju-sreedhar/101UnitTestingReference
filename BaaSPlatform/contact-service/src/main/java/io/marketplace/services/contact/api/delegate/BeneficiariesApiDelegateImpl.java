package io.marketplace.services.contact.api.delegate;

import static io.marketplace.services.contact.utils.Constants.*;

import io.marketplace.commons.model.event.EventMessage;
import io.marketplace.commons.model.event.EventStatus;
import io.marketplace.services.contact.api.BeneficiariesApiDelegate;

import io.marketplace.services.contact.service.BeneficiaryService;
import io.marketplace.services.contact.service.ContactService;
import io.marketplace.services.contact.service.MockBeneficiaryService;
import io.marketplace.services.contact.utils.Constants;
import io.marketplace.services.pxchange.client.annotation.PXLogEventMessage;
import io.marketplace.services.pxchange.client.service.PXChangeServiceClient;
import io.marketplace.services.contact.model.WalletResponse;
import io.marketplace.services.contact.model.LookupBeneficiaryAccountResponse;
import io.marketplace.services.contact.model.LookupBeneficiaryResponse;
import io.marketplace.services.contact.model.Beneficiary;
import io.marketplace.services.contact.model.BeneficiaryAccountDetails;
import io.marketplace.services.contact.model.BeneficiaryDeleteResponse;
import io.marketplace.services.contact.model.BeneficiaryValidation;
import io.marketplace.services.contact.model.BeneficiaryCreatedData;
import io.marketplace.services.contact.model.UpdateBeneficiaryRecord;
import io.marketplace.services.contact.model.CreateBeneficiaryRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BeneficiariesApiDelegateImpl implements BeneficiariesApiDelegate {

    @Autowired
    private ContactService contactService;

    @Autowired
    private BeneficiaryService beneficiaryService;

    @Autowired
    private MockBeneficiaryService mockBeneficiaryService;

    @Autowired
    private PXChangeServiceClient pxClient;

    @Value("${mock.enabled:false}")
    private String mockEnabled;

    @PXLogEventMessage(
            activityName = Constants.RECEIVING_THE_REQUEST_TO_GET_BENEFICIARY_ACTIVITY,
            eventCode = Constants.RECV_GET_BEN_REQUEST,
            eventTitle = Constants.RECEIVING_THE_REQUEST_TO_GET_BENEFICIARY,
            businessIdName = {SEARCH_REQUEST_BUSINESS_DATA_BENEFICIARY},
            businessIdIndex = {0, 1, 2})
    @Override
    public ResponseEntity<WalletResponse> lookupBeneficiary(
            String mobileNumber, String accountNumber, String accountType) {

        List<Beneficiary> beneficiaryDtoList =
                contactService.getBeneficiaryInformationWithRateLimit(
                        mobileNumber, accountNumber, accountType);

        return ResponseEntity.ok(WalletResponse.builder().data(beneficiaryDtoList).build());
    }

    @PXLogEventMessage(
            activityName = Constants.RECEIVING_THE_REQUEST_TO_LOOKUP_BENEFICIARY_ACCOUNT_ACTIVITY,
            eventCode = Constants.EVENT_GET_BENEFICIARY_ACCOUNT,
            eventTitle = Constants.RECEIVING_THE_REQUEST_TO_LOOKUP_BENEFICIARY_ACCOUNT,
            businessIdName = {SEARCH_REQUEST_BUSINESS_DATA_BENEFICIARY_BY_IDENTIFICATION},
            businessIdIndex = {0, 1})
    @Override
    public ResponseEntity<LookupBeneficiaryResponse> lookupBeneficiaryAccount(
            String type, String identification, String bankCode, String walletId, String beneficiaryAccountType) {

        return ResponseEntity.ok(
                beneficiaryService.lookupBeneficiaryByIdentification(
                        type, identification, bankCode, walletId, beneficiaryAccountType));
    }

    @PXLogEventMessage(
            activityName = Constants.BENEFICIARY_ACCOUNT_UPDATE_ACTIVITY,
            eventCode = Constants.EVENT_UPDATE_BENEFICIARY_ACCOUNT + Constants.SEQUENCE_REQUEST,
            eventTitle = Constants.RECEIVING_THE_REQUEST_TO_UPDATE_BENEFICIARY_ACCOUNT,
            businessIdName = {UPDATE_REQUEST_BUSINESS_DATA_BENEFICIARY_BY_IDENTIFICATION},
            businessIdIndex = {0, 1})
    @Override
    public ResponseEntity<BeneficiaryAccountDetails> updateBeneficiary(
            String type, String identification, UpdateBeneficiaryRecord updateBeneficiaryRecord) {
        if ("true".equals(mockEnabled)) {
            return new ResponseEntity<BeneficiaryAccountDetails>(
                    mockBeneficiaryService.updateBeneficiary(
                            type, identification, updateBeneficiaryRecord),
                    HttpStatus.OK);
        }
        BeneficiaryAccountDetails response =
                beneficiaryService.updateBeneficiary(type, identification, updateBeneficiaryRecord);
        return ResponseEntity.ok().body(response);
    }

    @PXLogEventMessage(
            activityName = Constants.REMOVE_BENEFICIARY_ACCOUNT_ACTIVITY,
            eventCode = Constants.EVENT_REMOVE_BENEFICIARY_ACCOUNT,
            eventTitle = Constants.RECEIVING_THE_REQUEST_TO_REMOVE_BENEFICIARY_ACCOUNT,
            businessIdIndex = {0, 1})
    @Override
    public ResponseEntity<BeneficiaryDeleteResponse> deleteBeneficiary(
            String type, String identification) {
        BeneficiaryDeleteResponse response = beneficiaryService.deleteBeneficiary(type, identification);

        pxClient.addEvent(
                EventMessage.builder()
                        .activityName(Constants.REMOVE_BENEFICIARY_ACCOUNT_ACTIVITY)
                        .eventTitle(Constants.RECEIVING_THE_REQUEST_TO_REMOVE_BENEFICIARY_ACCOUNT)
                        .eventCode(
                                Constants.EVENT_REMOVE_BENEFICIARY_ACCOUNT
                                        + Constants.SEQUENCE_RESPONSE)
                        .eventStatus(EventStatus.SUCCESS)
                        .eventBusinessId(String.format("%s:%s", type, identification))
                        .businessData(response)
                        .build());

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PXLogEventMessage(
            activityName = Constants.CREATE_BENEFICIARY_ACCOUNT_ACTIVITY,
            eventCode = Constants.CREATE_BENEFICIARY_ACCOUNT_EVENT,
            eventTitle = Constants.CREATE_BENEFICIARY_ACCOUNT_REQUEST_MSG,
            businessIdIndex = {0})
    @Override
    public ResponseEntity<BeneficiaryCreatedData> createBeneficiary(CreateBeneficiaryRecord createBeneficiaryRecord) {


        BeneficiaryCreatedData response = beneficiaryService.createBeneficiary(createBeneficiaryRecord);

        pxClient.addEvent(
                EventMessage.builder()
                        .activityName(Constants.CREATE_BENEFICIARY_ACCOUNT_ACTIVITY)
                        .eventTitle(Constants.CREATE_BENEFICIARY_ACCOUNT_RESPONSE_MSG)
                        .eventCode(
                                Constants.CREATE_BENEFICIARY_ACCOUNT_EVENT
                                        + Constants.SEQUENCE_RESPONSE)
                        .eventStatus(EventStatus.SUCCESS)
                        .eventBusinessId(String.format("%s:%s",
                                createBeneficiaryRecord.getType(),
                                createBeneficiaryRecord.getIdentification()))
                        .businessData(response)
                        .build());

        return new ResponseEntity<>(response, HttpStatus.OK);

    }

    @PXLogEventMessage(
            activityName = Constants.RECEIVING_THE_REQUEST_TO_LOOKUP_BENEFICIARY_QR_ACTIVITY,
            eventCode = Constants.EVENT_GET_BENEFICIARY_QR,
            eventTitle = Constants.RECEIVING_THE_REQUEST_TO_LOOKUP_BENEFICIARY_QR,
            businessIdIndex = {0})
    @Override
    public ResponseEntity<LookupBeneficiaryResponse> lookupBeneficiaryByQrCode(BeneficiaryValidation request) {

        return new ResponseEntity<LookupBeneficiaryResponse>(
                beneficiaryService.lookupBeneficiaryByQrCode(request),
                HttpStatus.OK);
    }
}

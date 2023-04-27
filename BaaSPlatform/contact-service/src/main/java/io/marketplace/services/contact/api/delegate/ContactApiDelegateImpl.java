package io.marketplace.services.contact.api.delegate;

import io.marketplace.commons.utils.StringUtils;
import io.marketplace.services.contact.api.ContactsApiDelegate;
import io.marketplace.services.contact.model.Beneficiary;
import io.marketplace.services.contact.model.BeneficiaryRecord;
import io.marketplace.services.contact.model.BeneficiaryResponse;
import io.marketplace.services.contact.model.LookupBeneficiaryResponse;
import io.marketplace.services.contact.service.ContactService;
import io.marketplace.services.contact.utils.Constants;
import io.marketplace.services.pxchange.client.annotation.PXLogEventMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static io.marketplace.services.contact.utils.Constants.DELETE_REQUEST_BUSINESS_DATA;
import static io.marketplace.services.contact.utils.Constants.SAVE_REQUEST_BUSINESS_DATA;
import static io.marketplace.services.contact.utils.Constants.SEARCH_REQUEST_BUSINESS_DATA;

@Service
public class ContactApiDelegateImpl implements ContactsApiDelegate {

    @Autowired
    private ContactService contactService;

    @PXLogEventMessage(
            activityName = Constants.RECEIVING_THE_REQUEST_TO_GET_ACTIVITY,
            eventCode = Constants.RECV_GET_REQUEST,
            eventTitle = Constants.RECEIVING_THE_REQUEST_TO_GET,
            businessIdName = {SEARCH_REQUEST_BUSINESS_DATA},
            businessIdIndex = {0, 1}
    )
    @Override
    public ResponseEntity<BeneficiaryResponse> getContactList(String userId,
                                                              String searchText,
                                                              String bankCode,
                                                              String listOrders,
                                                              Integer pageSize,
                                                              Integer pageNum) {

        BeneficiaryResponse beneficiaryResponse =
                contactService.getContactList(userId,
                        searchText, bankCode, pageSize, pageNum,
                        StringUtils.stringToList(listOrders));

        return ResponseEntity.ok(beneficiaryResponse);
    }

    @PXLogEventMessage(
            activityName = Constants.RECEIVING_THE_REQUEST_TO_SAVE_ACTIVITY,
            eventCode = Constants.RECV_SAVE_REQUEST,
            eventTitle = Constants.RECEIVING_THE_REQUEST_TO_SAVE_CONTACT,
            businessIdName = {SAVE_REQUEST_BUSINESS_DATA},
            businessIdIndex = {0}
    )
    @Override
    public ResponseEntity<LookupBeneficiaryResponse> createContact(BeneficiaryRecord beneficiaryRecord) {

        return ResponseEntity.ok(LookupBeneficiaryResponse
                .builder()
                .data(contactService.createContact(beneficiaryRecord))
                .build());
    }

    @PXLogEventMessage(
            activityName = Constants.RECEIVING_THE_REQUEST_TO_DELETE_ACTIVITY,
            eventCode = Constants.RECV_DELETE_REQUEST,
            eventTitle = Constants.RECEIVING_THE_REQUEST_TO_DELETE_ACTIVITY,
            businessIdName = {DELETE_REQUEST_BUSINESS_DATA},
            businessIdIndex = {0}
    )
    @Override
    public ResponseEntity<BeneficiaryResponse> deleteContactById(String contactId) {

        Beneficiary beneficiaryData = contactService.deleteContact(contactId);
        List<Beneficiary> beneficiaryDataList = new ArrayList<>();
        beneficiaryDataList.add(beneficiaryData);

        return ResponseEntity.noContent().build();
    }
}

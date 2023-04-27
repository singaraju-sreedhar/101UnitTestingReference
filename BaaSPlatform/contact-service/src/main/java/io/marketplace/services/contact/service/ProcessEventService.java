package io.marketplace.services.contact.service;

import io.marketplace.commons.logging.Logger;
import io.marketplace.commons.logging.LoggerFactory;
import io.marketplace.services.contact.model.BeneficiaryRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProcessEventService {

    private static final Logger LOG = LoggerFactory.getLogger(ProcessEventService.class);

    @Autowired
    private ContactService contactService;

    public void process(BeneficiaryRecord beneficiaryRecord) {

        LOG.info("BeneficiaryRecord from the kafka topic {}", beneficiaryRecord);

        contactService.createContact(beneficiaryRecord);

    }
}

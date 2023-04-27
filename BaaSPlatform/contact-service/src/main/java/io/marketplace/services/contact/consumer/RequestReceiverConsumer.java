package io.marketplace.services.contact.consumer;

import io.marketplace.commons.exception.InternalServerErrorException;
import io.marketplace.commons.logging.Error;
import io.marketplace.commons.logging.Logger;
import io.marketplace.commons.logging.LoggerFactory;
import io.marketplace.commons.model.event.AsyncRequestScopeAttributes;
import io.marketplace.commons.model.event.EventMessage;
import io.marketplace.commons.util.EventMessageUtil;
import io.marketplace.commons.utils.ThreadContextUtils;
import io.marketplace.services.contact.model.BeneficiaryRecord;
import io.marketplace.services.contact.service.ProcessEventService;
import io.marketplace.services.contact.utils.Constants;
import io.marketplace.services.contact.utils.ErrorCode;
import io.marketplace.services.pxchange.client.service.PXChangeServiceClient;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;

import static io.marketplace.services.contact.utils.Constants.RECEIVING_THE_REQUEST_TO_SAVE_CONTACT;

@Service
public class RequestReceiverConsumer {

    private static final Logger LOG = LoggerFactory.getLogger(RequestReceiverConsumer.class);

    @Autowired
    private ProcessEventService processEventService;

    @Autowired
    private EventMessageUtil eventMessageUtil;

    @Autowired
    private PXChangeServiceClient pxClient;

    @Value("${spring.application.name:}")
    protected String applicationName;

    @KafkaListener(topics = "${kafka.consumer.contact-request-topic:payment-contact-data-changed}",
            groupId = "${kafka.consumer.contact-data-changed.group-id:contact-service}",
            containerFactory = "kafkaListenerContainerFactory")
    public void listenRequest(@Payload String message,
                              @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                              @Header(KafkaHeaders.RECEIVED_PARTITION_ID) String partition) {
        LOG.info("received request from topic:'{}', partition: '{}'", topic, partition);

        try {

            RequestContextHolder.setRequestAttributes(new AsyncRequestScopeAttributes());
            MDC.put("eventTraceId", ThreadContextUtils.getCustomRequest().getRequestId());

            EventMessage<BeneficiaryRecord> eventMessage = eventMessageUtil.fromQueueMessage(message,
                    BeneficiaryRecord.class);

            EventMessage<BeneficiaryRecord> event = EventMessage.<BeneficiaryRecord>builder()
                    .activityName(RECEIVING_THE_REQUEST_TO_SAVE_CONTACT)
                    .eventCode(Constants.RECV_SAVE_REQUEST)
                    .eventTitle(RECEIVING_THE_REQUEST_TO_SAVE_CONTACT)
                    .businessData(eventMessage.getBusinessData())
                    .eventBusinessId(
                            String.format("Account Number: %s", eventMessage.getBusinessData().getAccountNumber()))
                    .eventSource(applicationName)
                    .eventTraceId(ThreadContextUtils.getCustomRequest().getRequestId())
                    .build();
            pxClient.addEvent(event);

            processEventService.process(eventMessage.getBusinessData());
            LOG.info("event message requestId ='{}'", eventMessage.getEventId());
        } catch (Exception ex) {
            LOG.error(ErrorCode.KAFKA_CONSUMER_ERROR_MESSAGE + topic, Error.of(ErrorCode.KAFKA_CONSUMER_ERROR_CODE));
            throw new InternalServerErrorException("Process request fail for topic: " + topic, ex);
        } finally {
            RequestContextHolder.resetRequestAttributes();
        }
    }
}

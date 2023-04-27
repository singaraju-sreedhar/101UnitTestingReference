package io.marketplace.services.contact.utils;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.marketplace.commons.exception.InternalServerErrorException;
import io.marketplace.commons.logging.Logger;
import io.marketplace.commons.logging.LoggerFactory;
import io.marketplace.commons.utils.StringUtils;
import io.marketplace.commons.utils.ThreadContextUtils;
import io.marketplace.services.contact.config.AdapterConfiguration;
import io.marketplace.services.contact.config.AdapterDefinition;

@Component
public class AdapterUtils {
	private static final Logger log = LoggerFactory.getLogger(AdapterUtils.class);

	private static final String ADAPTER_KEY_TEMPLATE = "%s-%s";
	private static final String DEFAULT_ENTITY_ID = "SYSTEM";
	private static final String DEFAULT_APP_ID = "SYSTEM";

	@Autowired
	private AdapterConfiguration adapterMapping;

	public AdapterDefinition getAdapterDefinition() {
		return getAdapterDefinition(ThreadContextUtils.getCustomRequest().getEntityId(),
				ThreadContextUtils.getCustomRequest().getAppId());
	}

	public String constructAdapterKey(final String entityId, final String appId) {
		String myEntityId = StringUtils.isEmpty(entityId) ? DEFAULT_ENTITY_ID : entityId;
		String myappId = StringUtils.isEmpty(appId) ? DEFAULT_APP_ID : appId;
		return String.format(ADAPTER_KEY_TEMPLATE, myEntityId, myappId);
	}

	public AdapterDefinition getAdapterDefinition(final String entityId, final String appId) {
		List<AdapterDefinition> mappingConfigs = adapterMapping.getMappings();
		final String specificKey = constructAdapterKey(entityId, appId);
		log.info("getAdapterDefinition key : {}", specificKey);
		if (mappingConfigs == null) {
			throw new InternalServerErrorException(ErrorCode.INVALID_MAPPING_CONFIG,
					String.format(ErrorCode.INVALID_MAPPING_MESSAGE, entityId, appId), specificKey);
		}
		return mappingConfigs.stream().filter(e -> e.getAdapterId().equals(specificKey)).findFirst().orElse(null);

	}
}

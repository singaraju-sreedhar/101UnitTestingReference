package io.marketplace.services.contact.adapters;

import static io.marketplace.services.contact.utils.ErrorCode.GET_USER_PROFILE_ERROR_CODE;
import static io.marketplace.services.contact.utils.ErrorCode.USER_GET_VIA_MOBILE_ERROR_CODE;

import com.google.gson.Gson;

import io.marketplace.commons.exception.InternalServerErrorException;
import io.marketplace.commons.logging.Error;
import io.marketplace.commons.logging.Logger;
import io.marketplace.commons.logging.LoggerFactory;
import io.marketplace.commons.model.event.EventMessage;
import io.marketplace.commons.utils.MembershipUtils;
import io.marketplace.services.contact.adapters.dto.UserListResponse;
import io.marketplace.services.contact.adapters.dto.UserProfileResponse;
import io.marketplace.services.contact.utils.Constants;
import io.marketplace.services.contact.utils.ErrorCode;
import io.marketplace.services.contact.utils.RestUtils;
import io.marketplace.services.pxchange.client.service.PXChangeServiceClient;

import org.apache.kafka.common.errors.ApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

@Component
public class MembershipAdapter {
    private static final Logger LOG = LoggerFactory.getLogger(MembershipAdapter.class);
    private final RestTemplate restTemplate;

    @Autowired private Gson gsonInstance;

    @Value("${membership.server.base-url:http://membership-service:8080}")
    private String membershipServiceUrl;

    @Autowired private PXChangeServiceClient pxClient;

    @Autowired private RestUtils restUtils;

    @Value("${jwt.header-name:X-JWT-Assertion}")
    private String jwtHeaderName;

    @Autowired
    public MembershipAdapter(@Qualifier("gson-rest-template") final RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Retryable(
            value = {InternalServerErrorException.class, ResourceAccessException.class},
            maxAttempts = 2,
            backoff = @Backoff(delay = 5000))
    public UserListResponse getUserInformation(String mobileNumber) {
        try {

            URI uri = URI.create(membershipServiceUrl + "/admin/users");

            if (mobileNumber != null && !mobileNumber.isEmpty()) {
                uri = restUtils.appendUri(uri, "mobileNumber=" + mobileNumber);
            }

            ResponseEntity<UserListResponse> response =
                    restTemplate.exchange(
                            uri, HttpMethod.GET, restUtils.getHttpEntity(), UserListResponse.class);

            LOG.info("User information GET response {}", gsonInstance.toJson(response));

            if (response.getBody() != null) {

                // Generate event for response
                pxClient.addEvent(
                        EventMessage.builder()
                                .activityName(
                                        Constants.RECEIVING_THE_REQUEST_TO_GET_BENEFICIARY_ACTIVITY)
                                .eventTitle(Constants.RECEIVING_USER_INFORMATION_GET)
                                .eventCode(Constants.RECV_GET_BEN_REQUEST)
                                .eventBusinessId("Mobile Number : " + mobileNumber)
                                .build());

                return response.getBody();
            }

        } catch (Exception ex) {
            LOG.error(
                    ErrorCode.USER_GET_VIA_MOBILE_ERROR_CODE_MESSAGE + ex.getMessage(),
                    Error.of(USER_GET_VIA_MOBILE_ERROR_CODE),
                    ex);
            throw new ApiException(ErrorCode.USER_GET_VIA_MOBILE_ERROR_CODE_MESSAGE, ex);
        }
        return null;
    }

    @Retryable(
            value = {InternalServerErrorException.class, ResourceAccessException.class},
            maxAttempts = 2,
            backoff = @Backoff(delay = 5000))
    public UserProfileResponse getUserProfileInfo() {
        try {

            String jwtToken = MembershipUtils.getJwtToken();

            HttpHeaders headers = new HttpHeaders();
            headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
            headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
            headers.add(jwtHeaderName, jwtToken);

            URI uri = URI.create(membershipServiceUrl + "/users/me");

            ResponseEntity<UserProfileResponse> response =
                    restTemplate.exchange(
                            uri,
                            HttpMethod.GET,
                            new HttpEntity<>("", headers),
                            UserProfileResponse.class);

            LOG.info("User profile information GET response {}", gsonInstance.toJson(response));

            if (response.getBody() != null) {
                return response.getBody();
            }

        } catch (Exception ex) {
            LOG.error(
                    ErrorCode.GET_USER_PROFILE_ERROR_MESSAGE + ex.getMessage(),
                    Error.of(GET_USER_PROFILE_ERROR_CODE),
                    ex);
            throw new ApiException(ErrorCode.GET_USER_PROFILE_ERROR_MESSAGE, ex);
        }
        return null;
    }
}

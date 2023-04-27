package io.marketplace.services.contact.utils;

import io.marketplace.commons.jwt.JWTFactory;
import io.marketplace.commons.logging.Logger;
import io.marketplace.commons.logging.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;

import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;

@Component
public class RestUtils {

    @Value("${jwt.default-membership-id:}")
    private String membershipAdminUser;

    private static final Logger LOG = LoggerFactory.getLogger(RestUtils.class);

    private static final String CONTENT_TYPE_HEADER_NAME = "Content-Type";
    private static final String AUTHORIZATION_HEADER_NAME = "X-JWT-Assertion";

    @Autowired
    @Qualifier("internal-jwt-factory")
    private JWTFactory jwtFactory;

    public <T> HttpEntity<T> getHttpEntity() {
        HttpHeaders headers = new HttpHeaders();

        LOG.trace("JWT token {}", getJWTToken());

        headers.set(AUTHORIZATION_HEADER_NAME, getJWTToken());
        headers.set(CONTENT_TYPE_HEADER_NAME, APPLICATION_JSON_VALUE);

        LOG.info(headers.toString());
        return new HttpEntity<>(headers);
    }

    public <T> HttpEntity<T> getHttpEntity(T obj) {
        HttpHeaders headers = new HttpHeaders();

        LOG.trace("JWT token {}", getJWTToken());

        headers.set(AUTHORIZATION_HEADER_NAME, getJWTToken());
        headers.set(CONTENT_TYPE_HEADER_NAME, APPLICATION_JSON_VALUE);

        LOG.info(headers.toString());
        return new HttpEntity<>(obj, headers);
    }


    private String getJWTToken() {
        return jwtFactory.generateUserToken(membershipAdminUser,
                Collections.singletonList(Constants.SUPER_ROLE));
    }


    public static URI appendUri(URI uri, String appendQuery) throws URISyntaxException {

        String newQuery = uri.getQuery();
        if (newQuery == null) {
            newQuery = appendQuery;
        } else {
            newQuery += "&" + appendQuery;
        }

        return new URI(uri.getScheme(), uri.getAuthority(),
                uri.getPath(), newQuery, uri.getFragment());
    }
}

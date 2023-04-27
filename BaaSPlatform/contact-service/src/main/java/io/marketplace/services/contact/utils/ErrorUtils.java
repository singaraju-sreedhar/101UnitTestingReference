package io.marketplace.services.contact.utils;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import io.marketplace.commons.exception.ApiResponseException;
import io.marketplace.commons.logging.Logger;
import io.marketplace.commons.logging.LoggerFactory;
import io.marketplace.commons.model.dto.ErrorDto;
import io.marketplace.commons.model.dto.ErrorResponseDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Component
public class ErrorUtils {
    private static final Logger log = LoggerFactory.getLogger(ErrorUtils.class);

    @Autowired
    private Gson gson;

    public ErrorDto errorStatus(HttpStatusCodeException ex) {
        return  Optional.ofNullable(getError(ex))
                .map(ErrorResponseDto::getErrors)
                .map(List::stream).flatMap(Stream::findFirst)
                .orElse(ErrorDto.builder()
                        .code("031.01.500.07")
                        .message("Invalid response")
                        .build());

    }

    @SuppressWarnings("Duplicates")
    public ErrorResponseDto getError(HttpStatusCodeException ex) {
        String body = ex.getResponseBodyAsString() + "";
        log.warn("Error response status {}", ex.getRawStatusCode());
        try {
            return gson.fromJson(body, ErrorResponseDto.class);
        } catch (JsonSyntaxException e) {
            log.warn("Response body is not json {}", body, e);
            return ErrorResponseDto.builder()
                    .errors(Collections.singletonList(ErrorDto.builder()
                            .code("031.01.500.07")
                            .message(body)
                            .build()))
                    .build();

        }

    }

    public String serviceErrorCode(String adapterErrorCode) {
        return adapterErrorCode;
    }

    @SuppressWarnings("Duplicates")
    public ApiResponseException handleError(HttpStatusCodeException ex, String businessId) {
        ErrorDto errorStatus = errorStatus(ex);
        return ApiResponseException.builder()
                .httpStatus(ex.getRawStatusCode())
                .businessId(businessId)
                .message(errorStatus.getMessage())
                .systemCode(errorStatus.getCode())
                .code(serviceErrorCode(errorStatus.getCode()))
                .throwable(ex)
                .build();
    }
}

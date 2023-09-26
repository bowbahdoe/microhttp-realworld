package dev.mccue.microhttp.realworld;

import org.microhttp.Response;

import java.util.List;

public final class ValidationException
        extends RuntimeException
        implements IntoResponse {
    private final IntoResponse intoResponse;
    ValidationException(Throwable e) {
        super(e);
        this.intoResponse = Responses.validationError(List.of(e.getMessage()));
    }

    @Override
    public Response intoResponse() {
        return this.intoResponse.intoResponse();
    }
}
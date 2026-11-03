package es.codeurjc.students.trainfyre.users.infrastructure.adapter.in.web.response;

import es.codeurjc.students.trainfyre.security.domain.AccessToken;

public record TokenResponse(String accessToken, String tokenType, long expiresIn) {

    private static final String BEARER = "Bearer";

    public static TokenResponse from(AccessToken token) {
        return new TokenResponse(token.value(), BEARER, token.lifetime().toSeconds());
    }

    @Override
    public String toString() {
        return "TokenResponse[accessToken=****, tokenType=" + tokenType + ", expiresIn=" + expiresIn + "]";
    }
}

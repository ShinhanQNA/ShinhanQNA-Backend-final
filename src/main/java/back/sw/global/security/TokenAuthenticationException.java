package back.sw.global.security;

import back.sw.global.exception.ServiceException;

public class TokenAuthenticationException extends ServiceException {
    private final TokenErrorType tokenErrorType;

    public TokenAuthenticationException(TokenErrorType tokenErrorType, String message) {
        super("401-1", message);
        this.tokenErrorType = tokenErrorType;
    }

    public TokenErrorType tokenErrorType() {
        return tokenErrorType;
    }

    public enum TokenErrorType {
        EXPIRED,
        INVALID
    }
}

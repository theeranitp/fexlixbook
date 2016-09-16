package com.packtpub.felix.bookshelf.service.api;

public interface Authentication {
    String login(String username, char[] password)
throws InvalidCredentialsException;
void logout(String sessionId) throws SessionNotValidRuntimeException;
boolean sessionIsValid(String sessionId);
}

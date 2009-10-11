package com.markupartist.iglaset.provider;

public class AuthenticationException extends Exception {
    private static final long serialVersionUID = 2086046363538541795L;

    AuthenticationException(String message) {
        super(message);
    }
}

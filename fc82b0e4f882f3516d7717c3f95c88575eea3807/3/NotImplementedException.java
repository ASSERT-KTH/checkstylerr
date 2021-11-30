package com.piksel.sequoia.clientsdk.exception;

import com.piksel.sequoia.annotations.PublicEvolving;

/**
 * Exception raised when a method from an interface has not been implemented
 */
@PublicEvolving
public class NotImplementedException extends RuntimeException {
    private static final long serialVersionUID = -6110581981604234856L;
}

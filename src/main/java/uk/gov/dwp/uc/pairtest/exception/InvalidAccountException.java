package uk.gov.dwp.uc.pairtest.exception;

public class InvalidAccountException extends RuntimeException{
    public InvalidAccountException(String message) {
        super(message);
    }
}

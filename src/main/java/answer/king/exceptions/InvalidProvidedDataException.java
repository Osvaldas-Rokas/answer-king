package answer.king.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidProvidedDataException extends RuntimeException {
    public InvalidProvidedDataException(String msg) {
        super("Invalid data provided. Could not proceed. Message: " + msg);
    }
}

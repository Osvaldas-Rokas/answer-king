package answer.king.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
class ItemNotFoundException extends RuntimeException {

    public ItemNotFoundException(String msg) {
        super("Could not find item. Message: '" + msg + "'.");
    }
}

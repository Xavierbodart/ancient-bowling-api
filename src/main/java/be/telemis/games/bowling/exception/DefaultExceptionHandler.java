package be.telemis.games.bowling.exception;

import be.telemis.games.bowling.model.base.ErrorMessage;
import be.telemis.games.bowling.model.base.ResultObject;
import jakarta.persistence.EntityNotFoundException;
import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@RestControllerAdvice
public class DefaultExceptionHandler {
    private final String appName = "Error management";

    private static final Logger logger = LoggerFactory.getLogger(DefaultExceptionHandler.class);

    @ExceptionHandler({Exception.class})
    public ResponseEntity<ResultObject<ErrorMessage>> handleException(Exception ex) {
        logger.error("Technical exception:", ex);
        final List<ErrorMessage> errorMessages = Collections.singletonList(new ErrorMessage(ex.getMessage(), "500"));
        return new ResponseEntity<>(new ResultObject<>(this.appName, false, null, errorMessages),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler({NoSuchElementException.class, EntityNotFoundException.class,
            EmptyResultDataAccessException.class})
    public ResponseEntity<ResultObject<ErrorMessage>> handleResourceNotFoundException(Exception ex) {
        logger.error("Resource not found exception", ex);
        final List<ErrorMessage> errorMessages =
                Collections.singletonList(new ErrorMessage("Resource not found: " + ex.getMessage(), "404"));
        return new ResponseEntity<>(new ResultObject<>(this.appName, false, null, errorMessages), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, DataIntegrityViolationException.class,
            ConstraintViolationException.class, IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<ResultObject<ErrorMessage>> handleBadRequests(Exception ex) {
        logger.error("Bad request exception", ex);
        final List<ErrorMessage> errorMessages =
                Collections.singletonList(new ErrorMessage("Bad request: " + ex.getMessage(), "400"));
        return new ResponseEntity<>(new ResultObject<>(this.appName, false, null, errorMessages),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResultObject<ErrorMessage>> notValid(MethodArgumentNotValidException ex) {
        logger.error("Argument object not valid", ex);
        List<ErrorMessage> errorMessages = ex.getAllErrors().stream().map(error -> new ErrorMessage("Object not " +
                "valid: " + error.getDefaultMessage(), "400")).collect(Collectors.toList());
        return new ResponseEntity<>(new ResultObject<>(this.appName, false, null, errorMessages),
                HttpStatus.BAD_REQUEST);
    }

}
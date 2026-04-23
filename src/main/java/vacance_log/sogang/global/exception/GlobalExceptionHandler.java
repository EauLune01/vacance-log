package vacance_log.sogang.global.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import vacance_log.sogang.global.dto.response.ApiResponse;
import vacance_log.sogang.global.exception.diary.DiaryNotFoundException;
import vacance_log.sogang.global.exception.embedding.EmbeddingFailedException;
import vacance_log.sogang.global.exception.image.InvalidImageUrlException;
import vacance_log.sogang.global.exception.photo.PhotoNotFoundException;
import vacance_log.sogang.global.exception.photoPlace.PhotoPlaceNotFoundException;
import vacance_log.sogang.global.exception.room.RoomCapacityExceededException;
import vacance_log.sogang.global.exception.room.RoomNotFoundException;
import vacance_log.sogang.global.exception.room.TravelAlreadyFinishedException;
import vacance_log.sogang.global.exception.user.UserNotFoundException;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<ApiResponse<?>> handleValidationExceptions(MethodArgumentNotValidException e) {
        String errorMessage = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .collect(Collectors.joining(", "));

        return buildErrorResponse(HttpStatus.BAD_REQUEST, errorMessage);
    }

    @ExceptionHandler(RoomCapacityExceededException.class)
    protected ResponseEntity<ApiResponse<?>> handleRoomCapacityExceededException(RoomCapacityExceededException e) {
        return buildErrorResponse(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(RoomNotFoundException.class)
    protected ResponseEntity<ApiResponse<?>> handleRoomNotFoundException(RoomNotFoundException e) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(UserNotFoundException.class)
    protected ResponseEntity<ApiResponse<?>> handleUserNotFoundException(UserNotFoundException e) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(PhotoPlaceNotFoundException.class)
    protected ResponseEntity<ApiResponse<?>> handlePhotoPlaceNotFoundException(PhotoPlaceNotFoundException e) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(PhotoNotFoundException.class)
    protected ResponseEntity<ApiResponse<?>> handlePhotoNotFoundException(PhotoNotFoundException e) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(DiaryNotFoundException.class)
    protected ResponseEntity<ApiResponse<?>> handleDiaryNotFoundException(DiaryNotFoundException e) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(InvalidImageUrlException.class)
    protected ResponseEntity<ApiResponse<?>> handleInvalidImageUrlException(InvalidImageUrlException e) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(TravelAlreadyFinishedException.class)
    protected ResponseEntity<ApiResponse<?>> handleTravelFinishedException(TravelAlreadyFinishedException e) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(EmbeddingFailedException.class)
    protected ResponseEntity<ApiResponse<?>> handleEmbeddingFailedException(EmbeddingFailedException e) {
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    protected ResponseEntity<ApiResponse<?>> handleException(Exception e) {
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다: " + e.getMessage());
    }

    private ResponseEntity<ApiResponse<?>> buildErrorResponse(HttpStatus status, String message) {
        ApiResponse<?> response = new ApiResponse<>(false, status.value(), message);
        return ResponseEntity.status(status).body(response);
    }

    private <T> ResponseEntity<ApiResponse<T>> buildErrorResponse(HttpStatus status, String message, T data) {
        ApiResponse<T> response = new ApiResponse<>(false, status.value(), message, data);
        return ResponseEntity.status(status).body(response);
    }
}

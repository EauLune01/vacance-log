package vacance_log.sogang.global.exception.room;

public class TravelAlreadyFinishedException extends RuntimeException {
    public TravelAlreadyFinishedException(String message) {
        super(message);
    }
}

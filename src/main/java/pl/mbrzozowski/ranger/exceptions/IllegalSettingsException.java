package pl.mbrzozowski.ranger.exceptions;

public class IllegalSettingsException extends RuntimeException {

    public IllegalSettingsException() {
        super();
    }

    public IllegalSettingsException(String message) {
        super(message);
    }
}

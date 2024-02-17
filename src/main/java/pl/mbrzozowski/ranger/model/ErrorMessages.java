package pl.mbrzozowski.ranger.model;

public enum ErrorMessages {
    NO_PERMISSIONS("Brak uprawnień!"),
    UNKNOWN_EXCEPTIONS("Wystąpił nieoczekiwany błąd. Spróbuj ponownie lub zgłoś problem!");

    private final String message;

    ErrorMessages(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}

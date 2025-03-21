package pl.mbrzozowski.ranger.exceptions;

public class ContentNotFoundException extends Exception {
    public ContentNotFoundException(String key) {
        super("Content not found for  key:  " + key);
    }
}
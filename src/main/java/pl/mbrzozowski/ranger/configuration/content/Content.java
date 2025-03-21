package pl.mbrzozowski.ranger.configuration.content;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class Content {
    private String message;
    private String title;
    private String description;
    private List<Field> fields;
    private List<Button> buttons;
}

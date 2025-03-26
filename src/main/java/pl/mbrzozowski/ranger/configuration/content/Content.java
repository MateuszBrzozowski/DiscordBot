package pl.mbrzozowski.ranger.configuration.content;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString
public class Content {
    private String message = "";
    private String url = "";
    private String title = "";
    private String description = "";
    private List<Field> fields = new ArrayList<>();
    private List<Button> buttons = new ArrayList<>();
}

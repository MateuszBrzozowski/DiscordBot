package pl.mbrzozowski.ranger.configuration.content;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Field {
    private String name;
    private String value;
    private boolean inline;
}

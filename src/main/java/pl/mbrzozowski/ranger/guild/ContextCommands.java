package pl.mbrzozowski.ranger.guild;

public enum ContextCommands {
    REPUTATION("+Rep");

    private final String name;

    ContextCommands(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}

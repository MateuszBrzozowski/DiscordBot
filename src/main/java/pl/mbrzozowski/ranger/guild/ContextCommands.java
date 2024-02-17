package pl.mbrzozowski.ranger.guild;

public enum ContextCommands {
    REPUTATION("+Rep"),
    RECRUIT_OPINION("Rekrut - opinia");

    private final String name;

    ContextCommands(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}

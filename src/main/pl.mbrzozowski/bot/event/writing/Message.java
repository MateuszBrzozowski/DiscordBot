package bot.event.writing;

public class Message {

    private String[] words;
    private String contentDisplay;
    private boolean admin;
    private boolean clanMember;

    public Message(String[] words, String contentDisplay, boolean admin, boolean clanMember) {
        this.words = words;
        this.contentDisplay = contentDisplay;
        this.admin = admin;
        this.clanMember = clanMember;
    }

    public String[] getWords() {
        return words;
    }

    public String getContentDisplay() {
        return contentDisplay;
    }

    public boolean isAdmin() {
        return admin;
    }

    public boolean isClanMember() {
        return clanMember;
    }
}

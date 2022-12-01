package pl.mbrzozowski.ranger.helpers;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class StringModifyTest {

    @Test
    public void removeDiscordMarkdowns_RemoveItalicsUnderscoreOneTime() {
        String string = "name_";
        String result = StringModify.removeDiscordMarkdowns(string);
        Assertions.assertEquals("name\\_", result);
    }

    @Test
    public void removeDiscordMarkdowns_RemoveItalicsUnderscoreTwoTimes() {
        String string = "_name_";
        String result = StringModify.removeDiscordMarkdowns(string);
        Assertions.assertEquals("\\_name\\_", result);
    }

    @Test
    public void removeDiscordMarkdowns_RemoveItalicsUnderscoreTwoTimesNextToEachOther() {
        String string = "name__";
        String result = StringModify.removeDiscordMarkdowns(string);
        Assertions.assertEquals("name\\_\\_", result);
    }

    @Test
    public void removeDiscordMarkdowns_RemoveItalicsStarOnce() {
        String string = "name*";
        String result = StringModify.removeDiscordMarkdowns(string);
        Assertions.assertEquals("name\\*", result);
    }

    @Test
    public void removeDiscordMarkdowns_RemoveStarTwoTimes() {
        String string = "*name*";
        String result = StringModify.removeDiscordMarkdowns(string);
        Assertions.assertEquals("\\*name\\*", result);
    }

    @Test
    public void removeDiscordMarkdowns_RemoveStarTwoTimesNextToEachOther() {
        String string = "name**";
        String result = StringModify.removeDiscordMarkdowns(string);
        Assertions.assertEquals("name\\*\\*", result);
    }

    @Test
    public void removeDiscordMarkdowns_RemoveStarFourTimesNextToEachOther() {
        String string = "**name**";
        String result = StringModify.removeDiscordMarkdowns(string);
        Assertions.assertEquals("\\*\\*name\\*\\*", result);
    }

    @Test
    public void removeDiscordMarkdowns_RemoveGreaterThan() {
        String string = ">name";
        String result = StringModify.removeDiscordMarkdowns(string);
        Assertions.assertEquals("\\>name", result);
    }

    @Test
    public void removeDiscordMarkdowns_RemoveGreaterThanThreeNextTo() {
        String string = ">>>name";
        String result = StringModify.removeDiscordMarkdowns(string);
        System.out.println("\\>>>name");
        Assertions.assertEquals("\\>>>name", result);
    }

    @Test
    public void removeClanTag_RemoveRangersPL() {
        String string = "name<RangersPL>";
        String result = StringModify.removeClanTag(string);
        Assertions.assertEquals("name", result);
    }

    @Test
    public void removeClanTag_RemoveRRangersPL() {
        String string = "name<rRangersPL>";
        String result = StringModify.removeClanTag(string);
        Assertions.assertEquals("name", result);
    }

}
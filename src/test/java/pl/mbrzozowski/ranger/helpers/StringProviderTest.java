package pl.mbrzozowski.ranger.helpers;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import pl.mbrzozowski.ranger.event.Event;
import pl.mbrzozowski.ranger.event.EventFor;
import pl.mbrzozowski.ranger.event.EventRequest;
import pl.mbrzozowski.ranger.response.EmbedSettings;

import java.time.LocalDateTime;

class StringProviderTest {

    @Test
    public void removeDiscordMarkdowns_RemoveItalicsUnderscoreOneTime() {
        String string = "name_";
        String result = StringProvider.removeDiscordMarkdowns(string);
        Assertions.assertEquals("name\\_", result);
    }

    @Test
    public void removeDiscordMarkdowns_RemoveItalicsUnderscoreTwoTimes() {
        String string = "_name_";
        String result = StringProvider.removeDiscordMarkdowns(string);
        Assertions.assertEquals("\\_name\\_", result);
    }

    @Test
    public void removeDiscordMarkdowns_RemoveItalicsUnderscoreTwoTimesNextToEachOther() {
        String string = "name__";
        String result = StringProvider.removeDiscordMarkdowns(string);
        Assertions.assertEquals("name\\_\\_", result);
    }

    @Test
    public void removeDiscordMarkdowns_RemoveItalicsStarOnce() {
        String string = "name*";
        String result = StringProvider.removeDiscordMarkdowns(string);
        Assertions.assertEquals("name\\*", result);
    }

    @Test
    public void removeDiscordMarkdowns_RemoveStarTwoTimes() {
        String string = "*name*";
        String result = StringProvider.removeDiscordMarkdowns(string);
        Assertions.assertEquals("\\*name\\*", result);
    }

    @Test
    public void removeDiscordMarkdowns_RemoveStarTwoTimesNextToEachOther() {
        String string = "name**";
        String result = StringProvider.removeDiscordMarkdowns(string);
        Assertions.assertEquals("name\\*\\*", result);
    }

    @Test
    public void removeDiscordMarkdowns_RemoveStarFourTimesNextToEachOther() {
        String string = "**name**";
        String result = StringProvider.removeDiscordMarkdowns(string);
        Assertions.assertEquals("\\*\\*name\\*\\*", result);
    }

    @Test
    public void removeDiscordMarkdowns_RemoveGreaterThan() {
        String string = ">name";
        String result = StringProvider.removeDiscordMarkdowns(string);
        Assertions.assertEquals("\\>name", result);
    }

    @Test
    public void removeDiscordMarkdowns_RemoveGreaterThanThreeNextTo() {
        String string = ">>>name";
        String result = StringProvider.removeDiscordMarkdowns(string);
        Assertions.assertEquals("\\>>>name", result);
    }

    @ParameterizedTest
    @ValueSource(strings = {"name<RangersPL>", "name<rRangersPL>", "name<RANGERSPL>", "name<rangerspl>", "name<RaNgErSpL>"})
    public void removeClanTag_RemovedRangersPL(String source) {
        String result = StringProvider.removeClanTag(source);
        Assertions.assertEquals("name", result);
    }

    @Test
    void getChannelName_NameWithBrain() {
        EventRequest eventRequest = new EventRequest();
        eventRequest.setEventFor(EventFor.TACTICAL_GROUP);
        eventRequest.setName("event");
        LocalDateTime dateTime = LocalDateTime.of(2023, 1, 1, 0, 0);
        eventRequest.setDateTime(dateTime);
        String excepted = EmbedSettings.BRAIN_WITH_GREEN + "event-1.1.2023-00:00";
        String result = StringProvider.getChannelName(eventRequest);
        Assertions.assertEquals(excepted, result);
    }

    @Test
    void getChannelName_NameWithoutBrain() {
        EventRequest eventRequest = new EventRequest();
        eventRequest.setEventFor(EventFor.CLAN_MEMBER);
        eventRequest.setName("event");
        LocalDateTime dateTime = LocalDateTime.of(2023, 1, 1, 0, 0);
        eventRequest.setDateTime(dateTime);
        String excepted = EmbedSettings.GREEN_CIRCLE + "event-1.1.2023-00:00";
        String result = StringProvider.getChannelName(eventRequest);
        Assertions.assertEquals(excepted, result);
    }

    @Test
    void getChannelName_ToLongName_CutTo100Length() {
        EventRequest eventRequest = new EventRequest();
        eventRequest.setEventFor(EventFor.CLAN_MEMBER_AND_RECRUIT);
        eventRequest.setName("0123456789".repeat(10));
        LocalDateTime dateTime = LocalDateTime.of(2023, 1, 1, 0, 0);
        eventRequest.setDateTime(dateTime);
        String excepted = (EmbedSettings.GREEN_CIRCLE + "0123456789".repeat(10) + "-1.1.2023-00:00").substring(0, 100);
        String result = StringProvider.getChannelName(eventRequest);
        Assertions.assertEquals(excepted, result);
    }

    @Test
    void getMessageForEventList_EventForClanMember() {
        EventRequest eventRequest = new EventRequest();
        eventRequest.setEventFor(EventFor.CLAN_MEMBER);
        String excepted = "<@&" + RoleID.CLAN_MEMBER_ID + "> Zapisy!";
        String message = StringProvider.getMessageForEventList(eventRequest);
        Assertions.assertEquals(excepted, message);
    }

    @Test
    void getMessageForEventList_EventForRecruit() {
        EventRequest eventRequest = new EventRequest();
        eventRequest.setEventFor(EventFor.RECRUIT);
        String excepted = "<@&" + RoleID.RECRUIT_ID + "> Zapisy!";
        String message = StringProvider.getMessageForEventList(eventRequest);
        Assertions.assertEquals(excepted, message);
    }

    @Test
    void getMessageForEventList_EventForClanMemberAndRecruit() {
        EventRequest eventRequest = new EventRequest();
        eventRequest.setEventFor(EventFor.CLAN_MEMBER_AND_RECRUIT);
        String excepted = "<@&" + RoleID.CLAN_MEMBER_ID + "> <@&" + RoleID.RECRUIT_ID + "> Zapisy!";
        String message = StringProvider.getMessageForEventList(eventRequest);
        Assertions.assertEquals(excepted, message);
    }

    @Test
    void getMessageForEventList_EventForTacticalGroup() {
        EventRequest eventRequest = new EventRequest();
        eventRequest.setEventFor(EventFor.TACTICAL_GROUP);
        String excepted = "<@&" + RoleID.TACTICAL_GROUP + "> Tactical meeting!";
        String message = StringProvider.getMessageForEventList(eventRequest);
        Assertions.assertEquals(excepted, message);
    }

    @Test
    void getMessageForEventList_EventForNull() {
        EventRequest eventRequest = new EventRequest();
        eventRequest.setEventFor(null);
        String message = StringProvider.getMessageForEventList(eventRequest);
        Assertions.assertEquals("", message);
    }

    @Test
    void removeAnyPrefixCircle_RemoveGreenCircle() {
        String channelName = EmbedSettings.GREEN_CIRCLE + "name";
        String result = StringProvider.removeAnyPrefixCircle(channelName);
        Assertions.assertEquals("name", result);
    }

    @Test
    void removeAnyPrefixCircle_RemoveYellowCircle() {
        String channelName = EmbedSettings.YELLOW_CIRCLE + "name";
        String result = StringProvider.removeAnyPrefixCircle(channelName);
        Assertions.assertEquals("name", result);
    }

    @Test
    void removeAnyPrefixCircle_RemoveRedCircle() {
        String channelName = EmbedSettings.RED_CIRCLE + "name";
        String result = StringProvider.removeAnyPrefixCircle(channelName);
        Assertions.assertEquals("name", result);
    }

    @Test
    void removeAnyPrefixCircle_RemoveGreenCircleWithBrain() {
        String channelName = EmbedSettings.BRAIN_WITH_GREEN + "name";
        String result = StringProvider.removeAnyPrefixCircle(channelName);
        Assertions.assertEquals("name", result);
    }

    @Test
    void removeAnyPrefixCircle_RemoveYellowCircleWithBrain() {
        String channelName = EmbedSettings.BRAIN_WITH_YELLOW + "name";
        String result = StringProvider.removeAnyPrefixCircle(channelName);
        Assertions.assertEquals("name", result);
    }

    @Test
    void addYellowCircleBeforeText_AddOnlyCircleForClanMember() {
        String result = StringProvider.addYellowCircleBeforeText("text", EventFor.CLAN_MEMBER);
        Assertions.assertEquals(EmbedSettings.YELLOW_CIRCLE + "text", result);
    }

    @Test
    void addYellowCircleBeforeText_AddOnlyCircleForClanMemberAndRecruit() {
        String result = StringProvider.addYellowCircleBeforeText("text", EventFor.CLAN_MEMBER_AND_RECRUIT);
        Assertions.assertEquals(EmbedSettings.YELLOW_CIRCLE + "text", result);
    }

    @Test
    void addYellowCircleBeforeText_AddOnlyCircleForRecruitAndNullText() {
        String result = StringProvider.addYellowCircleBeforeText(null, EventFor.RECRUIT);
        Assertions.assertEquals(EmbedSettings.YELLOW_CIRCLE + "", result);
    }

    @Test
    void addYellowCircleBeforeText_AddCircleWithBrainForTacticalGroup() {
        String result = StringProvider.addYellowCircleBeforeText("text", EventFor.TACTICAL_GROUP);
        Assertions.assertEquals(EmbedSettings.BRAIN_WITH_YELLOW + "text", result);
    }

    @Test
    void getChannelNameWithGreenCircle() {
        Event event = Event.builder().name("name").build();
        String dateTime = "1.1.2023 23:59";
        String result = StringProvider.getChannelNameWithGreenCircle(event, dateTime);
        Assertions.assertEquals(EmbedSettings.GREEN_CIRCLE + event.getName() + dateTime, result);
    }

    @Test
    void getChannelNameWithGreenCircle_ToLongName_CutTo100Length() {
        Event event = Event.builder().name("0123456789".repeat(10)).build();
        String dateTime = "1.1.2023 23:59";
        String result = StringProvider.getChannelNameWithGreenCircle(event, dateTime);
        Assertions.assertEquals((EmbedSettings.GREEN_CIRCLE + event.getName() + dateTime).substring(0, 100), result);
    }

    @Test
    void getChannelNameWithGreenCircle_NullEventName_ReturnOnlyCircleAndDate() {
        Event event = new Event();
        String dateTime = "1.1.2023 23:59";
        String result = StringProvider.getChannelNameWithGreenCircle(event, dateTime);
        Assertions.assertEquals(EmbedSettings.GREEN_CIRCLE + dateTime, result);
    }

    @Test
    void getChannelNameWithGreenCircle_NullDateTime_ReturnOnlyCircleAndName() {
        Event event = Event.builder().name("name").build();
        String result = StringProvider.getChannelNameWithGreenCircle(event, null);
        Assertions.assertEquals(EmbedSettings.GREEN_CIRCLE + "name", result);
    }

    @Test
    void getChannelNameWithGreenCircle_NullDateTimeAndEventName_ReturnOnlyCircle() {
        Event event = Event.builder().build();
        String result = StringProvider.getChannelNameWithGreenCircle(event, null);
        Assertions.assertEquals(EmbedSettings.GREEN_CIRCLE, result);
    }

    @Test
    void getStringOfEventDateTime_Correct_ReturnString() {
        LocalDateTime dateTime = LocalDateTime.of(2023, 1, 1, 23, 59);
        Event event = Event.builder().date(dateTime).build();
        String result = StringProvider.getStringOfEventDateTime(event);
        String excepted = "1.01.2023 23:59";
        Assertions.assertEquals(excepted, result);
    }

    @Test
    void getStringOfEventDateTime_NullDateTime_ReturnEmptyString() {
        Event event = Event.builder().build();
        String result = StringProvider.getStringOfEventDateTime(event);
        String excepted = "";
        Assertions.assertEquals(excepted, result);
    }
}
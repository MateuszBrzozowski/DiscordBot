package pl.mbrzozowski.ranger.games.birthday;

import net.dv8tion.jda.api.entities.Member;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import pl.mbrzozowski.ranger.guild.RangersGuild;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

class BirthdayProviderTest {

    @Test
    void getToday_NullProviderList_ReturnEmptyList() {
        Assertions.assertArrayEquals(new ArrayList<>().toArray(), BirthdayProvider.getListWithToday(null).toArray());
    }

    @Test
    void getToday_EmptyList_ReturnEmptyList() {
        Assertions.assertArrayEquals(new ArrayList<>().toArray(), BirthdayProvider.getListWithToday(new ArrayList<>()).toArray());
    }

    @Test
    void getToday_OneToday_ReturnListWithThisOne() {
        Birthday birthday = Birthday.builder().userId("1").userName("name").date(LocalDate.now()).build();
        ArrayList<Birthday> expected = new ArrayList<>(List.of(birthday));
        ArrayList<Birthday> toCheck = new ArrayList<>(List.of(birthday));
        Assertions.assertArrayEquals(expected.toArray(), BirthdayProvider.getListWithToday(toCheck).toArray());
    }

    @Test
    void getToday_OneTomorrow_ReturnEmptyList() {
        Birthday birthday = Birthday.builder().userId("1").userName("name").date(LocalDate.now().plusDays(1)).build();
        ArrayList<Birthday> toCheck = new ArrayList<>(List.of(birthday));
        Assertions.assertArrayEquals(new ArrayList<>().toArray(), BirthdayProvider.getListWithToday(toCheck).toArray());
    }

    @Test
    void getToday_OneYesterday_ReturnEmptyList() {
        Birthday birthday = Birthday.builder().userId("1").userName("name").date(LocalDate.now().minusDays(1)).build();
        ArrayList<Birthday> toCheck = new ArrayList<>(List.of(birthday));
        Assertions.assertArrayEquals(new ArrayList<>().toArray(), BirthdayProvider.getListWithToday(toCheck).toArray());
    }

    @Test
    void getToday_OneYesterdayOneTodayOneTomorrow_ReturnListWithOneElement() {
        Birthday birthday = Birthday.builder().userId("1").userName("name").date(LocalDate.now().minusDays(1)).build();
        Birthday birthday1 = Birthday.builder().userId("1").userName("name").date(LocalDate.now().plusDays(1)).build();
        Birthday birthday2 = Birthday.builder().userId("1").userName("name").date(LocalDate.now()).build();
        ArrayList<Birthday> toCheck = new ArrayList<>(List.of(birthday, birthday1, birthday2));
        ArrayList<Birthday> expected = new ArrayList<>(List.of(birthday2));
        Assertions.assertArrayEquals(expected.toArray(), BirthdayProvider.getListWithToday(toCheck).toArray());
    }

    @Test
    void getNext_NullProviderList_ReturnEmptyList() {
        Assertions.assertArrayEquals(new ArrayList<>().toArray(), BirthdayProvider.getListWithSortedSinceNow(null).toArray());
    }

    @Test
    void getNext_EmptyList_ReturnEmptyList() {
        Assertions.assertArrayEquals(new ArrayList<>().toArray(), BirthdayProvider.getListWithSortedSinceNow(new ArrayList<>()).toArray());
    }

    @Test
    void getNext_OneNext_ReturnListWithThisElement() {
        Birthday birthday = Birthday.builder()
                .userId("1")
                .userName("name")
                .date(LocalDate.now().minusDays(1))
                .build();
        ArrayList<Birthday> toCheck = new ArrayList<>(List.of(birthday));
        ArrayList<Birthday> expected = new ArrayList<>(List.of(birthday));
        Assertions.assertArrayEquals(expected.toArray(), BirthdayProvider.getListWithSortedSinceNow(toCheck).toArray());
    }

    @Test
    void getNext_OneBefore_ReturnListWithThisElement() {
        Birthday birthday = Birthday.builder()
                .userId("1")
                .userName("name")
                .date(LocalDate.now().plusDays(1))
                .build();
        ArrayList<Birthday> toCheck = new ArrayList<>(List.of(birthday));
        ArrayList<Birthday> expected = new ArrayList<>(List.of(birthday));
        Assertions.assertArrayEquals(expected.toArray(), BirthdayProvider.getListWithSortedSinceNow(toCheck).toArray());
    }

    @Test
    void getNext_TwoRecords_ReturnListWithElementsWhichHasNotTodayDate() {
        Birthday birthday = Birthday.builder()
                .userId("1")
                .userName("name")
                .date(LocalDate.now().plusDays(1))
                .build();
        Birthday birthday1 = Birthday.builder()
                .userId("1")
                .userName("name")
                .date(LocalDate.now())
                .build();
        ArrayList<Birthday> toCheck = new ArrayList<>(List.of(birthday, birthday1));
        ArrayList<Birthday> expected = new ArrayList<>(List.of(birthday));
        Assertions.assertArrayEquals(expected.toArray(), BirthdayProvider.getListWithSortedSinceNow(toCheck).toArray());
    }

    @Test
    void getNext_TwoRecords_ReturnSortedList() {
        Birthday birthday = Birthday.builder()
                .userId("1")
                .userName("name")
                .date(LocalDate.now().plusDays(1).withYear(1990))
                .build();
        Birthday birthday1 = Birthday.builder()
                .userId("1")
                .userName("name")
                .date(LocalDate.now().plusDays(2).withYear(1991))
                .build();
        ArrayList<Birthday> toCheck = new ArrayList<>(List.of(birthday1, birthday));
        ArrayList<Birthday> expected = new ArrayList<>(List.of(birthday, birthday1));
        Assertions.assertArrayEquals(expected.toArray(), BirthdayProvider.getListWithSortedSinceNow(toCheck).toArray());
    }

    @Test
    void getNext_ThreeRecordsOneBeforeNowOneToday_ReturnSortedList() {
        Birthday birthday = Birthday.builder()
                .userId("1")
                .userName("name")
                .date(LocalDate.now().plusDays(1).withYear(1990))
                .build();
        Birthday birthday1 = Birthday.builder()
                .userId("1")
                .userName("name")
                .date(LocalDate.now().minusDays(1).withYear(1991))
                .build();
        Birthday birthdayToday = Birthday.builder()
                .userId("1")
                .userName("name")
                .date(LocalDate.now().withYear(2000))
                .build();
        ArrayList<Birthday> toCheck = new ArrayList<>(List.of(birthday1, birthday, birthdayToday));
        ArrayList<Birthday> expected = new ArrayList<>(List.of(birthday, birthday1));
        Assertions.assertArrayEquals(expected.toArray(), BirthdayProvider.getListWithSortedSinceNow(toCheck).toArray());
    }

    @Test
    void getNext_ThreeRecordsOneBeforeNowOneToday2_ReturnSortedList() {
        Birthday birthday = Birthday.builder()
                .userId("1")
                .userName("name")
                .date(LocalDate.now().plusDays(1).withYear(1992))
                .build();
        Birthday birthday1 = Birthday.builder()
                .userId("1")
                .userName("name")
                .date(LocalDate.now().minusDays(1).withYear(1991))
                .build();
        Birthday birthdayToday = Birthday.builder()
                .userId("1")
                .userName("name")
                .date(LocalDate.now().withYear(2000))
                .build();
        ArrayList<Birthday> toCheck = new ArrayList<>(List.of(birthday1, birthday, birthdayToday));
        ArrayList<Birthday> expected = new ArrayList<>(List.of(birthday, birthday1));
        Assertions.assertArrayEquals(expected.toArray(), BirthdayProvider.getListWithSortedSinceNow(toCheck).toArray());
    }

    @Test
    void getStringTodayBirthday_NullList_ReturnBlank() {
        Assertions.assertEquals("", BirthdayProvider.getStringTodayBirthday(null));
    }

    @Test
    void getStringTodayBirthday_EmptyList_ReturnBlank() {
        List<Birthday> birthdays = new ArrayList<>();
        Assertions.assertEquals("", BirthdayProvider.getStringTodayBirthday(birthdays));
    }

    @Test
    void getStringTodayBirthday_OneElement_ReturnLine() {
        Birthday birthday = Birthday.builder().userId("1").date(LocalDate.now()).build();
        List<Birthday> birthdays = new ArrayList<>(List.of(birthday));
        try (MockedStatic<RangersGuild> mockedStatic = Mockito.mockStatic(RangersGuild.class)) {
            Member member = Mockito.mock(Member.class);
            Mockito.when(member.getAsMention()).thenReturn("<@1>");
            mockedStatic.when((MockedStatic.Verification) RangersGuild.getMemberByID("1")).thenReturn(member);
            String exampleLine = birthday.getDate().getDayOfMonth() + "." + String.format("%02d", birthday.getDate().getMonthValue()) + " - <@1>\n";
            Assertions.assertEquals(exampleLine, BirthdayProvider.getStringTodayBirthday(birthdays));
        }
    }

    @Test
    void getStringTodayBirthday_TwoElements_ReturnLines() {
        Birthday birthday = Birthday.builder().userId("1").date(LocalDate.now()).build();
        Birthday birthday1 = Birthday.builder().userId("1").date(LocalDate.now()).build();
        List<Birthday> birthdays = new ArrayList<>(List.of(birthday, birthday1));
        try (MockedStatic<RangersGuild> mockedStatic = Mockito.mockStatic(RangersGuild.class)) {
            Member member = Mockito.mock(Member.class);
            Mockito.when(member.getAsMention()).thenReturn("<@1>");
            mockedStatic.when((MockedStatic.Verification) RangersGuild.getMemberByID("1")).thenReturn(member);
            String exampleLine = birthday.getDate().getDayOfMonth() + "." + String.format("%02d", birthday.getDate().getMonthValue()) + " - <@1>\n";
            Assertions.assertEquals(exampleLine + exampleLine, BirthdayProvider.getStringTodayBirthday(birthdays));
        }
    }

    @Test
    void getStringNextBirthday_NullList_ReturnBlank() {
        Assertions.assertEquals("", BirthdayProvider.getStringNextBirthday(null));
    }

    @Test
    void getStringNextBirthday_EmptyList_ReturnBlank() {
        List<Birthday> birthdays = new ArrayList<>();
        Assertions.assertEquals("", BirthdayProvider.getStringNextBirthday(birthdays));
    }

    @Test
    void getStringNextBirthday_OneElement_ReturnLine() {
        Birthday birthday = Birthday.builder().userId("1").date(LocalDate.now()).build();
        List<Birthday> birthdays = new ArrayList<>(List.of(birthday));
        try (MockedStatic<RangersGuild> mockedStatic = Mockito.mockStatic(RangersGuild.class)) {
            Member member = Mockito.mock(Member.class);
            Mockito.when(member.getAsMention()).thenReturn("<@1>");
            mockedStatic.when((MockedStatic.Verification) RangersGuild.getMemberByID("1")).thenReturn(member);
            String exampleLine = birthday.getDate().getDayOfMonth() + "." + String.format("%02d", birthday.getDate().getMonthValue()) + " - <@1>\n";
            Assertions.assertEquals(exampleLine, BirthdayProvider.getStringNextBirthday(birthdays));
        }
    }

    @Test
    void getStringNextBirthday_TwoElementDifferentDate_ReturnLine() {
        Birthday birthday = Birthday.builder().userId("1").date(LocalDate.now()).build();
        Birthday birthday1 = Birthday.builder().userId("1").date(LocalDate.now().plusDays(1)).build();
        List<Birthday> birthdays = new ArrayList<>(List.of(birthday, birthday1));
        try (MockedStatic<RangersGuild> mockedStatic = Mockito.mockStatic(RangersGuild.class)) {
            Member member = Mockito.mock(Member.class);
            Mockito.when(member.getAsMention()).thenReturn("<@1>");
            mockedStatic.when((MockedStatic.Verification) RangersGuild.getMemberByID("1")).thenReturn(member);
            String exampleLine = birthday.getDate().getDayOfMonth() + "." + String.format("%02d", birthday.getDate().getMonthValue()) + " - <@1>\n";
            Assertions.assertEquals(exampleLine, BirthdayProvider.getStringNextBirthday(birthdays));
        }
    }

    @Test
    void getStringNextBirthday_TwoElementSameDate_ReturnLine() {
        Birthday birthday = Birthday.builder().userId("1").date(LocalDate.now()).build();
        Birthday birthday1 = Birthday.builder().userId("1").date(LocalDate.now()).build();
        Birthday birthday2 = Birthday.builder().userId("1").date(LocalDate.now().plusDays(1)).build();
        List<Birthday> birthdays = new ArrayList<>(List.of(birthday, birthday1, birthday2));
        try (MockedStatic<RangersGuild> mockedStatic = Mockito.mockStatic(RangersGuild.class)) {
            Member member = Mockito.mock(Member.class);
            Mockito.when(member.getAsMention()).thenReturn("<@1>");
            mockedStatic.when((MockedStatic.Verification) RangersGuild.getMemberByID("1")).thenReturn(member);
            String exampleLine = birthday.getDate().getDayOfMonth() + "." + String.format("%02d", birthday.getDate().getMonthValue()) + " - <@1>\n";
            Assertions.assertEquals(exampleLine + exampleLine, BirthdayProvider.getStringNextBirthday(birthdays));
        }
    }

}
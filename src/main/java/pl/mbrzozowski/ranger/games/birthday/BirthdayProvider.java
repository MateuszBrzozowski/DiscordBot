package pl.mbrzozowski.ranger.games.birthday;

import net.dv8tion.jda.api.entities.Member;
import org.jetbrains.annotations.NotNull;
import pl.mbrzozowski.ranger.guild.RangersGuild;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class BirthdayProvider {

    @NotNull
    public static List<Birthday> getListWithToday(List<Birthday> all) {
        if (isBlank(all)) {
            return new ArrayList<>();
        }
        LocalDateTime now = LocalDateTime.now();
        List<Birthday> birthdaysToday = all.stream().filter(birthday -> birthday.getDate().getMonthValue() == now.getMonthValue() &&
                birthday.getDate().getDayOfMonth() == now.getDayOfMonth()).toList();
        if (!birthdaysToday.isEmpty()) {
            return birthdaysToday;
        } else {
            return new ArrayList<>();
        }
    }

    @NotNull
    public static List<Birthday> getListWithSortedSinceNow(List<Birthday> all) {
        if (isBlank(all)) {
            return new ArrayList<>();
        }
        LocalDateTime now = LocalDateTime.now();
        List<Birthday> birthdaysNext = new ArrayList<>(all.stream().filter(birthday ->
                birthday.getDate().getMonthValue() != now.getMonthValue() ||
                        (birthday.getDate().getMonthValue() == now.getMonthValue() &&
                                birthday.getDate().getDayOfMonth() != now.getDayOfMonth())).toList());
        if (birthdaysNext.isEmpty()) {
            return new ArrayList<>();
        } else {
            birthdaysNext.sort(Comparator.comparingInt(o -> o.getDate().getMonthValue()));
            List<Birthday> birthdays = sortDaysInMonth(birthdaysNext);
            moveAllToEndIfDayOfYearIsBeforeNow(birthdays);
            return birthdays;
        }
    }

    private static void moveAllToEndIfDayOfYearIsBeforeNow(@NotNull List<Birthday> birthdays) {
        int i = 0;
        int monthValue = birthdays.get(i).getDate().getMonthValue();
        while (monthValue < LocalDate.now().getMonthValue() && i < birthdays.size()) {
            birthdays.add(birthdays.remove(0));
            monthValue = birthdays.get(i).getDate().getMonthValue();
            i++;
        }
        i = 0;
        int dayOfMonth = birthdays.get(i).getDate().getDayOfMonth();
        while (monthValue == LocalDate.now().getMonthValue() &&
                dayOfMonth < LocalDate.now().getDayOfMonth() &&
                i < birthdays.size()) {
            birthdays.add(birthdays.remove(0));
            monthValue = birthdays.get(i).getDate().getMonthValue();
            dayOfMonth = birthdays.get(i).getDate().getDayOfMonth();
            i++;
        }
    }

    @NotNull
    private static List<Birthday> sortDaysInMonth(List<Birthday> birthdaysNext) {
        List<Birthday> result = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            int finalI = i;
            List<Birthday> month = new ArrayList<>(birthdaysNext.stream().filter(b -> b.getDate().getMonthValue() == finalI).toList());
            month.sort(Comparator.comparingInt(o -> o.getDate().getDayOfMonth()));
            result.addAll(month);
        }
        return result;
    }

    private static boolean isBlank(List<Birthday> all) {
        if (all == null) {
            return true;
        }
        return all.isEmpty();
    }

    /**
     * @param list of {@link Birthday} sorted via day of year from today
     * @return All Members from list as mention. If list is not null or empty return blank String.
     */
    @NotNull
    public static String getStringTodayBirthday(List<Birthday> list) {
        if (isBlank(list)) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (Birthday birthday : list) {
            appendLineFromBirthday(builder, birthday);
        }
        return builder.toString();
    }

    private static void appendLineFromBirthday(StringBuilder builder, @NotNull Birthday birthday) {
        Member member = RangersGuild.getMemberByID(birthday.getUserId());
        if (member != null) {
            builder.append(birthday.getDate().getDayOfMonth()).append(".")
                    .append(String.format("%02d", birthday.getDate().getMonthValue()))
                    .append(" - ")
                    .append(member.getAsMention()).append("\n");
        }
    }

    /**
     * @param list of {@link Birthday} sorted via day of year from today
     * @return Member as mention who next on the list if list is not null or empty. Otherwise, blank string.
     */
    @NotNull
    public static String getStringNextBirthday(List<Birthday> list) {
        if (isBlank(list)) {
            return "";
        }
        List<Birthday> birthdays = new ArrayList<>(List.of(list.get(0)));
        LocalDate next = list.get(0).getDate();
        int i = 1;
        while (i < list.size() &&
                next.getMonthValue() == list.get(i).getDate().getMonthValue() &&
                next.getDayOfMonth() == list.get(i).getDate().getDayOfMonth()) {
            birthdays.add(list.get(i));
            i++;
        }
        StringBuilder builder = new StringBuilder();
        for (Birthday birthday : birthdays) {
            appendLineFromBirthday(builder, birthday);
        }
        return builder.toString();
    }
}

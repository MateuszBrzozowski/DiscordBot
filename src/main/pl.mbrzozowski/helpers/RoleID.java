package helpers;

import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ranger.RangerBot;

import java.util.List;

public class RoleID {

    protected static final Logger logger = LoggerFactory.getLogger(RangerBot.class.getName());
    public static final String RADA_KLANU = "773233884145647666";
    public static final String CLAN_MEMBER_ID = "311978154291888141";
    public static final String RECRUT_ID = "410808008331886592";
    public static final String DRILL_INSTRUCTOR_ID = "534737692911468554";
    public static final String HSR_ID = "695036426156376195";
    public static final String PC_ID = "748799503624306688";
    public static final String PEC_ID = "748800075806932993";
    public static final String RN_ID = "748800245575712910";

    public static boolean isRoleMessageRecived(List<Role> roles, String rola) {
        for (Role r : roles) {
            if (r.getId().equalsIgnoreCase(rola)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param event Kliknięcie w przycisk
     * @param rola Rola którą sprawdzamy
     * @return Zwraca true jeżeli użytkownik posiada rolę przekazaną w parametrze, Zwraca false jeżeli użytkownik nie ma tej roli.
     */
    public static boolean isRoleButtonClick(ButtonClickEvent event, String rola) {
        List<Role> roles = event.getMember().getRoles();
        for (Role r : roles) {
            if (r.getId().equalsIgnoreCase(rola)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param event Kliknięcie w przycisk
     * @return Zwraca prawdę jeżeli użytkownik ma przypisaną rangę innego klanu, Jeżeli nie ma żadnej rangi zwraca false
     */
    public static boolean isRoleAnotherClanButtonClick(ButtonClickEvent event) {
        String[] clans = {HSR_ID, PC_ID, PEC_ID, RN_ID};
        for (int i = 0; i < clans.length; i++) {
            if (isRoleButtonClick(event, clans[i])) {
                return true;
            }
        }
        return false;
    }

}

package helpers;

import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.util.List;

public class RoleID {

    public static final String RADA_KLANU ="773233884145647666";
    public static final String CLAN_MEMBER_ID = "311978154291888141";
    public static final String RECRUT_ID = "410808008331886592";
    public static final String DRILL_INSTRUCTOR_ID = "534737692911468554";
    public static final String HSR_ID = "695036426156376195";
    public static final String PC_ID = "748799503624306688";
    public static final String PEC_ID = "748800075806932993";
    public static final String RN_ID = "748800245575712910";

    public static boolean isRoleMessageRecived(GuildMessageReceivedEvent event, String rola){
        List<Role> roles = event.getMember().getRoles();
        for (Role r: roles){
            if (r.getId().equalsIgnoreCase(rola)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isRoleButtonClick(ButtonClickEvent event, String rola) {
        List<Role> roles = event.getMember().getRoles();
        for (Role r: roles){
            if (r.getId().equalsIgnoreCase(rola)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isRoleAnotherClanButtonClick(ButtonClickEvent event) {
        String[] clans = {HSR_ID,PC_ID,PEC_ID,RN_ID};
        for (int i = 0; i < clans.length; i++) {
            if (isRoleButtonClick(event,clans[i])){
                return true;
            }
        }
        return false;
    }
}

package helpers;

import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ranger.RangerBot;

public interface RoleID {

    String DEV_ID = "642402714382237716";
    String RADA_KLANU = "773233884145647666";
    String CLAN_MEMBER_ID = "311978154291888141";
    String RECRUT_ID = "410808008331886592";
    String DRILL_INSTRUCTOR_ID = "534737692911468554";
    String HSR_ID = "695036426156376195";
    String PC_ID = "748799503624306688";
    String PEC_ID = "748800075806932993";
    String RN_ID = "748800245575712910";
    String VIRTUAL_REALITY = "884377874575007764";
    String SERVER_ADMIN = "740894839305928764";
    String MODERATOR = "311978200739741696";
    String SEED_ID = "960892010452103178";

    String SQUAD = "976110879223451688";
    String CS = "976111919737995356";
    String WAR_THUNDER = "976112319245463592";
    String TARKOV = "925864474034974780";
    String MINECRAFT = "976112566910734376";
    String RAINBOW_SIX = "976112886286024736";
    String WARGAME = "976112956456697866";
    String ARMA = "976113019862016090";

    static SelectMenu getRoleToSelectMenu() {
        return SelectMenu.create(ComponentId.ROLES)
                .setPlaceholder("Choose a role")
                .setRequiredRange(1,1)
                .addOption("VR", VIRTUAL_REALITY)
                .addOption("Squad", SQUAD)
                .addOption("CS", CS)
                .addOption("War Thunder", WAR_THUNDER)
                .addOption("Tarkov", TARKOV)
                .addOption("Minecraft", MINECRAFT)
                .addOption("Rainbow six siege", RAINBOW_SIX)
                .addOption("Wargame", WARGAME)
                .addOption("Arma", ARMA)
                .build();
    }
}

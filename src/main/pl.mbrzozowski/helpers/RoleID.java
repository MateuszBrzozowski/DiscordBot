package helpers;

import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ranger.RangerBot;

public class RoleID {

    protected static final Logger logger = LoggerFactory.getLogger(RangerBot.class.getName());
    public static final String DEV_ID = "642402714382237716";
    public static final String RADA_KLANU = "773233884145647666";
    public static final String CLAN_MEMBER_ID = "311978154291888141";
    public static final String RECRUT_ID = "410808008331886592";
    public static final String DRILL_INSTRUCTOR_ID = "534737692911468554";
    public static final String HSR_ID = "695036426156376195";
    public static final String PC_ID = "748799503624306688";
    public static final String PEC_ID = "748800075806932993";
    public static final String RN_ID = "748800245575712910";
    public static final String VIRTUAL_REALITY = "884377874575007764";
    public static final String SERVER_ADMIN = "740894839305928764";
    public static final String MODERATOR = "311978200739741696";
    public static final String SEED_ID = "960892010452103178";

    public static final String SQUAD = "976110879223451688";
    public static final String CS = "976111919737995356";
    public static final String WAR_THUNDER = "976112319245463592";
    public static final String TARKOV = "925864474034974780";
    public static final String MINECRAFT = "976112566910734376";
    public static final String RAINBOW_SIX = "976112886286024736";
    public static final String WARGAME = "976112956456697866";
    public static final String ARMA = "976113019862016090";

    public static SelectMenu getRoleToSelectMenu() {
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

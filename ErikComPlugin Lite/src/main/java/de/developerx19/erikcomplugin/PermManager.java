package de.developerx19.erikcomplugin;

import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permissible;

public class PermManager
{

    public static final int VANISH = 1;
    public static final int CREATIVE = 2;
    public static final int TELEPORT = 3;
    public static final int TAGS = 4;
    public static final int PVP = 5;
    public static final int CHAT = 6;
    public static final int STAFF = 7;
    public static final int LIVE = 8;
    public static final int SIGNITEM = 9;
    public static final int PLAYERSTATS = 10;
    public static final int COUNTDOWN = 11;
    public static final int TEAMINFO = 12;
    public static final int SUBPERK = 13;
    public static final int OPERATOR = 9999;
    public static final int DEVELOPER = -1;

    public static boolean check(Object user, int perm)
    {
        if (!(user instanceof Permissible))
        {
            ErikComPlugin.console.severe("Could not check permissions of object with type " + user.getClass().getName());
            return false;
        }
        Permissible u = (Permissible) user;
        PlayerData data = null;
        boolean streamermodeN = true;
        boolean pvpmodeN = true;
        if (user instanceof Player)
        {
            data = PlayerData.get((Player)user);
            streamermodeN = !data.streamer_mode();
            pvpmodeN = !data.pvpOn();
        }
        boolean has_perm;
        switch (perm)
        {
            case VANISH -> has_perm = u.hasPermission("devx.vanish") && streamermodeN && pvpmodeN;
            case TEAMINFO -> has_perm = u.hasPermission("devx.teaminfo");
            case CREATIVE -> has_perm = u.hasPermission("devx.creative") && streamermodeN && pvpmodeN;
            case TELEPORT -> has_perm = u.hasPermission("devx.teleport") && streamermodeN && pvpmodeN;
            case SUBPERK -> has_perm = u.hasPermission("devx.subperk") && streamermodeN && pvpmodeN;
            case TAGS -> has_perm = u.hasPermission("devx.tags");
            case PVP -> has_perm = u.hasPermission("devx.pvp") && (data == null || !data.vanished);
            case CHAT -> has_perm = u.hasPermission("devx.chat");
            case STAFF -> has_perm = u.hasPermission("devx.staff");
            case LIVE -> has_perm = u.hasPermission("devx.streamer") || (data != null && data.streamer_mode());
            case SIGNITEM -> has_perm = u.hasPermission("devx.signitem");
            case PLAYERSTATS -> has_perm = u.hasPermission("devx.player_stats");
            case COUNTDOWN -> has_perm = u.hasPermission("devx.countdown");
            case OPERATOR -> has_perm = (user instanceof ConsoleCommandSender) || (data != null && data.is_operator());
            case DEVELOPER -> has_perm = (user instanceof ConsoleCommandSender) || (data != null && data.dev_member());
            default ->
            {
                ErikComPlugin.console.severe("Could not check unregistered permission '" + perm + "'");
                return false;
            }
        }
        return has_perm;
    }
}

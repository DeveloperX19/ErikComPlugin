package de.developerx19.erikcomplugin;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PlayerStatCommand implements CommandExecutor, TabCompleter
{

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args)
    {
        if (!ErikComPlugin.commands_active())
        {
            sender.sendMessage(InfoMap.cmd_fb + ChatColor.RED + "Command is currently deactivated");
            return false;
        }
        if (!PermManager.check(sender, PermManager.PLAYERSTATS))
        {
            sender.sendMessage(InfoMap.cmd_fb + InfoMap.cmd_permit_err);
            return false;
        }
        if (args.length != 2)
        {
            sender.sendMessage(InfoMap.cmd_fb + ChatColor.RED + "Invalid number of arguments");
            return false;
        }
        OfflinePlayer target = ErikComPlugin.getPlayer(args[1]);
        if (target == null)
        {
            sender.sendMessage(InfoMap.cmd_fb + ChatColor.RED + "Player not found");
            return false;
        }
        if (args[0].equalsIgnoreCase("join_date"))
        {
            String j_date;
            if (target.getPlayer() != null)
                j_date = PlayerData.date_parser.format(PlayerData.get(target.getPlayer()).init_join_date());
            else
                j_date = Database.getDataString(Database.getPlayerID(target), Database.PlayerField.JOIN_DATE, "INVALID OR NO DATA");
            Date r_date = new Date();
            try
            {
                r_date = PlayerData.date_parser.parse(j_date);
            }
            catch (Exception ignored) {}
            double days_diff = ((double)(System.currentTimeMillis()) - (double)(r_date.getTime())) / ((double)(24L * 60 * 60 * 1000));
            sender.sendMessage(InfoMap.cmd_fb + ChatColor.BLUE + j_date + ChatColor.AQUA + " (" + String.format("%.1f",days_diff) + " days ago)");
        }
        else if (args[0].equalsIgnoreCase("play_time"))
        {
            long millis;
            if (target.getPlayer() != null)
            {
                PlayerData data = PlayerData.get(target.getPlayer());
                millis = data.totalPlayTime();
                if (!data.afk)
                    millis += System.currentTimeMillis() - data.lastNonAFKStamp;
            }
            else
                millis = Database.getDataInteger(Database.getPlayerID(target), Database.PlayerField.PLAYTIME, 0);
            Duration playtime = Duration.ofMillis(millis);
            long days = playtime.toDays();
            playtime = playtime.minusDays(days);
            long hours = playtime.toHours();
            playtime = playtime.minusHours(hours);
            long minutes = playtime.toMinutes();
            playtime = playtime.minusMinutes(minutes);
            long seconds = playtime.toSeconds();
            sender.sendMessage(InfoMap.cmd_fb + ChatColor.BLUE + String.format("%d d. %d h. %d min. %d sec.", days, hours, minutes, seconds) + ChatColor.AQUA + " (non-AFK)");
        }
        else
        {
            sender.sendMessage(InfoMap.cmd_fb + ChatColor.RED + "Statistic does not exist");
            return false;
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args)
    {
        List<String> suggestions = new ArrayList<>();
        if (!PermManager.check(sender, PermManager.PLAYERSTATS)) return suggestions;
        if (args.length <= 1)
        {
            suggestions.add("join_date");
            suggestions.add("play_time");
        }
        if (args.length == 2)
        {
            for (Player p : ErikComPlugin.server.getOnlinePlayers())
                suggestions.add(p.getName().toLowerCase());
        }
        return suggestions;
    }
}

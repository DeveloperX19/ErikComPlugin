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

import java.util.ArrayList;
import java.util.List;

public class OpCommand implements CommandExecutor, TabCompleter
{

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args)
    {
        if (!ErikComPlugin.commands_active() && !PermManager.check(sender, PermManager.DEVELOPER))
        {
            sender.sendMessage(InfoMap.cmd_fb + ChatColor.RED + "Command is currently deactivated");
            return false;
        }
        if (!PermManager.check(sender, PermManager.OPERATOR))
        {
            sender.sendMessage(InfoMap.cmd_fb + InfoMap.cmd_permit_err);
            return false;
        }
        if (args.length != 1)
        {
            sender.sendMessage(InfoMap.cmd_fb + ChatColor.RED + "Invalid number of arguments");
            return false;
        }

        OfflinePlayer target = ErikComPlugin.getPlayer(args[0]);
        if (target == null)
        {
            sender.sendMessage(InfoMap.cmd_fb + ChatColor.RED + "Player not found");
            return false;
        }
        if (target.getPlayer() != null)
        {
            PlayerData data = PlayerData.get(target.getPlayer());
            data.is_operator(true);
            data.setOpMode();
        }
        else
        {
            long id = Database.getPlayerID(target.getUniqueId());
            Database.setDataBoolean(id, Database.PlayerField.IS_OPERATOR, true);
        }
        sender.sendMessage(InfoMap.cmd_fb + ChatColor.BLUE + target.getName() + ChatColor.GREEN + " is now an operator");
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args)
    {
        List<String> suggestions = new ArrayList<>();
        if (!PermManager.check(sender, PermManager.OPERATOR)) return suggestions;
        if (args.length > 1) return suggestions;
        for (Player p : ErikComPlugin.server.getOnlinePlayers())
            suggestions.add(p.getName().toLowerCase());
        return suggestions;
    }
}

package de.developerx19.erikcomplugin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class TellCommand implements CommandExecutor, TabCompleter
{

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args)
    {
        if (!ErikComPlugin.commands_active())
        {
            sender.sendMessage(InfoMap.cmd_fb + ChatColor.RED + "Command is currently deactivated");
            return false;
        }
        if (!PermManager.check(sender, PermManager.CHAT))
        {
            sender.sendMessage(InfoMap.cmd_fb + InfoMap.cmd_permit_err);
            return false;
        }
        if (!(sender instanceof Player)) return false;
        Player sending = (Player) sender;
        if (args.length < 2) return false;
        String target = args[0];
        if (target.charAt(0) == '@')
            target = target.substring(1);
        Player receiving = ErikComPlugin.server.getPlayer(target);
        if (receiving == null || target.isEmpty())
        {
            sending.sendMessage(ChatColor.RED + ">> Player is not online or vanished");
            return true;
        }
        PlayerData rdata = PlayerData.get(receiving);
        if (rdata.streamer_mode())
        {
            if (rdata.vanished && !PermManager.check(sender, PermManager.VANISH))
                sending.sendMessage(ChatColor.RED + ">> Player is not online or vanished");
            else
                sending.sendMessage(ChatColor.RED + ">> Player is not receiving messages");
            return true;
        }
        StringBuilder message = new StringBuilder();
        for (int i = 1; i < args.length; i++)
            message.append(args[i]).append(" ");
        String payload = ChatColor.GRAY + message.toString();
        if (rdata.vanished && !PermManager.check(sender, PermManager.VANISH))
            sending.sendMessage(ChatColor.RED + ">> Player is not online or vanished");
        else
        {
            String header = ChatColor.GREEN  + "You -> " + receiving.getName() + ": ";
            Component full_message = Component.text(header).clickEvent(ClickEvent.suggestCommand("/msg " + receiving.getName() + " "));
            full_message = full_message.append(Component.text(payload));
            sender.sendMessage(full_message);
        }
        String header = ChatColor.GREEN + sending.getName() + " -> You: ";
        Component full_message = Component.text(header).clickEvent(ClickEvent.suggestCommand("/msg " + sending.getName() + " "));
        full_message = full_message.append(Component.text(payload));
        receiving.sendMessage(full_message);
        rdata.addChat(sending);
        PlayerData.get(sending).addChat(receiving);
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args)
    {
        List<String> suggestions = new ArrayList<>();
        if (!PermManager.check(sender, PermManager.CHAT)) return suggestions;
        if (!(sender instanceof Player)) return suggestions;
        Player player = (Player) sender;
        if (args.length <= 1 && (args[0].isEmpty() || args[0].charAt(0) != '@'))
        {
            for (var p : ErikComPlugin.server.getOnlinePlayers())
                suggestions.add(p.getName().toLowerCase());
        }
        if (args.length <= 1)
        {
            PlayerData data = PlayerData.get(player);
            for (var p : data.lastChats)
                if (p.isOnline())
                    suggestions.add("@" + p.getName().toLowerCase());
        }
        return suggestions;
    }
}

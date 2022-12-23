package de.developerx19.erikcomplugin;

import org.bukkit.ChatColor;

/** @noinspection unused*/
public class Debug
{
    public static void log(String message)
    {
        if (ErikComPlugin.is_release) return;
        ErikComPlugin.console.severe("[DEBUG] " + message);
        for (var p : ErikComPlugin.server.getOnlinePlayers())
            p.sendMessage(ChatColor.DARK_PURPLE + "[DEBUG] " + ChatColor.LIGHT_PURPLE + message);
    }
}

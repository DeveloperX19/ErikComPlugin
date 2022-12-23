package de.developerx19.erikcomplugin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;

public class EventManager
{
    public static final String head_c4 = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNGNlZTY5NGI1ZGYzNGY0MGQ1ZTc3NGJkMzA0NmRiODQ5ZTM0ZmY1NWE0ODJkMDczMWU5ZDdhN2JiNzRhMTIifX19";

    public static void start_global_countdown(String name, long millis_count, int priority, boolean announce_now)
    {
        long millis = millis_count;
        String timeStr = "  :  in ";
        millis_count += 100;
        long days = millis_count / 86400000;
        millis_count -= days * 86400000;
        long hours = millis_count / 3600000;
        millis_count -= hours * 3600000;
        long minutes = millis_count / 60000;
        millis_count -= minutes * 60000;
        long seconds = millis_count / 1000;
        if (days > 0) timeStr += days + "d ";
        if (hours > 0) timeStr += hours + "h ";
        if (minutes > 0) timeStr += minutes + "m ";
        if (seconds > 0) timeStr += seconds + "s ";
        Component msg = Component.text("  >>>  ").color(TextColor.color(0xAA00AA));
        msg = msg.append(Component.text(name).color(TextColor.color(0xFF55FF)));
        if (days == 0 && hours == 0 && minutes == 0 && seconds == 0)
        {
            msg = msg.append(Component.text("  :  NOW !").color(TextColor.color(0x55FFFF)));
            ErikComPlugin.server.sendMessage(msg);
            return;
        }
        if (announce_now)
        {
            msg = msg.append(Component.text(timeStr).color(TextColor.color(0x55FFFF)));
            ErikComPlugin.server.sendMessage(msg);
        }
        if (priority <= 0) return;

        long millis_till_next;
        if (millis < 11000) millis_till_next = 1000;                                        // announce at 9, 8, ... seconds    -> priority 1
        else if (millis < 31000 || priority <= 1) millis_till_next = millis - 10000;        // announce at 10 seconds           -> priority 1
        else if (millis < 61000 || priority <= 2) millis_till_next = millis - 30000;        // announce at 30 seconds           -> priority 2
        else if (millis < 121000 || priority <= 3) millis_till_next = millis - 60000;       // announce at 1 minutes            -> priority 3
        else if (millis < 301000 || priority <= 4) millis_till_next = millis - 120000;      // announce at 2 minutes            -> priority 4
        else if (millis < 601000 || priority <= 5) millis_till_next = millis - 300000;      // announce at 5 minutes            -> priority 5
        else if (millis < 3601000 || priority <= 6) millis_till_next = millis - 600000;     // announce at 10 minutes           -> priority 6
        else millis_till_next = millis - 3600000;                                           // announce at 60 minutes           -> priority 7

        new Thread(() -> {
            long start = System.currentTimeMillis();
            try { Thread.sleep(millis_till_next - 10); }
            catch (Exception ignored) {}
            Bukkit.getScheduler().runTask(ErikComPlugin.plugin, () -> {
                long millis_passed = System.currentTimeMillis() - start;
                start_global_countdown(name, millis - millis_passed, priority, true);
            });
        }).start();
    }
}

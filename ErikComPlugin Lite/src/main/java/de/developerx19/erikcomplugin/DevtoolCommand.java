package de.developerx19.erikcomplugin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerKickEvent;
import org.codehaus.plexus.util.ExceptionUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DevtoolCommand implements CommandExecutor, TabCompleter
{
    private static final String fb = ChatColor.DARK_PURPLE + " >> ";
    private static final String usage = ChatColor.DARK_PURPLE + " >> Usage : /devtool <sub-command>";
    private static final String subcmds = ChatColor.LIGHT_PURPLE + " >> Sub-Commands : " +
            "dev_team, broadcast, toggle_cmds, maintenance_mode, xmas22event, xmas22day, db_execute, db_update, db_help";

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args)
    {
        if (!PermManager.check(sender, PermManager.DEVELOPER))
        {
            sender.sendMessage(InfoMap.cmd_fb + InfoMap.cmd_permit_err);
            return false;
        }
        if (args.length == 0)
        {
            sender.sendMessage(fb + "Invalid number of arguments!");
            sender.sendMessage(usage);
            sender.sendMessage(subcmds);
            return false;
        }
        boolean result;
        String subcmd = args[0];
        if (subcmd.equalsIgnoreCase("broadcast"))
            result = cmd_broadcast(sender, args);
        else if (subcmd.equalsIgnoreCase("dev_team"))
            result = cmd_dev_team(sender, args);
        else if (subcmd.equalsIgnoreCase("toggle_cmds"))
            result = cmd_togglecmds(sender, args);
        else if (subcmd.equalsIgnoreCase("maintenance_mode"))
            result = cmd_maintenance_mode(sender, args);
        else if (subcmd.equalsIgnoreCase("xmas22event"))
            result = cmd_xmas22event(sender, args);
        else if (subcmd.equalsIgnoreCase("xmas22day"))
            result = cmd_xmas22day(sender, args);
        else if (subcmd.equalsIgnoreCase("custom_item"))
            result = cmd_custom_item(sender, args);
        else if (subcmd.equalsIgnoreCase("db_execute"))
            result = cmd_db_execute(sender, args);
        else if (subcmd.equalsIgnoreCase("db_update"))
            result = cmd_db_update(sender, args);
        else if (subcmd.equalsIgnoreCase("db_help"))
            result = cmd_db_help(sender);
        else
        {
            sender.sendMessage(usage);
            sender.sendMessage(subcmds);
            return false;
        }
        sender.sendMessage(ChatColor.GOLD + " >> Command executed succesfully!");
        return result;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args)
    {
        List<String> suggestions =  new ArrayList<>();
        if (!PermManager.check(sender, PermManager.DEVELOPER)) return suggestions;
        if (args.length == 1 && args[0].startsWith("db")) return suggestions;
        if (args.length <= 1)
        {
            suggestions.add("dev_team");
            suggestions.add("broadcast");
            suggestions.add("toggle_cmds");
            suggestions.add("maintenance_mode");
            suggestions.add("xmas22event");
            suggestions.add("xmas22day");
            suggestions.add("custom_item");
        }
        String subcmd = args[0];
        if (args.length == 2)
        {
            if (subcmd.equalsIgnoreCase("dev_team"))
            {
                suggestions.add("add");
                suggestions.add("remove");
            }
            if (subcmd.equalsIgnoreCase("broadcast"))
                suggestions.add("<message>");
            if (subcmd.equalsIgnoreCase("toggle_cmds"))
            {
                suggestions.add("on");
                suggestions.add("off");
            }
            if (subcmd.equalsIgnoreCase("maintenance_mode"))
            {
                suggestions.add("on");
                suggestions.add("off");
            }
            if (subcmd.equalsIgnoreCase("xmas22event"))
            {
                suggestions.add("start");
                suggestions.add("setup");
            }
            if (subcmd.equalsIgnoreCase("custom_item"))
            {
                suggestions.add("demon_root");
                suggestions.add("explosive_c4");
                suggestions.add("gaensebraten");
                suggestions.add("gluehwein");
                suggestions.add("midas_sword");

                suggestions.add("item_coupon_gestein");
                suggestions.add("item_coupon_pflanze");
                suggestions.add("item_coupon_wolle");
                suggestions.add("item_coupon_nahrung");
                suggestions.add("item_coupon_staemme");
                suggestions.add("item_coupon_glas");
                suggestions.add("item_coupon_trank");
                suggestions.add("item_coupon_beton");
                suggestions.add("item_coupon_redstone");
            }
        }
        if (args.length == 3)
        {
            if (subcmd.equalsIgnoreCase("dev_team"))
                suggestions.addAll(Database.getDevelopers());
        }
        return suggestions;
    }

    private boolean cmd_broadcast(@NotNull CommandSender sender, @NotNull String[] args)
    {
        if (args.length == 1)
        {
            sender.sendMessage(fb + "Invalid number of arguments!");
            return false;
        }
        StringBuilder message = new StringBuilder();
        for (int i = 1; i < args.length; i++)
            message.append(args[i]).append(" ");
        Component frame = Component.text("-------------------------")
                .color(TextColor.color(0xDD00DD));
        Component msg = Component.text(message.toString()).color(TextColor.color(0xFF0000));
        ErikComPlugin.server.sendMessage(frame);
        ErikComPlugin.server.sendMessage(msg);
        ErikComPlugin.server.sendMessage(frame);
        return true;
    }
    private boolean cmd_dev_team(@NotNull CommandSender sender, @NotNull String[] args)
    {
        if (args.length == 3)
        {
            boolean newState;
            if (args[1].equalsIgnoreCase("add"))
                newState = true;
            else if (args[1].equalsIgnoreCase("remove"))
                newState = false;
            else
            {
                sender.sendMessage(fb + "Invalid argument!");
                return false;
            }
            OfflinePlayer p = ErikComPlugin.getPlayer(args[2]);
            if (p == null)
            {
                sender.sendMessage(fb + "Player not found!");
                return false;
            }
            if (p.getPlayer() != null)
                PlayerData.get(p.getPlayer()).dev_member(newState);
            else
            {
                long id = Database.getPlayerID(p.getUniqueId());
                Database.setDataBoolean(id, Database.PlayerField.DEV_MEMBER, newState);
            }
        }
        else
        {
            sender.sendMessage(fb + "Invalid number of arguments!");
            return false;
        }
        return true;
    }
    private boolean cmd_togglecmds(@NotNull CommandSender sender, @NotNull String[] args)
    {

        if (args.length == 1)
        {
            sender.sendMessage(fb + "ErikComPlugin commands mode are currently " + ChatColor.AQUA + (ErikComPlugin.commands_active() ? "ENABLED" : "DISABLED"));
        }
        else if (args.length == 2)
        {
            if (args[1].equalsIgnoreCase("on"))
            {
                ErikComPlugin.commands_active(true);
                sender.sendMessage(fb + "ErikComPlugin commands mode are now " + ChatColor.AQUA + "ENABLED");
            }
            else if (args[1].equalsIgnoreCase("off"))
            {
                ErikComPlugin.commands_active(false);
                sender.sendMessage(fb + "ErikComPlugin commands mode are now " + ChatColor.AQUA + "DISABLED");
            }
            else
            {
                sender.sendMessage(fb + "Invalid argument!");
                return false;
            }
        }
        else
        {
            sender.sendMessage(fb + "Invalid number of arguments!");
            return false;
        }
        return true;
    }
    private boolean cmd_maintenance_mode(@NotNull CommandSender sender, @NotNull String[] args)
    {
        if (args.length == 1)
        {
            sender.sendMessage(fb + "Maintenance mode is currently " + ChatColor.AQUA + (ErikComPlugin.is_maintenance() ? "ENABLED" : "DISABLED"));
        }
        else if (args.length == 2)
        {
            if (args[1].equalsIgnoreCase("on"))
            {
                ErikComPlugin.is_maintenance(true);
                for (var p : ErikComPlugin.server.getOnlinePlayers())
                {
                    PlayerData d = PlayerData.get(p);
                    if (d.staff_member() || d.dev_member()) continue;
                    p.kick(Component.text(
                            ChatColor.RED + "The server is currently in maintenance mode\n" +
                                    ChatColor.DARK_PURPLE + "You are not permitted to join right now!\n" +
                                    ChatColor.GOLD + "Please contact staff members for more information"), PlayerKickEvent.Cause.PLUGIN);
                }
                sender.sendMessage(fb + "Maintenance mode is now " + ChatColor.AQUA + "ENABLED");
            }
            else if (args[1].equalsIgnoreCase("off"))
            {
                ErikComPlugin.is_maintenance(false);
                sender.sendMessage(fb + "Maintenance mode is now " + ChatColor.AQUA + "DISABLED");
            }
            else
            {
                sender.sendMessage(fb + "Invalid argument!");
                return false;
            }
        }
        else
        {
            sender.sendMessage(fb + "Invalid number of arguments!");
            return false;
        }
        return true;
    }
    private boolean cmd_xmas22event(@NotNull CommandSender sender, @NotNull String[] args)
    {
        if (args.length == 1)
        {
            sender.sendMessage(fb + "Xmas22-Event is currently " + ChatColor.AQUA + (ErikComPlugin.xmas22active() ? "ACTIVE" : "IN SETUP"));
        }
        else if (args.length == 2)
        {
            if (args[1].equalsIgnoreCase("start"))
            {
                ErikComPlugin.xmas22active(true);
                sender.sendMessage(fb + "Xmas22-Event is now " + ChatColor.AQUA + "ACTIVE");
            }
            else if (args[1].equalsIgnoreCase("setup"))
            {
                ErikComPlugin.xmas22active(false);
                sender.sendMessage(fb + "Xmas22-Event is now " + ChatColor.AQUA + "IN SETUP");
            }
            else
            {
                sender.sendMessage(fb + "Invalid argument!");
                return false;
            }
        }
        else
        {
            sender.sendMessage(fb + "Invalid number of arguments!");
            return false;
        }
        return true;
    }
    private boolean cmd_xmas22day(@NotNull CommandSender sender, @NotNull String[] args)
    {
        if (args.length == 1)
        {
            sender.sendMessage(fb + "day_of_december : " + ChatColor.AQUA + EventXmas22Manager.day_of_december);
        }
        else if (args.length == 2)
        {
            int day;
            try
            {
                day = Integer.parseInt(args[1]);
                if (day < -1) throw new Exception();
            }
            catch (Exception ignored)
            {
                sender.sendMessage(InfoMap.cmd_fb + ChatColor.RED + "'" + args[1] + "' is not a valid number");
                return false;
            }
            EventXmas22Manager.day_of_december_override = day;
            if (day != -1)
                EventXmas22Manager.day_of_december = day;
        }
        else
        {
            sender.sendMessage(fb + "Invalid number of arguments!");
            return false;
        }
        return true;
    }

    private boolean cmd_custom_item(@NotNull CommandSender sender, @NotNull String[] args)
    {
        if (!(sender instanceof Player))
        {
            sender.sendMessage(InfoMap.cmd_fb + ChatColor.RED + "Command must be issued by a player");
            return false;
        }
        Player player = (Player) sender;
        var inv = player.getInventory();
        if (args.length != 2)
        {
            sender.sendMessage(fb + "Invalid number of arguments!");
            return false;
        }
        if (args[1].equalsIgnoreCase("demon_root"))
            inv.addItem(CustomItemGenerator.demonic_root(10));
        if (args[1].equalsIgnoreCase("explosive_c4"))
            inv.addItem(CustomItemGenerator.explosive_c4(10));
        if (args[1].equalsIgnoreCase("gaensebraten"))
            inv.addItem(CustomItemGenerator.gaensebraten(10));
        if (args[1].equalsIgnoreCase("gluehwein"))
            inv.addItem(CustomItemGenerator.gluehwein());
        if (args[1].equalsIgnoreCase("midas_sword"))
            inv.addItem(CustomItemGenerator.midas_sword());
        if (args[1].equalsIgnoreCase("item_coupon_gestein"))
            inv.addItem(CustomItemGenerator.item_couponXgestein(10));
        if (args[1].equalsIgnoreCase("item_coupon_pflanze"))
            inv.addItem(CustomItemGenerator.item_couponXpflanze(10));
        if (args[1].equalsIgnoreCase("item_coupon_wolle"))
            inv.addItem(CustomItemGenerator.item_couponXwolle(10));
        if (args[1].equalsIgnoreCase("item_coupon_nahrung"))
            inv.addItem(CustomItemGenerator.item_couponXnahrung(10));
        if (args[1].equalsIgnoreCase("item_coupon_staemme"))
            inv.addItem(CustomItemGenerator.item_couponXstaemme(10));
        if (args[1].equalsIgnoreCase("item_coupon_glas"))
            inv.addItem(CustomItemGenerator.item_couponXglas(10));
        if (args[1].equalsIgnoreCase("item_coupon_trank"))
            inv.addItem(CustomItemGenerator.item_couponXtrank(10));
        if (args[1].equalsIgnoreCase("item_coupon_beton"))
            inv.addItem(CustomItemGenerator.item_couponXbeton(10));
        if (args[1].equalsIgnoreCase("item_coupon_redstone"))
            inv.addItem(CustomItemGenerator.item_couponXredstone(10));
        return true;
    }

    private boolean cmd_db_execute(@NotNull CommandSender sender, @NotNull String[] args)
    {
        StringBuilder message = new StringBuilder();
        for (int i = 1; i < args.length; i++)
            message.append(args[i]).append(" ");
        String sql_raw = message.toString();
        String sql = "";

        int begin = 0;
        Matcher match = Pattern.compile("\\{[^{]*}", Pattern.CASE_INSENSITIVE).matcher(sql_raw);
        while (match.find())
        {
            //noinspection StringConcatenationInLoop
            sql += sql_raw.substring(begin, match.start());
            String value = match.group().substring(1, match.group().length() - 1);
            OfflinePlayer player = ErikComPlugin.getPlayer(value);
            if (player == null)
            {
                String msg = ChatColor.DARK_PURPLE +  " >> Player " + ChatColor.LIGHT_PURPLE + value + ChatColor.DARK_PURPLE + " not found";
                sender.sendMessage(msg);
                return false;
            }
            sql += Database.getPlayerID(player.getUniqueId());
            begin = match.end();
        }
        sql += sql_raw.substring(begin);

        try(Connection db = DriverManager.getConnection(Database.url))
        {
            try(PreparedStatement query = Database.prepareQuery(db, sql))
            {
                ResultSet result = query.executeQuery();
                ResultSetMetaData rmeta = result.getMetaData();
                int count = 0;
                while (result.next())
                {
                    StringBuilder row = new StringBuilder();
                    for (int i = 1; i <= rmeta.getColumnCount(); i++)
                    {
                        row.append(rmeta.getColumnName(i));
                        row.append("=");
                        row.append(result.getString(i));
                        row.append("   ");
                    }
                    sender.sendMessage(ChatColor.DARK_AQUA + " >> " + row.toString());
                    if (++count == 100)
                    {
                        sender.sendMessage(ChatColor.DARK_AQUA + " >> [...]");
                        break;
                    }
                }
                if (count == 0)
                {
                    sender.sendMessage(ChatColor.DARK_AQUA + " >> [no results]");
                }
            }
        }
        catch (Exception e)
        {
            String stackTrace = ExceptionUtils.getStackTrace(e);
            String msg = ChatColor.DARK_PURPLE +  " >> Failed to execute query!";
            Component output = Component.text(msg)
                    .hoverEvent(HoverEvent.showText(Component.text(ChatColor.LIGHT_PURPLE + "click to copy stack trace")))
                    .clickEvent(ClickEvent.copyToClipboard(stackTrace));
            sender.sendMessage(output);
        }

        return true;
    }
    private boolean cmd_db_update(@NotNull CommandSender sender, @NotNull String[] args)
    {
        StringBuilder message = new StringBuilder();
        for (int i = 1; i < args.length; i++)
            message.append(args[i]).append(" ");
        String sql_raw = message.toString();
        String sql = "";

        int begin = 0;
        Matcher match = Pattern.compile("\\{[^{]*}", Pattern.CASE_INSENSITIVE).matcher(sql_raw);
        while (match.find())
        {
            //noinspection StringConcatenationInLoop
            sql += sql_raw.substring(begin, match.start());
            String value = match.group().substring(1, match.group().length() - 1);
            OfflinePlayer player = ErikComPlugin.getPlayer(value);
            if (player == null)
            {
                String msg = ChatColor.DARK_PURPLE +  " >> Player " + ChatColor.LIGHT_PURPLE + value + ChatColor.DARK_PURPLE + " not found";
                sender.sendMessage(msg);
                return false;
            }
            sql += Database.getPlayerID(player.getUniqueId());
            begin = match.end();
        }
        sql += sql_raw.substring(begin);

        try(Connection db = DriverManager.getConnection(Database.url))
        {
            try(PreparedStatement query = Database.prepareQuery(db, sql))
            {
                int result = query.executeUpdate();
                sender.sendMessage(ChatColor.DARK_AQUA + " >> row_count=" + result);
            }
        }
        catch (Exception e)
        {
            String stackTrace = ExceptionUtils.getStackTrace(e);
            String msg = ChatColor.DARK_PURPLE +  " >> Failed to execute query!";
            Component output = Component.text(msg)
                    .hoverEvent(HoverEvent.showText(Component.text(ChatColor.LIGHT_PURPLE + "click to copy stack trace")))
                    .clickEvent(ClickEvent.copyToClipboard(stackTrace));
            sender.sendMessage(output);
        }

        return true;
    }
    private boolean cmd_db_help(@NotNull CommandSender sender)
    {
        sender.sendMessage(ChatColor.GOLD + " >> Table " + Database.TableName.players  + " : Int* id, String uuid");
        sender.sendMessage(ChatColor.GOLD + " >> Table " + Database.TableName.playerData  + " : Int* user, Int* field, String data");
        sender.sendMessage(ChatColor.GOLD + " >> Table " + Database.TableName.groupTags  + " : Int* id, String name, Int priority, String tag_display, String tag_color");
        sender.sendMessage(ChatColor.GOLD + " >> Table " + Database.TableName.groupMembers  + " : Int* playerID, Int* groupID");
        sender.sendMessage(ChatColor.GOLD + " >> Table " + Database.TableName.xmas22_global  + " : Int* id, Int pos_pool, Int posX, Int posY, Int posZ");
        sender.sendMessage(ChatColor.GOLD + " >> Table " + Database.TableName.xmas22_player  + " : Int* user, Int* present_index, Int posX, Int posY, Int posZ, Bool opened");
        sender.sendMessage(ChatColor.LIGHT_PURPLE + " >> ---------------------------------");
        sender.sendMessage(ChatColor.GOLD + " >> ServerData ID : " + Database.serverID);
        sender.sendMessage(ChatColor.LIGHT_PURPLE + " >> ---------------------------------");
        sender.sendMessage(ChatColor.GOLD + " >> PlayerField.FLYING : " + Database.PlayerField.FLYING.id());
        sender.sendMessage(ChatColor.GOLD + " >> PlayerField.PVP_ON : " + Database.PlayerField.PVP_ON.id());
        sender.sendMessage(ChatColor.GOLD + " >> PlayerField.VANISH_SILENT_JOIN : " + Database.PlayerField.VANISH_SILENT_JOIN.id());
        sender.sendMessage(ChatColor.GOLD + " >> PlayerField.VANISH_SILENT_STEP : " + Database.PlayerField.VANISH_SILENT_STEP.id());
        sender.sendMessage(ChatColor.GOLD + " >> PlayerField.BACKPOS_WOLRD : " + Database.PlayerField.BACKPOS_WOLRD.id());
        sender.sendMessage(ChatColor.GOLD + " >> PlayerField.BACKPOS_X : " + Database.PlayerField.BACKPOS_X.id());
        sender.sendMessage(ChatColor.GOLD + " >> PlayerField.BACKPOS_Y : " + Database.PlayerField.BACKPOS_Y.id());
        sender.sendMessage(ChatColor.GOLD + " >> PlayerField.BACKPOS_Z : " + Database.PlayerField.BACKPOS_Z.id());
        sender.sendMessage(ChatColor.GOLD + " >> PlayerField.JOIN_DATE : " + Database.PlayerField.JOIN_DATE.id());
        sender.sendMessage(ChatColor.GOLD + " >> PlayerField.PLAYTIME : " + Database.PlayerField.PLAYTIME.id());
        sender.sendMessage(ChatColor.GOLD + " >> PlayerField.STREAMER_MODE : " + Database.PlayerField.STREAMER_MODE.id());
        sender.sendMessage(ChatColor.GOLD + " >> PlayerField.STAFF_MEMBER : " + Database.PlayerField.STAFF_MEMBER.id());
        sender.sendMessage(ChatColor.GOLD + " >> PlayerField.DEV_MEMBER : " + Database.PlayerField.DEV_MEMBER.id());
        sender.sendMessage(ChatColor.GOLD + " >> PlayerField.IS_OPERATOR : " + Database.PlayerField.IS_OPERATOR.id());
        sender.sendMessage(ChatColor.LIGHT_PURPLE + " >> ---------------------------------");
        sender.sendMessage(ChatColor.GOLD + " >> ServerField.IS_MAINTENANCE : " + Database.ServerField.IS_MAINTENANCE.id());
        sender.sendMessage(ChatColor.GOLD + " >> ServerField.COMMANDS_ACTIVE : " + Database.ServerField.COMMANDS_ACTIVE.id());
        sender.sendMessage(ChatColor.LIGHT_PURPLE + " >> ---------------------------------");
        sender.sendMessage(ChatColor.GOLD + " >> Regex : " + ChatColor.DARK_PURPLE + "{player_name}" + ChatColor.GOLD + " will be replaced with player_name's " + ChatColor.DARK_PURPLE + Database.TableName.players + ".id");
        return true;
    }
}

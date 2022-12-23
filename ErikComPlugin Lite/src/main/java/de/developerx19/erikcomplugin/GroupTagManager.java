package de.developerx19.erikcomplugin;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket;
import net.minecraft.world.scores.PlayerTeam;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_19_R1.scoreboard.CraftScoreboard;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GroupTagManager implements Listener
{
    public static class GroupTag implements Comparable<GroupTag>
    {
        String name;
        String tag;
        NamedTextColor color;
        int sortIndex;

        public GroupTag(String n, String t, String c, int i)
        {
            name = n;
            tag = t;
            color = getColor(c);
            sortIndex = i;
        }

        public static NamedTextColor getColor(String c)
        {
            return NamedTextColor.NAMES.value(c.toLowerCase());
        }

        @Override
        public int compareTo(@NotNull GroupTagManager.GroupTag o)
        {
            return o.sortIndex - sortIndex;
        }
    }

    private final static HashMap<String, GroupTag> groups = new HashMap<>();

    public static void init()
    {
        Scoreboard sb = ErikComPlugin.server.getScoreboardManager().getMainScoreboard();
        for (var t : sb.getTeams())
            if (t.getName().contains("devx"))
                t.unregister();
    }

    public static GroupTag addGroup(String name, String tag, String color, int sortIndex)
    {
        GroupTag group = new GroupTag(name, tag, color, sortIndex);
        groups.put(name, group);
        return group;
    }
    /** @noinspection unused*/
    public static void removeGroup(String name)
    {
        groups.remove(name);
    }
    public static void removeGroup(GroupTag groupTag)
    {
        groups.remove(groupTag.name);
    }
    public static GroupTag getGroup(String name)
    {
        return groups.get(name);
    }
    public static boolean hasGroup(String name)
    {
        return groups.containsKey(name);
    }
    public static Set<String> getGroups()
    {
        return groups.keySet();
    }

    public static void createGroup(String name, String display, String color, int priority)
    {
        GroupTag group = addGroup(name, display, color, priority);
        Database.createGroupTag(group);
    }
    public static void updateGroup(String name, @Nullable String display, @Nullable String color, @Nullable Integer priority)
    {
        GroupTag group = getGroup(name);

        List<Player> update = new ArrayList<>();
        for (Player player : ErikComPlugin.server.getOnlinePlayers())
        {
            PlayerData data = PlayerData.get(player);
            if (data.groups.contains(group))
            {
                data.groups.remove(group);
                update.add(player);
            }
        }

        if (display != null)
            group.tag = display.equals(" ") ? "" : display;
        if (color != null)
            group.color = GroupTag.getColor(color);
        if (priority != null)
            group.sortIndex = priority;
        for (Player player : update)
        {
            PlayerData data = PlayerData.get(player);
            data.groups.add(group);
            updateNameTagOf(player);
        }
        Database.updateGroupTag(group);
    }
    public static void deleteGroup(String name)
    {
        GroupTag group = getGroup(name);
        removeGroup(group);
        Database.deleteGroupTag(name);
        for (Player player : ErikComPlugin.server.getOnlinePlayers())
            if (PlayerData.get(player).groups.remove(group))
                updateNameTagOf(player);
    }
    public static void addMember(String group, Player player)
    {
        PlayerData.get(player).groups.add(getGroup(group));
        updateNameTagOf(player);
    }
    public static void removeMember(String group, Player player)
    {
        PlayerData.get(player).groups.remove(getGroup(group));
        updateNameTagOf(player);
    }

    private static Team makeTeam(Player player, String name)
    {
        Scoreboard sb = player.getScoreboard();
        Team t = sb.getTeam(name);
        if (t == null) t = sb.registerNewTeam(name);
        return t;
    }
    private static void removeTeams(Player player)
    {
        Scoreboard sb = player.getScoreboard();
        for (var t : sb.getTeams())
            if (t.getName().contains("devx") && t.getName().endsWith(":" + player.getName().toLowerCase()))
                t.unregister();
    }

    private static NamedTextColor getMainColor(PlayerData data)
    {
        if (data.groups.isEmpty())
            return NamedTextColor.WHITE;
        return data.groups.first().color;
    }

    private static Component getTag(GroupTag gt)
    {
        Component c = Component.empty();
        if (!gt.tag.isEmpty())
            c = Component.text(gt.tag + " ").color(gt.color);
        return c;
    }

    private static void updateNTChat(Team nt, PlayerData data)
    {
        nt.color(getMainColor(data));
        nt.prefix(Component.empty());
        nt.suffix(Component.empty());
    }
    private static void updateNTDefault(Team nt, PlayerData data)
    {
        Component pre = Component.empty();
        Component suf = Component.empty();

        for (var g : data.groups)
            pre = pre.append(getTag(g));

        if (data.streamer_mode())
            suf = suf.append(Component.text(" [LIVE]").color(TextColor.color(0xFF00FF)));
        if (data.pvpOn())
            suf = suf.append(Component.text(" [WILD]").color(TextColor.color(0xFF0000)));
        else
            suf = suf.append(Component.text(" [ZAHM]").color(TextColor.color(0x00FF00)));
        if (data.afk)
            suf = suf.append(Component.text(" [AFK]").color(TextColor.color(0xFFFF00)));

        nt.color(getMainColor(data));
        nt.prefix(pre);
        nt.suffix(suf);
    }
    private static void updateNTVanish(Team nt, PlayerData data)
    {
        Component pre = Component.empty();
        Component suf = Component.empty();

        if (data.vanished)
            pre = pre.append(Component.text("V ").color(TextColor.color(0xFF0000)));

        for (var g : data.groups)
            pre = pre.append(getTag(g));

        if (data.streamer_mode())
            suf = suf.append(Component.text(" [LIVE]").color(TextColor.color(0xFF00FF)));
        if (data.pvpOn())
            suf = suf.append(Component.text(" [WILD]").color(TextColor.color(0xFF0000)));
        else
            suf = suf.append(Component.text(" [ZAHM]").color(TextColor.color(0x00FF00)));
        if (data.afk)
            suf = suf.append(Component.text(" [AFK]").color(TextColor.color(0xFFFF00)));
        // 30 days
        if (System.currentTimeMillis() - data.init_join_date().getTime() < PlayerData.newbie_time_ms)
            suf = suf.append(Component.text(" [NEU]").color(TextColor.color(0x00CCFF)));

        nt.color(getMainColor(data));
        nt.prefix(pre);
        nt.suffix(suf);
    }

    public static void updateNameTagOf(Player player)
    {
        if (!player.isOnline()) return;
        PlayerData data = PlayerData.get(player);
        Scoreboard sb = player.getScoreboard();

        String teamName = "";
        teamName += data.afk ? 'Z' : 'A';
        for (var g : data.groups)
            //noinspection StringConcatenationInLoop
            teamName += String.format("%010d", Integer.MAX_VALUE - g.sortIndex);
        teamName += ":" + player.getName().toLowerCase();
        data.teamName = teamName;

        removeTeams(player);
        Team NTC = makeTeam(player, "devxC-" + teamName);
        Team NTD = makeTeam(player, "devxD-" + teamName);
        Team NTV = makeTeam(player, "devxV-" + teamName);

        updateNTChat(NTC, data);
        updateNTDefault(NTD, data);
        updateNTVanish(NTV, data);

        NTC.addPlayer(player);

        Packet<ClientGamePacketListener> packetDefault = ClientboundSetPlayerTeamPacket.createPlayerPacket(
                new PlayerTeam(((CraftScoreboard)sb).getHandle(), "devxD-" + teamName),
                player.getName(),
                ClientboundSetPlayerTeamPacket.Action.ADD);

        Packet<ClientGamePacketListener> packetVanish = ClientboundSetPlayerTeamPacket.createPlayerPacket(
                new PlayerTeam(((CraftScoreboard)sb).getHandle(), "devxV-" + teamName),
                player.getName(),
                ClientboundSetPlayerTeamPacket.Action.ADD);
        for (var p : ErikComPlugin.server.getOnlinePlayers())
        {
            boolean vanish_info = PermManager.check(p, PermManager.VANISH) || PermManager.check(p, PermManager.TEAMINFO);
            ((CraftPlayer)p).getHandle().connection.send(vanish_info ? packetVanish : packetDefault);
        }
    }

    public static void updateNameTagsFor(Player player)
    {
        if (!player.isOnline()) return;
        String teamType;
        if (PermManager.check(player, PermManager.VANISH) || PermManager.check(player, PermManager.TEAMINFO))
            teamType = "devxV-";
        else
            teamType = "devxD-";
        for (var p : ErikComPlugin.server.getOnlinePlayers())
        {
            Packet<ClientGamePacketListener> packet = ClientboundSetPlayerTeamPacket.createPlayerPacket(
                    new PlayerTeam(((CraftScoreboard)p.getScoreboard()).getHandle(), teamType + PlayerData.get(p).teamName),
                    p.getName(),
                    ClientboundSetPlayerTeamPacket.Action.ADD);
            ((CraftPlayer)player).getHandle().connection.send(packet);
        }
    }

    private static void destroyNameTag(Player player)
    {
        removeTeams(player);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChatMessage(AsyncChatEvent event)
    {
        if (event.isCancelled()) return;
        Player player = event.getPlayer();
        PlayerData data = PlayerData.get(player);
        Component message = event.message();
        event.setCancelled(true);
        String msg = ((TextComponent)message).content();
        TextColor msg_color = data.daemonic ? TextColor.color(0xAA0000) : TextColor.color(0xFFFFFF);
        for (var p : event.viewers())
        {
            if (p instanceof Player)
            {
                PlayerData pdata = PlayerData.get((Player)p);
                if (pdata.streamer_mode() && p != player) continue;
                String name = ((Player)p).getName().toLowerCase();
                if (pdata.staff_member())
                    name = "(@" + name + "|@team|@staff)";
                else
                    name = "@" + name;
                Matcher match = Pattern.compile(name + "(?:$|\\s)", Pattern.CASE_INSENSITIVE).matcher(msg);
                int begin = 0;
                message = Component.empty();
                while (match.find())
                {
                    message = message.append(Component.text(msg.substring(begin, match.start())).color(msg_color));
                    message = message.append(Component.text(match.group()).color(pdata.streamer_mode() ? msg_color : TextColor.color(0x00FFFF)));
                    begin = match.end();
                }
                message = message.append(Component.text(msg.substring(begin)).color(msg_color));
            }
            Component messageString = player.displayName().color(getMainColor(data)).clickEvent(ClickEvent.suggestCommand("/msg " + player.getName() + " "));
            messageString = messageString.append(Component.text(": ")).append(message);
            p.sendMessage(messageString);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event)
    {
        updateNameTagOf(event.getPlayer());
        Bukkit.getScheduler().scheduleSyncDelayedTask(ErikComPlugin.plugin, () ->
                updateNameTagsFor(event.getPlayer()), 25);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerLeave(PlayerQuitEvent event)
    {
        destroyNameTag(event.getPlayer());
    }
}

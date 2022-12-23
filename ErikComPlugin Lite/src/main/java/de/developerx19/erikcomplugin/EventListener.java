package de.developerx19.erikcomplugin;

import io.papermc.paper.event.player.AsyncChatEvent;
import io.papermc.paper.event.player.PlayerTradeEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.block.Sign;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.server.ServerListPingEvent;

public class EventListener implements Listener
{

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent event)
    {
        Player player = event.getPlayer();
        Database.loadPlayerData(player);
        PlayerData data = PlayerData.get(player);
        data.setFlightMode();
        data.setOpMode();
        data.resetAFK();
        EventXmas22Manager.initPlayer(player);
        int playerAutoUpdateLoop = Bukkit.getScheduler().scheduleSyncRepeatingTask(ErikComPlugin.plugin, data::autoUpdateLoop, 12000, 12000);
        data.owned_tasks.add(playerAutoUpdateLoop);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerLeave(PlayerQuitEvent event)
    {
        Player player = event.getPlayer();
        Database.savePlayerData(player);
        PlayerData data = PlayerData.get(player);
        data.setAFK();

        for (int task : PlayerData.get(player).owned_tasks)
            Bukkit.getScheduler().cancelTask(task);

        Bukkit.getScheduler().runTaskLater(ErikComPlugin.plugin, () ->
        {
            if (player.isOnline()) return;
            PlayerData.map.remove(player.getUniqueId());
        }, 20);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event)
    {
        PlayerData.get(event.getPlayer()).setFlightMode();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerGameModeChange(PlayerGameModeChangeEvent event)
    {
        if (event.isCancelled()) return;
        Player player = event.getPlayer();
        Bukkit.getScheduler().scheduleSyncDelayedTask(ErikComPlugin.plugin, () ->
        {
            if (player.isOnline())
                PlayerData.get(player).setFlightMode();
        }, 1L);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent event)
    {
        var msg = event.deathMessage();
        if (msg == null) return;
        for (var player : ErikComPlugin.server.getOnlinePlayers())
        {
            PlayerData data = PlayerData.get(player);
            if (data.streamer_mode())
                player.sendMessage(Component.text("☠ ").append(event.getEntity().teamDisplayName()));
            else
                player.sendMessage(msg);
        }
        event.deathMessage(null);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerRespawn(PlayerRespawnEvent event)
    {
        PlayerData.get(event.getPlayer()).setFlightMode();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerToggleFlight(PlayerToggleFlightEvent event)
    {
        if (event.isCancelled()) return;
        PlayerData data = PlayerData.get(event.getPlayer());
        data.flying(event.isFlying());
        data.setFlightMode();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockExplosion (BlockExplodeEvent event)
    {
        if (event.isCancelled()) return;
        Location p = event.getBlock().getLocation();
        if (p.distance(new Location(p.getWorld(), 0, p.getY(), 0)) > 500) return;
        event.setCancelled(true);
        p.getWorld().playSound(p, Sound.ENTITY_WITCH_AMBIENT, 3.0f, 1.0f);
        p.getWorld().playSound(p, Sound.ENTITY_WITCH_CELEBRATE, 3.0f, 1.2f);
        p.getWorld().playSound(p, Sound.ENTITY_RAVAGER_CELEBRATE, 3.0f, 1.4f);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityExplosion(EntityExplodeEvent event)
    {
        if (event.isCancelled()) return;
        Location p = event.getEntity().getLocation();
        if (p.distance(new Location(p.getWorld(), 0, p.getY(), 0)) > 500) return;
        event.setCancelled(true);
        p.getWorld().playSound(p, Sound.ENTITY_WITCH_AMBIENT, 3.0f, 1.0f);
        p.getWorld().playSound(p, Sound.ENTITY_WITCH_CELEBRATE, 3.0f, 1.2f);
        p.getWorld().playSound(p, Sound.ENTITY_RAVAGER_CELEBRATE, 3.0f, 1.4f);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onExplosionDamage(EntityDamageEvent event)
    {
        if (event.isCancelled()) return;
        Location p = event.getEntity().getLocation();
        if (p.distance(new Location(p.getWorld(), 0, p.getY(), 0)) > 500) return;
        if (event.getCause() != EntityDamageEvent.DamageCause.ENTITY_EXPLOSION
                && event.getCause() != EntityDamageEvent.DamageCause.BLOCK_EXPLOSION) return;
        event.setCancelled(true);
    }

    @EventHandler
    public void onExplosionWind(HangingBreakEvent event)
    {
        if (event.isCancelled()) return;
        Location p = event.getEntity().getLocation();
        if (p.distance(new Location(p.getWorld(), 0, p.getY(), 0)) > 500) return;
        if (event.getCause() != HangingBreakEvent.RemoveCause.EXPLOSION) return;
        event.setCancelled(true);
    }


    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerConnect(PlayerLoginEvent event)
    {
        if (!ErikComPlugin.is_maintenance()) return;
        long id = Database.getPlayerID(event.getPlayer());
        boolean staff = Database.getDataBoolean(id, Database.PlayerField.STAFF_MEMBER, false);
        boolean dev = Database.getDataBoolean(id, Database.PlayerField.DEV_MEMBER, false);
        if (staff || dev) return;
        event.disallow(PlayerLoginEvent.Result.KICK_OTHER, Component.text(
                ChatColor.RED + "The server is currently in maintenance mode\n" +
                        ChatColor.DARK_PURPLE + "You are not permitted to join right now!\n" +
                        ChatColor.GOLD + "Please contact staff members for more information"));
    }
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerPing(ServerListPingEvent event)
    {
        var i = event.iterator();
        while(i.hasNext())
        {
            PlayerData data = PlayerData.get(i.next());
            if (data == null || data.vanished)
                i.remove();
        }
        if (!ErikComPlugin.is_maintenance()) return;
        event.motd(Component.text(ChatColor.RED + "Server maintenance mode is currently enabled!"));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerChat(AsyncChatEvent event)
    {
        PlayerData.get(event.getPlayer()).resetAFK();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerMoveCursor(PlayerMoveEvent event)
    {
        if (event.getFrom().getPitch() == event.getTo().getPitch() && event.getFrom().getYaw() == event.getTo().getYaw()) return;
        PlayerData.get(event.getPlayer()).resetAFK();
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onVoidTrade(PlayerTradeEvent event)
    {
        if (event.isCancelled()) return;
        Location pos = event.getVillager().getLocation();
        if (pos.getWorld().getBlockAt(pos.add(0.0, -2.0, 0.0)).getType() != Material.END_GATEWAY) return;
        event.setIncreaseTradeUses(false);
        event.setRewardExp(false);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPhantomSpawn(EntitySpawnEvent event)
    {
        if (!(event.getEntity() instanceof Phantom)) return;
        Location p = event.getLocation();
        if (p.getWorld().getEnvironment() != World.Environment.NORMAL) return;
        if (p.distance(new Location(p.getWorld(), 0, p.getY(), 0)) > 500) return;
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onMobGrief(EntityChangeBlockEvent event)
    {
        if (event.isCancelled()) return;
        Location p = event.getBlock().getLocation();
        if (p.distance(new Location(p.getWorld(), 0, p.getY(), 0)) > 500) return;
        if (event.getEntity() instanceof Enderman)
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onSignEdit(PlayerInteractEvent event)
    {
        if (event.getClickedBlock() == null) return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getItem() != null && event.getItem().getType() != Material.FEATHER) return;
        var block = event.getClickedBlock();
        if (!block.getType().toString().endsWith("_SIGN")) return;
        var player = event.getPlayer();
        player.openSign((Sign)block.getState());
    }
}

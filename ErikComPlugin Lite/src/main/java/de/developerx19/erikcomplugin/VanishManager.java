package de.developerx19.erikcomplugin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.GameEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockReceiveGameEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.TabCompleteEvent;

import java.util.ArrayList;
import java.util.List;

public class VanishManager implements Listener
{
    public static void updateVisibilityOf(Player player)
    {
        PlayerData data = PlayerData.get(player);
        player.setCollidable(!data.vanished);
        data.setFlightMode();
        for (var p : ErikComPlugin.server.getOnlinePlayers())
        {
            if (data.vanished && !PermManager.check(p, PermManager.VANISH))
                p.hidePlayer(ErikComPlugin.plugin, player);
            else
                p.showPlayer(ErikComPlugin.plugin, player);
        }
    }

    public static void updateVisibilityFor(Player player)
    {
        PlayerData pdata = PlayerData.get(player);
        player.setCollidable(!pdata.vanished);
        for (var p : ErikComPlugin.server.getOnlinePlayers())
        {
            PlayerData data = PlayerData.get(p);
            if (data.vanished && !PermManager.check(player, PermManager.VANISH))
                player.hidePlayer(ErikComPlugin.plugin, p);
            else
                player.showPlayer(ErikComPlugin.plugin, p);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerDamage(EntityDamageEvent event)
    {
        if (!(event.getEntity() instanceof Player)) return;
        if (!PlayerData.get((Player) event.getEntity()).vanished) return;
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent event)
    {
        if (!PlayerData.get(event.getPlayer()).vanished) return;
        Component deathMessage = event.deathMessage();
        if (deathMessage == null) return;
        event.deathMessage(null);
        deathMessage = deathMessage.append(Component.text(" (silent)").color(TextColor.color(0xFF0000)));
        for (var p : ErikComPlugin.server.getOnlinePlayers())
            if (PermManager.check(p, PermManager.VANISH))
                p.sendMessage(deathMessage);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onFoodLevelChange(FoodLevelChangeEvent event)
    {
        if (!(event.getEntity() instanceof Player)) return;
        if (!PlayerData.get((Player) event.getEntity()).vanished) return;
        if (event.getEntity().getFoodLevel() < event.getFoodLevel()) return;
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onTarget(EntityTargetEvent event)
    {
        if (!(event.getTarget() instanceof Player)) return;
        if (!PlayerData.get((Player) event.getTarget()).vanished) return;
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onSculkSensorTrigger(BlockReceiveGameEvent event)
    {
        if (event.isCancelled()) return;
        if (event.getEvent() != GameEvent.SCULK_SENSOR_TENDRILS_CLICKING) return;
        if (!(event.getEntity() instanceof Player)) return;
        PlayerData data = PlayerData.get(event.getEntity().getUniqueId());
        if (!data.vanished) return;
        if (!data.silent_step()) return;
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerCropTrample(PlayerInteractEvent event)
    {
        PlayerData data = PlayerData.get(event.getPlayer());
        if (!data.vanished) return;
        if (!data.silent_step()) return;
        if (event.getAction() != Action.PHYSICAL) return;
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerCollectItem(EntityPickupItemEvent event)
    {
        if (!(event.getEntity() instanceof Player)) return;
        PlayerData data = PlayerData.get((Player) event.getEntity());
        if (!data.vanished) return;
        if (!data.silent_step()) return;
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onTabComplete(TabCompleteEvent event)
    {
        if (PermManager.check(event.getSender(), PermManager.VANISH)) return;
        List<String> vanished = new ArrayList<>();
        for (var c : event.getCompletions())
        {
            Player player = ErikComPlugin.server.getPlayer(c);
            if (player == null) continue;
            if (!player.getName().equalsIgnoreCase(c)) continue;
            PlayerData data = PlayerData.get(player);
            if (data.vanished)
                vanished.add(c);
        }
        event.getCompletions().removeAll(vanished);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event)
    {
        Player player = event.getPlayer();
        PlayerData pdata = PlayerData.get(player);
        if (PermManager.check(player, PermManager.VANISH) && pdata.silent_join())
            pdata.vanished = true;
        Component messageString = Component.text(player.getName() + " joined the server").color(TextColor.color(0xFFFF55));
        if (pdata.vanished)
            messageString = messageString.append(Component.text(" silently").color(TextColor.color(0xFF0000)));
        event.joinMessage(null);
        for (var p : ErikComPlugin.server.getOnlinePlayers())
        {
            if ((!pdata.vanished) || PermManager.check(p, PermManager.VANISH))
                p.sendMessage(messageString);
        }
        updateVisibilityOf(player);
        updateVisibilityFor(player);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerLeave(PlayerQuitEvent event)
    {
        Player player = event.getPlayer();
        PlayerData pdata = PlayerData.get(player);
        Component messageString = Component.text(player.getName() + " left the server").color(TextColor.color(0xFFFF55));
        if (pdata.vanished)
            messageString = messageString.append(Component.text(" silently").color(TextColor.color(0xFF0000)));
        event.quitMessage(null);
        for (var p : ErikComPlugin.server.getOnlinePlayers())
        {
            if ((!pdata.vanished) || PermManager.check(p, PermManager.VANISH))
                p.sendMessage(messageString);
        }
    }
}

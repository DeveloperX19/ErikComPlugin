package de.developerx19.erikcomplugin;

import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.AreaEffectCloudApplyEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PVPManager implements Listener
{

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockExplosion(PlayerInteractEvent event)
    {
        if (event.getClickedBlock() == null) return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        var block = event.getClickedBlock();

        if (
                !(block.getType() == Material.RESPAWN_ANCHOR && block.getWorld().getEnvironment() != World.Environment.NETHER)
                && ! (block.getType().toString().toLowerCase().endsWith("bed") && block.getWorld().getEnvironment() != World.Environment.NORMAL)
        ) return;

        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onCrystalActivate(EntityDamageByEntityEvent event)
    {
        if (event.isCancelled()) return;
        if (!(event.getEntity() instanceof EnderCrystal)) return;
        Player killer = null;

        if (event.getDamager() instanceof Player)
            killer = (Player) event.getDamager();

        if (event.getDamager() instanceof Projectile)
        {
            Projectile projectile = (Projectile) event.getDamager();
            if (projectile.getShooter() instanceof Player)
                killer = (Player) projectile.getShooter();
        }

        if (event.getDamager() instanceof TNTPrimed)
        {
            TNTPrimed tnt = (TNTPrimed) event.getDamager();
            if (tnt.getSource() instanceof Player)
                killer = (Player) tnt.getSource();
        }

        NamespacedKey key = new NamespacedKey(ErikComPlugin.plugin, "PVP_CrystalKiller");
        if (event.getDamager() instanceof EnderCrystal)
        {
            EnderCrystal crystal = (EnderCrystal) event.getDamager();
            var data = crystal.getPersistentDataContainer();
            String uuid = data.get(key, PersistentDataType.STRING);
            if (uuid != null)
                killer = ErikComPlugin.server.getPlayer(UUID.fromString(uuid));
        }

        if (killer == null) return;
        event.getEntity().getPersistentDataContainer().set(key, PersistentDataType.STRING, killer.getUniqueId().toString());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerPVP(EntityDamageByEntityEvent event)
    {
        if (event.isCancelled()) return;

        Player attacker = null;

        if (event.getDamager() instanceof Player)
            attacker = (Player) event.getDamager();

        if (event.getDamager() instanceof Projectile)
        {
            Projectile projectile = (Projectile) event.getDamager();
            if (projectile.getShooter() instanceof Player)
                attacker = (Player) projectile.getShooter();
        }

        if (event.getDamager() instanceof AreaEffectCloud)
        {
            AreaEffectCloud effectCloud = (AreaEffectCloud) event.getDamager();
            if (effectCloud.getSource() instanceof Player)
                attacker = (Player) effectCloud.getSource();
        }

        if (event.getDamager() instanceof TNTPrimed)
        {
            TNTPrimed tnt = (TNTPrimed) event.getDamager();
            if (tnt.getSource() instanceof Player)
                attacker = (Player) tnt.getSource();
        }

        if (event.getDamager() instanceof EnderCrystal)
        {
            EnderCrystal crystal = (EnderCrystal) event.getDamager();
            NamespacedKey key = new NamespacedKey(ErikComPlugin.plugin, "PVP_CrystalKiller");
            var data = crystal.getPersistentDataContainer();
            String uuid = data.get(key, PersistentDataType.STRING);
            if (uuid != null)
                attacker = ErikComPlugin.server.getPlayer(UUID.fromString(uuid));
        }

        if (attacker == null) return;

        Player victim;
        if (event.getEntity() instanceof Player)
            victim = (Player) event.getEntity();
        else if (event.getEntity() instanceof Tameable)
        {
            Tameable vic = (Tameable) event.getEntity();
            if (vic.getOwnerUniqueId() == null) return;
            victim = ErikComPlugin.server.getPlayer(vic.getOwnerUniqueId());
        }
        else return;
        if (victim == null)
        {
            event.setCancelled(true);
            Component text = Component.text(ChatColor.RED + "PVP disabled");
            attacker.sendActionBar(text);
            return;
        }
        PlayerData vdata = PlayerData.get(victim);
        PlayerData adata = PlayerData.get(attacker);
        if (!(vdata.pvpOn() && adata.pvpOn()) && victim != attacker)
        {
            event.setCancelled(true);
            Component text = Component.text(ChatColor.RED + "PVP disabled");
            attacker.sendActionBar(text);
        }
        else
            adata.pvpLastChange = vdata.pvpLastChange = System.currentTimeMillis();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerPVP_CloudPotion(AreaEffectCloudApplyEvent event)
    {
        if (event.isCancelled()) return;
        AreaEffectCloud potion = event.getEntity();
        if (!(potion.getSource() instanceof Player)) return;
        Player attacker = (Player) potion.getSource();
        if (!InfoMap.potion_effect_bad_damaging.contains(potion.getBasePotionData().getType().getEffectType())) return;
        PlayerData adata = PlayerData.get(attacker);

        var victims = event.getAffectedEntities();
        List<LivingEntity> friendly = new ArrayList<>();
        for (var entity : victims)
        {
            Player victim;
            if (entity instanceof Player)
                victim = (Player) entity;
            else if (entity instanceof Tameable)
            {
                Tameable vic = (Tameable) entity;
                if (vic.getOwnerUniqueId() == null) continue;
                victim = ErikComPlugin.server.getPlayer(vic.getOwnerUniqueId());
            }
            else continue;
            if (victim == null)
            {
                friendly.add(entity);
                Component text = Component.text(ChatColor.RED + "PVP disabled");
                attacker.sendActionBar(text);
                continue;
            }

            PlayerData vdata = PlayerData.get(victim);
            if (!(vdata.pvpOn() && adata.pvpOn()) && victim != attacker)
            {
                friendly.add(entity);
                Component text = Component.text(ChatColor.RED + "PVP disabled");
                attacker.sendActionBar(text);
            }
            else
                adata.pvpLastChange = vdata.pvpLastChange = System.currentTimeMillis();
        }
        victims.removeAll(friendly);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerPVP_Potion(PotionSplashEvent event)
    {
        if (event.isCancelled()) return;
        ThrownPotion potion = event.getEntity();
        if (!(potion.getShooter() instanceof Player)) return;
        Player attacker = (Player) potion.getShooter();
        var victims = event.getAffectedEntities();

        boolean bad_effect = false;
        List<PotionEffectType> effects = new ArrayList<>();
        for (var e : potion.getEffects())
            effects.add(e.getType());
        for (var e : InfoMap.potion_effect_bad_damaging)
            bad_effect |= effects.contains(e);
        if (!bad_effect) return;

        PlayerData adata = PlayerData.get(attacker);
        for (var entity : victims)
        {
            Player victim;
            if (entity instanceof Player)
                victim = (Player) entity;
            else if (entity instanceof Tameable)
            {
                Tameable vic = (Tameable) entity;
                if (vic.getOwnerUniqueId() == null) continue;
                victim = ErikComPlugin.server.getPlayer(vic.getOwnerUniqueId());
            }
            else continue;
            if (victim == null)
            {
                event.setIntensity(entity, 0);
                Component text = Component.text(ChatColor.RED + "PVP disabled");
                attacker.sendActionBar(text);
                continue;
            }

            PlayerData vdata = PlayerData.get(victim);
            if (!(vdata.pvpOn() && adata.pvpOn()) && victim != attacker)
            {
                event.setIntensity(entity, 0);
                Component text = Component.text(ChatColor.RED + "PVP disabled");
                attacker.sendActionBar(text);
            }
            else
                adata.pvpLastChange = vdata.pvpLastChange = System.currentTimeMillis();
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerPVP_Fire(BlockPlaceEvent event)
    {
        if (event.isCancelled()) return;
        if (event.getBlock().getType() != Material.FIRE) return;

        Player attacker = event.getPlayer();
        PlayerData adata = PlayerData.get(attacker);

        for (var entity : event.getBlock().getWorld().getNearbyEntities(event.getBlock().getLocation(), 3, 2, 3))
        {
            Player victim;
            if (entity instanceof Player)
                victim = (Player) entity;
            else if (entity instanceof Tameable)
            {
                Tameable vic = (Tameable) entity;
                if (vic.getOwnerUniqueId() == null) continue;
                victim = ErikComPlugin.server.getPlayer(vic.getOwnerUniqueId());
            }
            else continue;
            if (victim == null)
            {
                event.setCancelled(true);
                Component text = Component.text(ChatColor.RED + "PVP disabled");
                attacker.sendActionBar(text);
                return;
            }
            PlayerData vdata = PlayerData.get(victim);
            if (victim == attacker || (adata.pvpOn() && vdata.pvpOn()) || ((vdata.vanished || victim.getGameMode() == GameMode.SPECTATOR) && entity instanceof Player)) continue;
            Component text = Component.text(ChatColor.RED + "PVP disabled");
            attacker.sendActionBar(text);
            event.setCancelled(true);
            return;
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerPVP_Lava(PlayerBucketEmptyEvent event)
    {
        if (event.isCancelled()) return;
        if (event.getBucket() != Material.LAVA_BUCKET) return;

        Player attacker = event.getPlayer();
        PlayerData adata = PlayerData.get(attacker);

        for (var entity : event.getBlock().getWorld().getNearbyEntities(event.getBlock().getLocation(), 4, 3, 4))
        {
            Player victim;
            if (entity instanceof Player)
                victim = (Player) entity;
            else if (entity instanceof Tameable)
            {
                Tameable vic = (Tameable) entity;
                if (vic.getOwnerUniqueId() == null) continue;
                victim = ErikComPlugin.server.getPlayer(vic.getOwnerUniqueId());
            }
            else continue;
            if (victim == null)
            {
                event.setCancelled(true);
                Component text = Component.text(ChatColor.RED + "PVP disabled");
                attacker.sendActionBar(text);
                return;
            }
            PlayerData vdata = PlayerData.get(victim);
            if (victim == attacker || (adata.pvpOn() && vdata.pvpOn()) || ((vdata.vanished || victim.getGameMode() == GameMode.SPECTATOR) && entity instanceof Player)) continue;
            Component text = Component.text(ChatColor.RED + "PVP disabled");
            attacker.sendActionBar(text);
            event.setCancelled(true);
            return;
        }
    }
}

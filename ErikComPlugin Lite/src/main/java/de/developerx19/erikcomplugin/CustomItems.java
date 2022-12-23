package de.developerx19.erikcomplugin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import java.util.Collections;


public class CustomItems implements Listener
{
    public boolean slot_is_air(ItemStack slot)
    {
        return slot == null || slot.getType() == Material.AIR || slot.getAmount() <= 0;
    }
    public boolean slot_is_mat(ItemStack slot, Material type)
    {
        return slot != null && slot.getType() == type && slot.getAmount() == 1;
    }

    // ---------------------------------------------------------------------------------------------------------------------------------------------
    //                                                           Dämonische Wurzel
    // ---------------------------------------------------------------------------------------------------------------------------------------------

    @EventHandler(priority = EventPriority.LOW)
    public void onDemonRootPreCraft(PrepareItemCraftEvent event)
    {
        var inventory = event.getInventory();
        var items = inventory.getMatrix();
        if (items.length != 9) return;

        boolean correct = false;

        // Crafting Rezept ist nicht öffentlich bekannt, daher im open source code nicht verfügbar

        if (!correct) return;

        inventory.setResult(CustomItemGenerator.demonic_root(1));
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onDemonRootUse(BlockPlaceEvent event)
    {
        if (event.isCancelled()) return;

        var key = new NamespacedKey(ErikComPlugin.plugin, "demonic_root");
        var item = event.getItemInHand();
        var meta = item.getItemMeta();
        if (meta == null || !meta.getPersistentDataContainer().has(key)) return;

        event.setCancelled(true);
        PlayerData pdata = PlayerData.get(event.getPlayer());
        if (pdata == null || pdata.pre_daemonic) return;
        item.setAmount(item.getAmount() - 1);
        pdata.pre_daemonic = true;
        Location location = event.getBlock().getLocation();
        Player player = event.getPlayer();
        location.getWorld().playSound(location, Sound.ENTITY_ZOMBIE_VILLAGER_CURE, SoundCategory.MASTER, 5.0f, 1.0f);
        location.getWorld().playSound(location, Sound.ENTITY_ENDER_DRAGON_GROWL, SoundCategory.MASTER, 5.0f, 1.0f);
        player.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 370, 1));

        for (int i = 0; i < 8; i++)
        {
            Bukkit.getScheduler().scheduleSyncDelayedTask(ErikComPlugin.plugin,
                    () -> location.getWorld().playSound(location, Sound.ENTITY_WARDEN_HEARTBEAT, SoundCategory.MASTER, 5.0f, 1.0f), 50 + i * 25);
        }

        var particleCounter = new Object(){ int count = 1; };
        int daemonParticleSpawner = Bukkit.getScheduler().scheduleSyncRepeatingTask(ErikComPlugin.plugin, () ->
        {
            particleCounter.count += 1;
            location.getWorld().spawnParticle(Particle.FALLING_LAVA, location.clone().add(.5, .5, .5), particleCounter.count, .4, 2, .4);
        }, 0, 3);

        Bukkit.getScheduler().scheduleSyncDelayedTask(ErikComPlugin.plugin, () ->
        {
            location.getWorld().playSound(location, Sound.BLOCK_CANDLE_EXTINGUISH, SoundCategory.MASTER, 5.0f, 0.7f);
            Bukkit.getScheduler().cancelTask(daemonParticleSpawner);
            PlayerData data = PlayerData.get(player);
            if (data != null)
            {
                for (int i = 0; i < 5; i++)
                    location.getWorld().spawnEntity(location, EntityType.LIGHTNING);
                data.daemonic = true;
            }
        }, 300);

        int playerParticleSpawner = Bukkit.getScheduler().scheduleSyncRepeatingTask(ErikComPlugin.plugin, () ->
        {
            if (!player.isOnline()) return;
            player.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, player.getLocation().add(0, 1, 0), 2, .15, .3, .15, .01);
        }, 300, 2);
        pdata.owned_tasks.add(playerParticleSpawner);

        int daemon_reset = Bukkit.getScheduler().scheduleSyncDelayedTask(ErikComPlugin.plugin, () ->
        {
            Bukkit.getScheduler().cancelTask(playerParticleSpawner);
            PlayerData data = PlayerData.get(player);
            if (data != null)
            {
                data.daemonic = false;
                data.pre_daemonic = false;
            }
        }, 2700);
        pdata.owned_tasks.add(daemon_reset);
    }

    // ---------------------------------------------------------------------------------------------------------------------------------------------
    //                                                           C4
    // ---------------------------------------------------------------------------------------------------------------------------------------------

    @EventHandler(priority = EventPriority.NORMAL)
    public void onUseC4(BlockPlaceEvent event)
    {
        if (event.isCancelled()) return;

        var key = new NamespacedKey(ErikComPlugin.plugin, "explosive_c4");
        var item = event.getItemInHand();
        var meta = item.getItemMeta();
        if (meta == null || !meta.getPersistentDataContainer().has(key)) return;
        Integer type = meta.getPersistentDataContainer().get(key, PersistentDataType.INTEGER);
        if (type == null || type != 1) return;

        event.setCancelled(true);
        if (event.getBlockAgainst().isEmpty()) return;
        if (event.getBlockAgainst().getType() == Material.COMMAND_BLOCK) return;
        if (event.getBlockAgainst().getType() == Material.CHAIN_COMMAND_BLOCK) return;
        if (event.getBlockAgainst().getType() == Material.REPEATING_COMMAND_BLOCK) return;
        if (event.getBlockAgainst().getType() == Material.BARRIER) return;
        if (event.getBlockAgainst().getType() == Material.STRUCTURE_VOID) return;
        if (event.getBlockAgainst().getType() == Material.STRUCTURE_BLOCK) return;
        if (event.getBlockAgainst().getType() == Material.END_PORTAL) return;
        if (event.getBlockAgainst().getType() == Material.NETHER_PORTAL) return;
        if (event.getBlockAgainst().getType() == Material.END_GATEWAY) return;


        event.getPlayer().getWorld().playSound(event.getBlock().getLocation(), Sound.ENTITY_GENERIC_EXPLODE, SoundCategory.BLOCKS, 0.85f, 1.1f);
        event.getPlayer().getWorld().spawnParticle(Particle.SMOKE_NORMAL,
                event.getBlock().getLocation().add(0.5, 0.5, 0.5), 80, 0.25, 0.25, 0.25, 0.08);
        ErikComPlugin.server.getPluginManager().callEvent(new BlockBreakEvent(event.getBlockAgainst(), event.getPlayer()));
        event.getBlockAgainst().breakNaturally();
        item.setAmount(item.getAmount() - 1);
    }

    // ---------------------------------------------------------------------------------------------------------------------------------------------
    //                                                           Gänsebraten
    // ---------------------------------------------------------------------------------------------------------------------------------------------

    @EventHandler(priority = EventPriority.NORMAL)
    public void onUseGaensebraten(PlayerItemConsumeEvent event)
    {
        if (event.isCancelled()) return;

        var key = new NamespacedKey(ErikComPlugin.plugin, "gaensebraten");
        var item = event.getItem();
        var meta = item.getItemMeta();
        if (meta == null || !meta.getPersistentDataContainer().has(key)) return;

        Player player = event.getPlayer();

        player.setFoodLevel(20);
        player.setSaturation(player.getSaturation() + 5);
    }

    // ---------------------------------------------------------------------------------------------------------------------------------------------
    //                                                           Midas-Schwert
    // ---------------------------------------------------------------------------------------------------------------------------------------------

    @EventHandler(priority = EventPriority.NORMAL)
    public void onUseMidasSword(EntityDamageByEntityEvent event)
    {
        if (event.isCancelled()) return;
        if (!(event.getDamager() instanceof Player)) return;
        if (!(event.getEntity() instanceof LivingEntity)) return;

        if (event.getFinalDamage() < ((LivingEntity) event.getEntity()).getHealth()) return;

        var key = new NamespacedKey(ErikComPlugin.plugin, "midas_sword");
        var item = ((Player)event.getDamager()).getInventory().getItemInMainHand();
        var meta = item.getItemMeta();
        if (meta == null || !meta.getPersistentDataContainer().has(key)) return;

        var durability = meta.getPersistentDataContainer().get(key, PersistentDataType.INTEGER);
        if (durability == null || durability <= 0) return;
        durability -= 1;
        meta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, durability);
        if (durability > 0)
            meta.lore(Collections.singletonList(Component.text("Haltbarkeit: " + durability + " / 5000")));
        else
            meta.lore(Collections.singletonList(Component.text("Abgenutzt")));
        item.setItemMeta(meta);

        Location centroid = event.getEntity().getLocation().add(0, 0, 0);

        int count = 1;
        while (true)
        {
            double roll = ErikComPlugin.rng.nextDouble();
            if (roll > .75) break;
            count += 1;
            if (roll > .5) continue;
            count += 1;
            if (roll > .25) continue;
            count += 1;
        }
        centroid.getWorld().dropItemNaturally(centroid, new ItemStack(Material.GOLD_NUGGET, count));

        count = 0;
        while (true)
        {
            double roll = ErikComPlugin.rng.nextDouble();
            if (roll > .5) break;
            count += 1;
        }
        centroid.getWorld().dropItemNaturally(centroid, new ItemStack(Material.GOLD_INGOT, count));

        count = 0;
        while (true)
        {
            double roll = ErikComPlugin.rng.nextDouble();
            if (roll > .1) break;
            count += 1;
        }
        centroid.getWorld().dropItemNaturally(centroid, new ItemStack(Material.RAW_GOLD, count));

        count = 0;
        for (int i = 0; i < 3; i++)
        {
            double roll = ErikComPlugin.rng.nextDouble();
            if (roll > .05) break;
            count += 1;
        }
        centroid.getWorld().dropItemNaturally(centroid, new ItemStack(Material.GOLD_BLOCK, count));

        count = 0;
        while (true)
        {
            double roll = ErikComPlugin.rng.nextDouble();
            if (roll > .01) break;
            count += 1;
        }
        centroid.getWorld().dropItemNaturally(centroid, new ItemStack(Material.RAW_GOLD_BLOCK, count));

        count = 0;
        for (int i = 0; i < 3; i++)
        {
            double roll = ErikComPlugin.rng.nextDouble();
            if (roll > .05) break;
            count += 1;
        }
        centroid.getWorld().dropItemNaturally(centroid, new ItemStack(Material.GOLDEN_APPLE, count));

        double roll = ErikComPlugin.rng.nextDouble();
        if (roll > .001) return;
        centroid.getWorld().dropItemNaturally(centroid, new ItemStack(Material.ENCHANTED_GOLDEN_APPLE, 1));

    }

    // ---------------------------------------------------------------------------------------------------------------------------------------------
    //                                                           Item-Coupon
    // ---------------------------------------------------------------------------------------------------------------------------------------------

    private static ItemStack makeABasicPotion(PotionType type)
    {
        ItemStack potion = new ItemStack(Material.POTION, 1);
        var meta = potion.getItemMeta();
        ((PotionMeta) meta).setBasePotionData(new PotionData(type, false, false));
        potion.setItemMeta(meta);
        return potion;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onCouponOpen(PlayerInteractEvent event)
    {
        if (!event.getAction().isRightClick()) return;
        var item = event.getPlayer().getInventory().getItemInMainHand();
        if (!item.equals(event.getItem())) return;

        var key = new NamespacedKey(ErikComPlugin.plugin, "item_coupon");
        var meta = item.getItemMeta();
        if (meta == null || !meta.getPersistentDataContainer().has(key)) return;
        String type = meta.getPersistentDataContainer().get(key, PersistentDataType.STRING);
        if (type == null) return;

        Player player = event.getPlayer();
        Inventory gui;

        if (type.equalsIgnoreCase("gestein"))
        {
            gui = Bukkit.createInventory(player, 27, Component.text("Wähle einen Slot").color(TextColor.color(0xFF00FF)));
            gui.setItem(0, new ItemStack(Material.COBBLESTONE, 64));
            gui.setItem(1, new ItemStack(Material.STONE, 64));
            gui.setItem(2, new ItemStack(Material.ANDESITE, 64));
            gui.setItem(3, new ItemStack(Material.DIORITE, 64));
            gui.setItem(4, new ItemStack(Material.CALCITE, 64));
            gui.setItem(5, new ItemStack(Material.DEEPSLATE, 64));
            gui.setItem(6, new ItemStack(Material.BASALT, 64));
            gui.setItem(7, new ItemStack(Material.COBBLED_DEEPSLATE, 64));
            gui.setItem(8, new ItemStack(Material.BLACKSTONE, 64));

            gui.setItem(9, new ItemStack(Material.TUFF, 64));
            gui.setItem(10, new ItemStack(Material.DRIPSTONE_BLOCK, 64));
            gui.setItem(11, new ItemStack(Material.GRANITE, 64));
            gui.setItem(12, new ItemStack(Material.COAL_ORE, 48));
            gui.setItem(13, new ItemStack(Material.RAW_IRON_BLOCK, 32));
            gui.setItem(14, new ItemStack(Material.RAW_COPPER_BLOCK, 32));
            gui.setItem(15, new ItemStack(Material.RAW_GOLD_BLOCK, 32));
            gui.setItem(16, new ItemStack(Material.NETHERRACK, 48));
            gui.setItem(17, new ItemStack(Material.NETHER_BRICKS, 48));

            gui.setItem(18, new ItemStack(Material.GRAVEL, 64));
            gui.setItem(19, new ItemStack(Material.REDSTONE_ORE, 32));
            gui.setItem(20, new ItemStack(Material.EMERALD_ORE, 16));
            gui.setItem(21, new ItemStack(Material.LAPIS_ORE, 16));
            gui.setItem(22, new ItemStack(Material.IRON_ORE, 48));
            gui.setItem(23, new ItemStack(Material.COPPER_ORE, 48));
            gui.setItem(24, new ItemStack(Material.GOLD_ORE, 48));
            gui.setItem(25, new ItemStack(Material.NETHER_GOLD_ORE, 48));
            gui.setItem(26, new ItemStack(Material.NETHER_QUARTZ_ORE, 48));
        }
        else if (type.equalsIgnoreCase("pflanze"))
        {
            gui = Bukkit.createInventory(player, 18, Component.text("Wähle einen Slot").color(TextColor.color(0xFF00FF)));
            gui.setItem(0, new ItemStack(Material.SUGAR_CANE, 64));
            gui.setItem(1, new ItemStack(Material.WHEAT_SEEDS, 64));
            gui.setItem(2, new ItemStack(Material.COCOA_BEANS, 64));
            gui.setItem(3, new ItemStack(Material.PUMPKIN, 64));
            gui.setItem(4, new ItemStack(Material.MELON_SLICE, 64));
            gui.setItem(5, new ItemStack(Material.SWEET_BERRIES, 64));
            gui.setItem(6, new ItemStack(Material.BEETROOT, 64));
            gui.setItem(7, new ItemStack(Material.BROWN_MUSHROOM, 64));
            gui.setItem(8, new ItemStack(Material.RED_MUSHROOM, 64));

            gui.setItem(9, new ItemStack(Material.KELP, 64));
            gui.setItem(10, new ItemStack(Material.APPLE, 64));
            gui.setItem(11, new ItemStack(Material.POTATO, 64));
            gui.setItem(12, new ItemStack(Material.CARROT, 64));
            gui.setItem(13, new ItemStack(Material.POPPY, 64));
            gui.setItem(14, new ItemStack(Material.WHEAT, 64));
            gui.setItem(15, new ItemStack(Material.SPORE_BLOSSOM, 8));
            gui.setItem(16, new ItemStack(Material.BAMBOO, 64));
            gui.setItem(17, new ItemStack(Material.VINE, 64));
        }
        else if (type.equalsIgnoreCase("wolle"))
        {
            gui = Bukkit.createInventory(player, 18, Component.text("Wähle einen Slot").color(TextColor.color(0xFF00FF)));
            gui.setItem(0, new ItemStack(Material.BLACK_WOOL, 64));
            gui.setItem(1, new ItemStack(Material.LIGHT_GRAY_WOOL, 64));
            gui.setItem(2, new ItemStack(Material.BROWN_WOOL, 64));
            gui.setItem(3, new ItemStack(Material.ORANGE_WOOL, 64));
            gui.setItem(4, new ItemStack(Material.GREEN_WOOL, 64));
            gui.setItem(5, new ItemStack(Material.CYAN_WOOL, 64));
            gui.setItem(6, new ItemStack(Material.BLUE_WOOL, 64));
            gui.setItem(7, new ItemStack(Material.MAGENTA_WOOL, 64));
            gui.setItem(8, new ItemStack(Material.RED_WOOL, 64));

            gui.setItem(10, new ItemStack(Material.GRAY_WOOL, 64));
            gui.setItem(11, new ItemStack(Material.WHITE_WOOL, 64));
            gui.setItem(12, new ItemStack(Material.YELLOW_WOOL, 64));
            gui.setItem(13, new ItemStack(Material.LIME_WOOL, 64));
            gui.setItem(14, new ItemStack(Material.LIGHT_BLUE_WOOL, 64));
            gui.setItem(15, new ItemStack(Material.PINK_WOOL, 64));
            gui.setItem(16, new ItemStack(Material.PURPLE_WOOL, 64));
        }
        else if (type.equalsIgnoreCase("nahrung"))
        {
            gui = Bukkit.createInventory(player, 9, Component.text("Wähle einen Slot").color(TextColor.color(0xFF00FF)));
            gui.setItem(0, new ItemStack(Material.GOLDEN_APPLE, 5));
            gui.setItem(1, new ItemStack(Material.CAKE, 10));
            gui.setItem(2, new ItemStack(Material.PUMPKIN_PIE, 48));
            gui.setItem(3, new ItemStack(Material.COOKED_COD, 64));
            gui.setItem(4, new ItemStack(Material.COOKED_PORKCHOP, 64));
            gui.setItem(5, new ItemStack(Material.BREAD, 64));
            gui.setItem(6, new ItemStack(Material.COOKED_RABBIT, 48));
            gui.setItem(7, new ItemStack(Material.RABBIT_STEW, 10));
            gui.setItem(8, CustomItemGenerator.gaensebraten(5));
        }
        else if (type.equalsIgnoreCase("staemme"))
        {
            gui = Bukkit.createInventory(player, 9, Component.text("Wähle einen Slot").color(TextColor.color(0xFF00FF)));
            gui.setItem(0, new ItemStack(Material.SPRUCE_LOG, 64));
            gui.setItem(1, new ItemStack(Material.DARK_OAK_LOG, 64));
            gui.setItem(2, new ItemStack(Material.JUNGLE_LOG, 64));
            gui.setItem(3, new ItemStack(Material.OAK_LOG, 64));
            gui.setItem(4, new ItemStack(Material.ACACIA_LOG, 64));
            gui.setItem(5, new ItemStack(Material.BIRCH_LOG, 64));
            gui.setItem(6, new ItemStack(Material.WARPED_STEM, 32));
            gui.setItem(7, new ItemStack(Material.CRIMSON_STEM, 32));
            gui.setItem(8, new ItemStack(Material.MANGROVE_LOG, 32));
        }
        else if (type.equalsIgnoreCase("glas"))
        {
            gui = Bukkit.createInventory(player, 27, Component.text("Wähle einen Slot").color(TextColor.color(0xFF00FF)));
            gui.setItem(0, new ItemStack(Material.BLACK_STAINED_GLASS, 64));
            gui.setItem(1, new ItemStack(Material.LIGHT_GRAY_STAINED_GLASS, 64));
            gui.setItem(2, new ItemStack(Material.BROWN_STAINED_GLASS, 64));
            gui.setItem(3, new ItemStack(Material.ORANGE_STAINED_GLASS, 64));
            gui.setItem(4, new ItemStack(Material.GREEN_STAINED_GLASS, 64));
            gui.setItem(5, new ItemStack(Material.CYAN_STAINED_GLASS, 64));
            gui.setItem(6, new ItemStack(Material.BLUE_STAINED_GLASS, 64));
            gui.setItem(7, new ItemStack(Material.MAGENTA_STAINED_GLASS, 64));
            gui.setItem(8, new ItemStack(Material.RED_STAINED_GLASS, 64));

            gui.setItem(10, new ItemStack(Material.GRAY_STAINED_GLASS, 64));
            gui.setItem(11, new ItemStack(Material.WHITE_STAINED_GLASS, 64));
            gui.setItem(12, new ItemStack(Material.YELLOW_STAINED_GLASS, 64));
            gui.setItem(13, new ItemStack(Material.LIME_STAINED_GLASS, 64));
            gui.setItem(14, new ItemStack(Material.LIGHT_BLUE_STAINED_GLASS, 64));
            gui.setItem(15, new ItemStack(Material.PINK_STAINED_GLASS, 64));
            gui.setItem(16, new ItemStack(Material.PURPLE_STAINED_GLASS, 64));

            gui.setItem(22, new ItemStack(Material.GLASS, 64));
        }
        else if (type.equalsIgnoreCase("trank"))
        {
            gui = Bukkit.createInventory(player, 18, Component.text("Wähle einen Slot").color(TextColor.color(0xFF00FF)));
            gui.setItem(1, makeABasicPotion(PotionType.INSTANT_HEAL));
            gui.setItem(2, makeABasicPotion(PotionType.STRENGTH));
            gui.setItem(3, makeABasicPotion(PotionType.REGEN));
            gui.setItem(4, makeABasicPotion(PotionType.FIRE_RESISTANCE));
            gui.setItem(5, makeABasicPotion(PotionType.JUMP));
            gui.setItem(6, makeABasicPotion(PotionType.SPEED));
            gui.setItem(7, makeABasicPotion(PotionType.NIGHT_VISION));

            gui.setItem(10, makeABasicPotion(PotionType.INSTANT_DAMAGE));
            gui.setItem(11, makeABasicPotion(PotionType.WEAKNESS));
            gui.setItem(12, makeABasicPotion(PotionType.POISON));
            gui.setItem(13, makeABasicPotion(PotionType.WATER_BREATHING));
            gui.setItem(14, makeABasicPotion(PotionType.SLOW_FALLING));
            gui.setItem(15, makeABasicPotion(PotionType.SLOWNESS));
            gui.setItem(16, makeABasicPotion(PotionType.INVISIBILITY));
        }
        else if (type.equalsIgnoreCase("beton"))
        {
            gui = Bukkit.createInventory(player, 36, Component.text("Wähle einen Slot").color(TextColor.color(0xFF00FF)));
            gui.setItem(0, new ItemStack(Material.BLACK_CONCRETE, 64));
            gui.setItem(1, new ItemStack(Material.LIGHT_GRAY_CONCRETE, 64));
            gui.setItem(2, new ItemStack(Material.BROWN_CONCRETE, 64));
            gui.setItem(3, new ItemStack(Material.ORANGE_CONCRETE, 64));
            gui.setItem(4, new ItemStack(Material.GREEN_CONCRETE, 64));
            gui.setItem(5, new ItemStack(Material.CYAN_CONCRETE, 64));
            gui.setItem(6, new ItemStack(Material.BLUE_CONCRETE, 64));
            gui.setItem(7, new ItemStack(Material.MAGENTA_CONCRETE, 64));
            gui.setItem(8, new ItemStack(Material.RED_CONCRETE, 64));

            gui.setItem(10, new ItemStack(Material.GRAY_CONCRETE, 64));
            gui.setItem(11, new ItemStack(Material.WHITE_CONCRETE, 64));
            gui.setItem(12, new ItemStack(Material.YELLOW_CONCRETE, 64));
            gui.setItem(13, new ItemStack(Material.LIME_CONCRETE, 64));
            gui.setItem(14, new ItemStack(Material.LIGHT_BLUE_CONCRETE, 64));
            gui.setItem(15, new ItemStack(Material.PINK_CONCRETE, 64));
            gui.setItem(16, new ItemStack(Material.PURPLE_CONCRETE, 64));

            gui.setItem(18, new ItemStack(Material.BLACK_CONCRETE_POWDER, 64));
            gui.setItem(19, new ItemStack(Material.LIGHT_GRAY_CONCRETE_POWDER, 64));
            gui.setItem(20, new ItemStack(Material.BROWN_CONCRETE_POWDER, 64));
            gui.setItem(21, new ItemStack(Material.ORANGE_CONCRETE_POWDER, 64));
            gui.setItem(22, new ItemStack(Material.GREEN_CONCRETE_POWDER, 64));
            gui.setItem(23, new ItemStack(Material.CYAN_CONCRETE_POWDER, 64));
            gui.setItem(24, new ItemStack(Material.BLUE_CONCRETE_POWDER, 64));
            gui.setItem(25, new ItemStack(Material.MAGENTA_CONCRETE_POWDER, 64));
            gui.setItem(26, new ItemStack(Material.RED_CONCRETE_POWDER, 64));

            gui.setItem(28, new ItemStack(Material.GRAY_CONCRETE_POWDER, 64));
            gui.setItem(29, new ItemStack(Material.WHITE_CONCRETE_POWDER, 64));
            gui.setItem(30, new ItemStack(Material.YELLOW_CONCRETE_POWDER, 64));
            gui.setItem(31, new ItemStack(Material.LIME_CONCRETE_POWDER, 64));
            gui.setItem(32, new ItemStack(Material.LIGHT_BLUE_CONCRETE_POWDER, 64));
            gui.setItem(33, new ItemStack(Material.PINK_CONCRETE_POWDER, 64));
            gui.setItem(34, new ItemStack(Material.PURPLE_CONCRETE_POWDER, 64));
        }
        else if (type.equalsIgnoreCase("redstone"))
        {
            gui = Bukkit.createInventory(player, 18, Component.text("Wähle einen Slot").color(TextColor.color(0xFF00FF)));
            gui.setItem(0, new ItemStack(Material.REDSTONE_LAMP, 32));
            gui.setItem(1, new ItemStack(Material.NOTE_BLOCK, 32));
            gui.setItem(2, new ItemStack(Material.TARGET, 32));
            gui.setItem(3, new ItemStack(Material.DISPENSER, 16));
            gui.setItem(4, new ItemStack(Material.DROPPER, 16));
            gui.setItem(5, new ItemStack(Material.OBSERVER, 16));
            gui.setItem(6, new ItemStack(Material.STICKY_PISTON, 16));
            gui.setItem(7, new ItemStack(Material.PISTON, 24));
            gui.setItem(8, new ItemStack(Material.DAYLIGHT_DETECTOR, 16));

            gui.setItem(9, new ItemStack(Material.TRIPWIRE_HOOK, 64));
            gui.setItem(10, new ItemStack(Material.REPEATER, 32));
            gui.setItem(11, new ItemStack(Material.COMPARATOR, 16));
            gui.setItem(12, new ItemStack(Material.REDSTONE_TORCH, 64));
            gui.setItem(13, new ItemStack(Material.REDSTONE_BLOCK, 32));
            gui.setItem(14, new ItemStack(Material.SLIME_BLOCK, 32));
            gui.setItem(15, new ItemStack(Material.OAK_TRAPDOOR, 64));
            gui.setItem(16, new ItemStack(Material.OAK_BUTTON, 64));
            gui.setItem(17, new ItemStack(Material.HEAVY_WEIGHTED_PRESSURE_PLATE, 32));
        }
        else return;

        player.openInventory(gui);
        PlayerData.get(player).currentCustomInventory = gui;
        player.playSound(player.getLocation(), Sound.BLOCK_SHULKER_BOX_OPEN, SoundCategory.MASTER, 0.6f, 1.0f);
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onCouponInvalidCraft(PrepareItemCraftEvent event)
    {
        var key = new NamespacedKey(ErikComPlugin.plugin, "item_coupon");
        var inventory = event.getInventory();
        var items = inventory.getMatrix();
        for (var item : items)
        {
            if (item == null) continue;

            var meta = item.getItemMeta();
            if (meta == null || !meta.getPersistentDataContainer().has(key)) continue;
            inventory.setResult(new ItemStack(Material.AIR));
            break;
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onCouponUseClick(InventoryClickEvent event)
    {
        if (event.isCancelled()) return;
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player)event.getWhoClicked();
        PlayerData data = PlayerData.get(player);
        if (!event.getInventory().equals(data.currentCustomInventory)) return;

        event.setCancelled(true);
        if (event.getClickedInventory() == null) { sfx_abort(player); return; }
        if (!event.getClickedInventory().equals(data.currentCustomInventory)) { sfx_abort(player); return; }
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null) { sfx_abort(player); return; }
        if (player.getInventory().firstEmpty() == -1) { sfx_abort(player); return; }

        var item = player.getInventory().getItemInMainHand();
        var key = new NamespacedKey(ErikComPlugin.plugin, "item_coupon");
        var meta = item.getItemMeta();
        if (meta == null || !meta.getPersistentDataContainer().has(key)) { sfx_abort(player); return; }

        item.setAmount(item.getAmount() - 1);
        player.getInventory().addItem(clicked);
        player.playSound(player.getLocation(), Sound.ENTITY_FOX_EAT, SoundCategory.MASTER, 0.6f, 1.2f);
        if (item.getAmount() == 0)
        {
            player.closeInventory();
            player.playSound(player.getLocation(), Sound.BLOCK_SHULKER_BOX_CLOSE, SoundCategory.MASTER, 0.6f, 1.0f);
        }
    }
    private void sfx_abort(Player player)
    {
        player.playSound(player.getLocation(), Sound.ENTITY_GOAT_RAM_IMPACT, SoundCategory.MASTER, 0.6f, 0.95f);
    }
}

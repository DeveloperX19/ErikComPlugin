package de.developerx19.erikcomplugin;

import com.mojang.datafixers.util.Pair;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.level.Level;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_19_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_19_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class EventXmas22Manager implements Listener
{
    public static int day_of_december_override = -1;  // -1 -> automatic
    public static int day_of_december = 0;  // 0 -> not december 2022
    private static final Vector world_center = new Vector(50, 75, 0);
    private static final Location fake_present_location = new Location(ErikComPlugin.world_normal, 122, 81, -5);
    private static double max_present_dist_sq = 0;

    private static final String[] present_skins = new String[11];
    static
    {
        present_skins[0] = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTcyNmQ5ZDA2MzJlNDBiZGE1YmNmNjU4MzliYTJjYzk4YTg3YmQ2MTljNTNhZGYwMDMxMGQ2ZmM3MWYwNDJiNSJ9fX0=";
        present_skins[1] = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTM3NTA2MWQwOGYxZDdiMzE3Njc1YWE3ZmE4ODAwZDZmMjA2NmUwMThkOWY5MWVjZGRmOWNhZjMwNGU5N2U5MiJ9fX0=";
        present_skins[2] = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODFlNDJlMzcyNWMyYjRhZTY5MDA1ODBjNGUyYTZiODMwZjZlY2EwMjExZjdhMzY0MTQzM2ZjNjdmYmM0M2QzZiJ9fX0=";
        present_skins[3] = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDZlMTJmZGFlMWZjZWJhNjg3OWY2NTk3OTYxMzJhN2ZmYTA4Y2Q5MmEyNmNiN2ExMDY3ZDQ5NzAzZDdiMWI0YiJ9fX0=";
        present_skins[4] = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjBiNDVjYmZiZGZiNmRjNjY5NGRlOTZkOTdkYTM2YWU3YjVmZTNjZDk0YTViMjYyNTA1NTRjNWYwMjJjYTdkMCJ9fX0=";
        present_skins[5] = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDA4Y2U3ZGViYTU2YjcyNmE4MzJiNjExMTVjYTE2MzM2MTM1OWMzMDQzNGY3ZDVlM2MzZmFhNmZlNDA1MiJ9fX0=";
        present_skins[6] = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWI2NzMwZGU3ZTViOTQxZWZjNmU4Y2JhZjU3NTVmOTQyMWEyMGRlODcxNzU5NjgyY2Q4ODhjYzRhODEyODIifX19";
        present_skins[7] = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTNlNThlYTdmMzExM2NhZWNkMmIzYTZmMjdhZjUzYjljYzljZmVkN2IwNDNiYTMzNGI1MTY4ZjEzOTFkOSJ9fX0=";
        present_skins[8] = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTNlNThlYTdmMzExM2NhZWNkMmIzYTZmMjdhZjUzYjljYzljZmVkN2IwNDNiYTMzNGI1MTY4ZjEzOTFkOSJ9fX0=";
        present_skins[9] = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWFiODBjNDM3MTU4OWNlZmVkODkxMGY2YmU2N2Y2ZDI5NmEzZWEwYWVlMmFiNTViNTY2ODAyMGQ2Yjg1MTRkOSJ9fX0=";
        present_skins[10] = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjE3N2MwZDdmZjFiOGFlM2MwNTljN2UyMzA3NzMwODA1Y2IzMTg4NjI2MjUxYjhjYjYyZDU2NmM1MTIzMjM2ZiJ9fX0=";
    }
    public static final List<Location> presentLocations_easy = new ArrayList<>();
    public static final List<Location> presentLocations_normal = new ArrayList<>();
    public static final List<Location> presentLocations_hard = new ArrayList<>();
    public static final int LOCATIONPOOL_EASY = 1;
    public static final int LOCATIONPOOL_NORMAL = 2;
    public static final int LOCATIONPOOL_HARD = 3;

    private static final String[] pool_skins = new String[3];
    static
    {
        pool_skins[0] = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTI3MTZiMzc1MjNmNDUzZTE4NzFmMjI2M2Y4MjNjMjgwYmI4ZGQ3M2Q2OTZkNTI3YjllZWM4N2NkZjMyIn19fQ==";
        pool_skins[1] = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDcyNzc2M2RmMWQxYmE1OTc1MDdlNjE0YjZjNzEzMmEwODY5Yzc0NzJjNGZkYmJlY2Q5OTEzNGNkNTUifX19";
        pool_skins[2] = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNmViOTExZWE5NGI1YTFjZjc3ZjNjYTYzN2EzYjE2NjJiMzUxMjFiZDcyZTExODY1MTE4NGYyZmIxMDYwZDEifX19";
    }

    public static String getPoolSkin(int pool)
    {
        if (pool == LOCATIONPOOL_EASY) return pool_skins[0];
        if (pool == LOCATIONPOOL_NORMAL) return pool_skins[1];
        if (pool == LOCATIONPOOL_HARD) return pool_skins[2];
        return null;
    }

    public static List<Location> presentLocationPool(int pool)
    {
        if (pool == LOCATIONPOOL_EASY) return presentLocations_easy;
        if (pool == LOCATIONPOOL_NORMAL) return presentLocations_normal;
        if (pool == LOCATIONPOOL_HARD) return presentLocations_hard;
        return null;
    }

    public static int getPool(int presentNumber)
    {
        if (presentNumber >= 0 && presentNumber < 12) return LOCATIONPOOL_EASY;
        if (presentNumber >= 12 && presentNumber < 20) return LOCATIONPOOL_NORMAL;
        if (presentNumber >= 20 && presentNumber < 24) return LOCATIONPOOL_HARD;
        return -1;
    }

    public static class Present
    {

        public AdventCalendar calender;

        public Location location;
        public boolean opened;

        public float rotation = ErikComPlugin.rng.nextFloat(360);
        public int skin = ErikComPlugin.rng.nextInt(present_skins.length);

        public int entity_id = -1;
        public boolean entity_loaded = false;

        public Present(AdventCalendar c) // invalid
        {
            calender = c;
            location = new Location(ErikComPlugin.world_normal, 0, 0, 0);
            opened = true;
        }
        public Present(AdventCalendar cal, Location loc, boolean open) // normal
        {
            calender = cal;
            location = loc;
            opened = open;
        }

        public Player player()
        {
            return calender.player;
        }
    }

    public static class AdventCalendar
    {
        Player player;
        public int collectedCount = 0;
        public final Present[] presents = new Present[24];

        public boolean is_invalid = false;

        public AdventCalendar(Player p)
        {
            player = p;
        }

        public void collect(int i)
        {
            Present present = presents[i];
            if (present.opened) return;
            if (present.player().getInventory().firstEmpty() == -1) return;
            if (!Database.openPresent(present.player(), i)) return;
            clipPresent(present);
            present.opened = true;
            collectedCount += 1;
            ItemStack reward = EventXmas22Calender.get_reward(collectedCount);
            if (reward != null)
                player.getInventory().addItem(reward);
            player.playSound(present.location, Sound.ENTITY_ALLAY_AMBIENT_WITHOUT_ITEM, SoundCategory.MASTER, 1.0f, 1.0f);
            player.spawnParticle(Particle.FIREWORKS_SPARK, present.location.add(0.5, .5, .5), 100, .15, .15, .15, .25);
        }
    }

    public static void addPresent(Location location, int pool_id)
    {
        location = location.toBlockLocation();
        var pool = presentLocationPool(pool_id);
        if (pool == null) return;
        if (presentLocations_easy.contains(location)) return;
        if (presentLocations_normal.contains(location)) return;
        if (presentLocations_hard.contains(location)) return;
        Database.savePresentToPool(location, pool_id);
        pool.add(location);
        recalculateMaxPresentDist();
    }
    public static void removePresent(Location location)
    {
        location = location.toBlockLocation();
        boolean remove = presentLocations_easy.remove(location);
        remove |= presentLocations_normal.remove(location);
        remove |= presentLocations_hard.remove(location);
        if (remove)
            Database.deletePresent(location);
        recalculateMaxPresentDist();
    }

    public static void renderPresent(Present present, List<Integer> entity_ids)
    {
        if (present.opened) return;
        Level l = ((CraftWorld)present.player().getWorld()).getHandle();
        ArmorStand stand = new ArmorStand(l, present.location.getX() + .5, present.location.getY() - 1.5, present.location.getZ() + .5);
        stand.setInvisible(true);
        stand.setMarker(true);
        stand.setRot(present.rotation, 0);
        ItemStack item = CustomItemGenerator.getHead(present_skins[present.skin], 1);

        var packCreate = new ClientboundAddEntityPacket(stand);
        ((CraftPlayer)present.player()).getHandle().connection.send(packCreate);
        var packNBT = new ClientboundSetEntityDataPacket(stand.getId(), stand.getEntityData(), true);
        ((CraftPlayer)present.player()).getHandle().connection.send(packNBT);
        var packHead = new ClientboundSetEquipmentPacket(stand.getId(), List.of(Pair.of(EquipmentSlot.HEAD, CraftItemStack.asNMSCopy(item))));
        ((CraftPlayer)present.player()).getHandle().connection.send(packHead);
        present.player().sendBlockChange(present.location, Bukkit.createBlockData(Material.BARRIER));

        entity_ids.add(stand.getId());
        present.entity_id = stand.getId();
        present.entity_loaded = true;
    }

    public static void renderPresentLocation(Player player, Location location, int pool, List<Integer> entity_ids)
    {
        Level l = ((CraftWorld)player.getWorld()).getHandle();
        ArmorStand stand = new ArmorStand(l, location.getX() + .5, location.getY() - 1.5, location.getZ() + .5);
        stand.setInvisible(true);
        stand.setMarker(true);
        ItemStack item = CustomItemGenerator.getHead(getPoolSkin(pool), 1);

        var packCreate = new ClientboundAddEntityPacket(stand);
        ((CraftPlayer)player).getHandle().connection.send(packCreate);
        var packNBT = new ClientboundSetEntityDataPacket(stand.getId(), stand.getEntityData(), true);
        ((CraftPlayer)player).getHandle().connection.send(packNBT);
        var packHead = new ClientboundSetEquipmentPacket(stand.getId(), List.of(Pair.of(EquipmentSlot.HEAD, CraftItemStack.asNMSCopy(item))));
        ((CraftPlayer)player).getHandle().connection.send(packHead);
        entity_ids.add(stand.getId());
    }

    public static void clipPresent(Present present)
    {
        present.player().sendBlockChange(present.location, present.location.getBlock().getBlockData());
        if (present.entity_loaded)
        {
            var packRemove = new ClientboundRemoveEntitiesPacket(present.entity_id);
            ((CraftPlayer)present.player()).getHandle().connection.send(packRemove);
        }
    }

    public static void clipPresents(Player player, List<Integer> entity_ids)
    {
        if (entity_ids.size() == 0) return;
        var packRemove = new ClientboundRemoveEntitiesPacket(entity_ids.stream().mapToInt(i -> i).toArray());
        ((CraftPlayer)player).getHandle().connection.send(packRemove);
    }

    public static boolean notAdventCalendarRange(Location location)
    {
        if (location.getWorld().getEnvironment() != World.Environment.NORMAL) return true;
        return location.toVector().distanceSquared(world_center) > max_present_dist_sq * 1.25;
    }

    public static Location chooseNewRandom(AdventCalendar calendar, List<Location> pool)
    {
        Location chosen;
        for (int i = 0; i < 256; i++)
        {
            chosen = pool.get(ErikComPlugin.rng.nextInt(pool.size()));
            boolean bad = false;
            for (var p : calendar.presents)
                bad = bad || (p != null && p.location.getBlock().equals(chosen.getBlock()));
            if (!bad) return chosen;
        }
        return null;
    }

    public static void initEvent()
    {
        loadEvent();
        Bukkit.getScheduler().scheduleSyncRepeatingTask(ErikComPlugin.plugin, EventXmas22Manager::updateEvent, 0, 6000);
    }

    private static void recalculateMaxPresentDist()
    {
        max_present_dist_sq = 0;
        for(var p : presentLocations_easy)
        {
            double dist = p.toVector().distanceSquared(world_center);
            if (dist > max_present_dist_sq)
                max_present_dist_sq = dist;
        }
        for(var p : presentLocations_normal)
        {
            double dist = p.toVector().distanceSquared(world_center);
            if (dist > max_present_dist_sq)
                max_present_dist_sq = dist;
        }
        for(var p : presentLocations_hard)
        {
            double dist = p.toVector().distanceSquared(world_center);
            if (dist > max_present_dist_sq)
                max_present_dist_sq = dist;
        }
    }

    public static void loadEvent()
    {
        Database.loadXmasPools();
        recalculateMaxPresentDist();
    }

    public static void updateEvent()
    {
        int day = 0;
        Calendar c = Calendar.getInstance();
        if (c.get(Calendar.MONTH) == Calendar.DECEMBER && c.get(Calendar.YEAR) == 2022)
            day = c.get(Calendar.DAY_OF_MONTH);
        if (day_of_december_override == -1)
            day_of_december = day;
    }

    public static void initPlayer(Player player)
    {
        loadPlayer(player);
        int player_xmas_loop = Bukkit.getScheduler().scheduleSyncRepeatingTask(ErikComPlugin.plugin, () -> updatePlayer(player), 60, 85);
        PlayerData data = PlayerData.get(player);
        data.owned_tasks.add(player_xmas_loop);
    }

    public static void loadPlayer(Player player)
    {
        PlayerData data = PlayerData.get(player);
        AdventCalendar calendar = data.adventCalendar;
        Database.loadAdventCalendar(data);
        if (calendar.is_invalid)
        {
            player.sendMessage(ChatColor.RED + " >> DEIN ADVENTSKALENDER KONNTE NICHT KORREKT GELADEN WERDEN !");
            player.sendMessage(ChatColor.RED + " >> FALLS DIESE NACHRICHT AUCH NACH RE-LOGGEN AUFTRITT, MELDE DICH BITTE BEIM TEAM / ENTWICKLER");
            for (int i = 0; i < 24; i++)
                calendar.presents[i] = new Present(calendar);
        }
        else
        {
            for (int i = 0; i < 24; i++)
            {
                if (data.adventCalendar.presents[i] == null)
                {
                    if (ErikComPlugin.xmas22active())
                    {
                        Location location = chooseNewRandom(calendar, presentLocationPool(getPool(i)));
                        calendar.presents[i] = new Present(calendar, location, false);
                        Database.saveAdventCalendarPresent(data, i);
                    }
                    else
                        calendar.presents[i] = new Present(calendar);
                }
                else if (calendar.presents[i].opened)
                    calendar.collectedCount += 1;
            }
        }
    }

    public static void updatePlayer(Player player)
    {
        PlayerData data = PlayerData.get(player);
        clipPresents(player, data.present_entity_ids);
        if (notAdventCalendarRange(player.getLocation())) return;
        if (data.renderAllPresents)
        {
            data.present_entity_ids.clear();
            for (var p_easy : presentLocations_easy)
                renderPresentLocation(player, p_easy, LOCATIONPOOL_EASY, data.present_entity_ids);
            for (var p_easy : presentLocations_normal)
                renderPresentLocation(player, p_easy, LOCATIONPOOL_NORMAL, data.present_entity_ids);
            for (var p_easy : presentLocations_hard)
                renderPresentLocation(player, p_easy, LOCATIONPOOL_HARD, data.present_entity_ids);
        }
        else
        {
            int size = Math.min(day_of_december, 24);
            data.present_entity_ids.clear();
            for (int iter = 0; iter < size; iter++)
                renderPresent(data.adventCalendar.presents[iter], data.present_entity_ids);
            var fakePresent = new Present(data.adventCalendar, fake_present_location, false);
            renderPresent(fakePresent, data.present_entity_ids);
        }
    }



    @EventHandler(priority = EventPriority.NORMAL)
    public void onOpenPresent(PlayerInteractEvent event)
    {
        var target = event.getClickedBlock();
        if (target == null || notAdventCalendarRange(target.getLocation())) return;
        Player player = event.getPlayer();
        PlayerData data = PlayerData.get(player);
        for (int i = 0; i < day_of_december && i < 24; i++)
        {
            Present present = data.adventCalendar.presents[i];
            if (present.opened) continue;
            if (!target.equals(present.location.getBlock())) continue;
            int finalI = i;
            Bukkit.getScheduler().runTask(ErikComPlugin.plugin, () -> data.adventCalendar.collect(finalI));
            event.setCancelled(true);
            break;
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onModifyPresentPool(BlockPlaceEvent event)
    {
        if (event.isCancelled()) return;

        var key = new NamespacedKey(ErikComPlugin.plugin, "present_pool_tool");
        var item = event.getItemInHand();
        var meta = item.getItemMeta();
        if (!meta.getPersistentDataContainer().has(key)) return;

        if (event.getItemInHand().getType() == Material.EMERALD_BLOCK)
        {
            event.setCancelled(true);
            addPresent(event.getBlock().getLocation(), LOCATIONPOOL_EASY);
        }
        if (event.getItemInHand().getType() == Material.GOLD_BLOCK)
        {
            event.setCancelled(true);
            addPresent(event.getBlock().getLocation(), LOCATIONPOOL_NORMAL);
        }
        if (event.getItemInHand().getType() == Material.REDSTONE_BLOCK)
        {
            event.setCancelled(true);
            addPresent(event.getBlock().getLocation(), LOCATIONPOOL_HARD);
        }
        if (event.getItemInHand().getType() == Material.COAL_BLOCK)
        {
            event.setCancelled(true);
            removePresent(event.getBlock().getLocation());
        }

        updatePlayer(event.getPlayer());
    }
}

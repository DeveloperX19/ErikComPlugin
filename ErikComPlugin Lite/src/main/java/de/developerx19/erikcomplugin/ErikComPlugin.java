package de.developerx19.erikcomplugin;
import org.bukkit.*;
import org.bukkit.command.*;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Calendar;
import java.util.Random;
import java.util.UUID;
import java.util.logging.Logger;

public final class ErikComPlugin extends JavaPlugin
{
    public static final boolean is_release = false;

    public static ErikComPlugin plugin = null;
    public static Server server = null;
    public static Logger console = null;
    public static final Random rng = new Random();

    public static World world_normal;
    public static World world_nether;
    public static World world_end;

    public static boolean db_is_maintenance = false;
    public static boolean db_commands_active = true;
    public static boolean db_xmas22active = false;

    @Override
    public void onEnable()
    {
        plugin = this;
        server = getServer();
        console = getLogger();
        Database.initDatabase();
        Database.loadGroupTags();
        loadServerData();

        registerCommand("cast", new CastCommand());
        registerCommand("ghost", new GhostCommand());
        registerCommand("god", new GodCommand());
        registerCommand("heal", new HealCommand());
        registerCommand("warp", new WarpCommand());
        registerCommand("back", new BackCommand());
        registerCommand("pvp", new PVPCommand());
        registerCommand("vanish", new VanishCommand());
        registerCommand("tell", new TellCommand());
        registerCommand("grouptag", new GroupTagCommand());
        registerCommand("staff", new StaffCommand());
        registerCommand("streamermode", new StreamermodeCommand());
        registerCommand("signitem", new SignItemCommand());
        registerCommand("op", new OpCommand());
        registerCommand("deop", new DeopCommand());
        registerCommand("devtool", new DevtoolCommand());
        registerCommand("player_stat", new PlayerStatCommand());
        registerCommand("countdown_event", new CountdownCommand());
        registerCommand("present_pool", new PresentPoolCommand());
        registerCommand("subperk", new SubperkCommand());

        server.getPluginManager().registerEvents(new EventListener(), this);
        server.getPluginManager().registerEvents(new VanishManager(), this);
        server.getPluginManager().registerEvents(new GroupTagManager(), this);
        server.getPluginManager().registerEvents(new PVPManager(), this);
        server.getPluginManager().registerEvents(new CustomItems(), this);
        server.getPluginManager().registerEvents(new EventXmas22Manager(), this);

        loadWorlds();
        GroupTagManager.init();
        fix_end_dim_gamerule();
        schedule_midnight_countdown();
        load_custom_recpies();
        EventXmas22Manager.initEvent();
    }

    @Override
    public void onDisable()
    {
        for (UUID pID : PlayerData.map.keySet())
            Database.savePlayerData(server.getPlayer(pID));
    }

    public static @Nullable OfflinePlayer getPlayer(String name)
    {
        Player p = server.getPlayer(name);
        if (p != null && p.getName().equalsIgnoreCase(name)) return p;
        OfflinePlayer player = server.getOfflinePlayerIfCached(name);
        if (player == null || !player.hasPlayedBefore() || player.getName() == null || !player.getName().equalsIgnoreCase(name)) return null;
        return player;
    }


    private void loadWorlds()
    {
        for (World world : ErikComPlugin.server.getWorlds())
        {
            if (world.getEnvironment() == World.Environment.NORMAL)
                world_normal = world;
            if (world.getEnvironment() == World.Environment.NETHER)
                world_nether = world;
            if (world.getEnvironment() == World.Environment.THE_END)
                world_end = world;
        }
    }

    private void registerCommand(@NotNull String command_label, @NotNull Object command_job)
    {
        PluginCommand cmd = getCommand(command_label);
        if (cmd != null && command_job instanceof CommandExecutor && command_job instanceof TabCompleter)
        {
            cmd.setExecutor((CommandExecutor) command_job);
            cmd.setTabCompleter((TabCompleter) command_job);
        }
        else
            console.severe("Failed to bind command \"/" + command_label + "\"");
    }

    private void loadServerData()
    {
        db_is_maintenance = Database.getDataBoolean(Database.serverID, Database.ServerField.IS_MAINTENANCE, db_is_maintenance);
        db_commands_active = Database.getDataBoolean(Database.serverID, Database.ServerField.COMMANDS_ACTIVE, db_commands_active);
        db_xmas22active = Database.getDataBoolean(Database.serverID, Database.ServerField.IS_XMAS22ACTIVE, db_xmas22active);
    }

    private void fix_end_dim_gamerule()
    {
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () ->
                world_end.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, true), 1200);
    }

    public void schedule_midnight_countdown()
    {
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () ->
        {
            Calendar c = Calendar.getInstance();
            c.add(Calendar.DAY_OF_MONTH, 1);
            c.set(Calendar.HOUR_OF_DAY, 0);
            c.set(Calendar.MINUTE, 0);
            c.set(Calendar.SECOND, 0);
            c.set(Calendar.MILLISECOND, 0);
            long millis_till_midnight = (c.getTimeInMillis()-System.currentTimeMillis());
            EventManager.start_global_countdown("Automatic Server restart", millis_till_midnight, 6, false);
        }, 1200);
    }

    public void load_custom_recpies()
    {
        {
            var key = new NamespacedKey(plugin, "explosive_c4");
            ItemStack explosive_c4 = CustomItemGenerator.explosive_c4(1);

            var recipe = new ShapedRecipe(key, explosive_c4);
            recipe.shape("INI", "CTC", "INI");
            recipe.setIngredient('I', Material.CLAY_BALL);
            recipe.setIngredient('C', Material.COPPER_INGOT);
            recipe.setIngredient('N', Material.NETHERITE_SCRAP);
            recipe.setIngredient('T', Material.TNT);
            Bukkit.addRecipe(recipe);
        }
    }

    public static boolean is_maintenance()
    {
        return db_is_maintenance;
    }
    public static void is_maintenance(boolean v)
    {
        db_is_maintenance = v;
        Database.setDataBooleanAsync(Database.serverID, Database.ServerField.IS_MAINTENANCE, v);
    }

    public static boolean commands_active()
    {
        return db_commands_active;
    }
    public static void commands_active(boolean v)
    {
        db_commands_active = v;
        Database.setDataBooleanAsync(Database.serverID, Database.ServerField.IS_MAINTENANCE, v);
    }
    public static boolean xmas22active()
    {
        return db_xmas22active;
    }
    public static void xmas22active(boolean v)
    {
        db_xmas22active = v;
        Database.setDataBooleanAsync(Database.serverID, Database.ServerField.IS_XMAS22ACTIVE, v);
    }
}

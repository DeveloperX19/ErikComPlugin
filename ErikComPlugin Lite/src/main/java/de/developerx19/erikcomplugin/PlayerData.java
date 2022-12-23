package de.developerx19.erikcomplugin;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.text.SimpleDateFormat;
import java.util.*;

public class PlayerData
{
    public static HashMap<UUID, PlayerData> map = new HashMap<>();

    public static PlayerData get(Player player)
    {
        return map.get(player.getUniqueId());
    }
    public static PlayerData get(UUID uuid)
    {
        return map.get(uuid);
    }
    public static SimpleDateFormat date_parser = new SimpleDateFormat("EEE MMM d HH:mm:ss zzz yyyy");
    public static final long newbie_time_ms = 30L * 24 * 60 * 60 * 1000;

    private final long db_id;

    private Location db_backPosition = null;
    private boolean db_flying = false;
    private boolean db_pvpOn = false;
    private boolean db_silent_join = false;
    private boolean db_silent_step = false;
    private boolean db_streamer_mode = false;
    private boolean db_staff_member = false;
    private boolean db_dev_member = false;
    private boolean db_is_operator = false;
    private Date db_init_join_date = new Date();
    private long db_totalPlayTime = 0;

    public SortedSet<GroupTagManager.GroupTag> groups = new TreeSet<>();
    public List<Integer> owned_tasks = new ArrayList<>();
    public EventXmas22Manager.AdventCalendar adventCalendar;

    public Player player;
    public String teamName;
    public List<Player> lastChats = new ArrayList<>();
    public long pvpLastChange = 0;
    public Integer afkTaskID = null;
    public long afkTaskLastUpdate;
    public boolean afk = false;
    public boolean vanished = false;
    public boolean pre_daemonic = false;
    public boolean daemonic = false;
    public boolean renderAllPresents = false;
    public List<Integer> present_entity_ids = new ArrayList<>();

    public long lastNonAFKStamp = System.currentTimeMillis();
    public Inventory currentCustomInventory = null;

    public PlayerData(Player p, long id)
    {
        player = p;
        db_id = id;
        adventCalendar = new EventXmas22Manager.AdventCalendar(player);
    }

    public void autoUpdateLoop()
    {
        if (!afk)
        {
            long now = System.currentTimeMillis();
            long delta = now - lastNonAFKStamp;
            totalPlayTime(totalPlayTime() + delta);
            lastNonAFKStamp = now;
        }
    }

    public void setFlightMode()
    {
        boolean forceFlight = (player.getGameMode() == GameMode.CREATIVE) || (player.getGameMode() == GameMode.SPECTATOR);
        db_flying |= vanished || forceFlight;
        player.setAllowFlight(db_flying);
        player.setFlying(db_flying);
    }

    public void setOpMode()
    {
        player.setOp(db_is_operator && !db_streamer_mode && !db_pvpOn);
    }

    public void addChat(Player player)
    {
        if (lastChats.contains(player)) return;
        if (lastChats.size() >= 5) lastChats.remove(0);
        lastChats.add(player);
    }

    public void setAFK()
    {
        afk = true;
        GroupTagManager.updateNameTagOf(player);
        long delta = System.currentTimeMillis() - lastNonAFKStamp;
        totalPlayTime(totalPlayTime() + delta);
    }

    public void resetAFK()
    {
        long currentTime = System.currentTimeMillis();
        if (afkTaskLastUpdate + 1000 > currentTime)
            return;
        afkTaskLastUpdate = currentTime;
        if (afk)
        {
            afk = false;
            GroupTagManager.updateNameTagOf(player);
            lastNonAFKStamp = System.currentTimeMillis();
        }
        if (afkTaskID != null)
            Bukkit.getScheduler().cancelTask(afkTaskID);
        afkTaskID = Bukkit.getScheduler().scheduleSyncDelayedTask(ErikComPlugin.plugin, () ->
        {
            if (!afk)
                setAFK();
            afkTaskID = null;
        }, InfoMap.afkTime / 50);
    }



    public long database_id()
    {
        return db_id;
    }

    public Location backPosition()
    {
        return db_backPosition;
    }
    public void backPosition(Location v)
    {
        db_backPosition = v;
        Database.setDataStringAsync(db_id, Database.PlayerField.BACKPOS_WOLRD, backPosition() != null ? backPosition().getWorld().getUID().toString() : "");
        Database.setDataFloatingAsync(db_id, Database.PlayerField.BACKPOS_X, backPosition() != null ? backPosition().getX() : 0.0);
        Database.setDataFloatingAsync(db_id, Database.PlayerField.BACKPOS_Y, backPosition() != null ? backPosition().getY() : 0.0);
        Database.setDataFloatingAsync(db_id, Database.PlayerField.BACKPOS_Z, backPosition() != null ? backPosition().getZ() : 0.0);
    }

    public boolean flying()
    {
        return db_flying;
    }
    public void flying(boolean v)
    {
        db_flying = v;
        Database.setDataBooleanAsync(db_id, Database.PlayerField.FLYING, v);
    }

    public boolean pvpOn()
    {
        return db_pvpOn;
    }
    public void pvpOn(boolean v)
    {
        db_pvpOn = v;
        Database.setDataBooleanAsync(db_id, Database.PlayerField.PVP_ON, v);
    }

    public boolean silent_join()
    {
        return db_silent_join;
    }
    public void silent_join(boolean v)
    {
        db_silent_join = v;
        Database.setDataBooleanAsync(db_id, Database.PlayerField.VANISH_SILENT_JOIN, v);
    }

    public boolean silent_step()
    {
        return db_silent_step;
    }
    public void silent_step(boolean v)
    {
        db_silent_step = v;
        Database.setDataBooleanAsync(db_id, Database.PlayerField.VANISH_SILENT_STEP, v);
    }

    public boolean streamer_mode()
    {
        return db_streamer_mode;
    }
    public void streamer_mode(boolean v)
    {
        db_streamer_mode = v;
        Database.setDataBooleanAsync(db_id, Database.PlayerField.STREAMER_MODE, v);
    }

    public boolean staff_member()
    {
        return db_staff_member;
    }
    public void staff_member(boolean v)
    {
        db_staff_member = v;
        Database.setDataBooleanAsync(db_id, Database.PlayerField.STAFF_MEMBER, v);
    }

    public boolean dev_member()
    {
        return db_dev_member;
    }
    public void dev_member(boolean v)
    {
        db_dev_member = v;
        Database.setDataBooleanAsync(db_id, Database.PlayerField.DEV_MEMBER, v);
    }

    public boolean is_operator()
    {
        return db_is_operator;
    }
    public void is_operator(boolean v)
    {
        db_is_operator = v;
        Database.setDataBooleanAsync(db_id, Database.PlayerField.IS_OPERATOR, v);
    }

    public Date init_join_date()
    {
        return db_init_join_date;
    }
    public void init_join_date(Date v)
    {
        db_init_join_date = v;
        Database.setDataStringAsync(db_id, Database.PlayerField.JOIN_DATE, date_parser.format(v));
    }

    public long totalPlayTime() { return db_totalPlayTime; }
    public void totalPlayTime(long v)
    {
        db_totalPlayTime = v;
        Database.setDataIntegerAsync(db_id, Database.PlayerField.PLAYTIME, v);
    }
}

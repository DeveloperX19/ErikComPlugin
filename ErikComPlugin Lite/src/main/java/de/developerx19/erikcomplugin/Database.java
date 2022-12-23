package de.developerx19.erikcomplugin;

import com.zaxxer.hikari.HikariDataSource;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.sql.*;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class Database
{
    private static final HikariDataSource dataSource = new HikariDataSource();

    public static final String databaseFileName = "ErikComDB";
    public static final String url = "jdbc:sqlite:" + ErikComPlugin.plugin.getDataFolder() + "/" + databaseFileName + ".db";
    public static class TableName
    {
        public static final String players = "players";
        public static final String playerData = "player_data";
        public static final String groupTags = "tag_groups";
        public static final String groupMembers = "player_groups";
        public static final String xmas22_global = "xmas22_data";
        public static final String xmas22_player = "xmas22_player_data";
    }

    public static long serverID = -2672973986257L;

    public interface DataField
    {
        int id();
    }
    public enum PlayerField implements DataField
    {
        FLYING(1),
        PVP_ON(2),
        VANISH_SILENT_JOIN(3),
        VANISH_SILENT_STEP(4),
        BACKPOS_WOLRD(5),
        BACKPOS_X(6),
        BACKPOS_Y(7),
        BACKPOS_Z(8),
        STREAMER_MODE(9),
        STAFF_MEMBER(10),
        DEV_MEMBER(11),
        IS_OPERATOR(12),
        JOIN_DATE(13),
        PLAYTIME(14);

        private final int p_id;
        public int id()
        {
            return p_id;
        }
        PlayerField(int i)
        {
            if (i <= 0) throw new SecurityException("PlayerField init with non pd ID : " + i);
            p_id = i;
        }
    }
    public enum ServerField implements DataField
    {
        IS_MAINTENANCE(1),
        COMMANDS_ACTIVE(2),
        IS_XMAS22ACTIVE(3);

        private final int s_id;
        public int id()
        {
            return s_id;
        }
        ServerField(int i)
        {
            if (i <= 0) throw new SecurityException("PlayerField init with non pd ID : " + i);
            s_id = -i;
        }
    }

    public static PreparedStatement prepareQuery(Connection db, String q, Object... args) throws Exception
    {
        PreparedStatement hook = db.prepareStatement(q);
        for (int i = 0; i < args.length; i++)
        {
            Object arg = args[i];
            if (arg instanceof Boolean)
                hook.setBoolean(i + 1, (boolean) arg);
            else if (arg instanceof Byte)
                hook.setByte(i + 1, (byte) arg);
            else if (arg instanceof Short)
                hook.setShort(i + 1, (short) arg);
            else if (arg instanceof Integer)
                hook.setInt(i + 1, (int) arg);
            else if (arg instanceof Long)
                hook.setLong(i + 1, (long) arg);
            else if (arg instanceof Float)
                hook.setFloat(i + 1, (float) arg);
            else if (arg instanceof Double)
                hook.setDouble(i + 1, (double) arg);
            else if (arg instanceof String)
                hook.setString(i + 1, (String) arg);
            else
                throw new RuntimeException("unhandled argument type : " + arg.getClass().toString());
        }
        return hook;
    }

    public static boolean executeUpdate(String q, Object... args)
    {
        try(Connection db = dataSource.getConnection();
            PreparedStatement hook = db.prepareStatement(q))
        {
            for (int i = 0; i < args.length; i++)
            {
                Object arg = args[i];
                if (arg instanceof Boolean)
                    hook.setBoolean(i + 1, (boolean) arg);
                else if (arg instanceof Byte)
                    hook.setByte(i + 1, (byte) arg);
                else if (arg instanceof Short)
                    hook.setShort(i + 1, (short) arg);
                else if (arg instanceof Integer)
                    hook.setInt(i + 1, (int) arg);
                else if (arg instanceof Long)
                    hook.setLong(i + 1, (long) arg);
                else if (arg instanceof Float)
                    hook.setFloat(i + 1, (float) arg);
                else if (arg instanceof Double)
                    hook.setDouble(i + 1, (double) arg);
                else if (arg instanceof String)
                    hook.setString(i + 1, (String) arg);
                else
                    throw new RuntimeException("unhandled argument type : " + arg.getClass().toString());
            }
            hook.executeUpdate();
        }
        catch (Exception e)
        {
            ErikComPlugin.console.severe("Failed to execute database update");
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static void initDatabase()
    {
        dataSource.setJdbcUrl(url);

        //noinspection ResultOfMethodCallIgnored
        ErikComPlugin.plugin.getDataFolder().mkdir();
        String sql = "CREATE TABLE IF NOT EXISTS " + TableName.players + "(\n"
                + "id INTEGER PRIMARY KEY,\n"
                + "uuid TEXT NOT NULL\n"
                + ")";
        executeUpdate(sql);
        sql = "CREATE TABLE IF NOT EXISTS " + TableName.playerData + "(\n"
                + "user INTEGER NOT NULL,\n"
                + "field INTEGER NOT NULL,\n"
                + "data TEXT NOT NULL,\n"
                + "PRIMARY KEY(user, field),\n"
                + "FOREIGN KEY(user) REFERENCES " + TableName.players + "(id) on delete cascade\n"
                + ")";
        executeUpdate(sql);
        sql = "CREATE TABLE IF NOT EXISTS " + TableName.groupTags + "(\n"
                + "id INTEGER PRIMARY KEY,\n"
                + "name TEXT NOT NULL,\n"
                + "priority INTEGER NOT NULL,\n"
                + "tag_display TEXT NOT NULL,\n"
                + "tag_color TEXT NOT NULL\n"
                + ")";
        executeUpdate(sql);
        sql = "CREATE TABLE IF NOT EXISTS " + TableName.groupMembers + "(\n"
                + "playerID INTEGER NOT NULL,\n"
                + "groupID INTEGER NOT NULL,\n"
                + "PRIMARY KEY(playerID, groupID),\n"
                + "FOREIGN KEY(playerID) REFERENCES " + TableName.players + "(id) on delete cascade,\n"
                + "FOREIGN KEY(groupID) REFERENCES " + TableName.groupTags + "(id) on delete cascade\n"
                + ")";
        executeUpdate(sql);
        sql = "CREATE TABLE IF NOT EXISTS " + TableName.xmas22_global + "(\n"
                + "id INTEGER PRIMARY KEY,\n"
                + "pos_pool INTEGER NOT NULL,\n"
                + "posX INTEGER NOT NULL,\n"
                + "posY INTEGER NOT NULL,\n"
                + "posZ INTEGER NOT NULL\n"
                + ")";
        executeUpdate(sql);
        sql = "CREATE TABLE IF NOT EXISTS " + TableName.xmas22_player + "(\n"
                + "user INTEGER NOT NULL,\n"
                + "present_index INTEGER NOT NULL,\n"
                + "posX INTEGER NOT NULL,\n"
                + "posY INTEGER NOT NULL,\n"
                + "posZ INTEGER NOT NULL,\n"
                + "opened BOOLEAN NOT NULL,\n"
                + "PRIMARY KEY(user, present_index)\n"
                + ")";
        executeUpdate(sql);
    }

    private static long createPlayer(String uuid)
    {
        String sql = "INSERT INTO " + TableName.players + "(uuid) VALUES (?)";
        executeUpdate(sql, uuid);

        sql = "SELECT id FROM " + TableName.players + " WHERE uuid=?";
        try(Connection db = dataSource.getConnection())
        {
            try(PreparedStatement query = prepareQuery(db, sql, uuid))
            {
                ResultSet result = query.executeQuery();
                if (result.next())
                {
                    return result.getLong("id");
                }
                else
                {
                    ErikComPlugin.console.severe("Database fault");
                    return -1;
                }
            }
        }
        catch (Exception e)
        {
            ErikComPlugin.console.severe("Failed to execute query");
            e.printStackTrace();
            return -1;
        }
    }
    public static long getPlayerID(UUID player)
    {
        String uuid = player.toString();
        String sql = "SELECT id FROM " + TableName.players + " WHERE uuid=?";
        try(Connection db = dataSource.getConnection())
        {
            try(PreparedStatement query = prepareQuery(db, sql, uuid))
            {
                ResultSet result = query.executeQuery();
                if (result.next())
                {
                    return result.getLong("id");
                }
                else
                {
                    return createPlayer(uuid);
                }
            }
        }
        catch (Exception e)
        {
            ErikComPlugin.console.severe("Failed to execute query");
            e.printStackTrace();
            return -1;
        }
    }
    public static long getPlayerID(OfflinePlayer player)
    {
        return getPlayerID(player.getUniqueId());
    }
    public static long getPlayerIfExists(UUID player)
    {
        String uuid = player.toString();
        String sql = "SELECT id FROM " + TableName.players + " WHERE uuid=?";
        try(Connection db = dataSource.getConnection())
        {
            try(PreparedStatement query = prepareQuery(db, sql, uuid))
            {
                ResultSet result = query.executeQuery();
                if (result.next())
                {
                    return result.getLong("id");
                }
                else
                {
                    return -1;
                }
            }
        }
        catch (Exception e)
        {
            ErikComPlugin.console.severe("Failed to execute query");
            e.printStackTrace();
            return -1;
        }
    }
    /** @noinspection unused*/
    public static long getPlayerIfExists(OfflinePlayer player)
    {
        return getPlayerIfExists(player.getUniqueId());
    }

    public static boolean getHasData(long playerID, DataField field)
    {
        String sql = "SELECT data FROM " + TableName.playerData + " WHERE user=? AND field=?";
        try(Connection db = dataSource.getConnection())
        {
            try(PreparedStatement query = prepareQuery(db, sql, playerID, field.id()))
            {
                ResultSet result = query.executeQuery();
                return result.next();
            }
        }
        catch (Exception e)
        {
            ErikComPlugin.console.severe("Failed to execute query");
            e.printStackTrace();
            return false;
        }
    }

    public static boolean getDataBoolean(long playerID, DataField field, boolean defaultValue)
    {
        String sql = "SELECT data FROM " + TableName.playerData + " WHERE user=? AND field=?";
        try(Connection db = dataSource.getConnection())
        {
            try(PreparedStatement query = prepareQuery(db, sql, playerID, field.id()))
            {
                ResultSet result = query.executeQuery();
                if (result.next())
                    return result.getBoolean("data");
                else
                    return defaultValue;
            }
        }
        catch (Exception e)
        {
            ErikComPlugin.console.severe("Failed to execute query");
            e.printStackTrace();
            return defaultValue;
        }
    }
    /** @noinspection unused*/
    public static long getDataInteger(long playerID, DataField field, long defaultValue)
    {
        String sql = "SELECT data FROM " + TableName.playerData + " WHERE user=? AND field=?";
        try(Connection db = dataSource.getConnection())
        {
            try(PreparedStatement query = prepareQuery(db, sql, playerID, field.id()))
            {
                ResultSet result = query.executeQuery();
                if (result.next())
                    return result.getLong("data");
                else
                    return defaultValue;
            }
        }
        catch (Exception e)
        {
            ErikComPlugin.console.severe("Failed to execute query");
            e.printStackTrace();
            return defaultValue;
        }
    }
    /** @noinspection unused*/
    public static double getDataFloating(long playerID, DataField field, double defaultValue)
    {
        String sql = "SELECT data FROM " + TableName.playerData + " WHERE user=? AND field=?";
        try(Connection db = dataSource.getConnection())
        {
            try(PreparedStatement query = prepareQuery(db, sql, playerID, field.id()))
            {
                ResultSet result = query.executeQuery();
                if (result.next())
                    return result.getDouble("data");
                else
                    return defaultValue;
            }
        }
        catch (Exception e)
        {
            ErikComPlugin.console.severe("Failed to execute query");
            e.printStackTrace();
            return defaultValue;
        }
    }
    /** @noinspection unused*/
    public static String getDataString(long playerID, DataField field, String defaultValue)
    {
        String sql = "SELECT data FROM " + TableName.playerData + " WHERE user=? AND field=?";
        try(Connection db = dataSource.getConnection())
        {
            try(PreparedStatement query = prepareQuery(db, sql, playerID, field.id()))
            {
                ResultSet result = query.executeQuery();
                if (result.next())
                    return result.getString("data");
                else
                    return defaultValue;
            }
        }
        catch (Exception e)
        {
            ErikComPlugin.console.severe("Failed to execute query");
            e.printStackTrace();
            return defaultValue;
        }
    }

    public static void setDataBoolean(long playerID, DataField field, boolean value)
    {
        String sql = "REPLACE INTO " + TableName.playerData + "(user, field, data) VALUES (?, ?, ?)";
        executeUpdate(sql, playerID, field.id(), value);
    }
    public static void setDataBooleanAsync(long playerID, DataField field, boolean value)
    {
        ErikComPlugin.server.getScheduler().runTaskAsynchronously(ErikComPlugin.plugin, () ->
        {
            String sql = "REPLACE INTO " + TableName.playerData + "(user, field, data) VALUES (?, ?, ?)";
            executeUpdate(sql, playerID, field.id(), value);
        });
    }
    /** @noinspection unused*/
    public static void setDataInteger(long playerID, DataField field, long value)
    {
        String sql = "REPLACE INTO " + TableName.playerData + "(user, field, data) VALUES (?, ?, ?)";
        executeUpdate(sql, playerID, field.id(), value);
    }
    public static void setDataIntegerAsync(long playerID, DataField field, long value)
    {
        ErikComPlugin.server.getScheduler().runTaskAsynchronously(ErikComPlugin.plugin, () ->
        {
            String sql = "REPLACE INTO " + TableName.playerData + "(user, field, data) VALUES (?, ?, ?)";
            executeUpdate(sql, playerID, field.id(), value);
        });
    }
    /** @noinspection unused*/
    public static void setDataFloating(long playerID, DataField field, double value)
    {
        String sql = "REPLACE INTO " + TableName.playerData + "(user, field, data) VALUES (?, ?, ?)";
        executeUpdate(sql, playerID, field.id(), value);
    }
    public static void setDataFloatingAsync(long playerID, DataField field, double value)
    {
        ErikComPlugin.server.getScheduler().runTaskAsynchronously(ErikComPlugin.plugin, () ->
        {
            String sql = "REPLACE INTO " + TableName.playerData + "(user, field, data) VALUES (?, ?, ?)";
            executeUpdate(sql, playerID, field.id(), value);
        });
    }
    /** @noinspection unused*/
    public static void setDataString(long playerID, DataField field, String value)
    {
        String sql = "REPLACE INTO " + TableName.playerData + "(user, field, data) VALUES (?, ?, ?)";
        executeUpdate(sql, playerID, field.id(), value);
    }
    public static void setDataStringAsync(long playerID, DataField field, String value)
    {
        ErikComPlugin.server.getScheduler().runTaskAsynchronously(ErikComPlugin.plugin, () ->
        {
            String sql = "REPLACE INTO " + TableName.playerData + "(user, field, data) VALUES (?, ?, ?)";
            executeUpdate(sql, playerID, field.id(), value);
        });
    }

    private static void loadPlayerGroups(PlayerData data)
    {
        String sql = "SELECT name FROM " + TableName.groupMembers + " JOIN " + TableName.groupTags + " ON groupID=id WHERE playerID=?";
        try(Connection db = dataSource.getConnection())
        {
            try(PreparedStatement query = prepareQuery(db, sql, data.database_id()))
            {
                ResultSet result = query.executeQuery();
                while (result.next())
                {
                    data.groups.add(GroupTagManager.getGroup(result.getString("name")));
                }
            }
        }
        catch (Exception e)
        {
            ErikComPlugin.console.severe("Failed to execute query");
            e.printStackTrace();
        }
    }
    private static void savePlayerGroups(PlayerData data)
    {
        String sql = "DELETE FROM " + TableName.groupMembers + " WHERE playerID=?";
        executeUpdate(sql, data.database_id());

        for (GroupTagManager.GroupTag group : data.groups)
        {
            sql = "INSERT INTO " + TableName.groupMembers + "(playerID, groupID) VALUES (?, (SELECT id FROM " + TableName.groupTags + " WHERE name=?))";
            executeUpdate(sql, data.database_id(), group.name);
        }
    }

    public static void loadPlayerData(Player player)
    {
        long playerID = getPlayerID(player);
        if (playerID < 0) return;
        PlayerData data = new PlayerData(player, playerID);
        PlayerData.map.put(player.getUniqueId(), data);
        loadPlayerGroups(data);


        String backpos_w = getDataString(playerID, PlayerField.BACKPOS_WOLRD, "");
        double backpos_x = getDataFloating(playerID, PlayerField.BACKPOS_X, 0.0);
        double backpos_y = getDataFloating(playerID, PlayerField.BACKPOS_Y, 0.0);
        double backpos_z = getDataFloating(playerID, PlayerField.BACKPOS_Z, 0.0);
        if (!backpos_w.isEmpty())
            data.backPosition(new Location(
                    ErikComPlugin.server.getWorld(UUID.fromString(backpos_w)),
                    backpos_x, backpos_y, backpos_z));

        data.flying(getDataBoolean(playerID, PlayerField.FLYING, data.flying()));
        data.pvpOn(getDataBoolean(playerID, PlayerField.PVP_ON, data.pvpOn()));
        data.silent_join(getDataBoolean(playerID, PlayerField.VANISH_SILENT_JOIN, data.silent_join()));
        data.silent_step(getDataBoolean(playerID, PlayerField.VANISH_SILENT_STEP, data.silent_step()));
        data.streamer_mode(getDataBoolean(playerID, PlayerField.STREAMER_MODE, data.streamer_mode()));
        data.staff_member(getDataBoolean(playerID, PlayerField.STAFF_MEMBER, data.staff_member()));
        data.dev_member(getDataBoolean(playerID, PlayerField.DEV_MEMBER, data.dev_member()));
        data.is_operator(getDataBoolean(playerID, PlayerField.IS_OPERATOR, data.is_operator()));
        if (getHasData(playerID, PlayerField.JOIN_DATE))
        {
            String dateStr = getDataString(playerID, PlayerField.JOIN_DATE, "");
            try
            {
                data.init_join_date(PlayerData.date_parser.parse(dateStr));
            }
            catch (ParseException e)
            {
                e.printStackTrace();
            }
        }
        else
        {
            data.init_join_date(data.init_join_date());
        }
        data.totalPlayTime(getDataInteger(playerID, PlayerField.PLAYTIME, data.totalPlayTime()));
    }

    public static void savePlayerData(Player player)
    {
        if (player == null) return;
        PlayerData data = PlayerData.get(player);
        savePlayerGroups(data);
    }

    public static void loadGroupTags()
    {
        String sql = "SELECT name, priority, tag_display, tag_color FROM " + TableName.groupTags;
        try(Connection db = dataSource.getConnection())
        {
            try(PreparedStatement query = prepareQuery(db, sql))
            {
                ResultSet result = query.executeQuery();
                while (result.next())
                {
                    String id = result.getString("name");
                    int index = result.getInt("priority");
                    String name = result.getString("tag_display");
                    String color_code = result.getString("tag_color");
                    if (!NamedTextColor.NAMES.keys().contains(color_code))
                    {
                        ErikComPlugin.console.severe("Failed to parse color '" + color_code + "'");
                        color_code = "WHITE";
                    }
                    GroupTagManager.addGroup(id, name, color_code, index);
                }
            }
        }
        catch (Exception e)
        {
            ErikComPlugin.console.severe("Failed to execute query");
            e.printStackTrace();
        }
    }

    public static void createGroupTag(GroupTagManager.GroupTag group)
    {
        String sql = "INSERT INTO " + TableName.groupTags + " (name, priority, tag_display, tag_color) VALUES (?, ?, ?, ?)";
        executeUpdate(sql, group.name, group.sortIndex, group.tag, group.color.toString());
    }

    public static void updateGroupTag(GroupTagManager.GroupTag group)
    {
        String sql = "UPDATE " + TableName.groupTags + " SET priority=?, tag_display=?, tag_color=? WHERE name=?";
        executeUpdate(sql, group.sortIndex, group.tag, group.color.toString(), group.name);
    }

    public static void deleteGroupTag(String name)
    {
        String sql = "DELETE FROM " + TableName.groupTags + " WHERE name=?";
        executeUpdate(sql, name);
    }

    public static void addGroupMember(String name, UUID player)
    {
        long playerID = getPlayerID(player);
        String sql = "INSERT OR IGNORE INTO " + TableName.groupMembers + "(playerID, groupID) VALUES (?, (SELECT id FROM " + TableName.groupTags + " WHERE name=?))";
        executeUpdate(sql, playerID, name);
    }

    public static void removeGroupMember(String name, UUID player)
    {
        long playerID = getPlayerID(player);
        String sql = "DELETE FROM " + TableName.groupMembers + " WHERE playerID=? and groupID=(SELECT id FROM " + TableName.groupTags + " WHERE name=?)";
        executeUpdate(sql, playerID, name);
    }

    public static List<String> getOperators()
    {
        List<String> players = new ArrayList<>();
        String sql = "SELECT uuid FROM " + TableName.players + " JOIN " + TableName.playerData + " ON id=user WHERE field=? and data=?";
        try(Connection db = dataSource.getConnection())
        {
            try(PreparedStatement query = prepareQuery(db, sql, PlayerField.IS_OPERATOR.id(), true))
            {
                ResultSet result = query.executeQuery();
                while (result.next())
                {
                    UUID id = UUID.fromString(result.getString("uuid"));
                    String p = ErikComPlugin.server.getOfflinePlayer(id).getName();
                    if (p == null) continue;
                    players.add(p.toLowerCase());
                }
            }
        }
        catch (Exception e)
        {
            ErikComPlugin.console.severe("Failed to execute query");
            e.printStackTrace();
        }
        return players;
    }

    public static List<String> getStaffMembers()
    {
        List<String> players = new ArrayList<>();
        String sql = "SELECT uuid FROM " + TableName.players + " JOIN " + TableName.playerData + " ON id=user WHERE field=? and data=?";
        try(Connection db = dataSource.getConnection())
        {
            try(PreparedStatement query = prepareQuery(db, sql, PlayerField.STAFF_MEMBER.id(), true))
            {
                ResultSet result = query.executeQuery();
                while (result.next())
                {
                    UUID id = UUID.fromString(result.getString("uuid"));
                    String p = ErikComPlugin.server.getOfflinePlayer(id).getName();
                    if (p == null) continue;
                    players.add(p.toLowerCase());

                }
            }
        }
        catch (Exception e)
        {
            ErikComPlugin.console.severe("Failed to execute query");
            e.printStackTrace();
        }
        return players;
    }

    public static List<String> getDevelopers()
    {
        List<String> players = new ArrayList<>();
        String sql = "SELECT uuid FROM " + TableName.players + " JOIN " + TableName.playerData + " ON id=user WHERE field=? and data=?";
        try(Connection db = dataSource.getConnection())
        {
            try(PreparedStatement query = prepareQuery(db, sql, PlayerField.DEV_MEMBER.id(), true))
            {
                ResultSet result = query.executeQuery();
                while (result.next())
                {
                    UUID id = UUID.fromString(result.getString("uuid"));
                    String p = ErikComPlugin.server.getOfflinePlayer(id).getName();
                    if (p == null) continue;
                    players.add(p.toLowerCase());
                }
            }
        }
        catch (Exception e)
        {
            ErikComPlugin.console.severe("Failed to execute query");
            e.printStackTrace();
        }
        return players;
    }
    public static List<String> getGroupMembers(GroupTagManager.GroupTag group)
    {
        List<String> players = new ArrayList<>();
        String sql = "SELECT uuid FROM " + TableName.players + " JOIN " + TableName.groupMembers + " ON id=playerID WHERE groupID=(SELECT id FROM " + TableName.groupTags + " WHERE name=?)";
        try(Connection db = dataSource.getConnection())
        {
            try(PreparedStatement query = prepareQuery(db, sql, group.name))
            {
                ResultSet result = query.executeQuery();
                while (result.next())
                {
                    UUID id = UUID.fromString(result.getString("uuid"));
                    String p = ErikComPlugin.server.getOfflinePlayer(id).getName();
                    if (p == null) continue;
                    players.add(p.toLowerCase());
                }
            }
        }
        catch (Exception e)
        {
            ErikComPlugin.console.severe("Failed to execute query");
            e.printStackTrace();
        }
        return players;
    }

    public static void savePresentToPool(Location location, int pool)
    {
        String sql = "INSERT INTO " + TableName.xmas22_global + "(pos_pool, posX, posY, posZ) VALUES (?, ?, ?, ?)";
        executeUpdate(sql, pool, location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    public static void deletePresent(Location location)
    {
        String sql = "DELETE FROM " + TableName.xmas22_global + " WHERE posX=? AND posY=? AND posZ=?";
        executeUpdate(sql, location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    public static void loadXmasPools()
    {
        String sql = "SELECT pos_pool, posX, posY, posZ FROM " + TableName.xmas22_global;
        try(Connection db = dataSource.getConnection())
        {
            try(PreparedStatement query = prepareQuery(db, sql))
            {
                ResultSet result = query.executeQuery();
                while (result.next())
                {
                    int pool_id = result.getInt("pos_pool");
                    int posX = result.getInt("posX");
                    int posY = result.getInt("posY");
                    int posZ = result.getInt("posZ");
                    var pool = EventXmas22Manager.presentLocationPool(pool_id);
                    if (pool != null)
                        pool.add(new Location(ErikComPlugin.world_normal, posX, posY, posZ));
                    else
                        ErikComPlugin.console.severe("loadXmasPools could not load into pool " + pool_id);
                }
            }
        }
        catch (Exception e)
        {
            ErikComPlugin.console.severe("Failed to execute query");
            e.printStackTrace();
        }
    }

    public static void loadAdventCalendar(PlayerData data)
    {
        String sql = "SELECT present_index, posX, posY, posZ, opened FROM " + TableName.xmas22_player + " WHERE user=?";
        try(Connection db = dataSource.getConnection())
        {
            try(PreparedStatement query = prepareQuery(db, sql, data.database_id()))
            {
                ResultSet result = query.executeQuery();
                while (result.next())
                {
                    int index = result.getInt("present_index");
                    int posX = result.getInt("posX");
                    int posY = result.getInt("posY");
                    int posZ = result.getInt("posZ");
                    boolean opened = result.getBoolean("opened");
                    EventXmas22Manager.Present present = new EventXmas22Manager.Present(data.adventCalendar, new Location(ErikComPlugin.world_normal, posX, posY, posZ), opened);
                    data.adventCalendar.presents[index] = present;
                }
            }
        }
        catch (Exception e)
        {
            data.adventCalendar.is_invalid = true;
            ErikComPlugin.console.severe("Failed to execute query");
            e.printStackTrace();
        }
    }

    public static void saveAdventCalendarPresent(PlayerData data, int index)
    {
        String sql = "REPLACE INTO " + TableName.xmas22_player + "(user, present_index, posX, posY, posZ, opened) VALUES (?, ?, ?, ?, ?, ?)";
        EventXmas22Manager.Present present = data.adventCalendar.presents[index];
        Location location = present.location;
        executeUpdate(sql,data.database_id(), index, location.getBlockX(), location.getBlockY(), location.getBlockZ(), present.opened);
    }

    public static boolean openPresent(Player player, int i)
    {
        String sql = "UPDATE " + TableName.xmas22_player + " SET opened=? WHERE user=? AND present_index=?";
        return executeUpdate(sql, true, PlayerData.get(player).database_id(), i);
    }
}

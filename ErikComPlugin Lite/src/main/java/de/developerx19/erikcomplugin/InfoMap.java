package de.developerx19.erikcomplugin;

import org.bukkit.ChatColor;
import org.bukkit.potion.PotionEffectType;

import java.util.List;

public class InfoMap
{
    static final long pvpCmdCooldown = 30000;
    static final long afkTime = 260000;

    public static final List<PotionEffectType> potion_effect_bad_all = List.of(
            PotionEffectType.SLOW,
            PotionEffectType.SLOW_DIGGING,
            PotionEffectType.HARM,
            PotionEffectType.CONFUSION,
            PotionEffectType.BLINDNESS,
            PotionEffectType.DARKNESS,
            PotionEffectType.HUNGER,
            PotionEffectType.WEAKNESS,
            PotionEffectType.POISON,
            PotionEffectType.WITHER,
            PotionEffectType.UNLUCK,
            PotionEffectType.LEVITATION);
    public static final List<PotionEffectType> potion_effect_bad_damaging = List.of(
                PotionEffectType.HARM,
                PotionEffectType.POISON,
                PotionEffectType.WITHER);

    public static final String cmd_fb = ChatColor.GRAY + " >> ";
    public static final String cmd_permit_err = ChatColor.DARK_RED + "You are not permitted to use this command.";

}

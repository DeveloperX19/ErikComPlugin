package de.developerx19.erikcomplugin;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.UUID;

public class CustomItemGenerator
{
    public static ItemStack getHead(String base64Skin, int stackSize) {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD, stackSize);
        SkullMeta skullMeta = (SkullMeta) item.getItemMeta();
        GameProfile gameProfile = new GameProfile(UUID.randomUUID(), null);
        gameProfile.getProperties().put("textures", new Property("textures", base64Skin));
        try {
            Field profileField = skullMeta.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            profileField.set(skullMeta, gameProfile);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }
        item.setItemMeta(skullMeta);
        return item;
    }

    public static ItemStack getHead(String base64Skin, Component name, int stackSize)
    {
        ItemStack item = getHead(base64Skin, stackSize);
        var meta = item.getItemMeta();
        meta.displayName(name);
        item.setItemMeta(meta);
        return item;
    }



    public static ItemStack demonic_root(int stackSize)
    {
        var key = new NamespacedKey(ErikComPlugin.plugin, "demonic_root");
        ItemStack item = new ItemStack(Material.WITHER_ROSE, stackSize);
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, 0);

        meta.displayName(Component.text("Dämonische Wurzel").color(TextColor.color(0xAE0000)));
        item.setItemMeta(meta);

        item.addUnsafeEnchantment(Enchantment.LOOT_BONUS_BLOCKS, 1);
        item.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        return item;
    }

    public static ItemStack explosive_c4(int stackSize)
    {
        var key = new NamespacedKey(ErikComPlugin.plugin, "explosive_c4");
        ItemStack item = getHead(EventManager.head_c4, stackSize);
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, 1);

        meta.displayName(Component.text("C4").color(TextColor.color(0xDB7300)));
        item.setItemMeta(meta);

        return item;
    }

    public static ItemStack gaensebraten(int stackSize)
    {
        var key = new NamespacedKey(ErikComPlugin.plugin, "gaensebraten");
        ItemStack item = new ItemStack(Material.COOKED_CHICKEN, stackSize);
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, 0);

        meta.displayName(Component.text("Gänsebraten").color(TextColor.color(0xDB7300)));
        item.setItemMeta(meta);

        item.addUnsafeEnchantment(Enchantment.LOOT_BONUS_BLOCKS, 1);
        item.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        return item;
    }

    public static ItemStack gluehwein()
    {
        ItemStack item = new ItemStack(Material.POTION, 1);
        var meta = item.getItemMeta();
        ((PotionMeta) meta).addCustomEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, 18000, 4), true);
        ((PotionMeta) meta).addCustomEffect(new PotionEffect(PotionEffectType.HUNGER, 1800, 2), true);

        meta.displayName(Component.text("Glühwein").color(TextColor.color(0xE60065)));
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack midas_sword()
    {
        var key = new NamespacedKey(ErikComPlugin.plugin, "midas_sword");
        ItemStack item = new ItemStack(Material.GOLDEN_SWORD, 1);
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, 5000);

        meta.displayName(Component.text("König Midas' Schwert").color(TextColor.color(0xFFDD00)));
        meta.lore(Collections.singletonList(Component.text("Haltbarkeit: " + 5000 + " / 5000")));
        meta.setUnbreakable(true);
        item.setItemMeta(meta);

        item.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 6);
        item.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        return item;
    }

    private static ItemStack item_coupon(int stackSize, String quickinfo, String nbtValue)
    {
        var key = new NamespacedKey(ErikComPlugin.plugin, "item_coupon");
        ItemStack item = new ItemStack(Material.PAPER, stackSize);
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, nbtValue);

        meta.displayName(Component.text("Item-Coupon ").color(TextColor.color(0xFF9600))
                .append(Component.text(quickinfo).color(TextColor.color(0xE6B58A))));
        item.setItemMeta(meta);

        item.addUnsafeEnchantment(Enchantment.LOOT_BONUS_BLOCKS, 1);
        item.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        return item;
    }

    public static ItemStack item_couponXgestein(int stackSize)
    {
        return item_coupon(stackSize, "(Gestein)", "gestein");
    }
    public static ItemStack item_couponXpflanze(int stackSize)
    {
        return item_coupon(stackSize, "(Nutzpflanzen)", "pflanze");
    }

    public static ItemStack item_couponXwolle(int stackSize)
    {
        return item_coupon(stackSize, "(Wolle)", "wolle");
    }

    public static ItemStack item_couponXnahrung(int stackSize)
    {
        return item_coupon(stackSize, "(Nahrungsmittel)", "nahrung");
    }

    public static ItemStack item_couponXstaemme(int stackSize)
    {
        return item_coupon(stackSize, "(Holzstämme)", "staemme");
    }

    public static ItemStack item_couponXglas(int stackSize)
    {
        return item_coupon(stackSize, "(Glas)", "glas");
    }

    public static ItemStack item_couponXtrank(int stackSize)
    {
        return item_coupon(stackSize, "(Trank)", "trank");
    }

    public static ItemStack item_couponXbeton(int stackSize)
    {
        return item_coupon(stackSize, "(Beton)", "beton");
    }

    public static ItemStack item_couponXredstone(int stackSize)
    {
        return item_coupon(stackSize, "(Redstone)", "redstone");
    }
}

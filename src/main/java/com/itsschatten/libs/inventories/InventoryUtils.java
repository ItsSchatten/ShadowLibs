package com.itsschatten.libs.inventories;

import com.itsschatten.libs.Utils;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class InventoryUtils {

    /**
     * Create an Item for an inventory. (Will be moved to the Utils class later.)
     *
     * @param inv    The inventory that the item should be created for.
     * @param where  The place where the item should be (should be the actual slot you want it in so if you want slot one use 1)
     * @param mat    The item type.
     * @param amount The amount of the item you want.
     * @param name   The name of the item.
     * @param lore   The lore of the item. (Use the colorize method.)
     */
    public static void createItem(@NotNull Inventory inv, int where, Material mat, int amount, String name, List<String> lore) {
        ItemStack item = new ItemStack(mat, amount);
        ItemMeta itemMeta = item.getItemMeta();

        if (itemMeta == null)
           return;

        itemMeta.setDisplayName(Utils.colorize(name));
        itemMeta.setLore(lore);

        itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);

        item.setItemMeta(itemMeta);
        inv.setItem(where - 1, item);

    }

    /**
     * Create an Item for an inventory. (Will be moved to the Utils class later.)
     *
     * @param inv    The inventory that the item should be created for.
     * @param where  The place where the item should be (should be the actual slot you want it in so if you want slot one use 1)
     * @param mat    The item type.
     * @param amount The amount of the item you want.
     * @param name   The name of the item.
     */
    public static void createItem(@NotNull Inventory inv, int where, Material mat, int amount, String name) {
        ItemStack item = new ItemStack(mat, amount);
        ItemMeta itemMeta = item.getItemMeta();

        if (itemMeta == null)
            return;

        itemMeta.setDisplayName(Utils.colorize(name));

        itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);


        item.setItemMeta(itemMeta);
        inv.setItem(where - 1, item);

    }

    /**
     * Create an Item for an inventory. (This should be implemented into the Utils class at some point.)
     *
     * @param inv    The inventory that the item should be created for.
     * @param where  The place where the item should be (should be the actual slot you want it in so if you want slot one use 1)
     * @param mat    The item type.
     * @param amount The amount of the item you want.
     * @param glow   Should the item have an enchantment applied that will make it glow?
     * @param name   The name of the item.
     * @param lore   The lore of the item. (Use the colorize method.)
     */
    public static void createItem(@NotNull Inventory inv, int where, Material mat, int amount, boolean glow, String name, List<String> lore) {
        ItemStack item = new ItemStack(mat, amount);
        ItemMeta itemMeta = item.getItemMeta();

        if (itemMeta == null)
            return;

        itemMeta.setDisplayName(Utils.colorize(name));

        if (glow) {
            itemMeta.addEnchant(Enchantment.ARROW_DAMAGE, 1, true);
        }

        itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        inv.setItem(where - 1, item);
    }

    /**
     * Create an Item for an inventory. (This should be implemented into the Utils class at some point.)
     *
     * @param inv    The inventory that the item should be created for.
     * @param where  The place where the item should be (should be the actual slot you want it in so if you want slot one use 1)
     * @param mat    The item type.
     * @param amount The amount of the item you want.
     * @param glow   Should the item have an enchantment applied that will make it glow?
     * @param name   The name of the item.
     */
    public static void createItem(@NotNull Inventory inv, int where, Material mat, int amount, boolean glow, String name) {
        ItemStack item = new ItemStack(mat, amount);
        ItemMeta itemMeta = item.getItemMeta();
        if (itemMeta == null)
            return;

        itemMeta.setDisplayName(Utils.colorize(name));

        if (glow) {
            itemMeta.addEnchant(Enchantment.ARROW_DAMAGE, 1, true);
            itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);
        }

        item.setItemMeta(itemMeta);
        inv.setItem(where - 1, item);
    }

    /**
     * Set the other inventory squares to a specific items. (Should be called FIRST so createItem can override it.)
     *
     * @param inv        The inventory.
     * @param fillerItem The item that should fill the inventory.
     */
    public static void setSparePanels(@NotNull Inventory inv, Material fillerItem) {
        ItemStack glass = new ItemStack(fillerItem, 1);
        ItemMeta glassMeta = glass.getItemMeta();

        if (glassMeta == null)
            return;

        glassMeta.setDisplayName(Utils.colorize("&f"));
        glassMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);

        glass.setItemMeta(glassMeta);

        for (int i = 0; i < inv.getSize(); i++) {
            inv.setItem(i, glass);
        }
    }

}

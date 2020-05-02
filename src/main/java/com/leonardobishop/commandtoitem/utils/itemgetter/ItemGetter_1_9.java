package com.leonardobishop.commandtoitem.utils.itemgetter;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

public class ItemGetter_1_9 implements ItemGetter {
    /*
    item reader for 1_9_R0_1 to 1_12_R0_1
    
    supporting:
     - name
     - material (+ DATA)
     - lore
     - enchantments (NOT NamespacedKey)
     - itemflags
     - unbreakable
     */
    @Override
    public ItemStack getItem(String path, FileConfiguration config, JavaPlugin plugin) {
        String cName = config.getString(path + ".name", path + ".name");
        String cType = config.getString(path + ".item", path + ".item");
        List<String> cLore = config.getStringList(path + ".lore");
        List<String> cItemFlags = config.getStringList(path + ".itemflags");
        boolean unbreakable = config.getBoolean(path + ".unbreakable", false);

        String name;
        Material type = null;
        int data = 0;

        // lore
        List<String> lore = new ArrayList<>();
        if (cLore != null) {
            for (String s : cLore) {
                lore.add(ChatColor.translateAlternateColorCodes('&', s));
            }
        }

        // name
        name = ChatColor.translateAlternateColorCodes('&', cName);

        // material
        if (Material.getMaterial(cType) != null) {
            type = Material.getMaterial(cType);
        } else if (cType.contains(":")) {
            String[] parts = cType.split(Pattern.quote(":"));
            if (parts.length > 1) {
                if (Material.getMaterial(parts[0]) != null) {
                    type = Material.getMaterial(parts[0]);
                }
                if (StringUtils.isNumeric(parts[1])) {
                    data = Integer.parseInt(parts[1]);
                }
            }
        }

        if (type == null) {
            plugin.getLogger().warning("Unrecognised material: " + cType);
            type = Material.STONE;
        }

        ItemStack is = new ItemStack(type, 1, (short) data);
        ItemMeta ism = is.getItemMeta();
        ism.setLore(lore);
        ism.setDisplayName(name);
        ism.setUnbreakable(unbreakable);

        // item flags
        if (config.isSet(path + ".itemflags")) {
            for (String flag : cItemFlags) {
                for (ItemFlag iflag : ItemFlag.values()) {
                    if (iflag.toString().equals(flag)) {
                        ism.addItemFlags(iflag);
                        break;
                    }
                }
            }
        }

        // Moving this before the enchantments code block somehow fixes item flags not appearing.
        // Not sure why that is. Likely some quirks with how Minecraft parses its item meta.
        is.setItemMeta(ism);
        
        // enchantments
        if (config.isSet(path + ".enchantments")) {
            for (String key : config.getStringList(path + ".enchantments")) {
                String[] split = key.split(":");
                String ench = split[0];
                String levelName;
                if (split.length >= 2) {
                    levelName = split[1];
                } else {
                    levelName = "1";
                }

                Enchantment enchantment;
                if ((enchantment = Enchantment.getByName(ench)) == null) {
                    plugin.getLogger().warning("Unrecognised enchantment: " + ench);
                    continue;
                }

                int level;
                try {
                    level = Integer.parseInt(levelName);
                } catch (NumberFormatException e) {
                    level = 1;
                }

                is.addUnsafeEnchantment(enchantment, level);
            }
        }
        return is;
    }
	
}

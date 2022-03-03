package ru.dmitriymx.bukkit.zerodurability;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.annotation.plugin.Plugin;
import org.bukkit.plugin.java.annotation.plugin.Website;
import org.bukkit.plugin.java.annotation.plugin.author.Author;

import java.util.stream.Stream;

@Plugin(name = PluginInfo.NAME, version = PluginInfo.VERSION)
@Author(PluginInfo.AUTHOR)
@Website(PluginInfo.WEBSITE)
public class ZeroDurabilityPlugin extends JavaPlugin implements Listener {
    private FileConfiguration configuration;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        configuration = this.getConfig();
        getServer().getPluginManager().registerEvents(this, this);
    }

    @EventHandler
    public void onCraft(CraftItemEvent event) {
        if (event.getCurrentItem() == null) {
            return;
        }

        findAndBreakItem(event.getCurrentItem());
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getItem() == null) {
            return;
        }

        switch (event.getAction()) {
            case LEFT_CLICK_AIR:
            case RIGHT_CLICK_AIR:
                return;
        }

        findAndBreakItem(event.getItem());
    }

    @SuppressWarnings("deprecation")
    private void findAndBreakItem(ItemStack itemStack) {
        Material material = itemStack.getType();
        Stream<String> stream;
        if (material.isLegacy()) {
            stream = configuration.getStringList("legacy").stream()
                    .filter(item -> item.equalsIgnoreCase(material.name()));
        } else {
            NamespacedKey key = material.getKey();
            stream = configuration.getStringList("modern." + key.getNamespace()).stream()
                    .filter(item -> item.equalsIgnoreCase(key.getKey()));
        }
        stream.findFirst().ifPresent(s -> breakItem(itemStack));
    }

    private void breakItem(ItemStack itemStack) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (!(itemMeta instanceof Damageable)) {
            return;
        }

        Damageable damageable = (Damageable) itemMeta;
        damageable.setDamage(itemStack.getType().getMaxDurability());
        itemStack.setItemMeta(damageable);
    }
}

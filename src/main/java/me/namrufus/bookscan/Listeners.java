package me.namrufus.bookscan;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.meta.BookMeta;

public class Listeners implements Listener {

    private final BookScan plugin;

    public Listeners(BookScan plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        if (!(event.getDamager() instanceof Player)) return;

        Player entity = (Player) event.getEntity();
        Player damager = (Player) event.getDamager();

        if (!damager.getItemInHand().getType().equals(Material.WRITTEN_BOOK)) return;
        if (plugin.requireGroundMaterial() && !entity.getLocation().subtract(0, 1, 0).getBlock().getType().equals(plugin.getGroundMaterial()))
            return;

        event.setCancelled(true);

        StringBuilder itemsFound = new StringBuilder();
        int totalTypes = 0;
        for (String line : Util.getAllLinesInBook((BookMeta) damager.getItemInHand().getItemMeta())) {
            line = line.trim().toUpperCase();
            String[] split = line.split(" +");

            if (split.length < 1) continue;

            Material m = Material.getMaterial(split[0]);

            if (m == null) continue;

            byte metaValue = -1;
            if (split.length > 1) {
                try {
                    metaValue = Byte.parseByte(split[1]);
                } catch (NumberFormatException e) {
                }
            }

            int itemCount = Util.getItemCountInInventory(entity, m, metaValue);
            if (itemCount != 0) {
                totalTypes++;
                itemsFound.append(itemCount + " " + m + (metaValue != -1 ? ":" + metaValue : "") + ", ");
            }
        }

        if (totalTypes == 0) {
            damager.sendMessage(plugin.getPrefix() + ChatColor.GOLD + "You found no items on " + ChatColor.YELLOW + entity.getName());
            entity.sendMessage(plugin.getPrefix() + ChatColor.YELLOW + damager.getName() + org.bukkit.ChatColor.GOLD + " tried to scan you for items!");
            entity.sendMessage(plugin.getPrefix() + ChatColor.YELLOW + "They didn't find anything, however :)");
        } else {
            damager.sendMessage(plugin.getPrefix() + ChatColor.GOLD + "You found " + totalTypes + " different types of items on " + ChatColor.YELLOW + entity.getName());
            damager.sendMessage(plugin.getPrefix() + ChatColor.GOLD + "The items found were: " + ChatColor.WHITE + itemsFound.toString().substring(0, itemsFound.toString().length() - 2));
            entity.sendMessage(plugin.getPrefix() + ChatColor.YELLOW + damager.getName() + org.bukkit.ChatColor.GOLD + " tried to scan you for items!");
            entity.sendMessage(plugin.getPrefix() + ChatColor.GOLD + "The items found were: " + ChatColor.WHITE + itemsFound.toString().substring(0, itemsFound.toString().length() - 2));
        }
    }

}

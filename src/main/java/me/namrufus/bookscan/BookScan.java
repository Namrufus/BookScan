package me.namrufus.bookscan;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;

public class BookScan extends JavaPlugin {

    private boolean requireGroundMaterial;
    private Material groundMaterial;
    private String prefix;

    @Override
    public void onEnable() {
        setupConfig();
    }

    private void setupConfig() {
        this.saveDefaultConfig();

        requireGroundMaterial = this.getConfig().getBoolean("require_ground_material");
        groundMaterial = Material.getMaterial(this.getConfig().getString("ground_material"));
        prefix = ChatColor.translateAlternateColorCodes('&', this.getConfig().getString("prefix"));
    }

    public boolean requireGroundMaterial() {
        return requireGroundMaterial;
    }

    public Material getGroundMaterial() {
        return groundMaterial;
    }

    public String getPrefix() {
        return prefix;
    }
}

package com.github.namrufus.BookScan;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

public class BookScan extends JavaPlugin implements Listener {
	private class Config {
		public final boolean REQUIRE_GROUND_MATERIAL;
		public final Material GROUND_MATERIAL;
		public Config(ConfigurationSection config) {
			REQUIRE_GROUND_MATERIAL = config.getBoolean("require_ground_material");
			GROUND_MATERIAL = Material.getMaterial(config.getString("ground_material"));
		}
	}
	
	// ================================================================================================================
	
	private Config config;
	
	// ================================================================================================================
	
	public void onEnable() {
		// if the config file doesn't exist, create it
		this.saveDefaultConfig();
		
		// load configurations
		config = new Config(this.getConfig());
		
	    // register events
		getServer().getPluginManager().registerEvents(this, this);
	}
		
	public void onDisable() {
	}
		
	// ----------------------------------------------------------------------------------------------------------------
		
	@EventHandler
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {	
		
		// if a player is not the damager, then quit
		if (!(event.getDamager().getType() == EntityType.PLAYER)) return;
		
		Player searcherPlayer = (Player)event.getDamager();
		
		// if a player is no being damage, then quit
		if (!(event.getEntityType() == EntityType.PLAYER)) return;
		
		Player searchedPlayer = (Player)event.getEntity();
		
		ItemStack itemStackInHand = searcherPlayer.getItemInHand();
		
		if (itemStackInHand.getType() == Material.WRITTEN_BOOK) {
			// cancel the event so the searched player does not get hurt
			event.setCancelled(true);
			
			getLogger().info("wah wah: " + getBlockMaterialStandingOn(searchedPlayer));
			
			// only do anything if the player is standing on the required ground material
			if (config.REQUIRE_GROUND_MATERIAL && getBlockMaterialStandingOn(searchedPlayer) != config.GROUND_MATERIAL) return;
			
			String searcherMessage = "Search found: ";
			BookMeta bookMeta = (BookMeta) itemStackInHand.getItemMeta();
			
			List<String> lines = getAllLinesInBook(bookMeta);
			
			for (String line : lines) {
				getLogger().info(line);
				
				// now parse the book data and check inventories
				
				line = line.trim().toUpperCase();
				
				String[] split = line.split(" +");
				
				if (split.length < 1) continue;
				
				Material checkMaterial = Material.getMaterial(split[0]);
				
				if (checkMaterial == null) continue;
				
				searcherMessage += ", " + getItemCountInInventory(searchedPlayer, checkMaterial) +  " " + checkMaterial.name();
			}
			
			searcherPlayer.sendMessage("§7[" + getDescription().getName() + "] " + searcherMessage);
		}
	}
	
	private List<String> getAllLinesInBook(BookMeta bookMeta) {
		List<String> result = new ArrayList<>();
		
		for (String page : bookMeta.getPages()) {
			for (String line : page.split("\\r?\\n|\\r")) {
				result.add(line);
			}
		}
		
		return result;
	}
	
	// returns the number of items in the player's inventory that match the given Material
	private int getItemCountInInventory(Player player, Material material) {
		int count = 0;
		
		for (ItemStack itemStack : player.getInventory().getContents()) {

			if (itemStack == null) continue;
			
			if (itemStack.getType() == material) {
				count += itemStack.getAmount();
			}
		}
		
		return count;
	}
	
	private Material getBlockMaterialStandingOn(Player player) {
		return player.getLocation().getBlock().getRelative(0, -1, 0).getType();
	}
}

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
		// determine if a player is hitting another player with a written book
		// the player that is swinging the book is the searcher player
		// the player that is hit by the book is the searched player
		if (!(event.getDamager().getType() == EntityType.PLAYER)) return;
		Player searcherPlayer = (Player)event.getDamager();
		
		if (!(event.getEntityType() == EntityType.PLAYER)) return;
		Player searchedPlayer = (Player)event.getEntity();
		
		ItemStack itemStackInHand = searcherPlayer.getItemInHand();
		if (itemStackInHand.getType() != Material.WRITTEN_BOOK) return;
		
		// cancel the event so the searched player does not get hurt
		event.setCancelled(true);
		
		// only do anything if the player is standing on the required ground material
		if (config.REQUIRE_GROUND_MATERIAL && getBlockMaterialStandingOn(searchedPlayer) != config.GROUND_MATERIAL) return;
		
		// loop through all lines in the book
		List<String> lines = getAllLinesInBook((BookMeta) itemStackInHand.getItemMeta());
		String searcherMessage = "";
		int totalFoundItemTypes = 0;
		String searchedMessage = "";
		for (String line : lines) {
			
			// clean the line and split the line by spaces
			line = line.trim().toUpperCase();
			String[] split = line.split(" +");
			
			// parse the material, if it does not exist, then just skip to the next line
			if (split.length < 1) continue;
			Material checkMaterial = Material.getMaterial(split[0]);	
			if (checkMaterial == null) continue;
			
			// parse the data value. If it exists, raise the "useData" flag
			byte checkData = 0;
			boolean useData = false;
			if (split.length >= 2) {
				try {
					checkData = Byte.parseByte(split[1]);
					useData = true;
				} catch (NumberFormatException e) {
					useData = false;
				}
			}
			
			// check inventory
			String materialName = checkMaterial.name() + (useData ? (":" + checkData) : "");
			searchedMessage += ", " + materialName;
			int itemCount = getItemCountInInventory(searchedPlayer, checkMaterial, checkData, useData);
			if (itemCount != 0) {
				totalFoundItemTypes++;
				searcherMessage += ", " + itemCount +  " " + materialName;
			}
		}
		
		if (totalFoundItemTypes == 0) {
			searcherMessage = "nothing";
		}
		searcherPlayer.sendMessage("§7[" + getDescription().getName() + "] " + "Scanned " + searchedPlayer.getName() + ", found: " + searcherMessage);
		searchedPlayer.sendMessage("§7[" + getDescription().getName() + "] " + searcherPlayer.getName() + " scanned you for: " + searchedMessage);
	}
	
	// ----------------------------------------------------------------------------------------------------------------
	
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
	// if the useData flag is raised, then also match by the provided data value
	private int getItemCountInInventory(Player player, Material material, byte data, boolean useData) {
		int count = 0;
		
		for (ItemStack itemStack : player.getInventory().getContents()) {

			if (itemStack == null) continue;
			
			if (itemStack.getType() == material) {
				if (!useData || itemStack.getData().getData() == data) {
					count += itemStack.getAmount();
				}
			}
		}
		
		return count;
	}
	
	private Material getBlockMaterialStandingOn(Player player) {
		return player.getLocation().getBlock().getRelative(0, -1, 0).getType();
	}
}

package com.outlook.corey_young.spellarrows;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

public final class SpellArrows extends JavaPlugin {
	//HashMaps for storing shot arrow's potion effects and player settings.
	public static HashMap<Integer, String> arrowTypeMap = new HashMap<Integer, String>();
	public static HashMap<Integer, String> arrowMap = new HashMap<Integer, String>();
	public static HashMap<String, Boolean> sortArrowMap = new HashMap<String, Boolean>();
	
	@Override
	public void onEnable() {
		getServer().getPluginManager().registerEvents(new MyListener(), this);
		getCommand("sortarrows").setExecutor(new MyCommandExecutor());
		loadConfiguration();
		createMagicArrows();
	}
	
	@Override
	public void onDisable() {
		//Save config.yml
		saveConfiguration();
	}
	
	public void loadConfiguration() {
		//load default configuration
		getConfig().options().copyDefaults(true);
		saveConfig();
		//load config player settings as Map, then copy into HashMap
		if (getConfig().contains("sortArrows")) {
			Map<String, Object> configMap = getConfig().getConfigurationSection("sortArrows").getValues(false);
			for (String playerName : configMap.keySet()) {
				boolean sortArrows = (boolean) configMap.get(playerName);
				sortArrowMap.put(playerName, sortArrows);
			}
		}
		//load config arrowTypes as Maps, then copy into HashMap
		if (getConfig().contains("arrowTypes.displayName")) {
			Map<String, Object> configNameMap = getConfig().getConfigurationSection("arrowTypes.displayName").getValues(false);
			Map<String, Object> configIDMap = getConfig().getConfigurationSection("arrowTypes.potionID").getValues(false);
			for (String number : configNameMap.keySet()) {
				String displayName = (String) configNameMap.get(number);
				int potionID = (int) configIDMap.get(number);
				arrowTypeMap.put(potionID, ChatColor.RED + displayName);
			}
		}
	}
	
	public void saveConfiguration() {
		//Save sortArrowMap to config.yml
		//The other HashMaps don't need to be saved, as they havn't changed since being loaded.
		for (String playerName : sortArrowMap.keySet()) {
			getConfig().set("sortArrows." + playerName, sortArrowMap.get(playerName));
		}
		saveConfig();
	}
	
	public void createMagicArrows() {
		//create Magic Arrow recipes for each arrowType in HashMap
		for (int ID : arrowTypeMap.keySet()) {
			createMagicArrow(arrowTypeMap.get(ID), ID);
		}
	}
	
	public void createMagicArrow(String displayName, int potionDatavalue) {
		//Actually, this creates a Magic Arrow recipe and display name. Change function name??
		ItemStack itemStack = createMagicArrowItemStack(displayName);
		ShapelessRecipe shapelessRecipe = createMagicArrowRecipe(itemStack, potionDatavalue);
		getServer().addRecipe(shapelessRecipe);
	}
	
	@SuppressWarnings("deprecation")
	public ShapelessRecipe createMagicArrowRecipe(ItemStack itemResult, int potionDatavalue) {
		ShapelessRecipe magicArrowRecipe = new ShapelessRecipe(itemResult);
		magicArrowRecipe.addIngredient(Material.ARROW);
		//Temporary. Fix this later!
		magicArrowRecipe.addIngredient(Material.POTION, potionDatavalue);
		return magicArrowRecipe;
	}
	
	public ItemStack createMagicArrowItemStack(String displayName) {
		//returns in Arrow ItemStack with specified DisplayName
		ItemStack itemStack = new ItemStack(Material.ARROW);
		ItemMeta itemMeta = itemStack.getItemMeta();
		itemMeta.setDisplayName(displayName);
		itemStack.setItemMeta(itemMeta);
		return itemStack;
	}
}
package com.outlook.corey_young.spellarrows;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

public class MyListener implements Listener {
	
	@EventHandler
	public void onLeftClick(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		boolean sortArrows = true;
		if (SpellArrows.sortArrowMap.containsKey(player.getName())) {
			sortArrows = SpellArrows.sortArrowMap.get(player.getName());
		}
		Inventory inventory = player.getInventory();
		if (sortArrows && inventory.first(Material.ARROW) != -1) {
			if (event.getAction() == Action.LEFT_CLICK_AIR
					|| event.getAction() == Action.LEFT_CLICK_BLOCK) {
				if (player.getItemInHand().getType() == Material.BOW) {
					//Sort arrowStacks
					ItemStack[] arrowStacks = getArrowStacks(inventory);
					int[] arrowStackIndices = getArrowStackIndices(inventory);
					int numberOfStacks = getNumberOfArrowStacks(inventory);
					ItemStack tempStack = arrowStacks[0];
					for (int i = 1; i < arrowStacks.length; i ++) {
						inventory.setItem(arrowStackIndices[i-1], arrowStacks[i]);
					}
					inventory.setItem(arrowStackIndices[numberOfStacks-1], tempStack);
					ItemStack arrowStackSelected = inventory.getItem(inventory.first(Material.ARROW));
					String arrowSelectedName = arrowStackSelected.getItemMeta().getDisplayName();
					if (isMagicArrow(arrowSelectedName)) {
						player.sendMessage(arrowSelectedName + " selected.");
					} else {
						player.sendMessage("Normal Arrow selected.");
					}
				}
			}
		}
	}

	@EventHandler
	public void onShoot(EntityShootBowEvent event) {
		if (event.getEntityType() == EntityType.PLAYER) {
			Player player = (Player) (event.getEntity());
			PlayerInventory inventory = player.getInventory();
			int itemPos = inventory.first(Material.ARROW);
			if (itemPos != -1) {
				ItemStack arrowConsumed = inventory.getItem(itemPos);
				String arrowType = arrowConsumed.getItemMeta().getDisplayName();
				Arrow arrow =  (Arrow) event.getProjectile();
				
				if(isArcherClass(player)) {
					SpellArrows.arrowMap.put(arrow.getEntityId(), arrowType);
				}
				
				if (event.getBow().getEnchantmentLevel(Enchantment.ARROW_INFINITE) > 0)
				{
					if (isMagicArrow(arrowConsumed.getItemMeta().getDisplayName()))
					{
						int amount = arrowConsumed.getAmount();
						if (amount > 1)
						{
							arrowConsumed.setAmount(amount - 1);
							inventory.setItem(itemPos, arrowConsumed);
						} else {
							inventory.setItem(itemPos, null);
						}
					}
				}
			}
		}
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler
	public void arrowHitMob(EntityDamageByEntityEvent event) {
		if (event.getDamager() instanceof Arrow) {
			Arrow arrow = (Arrow) event.getDamager();
			LivingEntity entity = (LivingEntity) event.getEntity();
			if (SpellArrows.arrowMap.containsKey(arrow.getEntityId())) {
				String arrowType = SpellArrows.arrowMap.get(arrow.getEntityId());
				if (isMagicArrow(arrowType)) {
					//event.setDamage(0);
				}
				if (arrowType != null) {
					//Negative Effects
					int ID = -1;
					for (int mapID : SpellArrows.arrowTypeMap.keySet()) {
						if (SpellArrows.arrowTypeMap.get(mapID).equals(arrowType)) {
							ID = mapID;
						}
					}
					if (ID != -1) {
						//Fix this later
						Potion potion = Potion.fromDamage(ID);
						//potion.apply(entity);
						if (!potion.getType().isInstant())
						{
							double vel = arrow.getVelocity().length() * 33.334d;
							double dis = arrow.getShooter().getLocation().distance(entity.getLocation());
							int dur = getArrowPotDuration(potion, vel, dis);
							PotionEffectType type = potion.getType().getEffectType();
							//check to see if the entity already has the effect and force the new effect if duration is longer, or if level is higher
							if (entity.hasPotionEffect(type)) {
								for(PotionEffect effect : entity.getActivePotionEffects()) {
									if (effect.getType().equals(type)) {
										if (effect.getDuration() < dur || effect.getAmplifier() < potion.getLevel() - 1) {
											entity.addPotionEffect(new PotionEffect(type, dur, potion.getLevel() - 1), true);
										}
									}
								}
							} else {
								entity.addPotionEffect(new PotionEffect(type, dur, potion.getLevel() - 1));
							}
							//Bukkit.broadcastMessage("Velocity: " + Double.toString(vel)); //debug
							//Bukkit.broadcastMessage("Distance: " + Double.toString(dis)); //debug
							//Bukkit.broadcastMessage("Duration: " + Integer.toString(dur/20)); //debug
						} else {
							//convert instant damage to bow damage
							if (potion.getType().getEffectType().getName().toString() == "HARM")
							{
								double dis = arrow.getShooter().getLocation().distance(entity.getLocation());
								double vel = arrow.getVelocity().length() * 33.334d;
								double dmg = event.getDamage() + ((potion.getLevel() * 6d * (dis/12.5d)) * (vel/100d));
								if (dmg > 120d) {
									dmg = 120d;
								}
								
								event.setDamage(dmg);
								//Bukkit.broadcastMessage("Damage: " + Double.toString(dmg)); //debug
							} else {
							potion.apply(entity);
							}
						}
						
					} else {
						//Bukkit.broadcastMessage("Error: Cannot apply potion effect!");
					}
				}
				SpellArrows.arrowMap.remove(arrow.getEntityId());
			}
		}
	}
	
	public boolean isArcherClass(Player player) {
		PlayerInventory inv = player.getInventory();
		if (inv.getHelmet().getType().equals(Material.LEATHER_HELMET) && inv.getChestplate().getType().equals(Material.LEATHER_CHESTPLATE) && inv.getLeggings().getType().equals(Material.LEATHER_LEGGINGS) && inv.getBoots().getType().equals(Material.LEATHER_BOOTS)) {
			return true;
		} else {
			return false;
		}
	}
	
	public int getArrowPotDuration(Potion potion, double velocity, double distance) {
		
		int d = 0;
		String pe = potion.getType().getEffectType().getName().toString();
		switch (pe) {
		case "REGENERATION": d = 45;
				break;
		case "SPEED": d = 180;
				break;
		case "FIRE_RESISTANCE": d = 180;
				break;
		case "NIGHT_VISION": d = 180;
				break;
		case "INVISIBILITY": d = 180;
				break;
		case "POISON": d = 45;
				break;
		case "WEAKNESS": d = 90;
				break;
		case "SLOW": d = 90;
				break;
		case "INCREASE_DAMAGE": d = 180;
				break;
		default: d = 0;
				break;
		}
		//Bukkit.broadcastMessage(pe); //debug
		if(d > 0)
		{
			if (potion.hasExtendedDuration()) {
				d = (int) Math.round(d * 2.667d);
				//Bukkit.broadcastMessage("extended"); //debug
			}
			if (potion.getLevel() > 1) {
				d = d/2;
				//Bukkit.broadcastMessage("tier 2"); //debug
			}
			d = (int) Math.round(d * (distance/75d) * (velocity/100d));
			
		}
		
		return d*20; //convert seconds to ticks	
	}
	
	@EventHandler
	public void onPlayerPickupItem(PlayerPickupItemEvent event) {
		//make this configurable later
		if (SpellArrows.arrowMap.containsKey(event.getItem().getEntityId())) {
			event.setCancelled(true);
			event.getItem().remove();
			ItemStack itemStack = event.getItem().getItemStack();
			ItemMeta itemMeta = itemStack.getItemMeta();
			itemMeta.setDisplayName(SpellArrows.arrowMap.get(event.getItem().getEntityId()));
			itemStack.setItemMeta(itemMeta);
			event.getPlayer().getInventory().addItem(itemStack);
			SpellArrows.arrowMap.remove(event.getItem().getEntityId());
		}
	}
	
	public void addEffectToEntity(Entity entity, PotionEffectType effectType, int length, int strength) {
		LivingEntity livingEntity = (LivingEntity) entity;
		livingEntity.addPotionEffect(new PotionEffect(effectType, length, strength));
	}
	
	public int getNumberOfArrowStacks(Inventory inventory) {
		int numberOfStacks = 0;
		for (int i = 0; i < inventory.getSize(); i ++) {
			if (inventory.getItem(i) != null) {
				if (inventory.getItem(i).getType() == Material.ARROW) {
					numberOfStacks ++;
				}
			}
		}
		return numberOfStacks;
	}
	
	public ItemStack[] getArrowStacks(Inventory inventory) {
		int numberOfStacks = getNumberOfArrowStacks(inventory);
		ItemStack [] arrowStacks = new ItemStack[numberOfStacks];
		int count = 0;
		while (count < numberOfStacks) {
			for (int i = 0; i < inventory.getSize(); i ++) {
				ItemStack itemStack = inventory.getItem(i);
				if (itemStack != null) {
					if (itemStack.getType() == Material.ARROW) {
						arrowStacks[count] = itemStack;
						count ++;
					}
				}
			}
		}
		return arrowStacks;
	}
	
	public int[] getArrowStackIndices(Inventory inventory) {
		int numberOfStacks = getNumberOfArrowStacks(inventory);
		int[] arrowStackIndices = new int[numberOfStacks];
		int count = 0;
		while (count < numberOfStacks) {
			for (int i = 0; i < inventory.getSize(); i ++) {
				ItemStack itemStack = inventory.getItem(i);
				if (itemStack != null) {
					if (itemStack.getType() == Material.ARROW) {
						arrowStackIndices[count] = i;
						count ++;
					}
				}
			}
		}
		return arrowStackIndices;
	}
	
	public Boolean isMagicArrow(String arrowType) {
		if (arrowType == null) {
			return false;
		}
		Boolean isMagic = false;
		for (String value : SpellArrows.arrowTypeMap.values()) {
			if (value.equals(arrowType)) {
				isMagic = true;
			}
		}
		return isMagic;
	}
}
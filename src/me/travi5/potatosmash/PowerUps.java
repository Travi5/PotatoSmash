package me.travi5.potatosmash;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

public class PowerUps implements Listener {
	PotatoSmash plugin;
	public PowerUps(PotatoSmash passedPlugin){
		this.plugin = passedPlugin;
	}

	@EventHandler
	public void onInteractEvent(PlayerInteractEvent e)
	{
		Player p = e.getPlayer();
		ItemStack item = p.getItemInHand();
		int id = item.getType().getId();
		if (p.getWorld().getName().equals(plugin.gameWorld)) {
			

			//Is player but what are they holding
			if (item.getType().equals(Material.COOKIE)){
				if (plugin.debug)Bukkit.broadcast( "Cookie ate by " + p.getName(), "minigame.mod");
				ItemStack cookie = new ItemStack(Material.COOKIE, 1);
				ItemMeta itemmeta = item.getItemMeta();
				itemmeta.setDisplayName(ChatColor.GREEN + "Speed Boost"); //!important item has  special name
				cookie.setItemMeta(itemmeta);
				p.getInventory().removeItem(new ItemStack(cookie));
				plugin.addSpeedPotion(p, 80, 4);
				p.getWorld().playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1); 
			}

			if (item.getType().equals(Material.CLAY_BALL)){
				if (plugin.debug)Bukkit.broadcast( "Clay Ball used by " + p.getName(), "minigame.mod");
				ItemStack clayBall = new ItemStack(Material.CLAY_BALL, 1);
				ItemMeta itemmeta = item.getItemMeta();
				itemmeta.setDisplayName(ChatColor.GREEN + "launcher");
				clayBall.setItemMeta(itemmeta);
				p.getInventory().removeItem(new ItemStack(clayBall));

				p.teleport(p.getLocation().add(new Vector(0, 2, 0)));
				p.setVelocity(new Vector(p.getVelocity().getX(), 1.0D, p.getVelocity().getZ()));
				p.setVelocity(p.getLocation().getDirection().multiply(plugin.launchPadForce)); //TODO change to var
				p.getWorld().playSound(p.getLocation(), Sound.ENTITY_GHAST_SHOOT, 1.0F, 2.0F);
			}


			if (item.getType().equals(Material.TNT)){
				
				ItemStack TNT = new ItemStack(Material.TNT, 1);
				ItemMeta itemmeta = item.getItemMeta();
				itemmeta.setDisplayName(ChatColor.GREEN + "BOMB");
				TNT.setItemMeta(itemmeta);
				p.getInventory().removeItem(new ItemStack(TNT));
				p.getWorld().spawnParticle(Particle.EXPLOSION_HUGE, p.getLocation().getX(), p.getLocation().getY(), p.getLocation().getZ(), 2);
				
				p.playSound(p.getLocation(),Sound.ENTITY_GENERIC_EXPLODE, 1, 1);
				for(Entity nearby : p.getNearbyEntities(10,10,10)) {
					if(nearby instanceof Player) {
						Player nearbyPlayer = (Player) nearby;
						plugin.knockBack(p,nearbyPlayer,10);
					}
				}
			}
			if (item.getType().equals(Material.STICK)){
				p.launchProjectile(Snowball.class);
			}
		}
	}

}

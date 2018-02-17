package me.travi5.potatosmash;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.weather.WeatherChangeEvent;

//TODO stop mob spawning
public class WorldProtection implements Listener {

	PotatoSmash plugin;
	public WorldProtection(PotatoSmash passedPlugin){
		this.plugin = passedPlugin;
	}

	//No Build
	@EventHandler
	public void onBlockBreak(BlockBreakEvent e) {
		Player p = e.getPlayer();
		if (p.getWorld().getName().equalsIgnoreCase(plugin.gameWorld) && !plugin.AllowAllBuild && !p.isOp())  //TODO remove OP
		{
			e.setCancelled(true);
		}
	}

	//No Block Place
	@EventHandler
	public void onBlockPlace(BlockPlaceEvent e)
	{
		Player p = e.getPlayer();
		if (p.getWorld().getName().equalsIgnoreCase(plugin.gameWorld) && !plugin.AllowAllBuild && !p.isOp()) //TODO remove OP
		{
			e.setCancelled(true);
		}
	}

	//No Drop
	@EventHandler
	public void onItemDrop (PlayerDropItemEvent e) {
		if (e.getPlayer() instanceof Player){
			Player p = e.getPlayer();
			if (p.getWorld().getName().equalsIgnoreCase(plugin.gameWorld)){
				e.setCancelled(true);
			}
		}
	}

	//Weather Lock
	@EventHandler
	public void onWeatherChange(WeatherChangeEvent e) {
		if (e.toWeatherState() && e.getWorld().getName().equalsIgnoreCase(plugin.gameWorld)) { 
			e.setCancelled(true);
		}
	}

	//Mob Spawning
	@EventHandler
	public void creatureSpawning(CreatureSpawnEvent e) {
		Entity p = e.getEntity();
		if (p.getWorld().getName().equalsIgnoreCase(plugin.gameWorld)) //TODO custom spawning
		{
			e.getEntity().remove();
			//Delete the mob
		}

	}

	//Stop Hunger
	@EventHandler
	
	public void onFoodChange(FoodLevelChangeEvent e) {
		if (e.getEntity() instanceof Player)
        {
			Player p = (Player) e.getEntity(); //TODO
			if (p.getWorld().getName().equalsIgnoreCase(plugin.gameWorld))
				{
					e.setCancelled(true);
				}
        }


	}

}

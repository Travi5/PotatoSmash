package me.travi5.potatosmash;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.Plugin;

public class PlayerJoinOrLeave implements Listener{
	PotatoSmash plugin;
	public PlayerJoinOrLeave(PotatoSmash passedPlugin){
		this.plugin = passedPlugin;
	}
	
	
	//Remove players on Quit. Kick and make sure when they return they're not in the Potate Worlds!
	@EventHandler
	public void onLeave(PlayerQuitEvent e){
		Player p = e.getPlayer();
		if (plugin.players.get(p.getName()) != null){
			plugin.removePlayer(p);
		}
	}
	
	@EventHandler
	public void onKick(PlayerKickEvent e){
		Player p = e.getPlayer();
		if (plugin.players.get(p.getName()) != null){
			plugin.removePlayer(p);
		}
	}
	
	@EventHandler
	public void onPlayerTeleportEvent(PlayerTeleportEvent e) {
		//if player uses TP signs or teleport commands to world while still banned
		Player p = e.getPlayer();
	    if (!e.getTo().getWorld().getName().equals(plugin.gameWorld) && plugin.players.get(p.getName()) != null) {
	    	if (plugin.debug)Bukkit.broadcast( "Player teleported, removed from players", "minigame.mod");
	    	plugin.removePlayer(p);
	    
	    }
	}

}

package me.travi5.potatosmash;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_12_R1.Overridden;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class CommandsMain implements CommandExecutor {

	//TODO have sign popup at mapSpawns if debug mode is on.
	
	
	PotatoSmash plugin;
	public CommandsMain(PotatoSmash passedPlugin){
		this.plugin = passedPlugin;
	}
	@Overridden
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
		Player p = (Player)sender;

		if (args.length == 0) {
			helpMessage(p);
		}

		if (args.length == 1 ){

			if (p.hasPermission("minigame.mod")){
				if (args[0].equalsIgnoreCase("mapadd")){
					p.sendMessage("Please define more.  MapAdd + [Mapname] + [Type]");
					p.sendMessage("Types advalible are: Normal, Maze");	
				}

				if (args[0].equalsIgnoreCase("lobby")){
					plugin.setTeleportLocation(p,"lobby.");
				}
				if (args[0].equalsIgnoreCase("spawn") || args[0].equalsIgnoreCase("gamespawn")){
					plugin.setTeleportLocation(p,"gameSpawn.");
				}
				if (args[0].equalsIgnoreCase("hub") || args[0].equalsIgnoreCase("return")){
					plugin.setTeleportLocation(p,"return-hub.");
				}
				if (args[0].equalsIgnoreCase("reload")){
					plugin.reloadConfig();
					p.sendMessage("Config Reloaded");
				}
				if (args[0].equalsIgnoreCase("clean")){
					plugin.cleanUpItems();
					p.sendMessage("All items have been removed from world");
				}
				if (args[0].equalsIgnoreCase("restart") || args[0].equalsIgnoreCase("reset")){
					plugin.gameReset();
					p.sendMessage("Game has been reset");
				}
				if (args[0].equalsIgnoreCase("allowbuild") || args[0].equalsIgnoreCase("build")){
					if (plugin.AllowAllBuild == true){
						plugin.AllowAllBuild = false;
						p.sendMessage("building disabled");
					}
					else{
						plugin.AllowAllBuild = true;
						p.sendMessage("building enabled");
					}
				}
				if (args[0].equalsIgnoreCase("debug")){
					if (plugin.debug == true){
						plugin.debug = false;
						p.sendMessage("debug disabled");
					}
					else{
						plugin.debug = true;
						p.sendMessage("debug enabled");
					}
				}

			}

			if (args[0].equalsIgnoreCase("exit") || args[0].equalsIgnoreCase("quit") || args[0].equalsIgnoreCase("leave")) {
				if (plugin.players.get(p.getName()) != null && p.getWorld().getName().equals(plugin.gameWorld) && plugin.gameWorld != null){
					if (plugin.players.get(p.getName()).equalsIgnoreCase("spectator")){
						plugin.removePlayer(p);
						plugin.teleportToLoc(p,"return-hub.");
						//TAGGED PLAYER LEFT THE GAME!
					}
					else {
						p.sendMessage("Only spectators are allowed to exit the game");
					}
				}
			}
		}
		//Map Code
		if (args.length > 1 ){
			if (p.hasPermission("minigame.mod")){
				
				if (args[0].equalsIgnoreCase("mapadd")){
					String mapName = args[1];
					p.sendMessage("Map name: " + mapName);
					
					if (args.length == 2) {
						p.sendMessage("Please define is Normal or Maze map");
						return false;
					}
					
					if (args[2].equalsIgnoreCase("normal") || args[2].equalsIgnoreCase("maze")){
						plugin.saveMapLoc(p,mapName,args[2]);
						p.sendMessage(ChatColor.GREEN + mapName + " " + args[2]);
					}
					else {
						p.sendMessage("Please define is Normal or Maze map");
						
					}
				}
			}

		}
		return false;

	}
	public void helpMessage(Player p){
		p.sendMessage(ChatColor.GREEN + "Potato Smash v1.0");
		p.sendMessage(ChatColor.GOLD + " Made by Travi5");
		p.sendMessage(ChatColor.WHITE + "Mapadd + [Type] - Add Map (Normal or Maze)");
		p.sendMessage(ChatColor.WHITE + "Current map " + plugin.mapGameNumber);
		//TODO
		
	}
}

package me.travi5.potatosmash;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class SignHandlers implements Listener {

	PotatoSmash plugin;
	int mapNumber = 0;

	public SignHandlers(PotatoSmash passedPlugin){
		this.plugin = passedPlugin;
	}
	//Sign Place

	@EventHandler
	public void signPlace(SignChangeEvent e) {
		if(e.getPlayer().hasPermission("minigame.mod")){
			Block bloc = e.getBlock();
			Player p = e.getPlayer();
			if(e.getLine(1).contains("[PotatoSign]")){
				e.setLine(0, "");
				e.setLine(1, ChatColor.DARK_BLUE + "Potato Smash");
				plugin.saveSignLoc(p, bloc, "teleportSign.");
			}


			//TODO Round Winner Sign
			if(e.getLine(1).contains("[PotatoWinner]")){
				e.setLine(0, "");
				e.setLine(1, ChatColor.DARK_BLUE + "[Round winner]");
				plugin.saveSignLoc(p, bloc, "winnerSign."); 
			}


			if(e.getLine(1).contains("[PotatoMap]")){
				e.setLine(0, "");
				e.setLine(1, ChatColor.GREEN + "Map Select");
				plugin.saveSignLoc(p, bloc, "mapSign."); 
			}



			if(e.getLine(1).contains("[PotatoQuit]")){
				e.setLine(0, "");
				e.setLine(1, ChatColor.RED + "[Leave]");
				p.sendMessage("Exit sign has been set!");

			}
			if(e.getLine(1).contains("[PotatoStart]")){
				e.setLine(0, "");
				e.setLine(1, ChatColor.GREEN + "[Start]");
				p.sendMessage("Start sign has been set!");
				plugin.saveSignLoc(p, bloc, "startSign.");  
			}
			if(e.getLine(1).contains("[PotatoHat]")){
				e.setLine(0, "");
				e.setLine(1, ChatColor.GREEN + "Allow Hat");
				e.setLine(2, ChatColor.GREEN + "True");
				plugin.hatsAllow = true;
				plugin.saveSignLoc(p, bloc, "hatSign.");  
			}
			if(e.getLine(1).contains("[PotatoBow]")){
				e.setLine(0, "");
				e.setLine(1, ChatColor.GREEN + "Allow Bows");
				e.setLine(2, ChatColor.GREEN + "True");
				plugin.bowsAllow = true;
				plugin.saveSignLoc(p, bloc, "bowSign.");
			}
			if(e.getLine(1).contains("[PotatoTime]")){
				e.setLine(0, "");
				e.setLine(1, ChatColor.GREEN + "Round Timer");
				e.setLine(2, plugin.burnTime/20 + " Seconds");
				plugin.saveSignLoc(p, bloc, "timeSign.");  
			}
			if(e.getLine(1).contains("[PotatoPowerup]")){
				e.setLine(0, "");
				e.setLine(1, ChatColor.GREEN + "Allow Powerups");
				e.setLine(2, ChatColor.RED + "False");
				plugin.powerupsAllow = false;
				plugin.saveSignLoc(p, bloc, "powerupsSign.");  
			}
		}
	}

	@EventHandler
	public void signInteract(PlayerInteractEvent e) {
		if(e.getAction() == Action.RIGHT_CLICK_BLOCK) {
			Block block = e.getClickedBlock();
			Player p = e.getPlayer();
			if ((block.getType() == Material.SIGN_POST) || (block.getType() == Material.WALL_SIGN) || (block.getType() == Material.SIGN))
			{
				Sign sign = (Sign)block.getState();
				if(sign.getLine(1).contains(ChatColor.DARK_BLUE + "Potato Smash")){
					if (plugin.players.size() < plugin.maxPlayers && plugin.gameOpen){
						//Check to see if maps have been made!
						if (plugin.getConfig().getString("lobby." + ".World") != null){
							if (plugin.getConfig().getString("return-hub." + ".World") != null){
								if (plugin.getConfig().getString("lobby." + ".World") != null){
									plugin.teleportToLoc(p,"lobby.");
									plugin.addPlayer(p);
								}
								else{
									p.sendMessage("Please contact server admin. No game spawn definded");
								}
							}
							else {
								p.sendMessage("Please contact server admin. No return spawn definded");
							}
						}
						else{
							p.sendMessage("Please contact server admin. No lobby spawn definded");
						}
					}
				}

				if(sign.getLine(1).contains(ChatColor.RED + "[Leave]")){
					if (plugin.getConfig().getString("return-hub." + ".World") != null) {
						plugin.teleportToLoc(p,"return-hub.");
						plugin.removePlayer(p);
					}
					else {
						p.sendMessage("No return world definded. Please contact the server admin");
					}
				}
				if(sign.getLine(1).contains(ChatColor.GREEN + "[Start]")){
					if (plugin.gameOpen == true) {
						if (plugin.getConfig().getString("maps." + mapNumber + ".World") != null) {
							plugin.gameStart();
						}
						else {
							p.sendMessage("No maps have been defined or world is missing. please contact server admin");
						}
					}
					else {
						p.sendMessage("Game has already Started");
					}


				}
				if(sign.getLine(1).contains(ChatColor.GREEN + "Allow Hat")){
					if (plugin.hatsAllow == false){
						plugin.hatsAllow = true;
						plugin.autoSignUpdater("hatSign.",ChatColor.GREEN + "Allow", null);
					}
					else{
						plugin.hatsAllow = false;
						plugin.autoSignUpdater("hatSign.",ChatColor.RED + "False", null);
					}
				}
				if(sign.getLine(1).contains(ChatColor.GREEN + "Allow Bow")){
					if (plugin.bowsAllow == false){
						plugin.bowsAllow = true;
						plugin.autoSignUpdater("bowSign.",ChatColor.GREEN + "Allow", null);
					}
					else{
						plugin.bowsAllow = false;
						plugin.autoSignUpdater("bowSign.",ChatColor.RED + "False", null);
					}
				}
				if(sign.getLine(1).contains(ChatColor.GREEN + "Allow Powerups")){
					if (plugin.powerupsAllow == false){
						plugin.powerupsAllow = true;
						plugin.autoSignUpdater("powerupsSign.",ChatColor.GREEN + "Allow", null);
					}
					else{
						plugin.powerupsAllow = false;
						plugin.autoSignUpdater("powerupsSign.",ChatColor.RED + "False", null);
					}
				}
				if(sign.getLine(1).contains(ChatColor.GREEN + "Round Timer")){
					if (plugin.burnTime < plugin.maxTime){
						plugin.burnTime = plugin.burnTime + 100;
						plugin.autoSignUpdater("timeSign.", plugin.burnTime/20 + " Seconds",null);
					}
					if (plugin.burnTime > plugin.maxTime){ //Removed >=
						plugin.burnTime = plugin.minTime;
						plugin.autoSignUpdater("timeSign.", plugin.burnTime/20 + " Seconds",null);
					}
				}

				//TODO

				if(sign.getLine(1).contains(ChatColor.GREEN + "Map Select")){
					//Map Select
					//Cycle through all maps available
					//if no map found after latest one return to 0. if map is null have sign say NULL in red

					int i ;
					for (i = mapNumber; i < plugin.maxMaps; i++){
						p.sendMessage("Searching for map" + Integer.toString(i));
						if (plugin.getConfig().get("maps." + i ) != null && i != mapNumber ) {
							//Map found
							String mapName = plugin.getConfig().getString("maps." + i + ".Name");
							plugin.autoSignUpdater("mapSign.", mapName ,Integer.toString(i));
							mapNumber = i;
							plugin.mapGameNumber = i;
							break;
						}
					}//End for loop

					if (i == plugin.maxMaps) {
						mapNumber = 0;
						i = 0;

						if (plugin.getConfig().get("maps." + i ) != null) {
							String mapName = plugin.getConfig().getString("maps." + i + ".Name");
							plugin.autoSignUpdater("mapSign.", mapName ,Integer.toString(i));
							p.sendMessage("Map Found!");
							plugin.mapGameNumber = i;

						}
						else {
							plugin.autoSignUpdater("mapSign.", ChatColor.RED + "No Map Found" ,Integer.toString(i));
							plugin.mapGameNumber = i;
						}
					}
				}
			}
		}
	}

}

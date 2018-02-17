package me.travi5.potatosmash;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftFirework;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;


public class PotatoSmash extends JavaPlugin implements Listener 
{
	//players = player.getName(),player status(player, tagged or spectator)
	Map<String, String> players = new HashMap<String, String>();
	ArrayList<String> usedArrow = new ArrayList<String>();
	String taggedPlayer = null;

	public int maxPlayers = 20; //default 20
	public int minPlayers = 2; //default 2
	public int burnTime = 200;
	public int maxTime = 1200;
	public int minTime = 200;
	public int powerUpSpread = 25;
	public int launchPadForce = 5;
	public int maxMaps = 50;
	int randomTime = new Random().nextInt(500);
	public int mapGameNumber = 0;

	public boolean debug = false;
	public boolean gameOpen = true;
	public boolean hatsAllow = true;
	public boolean bowsAllow = true;
	public boolean powerupsAllow = false;
	public boolean AllowAllBuild = false;

	public String gameWorld;
	//public String currentMap = "gameSpawn.";
	
	//TODO fix timer
	
	public PotatoSmash() {
		gameWorld = getConfig().getString("lobby." + ".World");
	}

	public void onEnable(){
		getServer().getPluginManager().registerEvents(this, this);

		//register other classes
		this.getCommand("potatosmash").setExecutor(new CommandsMain (this));
		this.getCommand("ps").setExecutor(new CommandsMain (this));
		this.getServer().getPluginManager().registerEvents(new SignHandlers(this), this);
		this.getServer().getPluginManager().registerEvents(new WorldProtection(this), this);
		this.getServer().getPluginManager().registerEvents(new PlayerJoinOrLeave(this), this);
		this.getServer().getPluginManager().registerEvents(new CombatListener(this), this);
		this.getServer().getPluginManager().registerEvents(new PowerUps(this), this);

		getConfig().options().copyDefaults(true);
		saveDefaultConfig();
		gameReset();
	}

	public void onDisable()
	{
		teleportGamePlayers("return-hub.");
		autoSignUpdater("teleportSign.","0/" + maxPlayers + " Players",ChatColor.RED + "RESETTING");//clear the last line
		//when sever is reloaded remove all players from the world
	}

	public void gameStart(){
		if (getConfig().getString("maps." + mapGameNumber + ".World") != null) {
			if (players.size() >= minPlayers && gameOpen == true){
				gameOpen = false;
				autoSignUpdater("teleportSign.",null,ChatColor.RED + "Game Started");		
				teleportGamePlayersMap(); //teleport all players from lobby to game arena
				if (debug)Bukkit.broadcast( "teleported players!", "minigame.mod");
				Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable(){
					public void run()
					{	            	
						//assign potato to random player and start timer
						if (debug)Bukkit.broadcast( "Delay finished", "minigame.mod");

						assignPotate();
						if (debug)Bukkit.broadcast( "assign potate!", "minigame.mod");

						randomDrops();

						if (bowsAllow)giveAllBowArrow();
					}
				}, 100);
			}
		}
	}
	//Assign a Potato to one of the players
	public void assignPotate(){	
		ArrayList<String> remainingPlayers = new ArrayList<String>();
		remainingPlayers.clear();

		for(Player p : Bukkit.getOnlinePlayers()){
			if (players.get(p.getName()) != null && players.get(p.getName()).contains("player")){
				//If the player is still a player add them to remainingPlayers array
				remainingPlayers.add(p.getName());
				if (debug)Bukkit.broadcast(p.getName() + " found as player and added to remaining players", "minigame.mod");	
			}
		}

		if (debug)Bukkit.broadcast(players.size() + " players left remaining", "minigame.mod");

		if (remainingPlayers.size() == 1){
			String lastplayer = remainingPlayers.get(0);
			Player winner = Bukkit.getPlayer(lastplayer);
			endRound();

			//WINNER
			winner.sendTitle(ChatColor.GREEN + "Congratulation!", ChatColor.GOLD + "You have won!");
			releaseFireworksPlayer(winner);
			messageAllPlayers(ChatColor.GOLD + winner.getName() + " has won! Congratulations!!",winner.getName());
			return;
		}

		if (remainingPlayers.size() == 0){
			if (debug)Bukkit.broadcast( "No remaining players.", "minigame.mod");

			//try and fetch tagged player
			Player tagged = Bukkit.getPlayer(taggedPlayer);
			if (tagged != null){
				endRound();
				tagged.sendTitle(ChatColor.GREEN + "Congratulation!", ChatColor.RED + "But did you really win?");
				releaseFireworksPlayer(tagged);
				messageAllPlayers(ChatColor.GOLD + tagged.getName() + " has somehow won despite blowing up! Congratulations I guess..",null);
				return;
			}
			else {
				if (debug)Bukkit.broadcast( "Woah. error in the potato game. No one wins!", "minigame.mod");
				teleportGamePlayers("return-hub.");
				gameReset();
			}
		}
		if (remainingPlayers.size() > 1){
			//Players are still in the game keep playing!
			String randomplayer = remainingPlayers.get(new Random().nextInt(remainingPlayers.size()));
			Player r = Bukkit.getPlayer(randomplayer);
			playerTagged(r);
			taggedPlayer = r.getName();

			if (debug)Bukkit.broadcast( r.getName() + " has been picked!", "minigame.mod");

			messageAllPlayers(ChatColor.GOLD + r.getName() + " has received the potato",r.getName());
			if (debug)Bukkit.broadcast( r.getName() + " has been picked!", "minigame.mod");
			playSoundsPlayers(Sound.ENTITY_ENDERDRAGON_GROWL, 1, 1);

			taggedPlayer = r.getName();
			countDownTimmer(r);
		}	
	}

	public void countDownTimmer(Player tagged){

		countDownNoteBlock(tagged, burnTime - 20);
		countDownNoteBlock(tagged, burnTime - 40);
		countDownNoteBlock(tagged, burnTime - 60);

		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable(){
			public void run()
			{	
				Player tagged = Bukkit.getPlayer(taggedPlayer);
				if (tagged != null &&  players.get(tagged.getName()) != null && players.get(tagged.getName()).contains("tagged")){
					playerBurnt(tagged);
					if (debug) Bukkit.broadcast( tagged.getName() + " was burnt", "minigame.mod");
				}
				else{
					assignPotate();
					if (debug)Bukkit.broadcast( "NO PLAYER FOUND! KEEP CHECKING! DONT FREAK OUT!", "minigame.mod");
				}
			}
		}, burnTime);
	}

	public void addPlayer (Player p){
		players.put(p.getName(), "player");
		String count = String.valueOf(players.size());
		p.getInventory().clear();
		autoSignUpdater("teleportSign.", count + "/" + maxPlayers + " Players", null);
	}

	public void removePlayer(Player p){
		if (players.get(p.getName()) != null){
			players.remove(p.getName());
			String count = String.valueOf(players.size());
			autoSignUpdater("teleportSign.", count + "/" + maxPlayers + " Players", null);
		}
	}

	public void playerBurnt (Player p){
		playerSpec(p); //if player is there it'll drop 2 arrows! wot?
		if (bowsAllow){
			if (usedArrow.contains(p.getName())){
				//player has used arror
			}
			else{
				int x = p.getLocation().getBlockX();
				int y = p.getLocation().getBlockY();
				int z = p.getLocation().getBlockZ();
				World w = getServer().getWorld(gameWorld);//get the world
				Location loc = new Location ((World) w, x, y + 2, z);
				w.dropItem(loc,new ItemStack(Material.ARROW,1));
				if (debug) Bukkit.broadcast( p.getName() + " dropped an arrow as he didnt use it", "minigame.mod");
			}
		}
		//play sound for all players
		p.getWorld().playSound(p.getLocation(), Sound.ENTITY_FIREWORK_BLAST, 1, 1); 
		p.getWorld().spawnParticle(Particle.EXPLOSION_HUGE, p.getLocation(), 0);
		messageAllPlayers(ChatColor.GOLD + p.getName() + " has exploded! better luck next time",p.getName());

		assignPotate();

	}

	@SuppressWarnings("deprecation")
	public void playerTagged(Player p){

		taggedPlayer = p.getName();
		players.put(p.getName(), "tagged");
		p.getInventory().clear();
		p.sendMessage(ChatColor.RED + "You have the potato!");

		ItemStack potato = new ItemStack(Material.POTATO_ITEM, 1);
		ItemMeta itemmeta = potato.getItemMeta();
		itemmeta.setDisplayName(ChatColor.RED + "HOT POTATO");
		potato.setItemMeta(itemmeta);
		p.getInventory().setItemInHand(new ItemStack(potato));

		if (debug) Bukkit.broadcast(p.getName()+ " was tagged!", "minigame.mod");
		if (debug && players.get(p.getName()).equals("tagged")) Bukkit.broadcast( "players tagged " + p.getName() + " True", "minigame.mod");
		if (debug && !players.get(p.getName()).equals("tagged")) Bukkit.broadcast(ChatColor.RED + "players tagged " + p.getName() + " False ERROR!!", "minigame.mod");

		if (hatsAllow){
			ItemStack helmet = new ItemStack(Material.CONCRETE_POWDER, 1, (short) 4);
			p.getInventory().setHelmet(helmet);
		}

	}

	public void playerUntagged(Player p){
		players.put(p.getName(), "player");
		p.getInventory().clear();
		p.getInventory().setHelmet(new ItemStack(Material.AIR,1)); //clear hat
		if (debug) Bukkit.broadcast( usedArrow.size() + " players found in array", "minigame.mod");
		if (bowsAllow){

			if (usedArrow.contains(p.getName())){
				giveBow(p);
				if (debug) Bukkit.broadcast( usedArrow.size() + " players found in array", "minigame.mod");
				if (debug) Bukkit.broadcast( " given just bow", "minigame.mod");
				if (debug) Bukkit.broadcast( p.getName() + " given just bow", "minigame.mod");
			}
			else {
				p.getInventory().setItem(0, new ItemStack(Material.ARROW, 1));
				giveBow(p);
				if (debug) Bukkit.broadcast( p.getName() + " given bow and 1 arror", "minigame.mod");

			}
		}
	}

	public void endRound (){
		//get winner and update sign
		//autoSignUpdater()
		//delay 5 sec and tp players
		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable(){
			public void run()
			{
				//end and update sign to open
				autoSignUpdater("teleportSign.",null,ChatColor.RED + "Finishing up");//clear the last line
				gameReset();
			}
		}, 150);
	}

	public void teleportToLoc (Player p, String type){
		World  w = Bukkit.getServer().getWorld(getConfig().getString(type + ".World"));
		double x = getConfig().getDouble(type + ".X");
		double y = getConfig().getDouble(type + ".Y");
		double z = getConfig().getDouble(type + ".Z");
		float pitch = getConfig().getInt(type + ".Pitch");
		float yaw = getConfig().getInt(type + ".Yaw");
		final Location loc = new Location ((World) w, x, y, z, yaw, pitch).add(new Vector(0.5, 0, 0.5));
		p.teleport(loc);
	}

	public void teleportToMap (Player p){
		World  w = Bukkit.getServer().getWorld(getConfig().getString("maps." + mapGameNumber + ".World"));
		double x = getConfig().getDouble("maps." + mapGameNumber + ".X");
		double y = getConfig().getDouble("maps." + mapGameNumber + ".Y");
		double z = getConfig().getDouble("maps." + mapGameNumber + ".Z");
		float pitch = getConfig().getInt("maps." + mapGameNumber + ".Pitch");
		float yaw = getConfig().getInt("maps." + mapGameNumber + ".Yaw");
		final Location loc = new Location ((World) w, x, y, z, yaw, pitch).add(new Vector(0.5, 0, 0.5));
		p.teleport(loc);
		Bukkit.broadcast( "Teleported to map " + mapGameNumber, "minigame.mod");
	}
	//teleport all players
	public void teleportGamePlayers (String type){
		for(Player p : Bukkit.getOnlinePlayers()){
			if (players.get(p.getName()) != null){
				//teleport to pos
				teleportToLoc(p,type);
			}
		}
	}
	public void teleportGamePlayersMap (){
		for(Player p : Bukkit.getOnlinePlayers()){
			if (players.get(p.getName()) != null){
				//teleport to pos
				teleportToMap(p);
			}
		}
	}

	public void countDownNoteBlock(Player tagged, int time){
		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable(){
			public void run()
			{
				playSoundsPlayers(Sound.BLOCK_NOTE_BELL, 1, 1);
			}
		}, time);
	}

	public void messageAllPlayers (String message, String exceptPlayer){
		for (Player p: Bukkit.getOnlinePlayers()){
			if (players.get(p.getName()) != null && !p.getName().equals(exceptPlayer)){
				if (message.contains(" " + p.getName() + " ")){
					message = message.replace(" " + p.getName() + " ", ChatColor.WHITE  + " " + p.getName() + " " + ChatColor.GOLD);
				}
				p.sendMessage(message);
			}
		}
	}

	public void knockBack(Player attacker, Player victim, Integer i){
		//knock back effect
		Location loc1 = attacker.getLocation();//Get the location from the source player
		Location loc2 = victim.getLocation();//Get the location from the target player

		double deltaX = loc2.getX() - loc1.getX();//Get X Delta
		double deltaZ = loc2.getZ() - loc1.getZ();//Get Z delta

		Vector vec = new Vector(deltaX, 0, deltaZ);//Create new vector
		vec.normalize();//Normalize it so we don't shoot the player into oblivion
		victim.setVelocity(vec.multiply(i / (Math.sqrt(Math.pow(deltaX, 2.0) + Math.pow(deltaZ, 2.0)))));
	}
	public void giveAllBowArrow(){
		for (Player p : Bukkit.getOnlinePlayers()){
			if (players.get(p.getName()) != null && players.get(p.getName()).contains("player")){
				p.getInventory().setItem(0, new ItemStack(Material.ARROW, 1));
				giveBow(p);

			}
		}
	}
	@EventHandler()
	public void onPlayerShootArrow(EntityShootBowEvent e){
		if(e.getEntity() instanceof Player && e.getEntity().getWorld().getName().equals(gameWorld)){
			Player p = (Player) e.getEntity();
			if (usedArrow.contains(p.getName())){
				//Don't Count them
				if (debug) Bukkit.broadcast( usedArrow.size() + " players found in array", "minigame.mod");
				return;
			}
			else{
				//Count them
				usedArrow.add(p.getName());
				if (debug) Bukkit.broadcast( usedArrow.size() + " players found in array", "minigame.mod");
			}
		}

	}
	public void giveBow(Player p){

		ItemStack item = new ItemStack(Material.BOW, 1);
		ItemMeta itemmeta = item.getItemMeta();
		itemmeta.setDisplayName("Potato Bow");
		ArrayList<String> lore = new ArrayList<String>();
		String[] string_message = "Slowness on other players!\nCloserange knockback on the\nHot Potato!".split("\n");
		int i = 0;
		for (String output : string_message)
		{
			lore.add(i, output);
			i ++;
		}
		itemmeta.setLore(lore);
		item.setItemMeta(itemmeta);

		p.getInventory().setItem(1,new ItemStack(item));
	}

	public void playerSpec(Player p){
		//set map value to "spectator"
		players.put(p.getName(),"spectator");
		p.getInventory().clear();
		p.setGameMode(GameMode.SPECTATOR);
		p.sendMessage("Use " + ChatColor.GOLD + "/ps exit " + ChatColor.WHITE + "to leave at any time");
	}



	public void playSoundsPlayers(Sound sound, Integer i, Integer i2){
		for (Player p: Bukkit.getOnlinePlayers()){
			if (players.get(p.getName()) != null){
				p.playSound(p.getLocation(),sound, i, i2);
			}
		}
	}

	public void gameReset (){
		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable(){
			public void run()
			{	
				teleportGamePlayers("return-hub.");

				//UPDATE ALL SIGNS
				autoSignUpdater("teleportSign.","0/" + maxPlayers + " Players"," "); //clear the last line

				if (hatsAllow){autoSignUpdater("hatSign.",ChatColor.GREEN + "Allow", null);}
				else{autoSignUpdater("hatSign.",ChatColor.RED + "FALSE", null);	}

				if (powerupsAllow){autoSignUpdater("powerupsSign.",ChatColor.GREEN + "Allow", null);}
				else{autoSignUpdater("powerupsSign.",ChatColor.RED + "FALSE", null);	}

				if (bowsAllow){autoSignUpdater("bowSign.",ChatColor.GREEN + "Allow", null);}
				else{autoSignUpdater("bowSign.",ChatColor.RED + "FALSE", null);	}

				if (hatsAllow){autoSignUpdater("hatSign.",ChatColor.GREEN + "Allow", null);}
				else{autoSignUpdater("hatSign.",ChatColor.RED + "FALSE", null);	}

				autoSignUpdater("timeSign.", burnTime/20 + " Seconds",null);
				//TODO add maps sign
				//Clear map and data
				cleanUpItems();
				players.clear();
				usedArrow.clear();
				taggedPlayer = null;
				gameOpen = true;

			} 
		}, 25);
	}

	public void cleanUpItems(){
		if (gameWorld != null) {
			World world = getServer().getWorld(gameWorld);//get the world
			List<Entity> entList = world.getEntities();//get all entities in the world
			for(Entity item : entList){//loop through the list
				if (item instanceof Item){//make sure we aren't deleting mobs/players
					item.remove();//remove it
				}
			}
			if (debug)Bukkit.broadcast( "World is now clean", "minigame.mod");

		}
	}

	public void releaseFireworksPlayer(Player p) {
		for (int i = 0; i < 5; i++) {
			Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable(){
				public void run()
				{
					Firework firework = p.getWorld().spawn(p.getLocation().add(0,2,0), Firework.class);
					FireworkMeta data = (FireworkMeta) firework.getFireworkMeta();
					data.addEffects(FireworkEffect.builder().withColor(Color.PURPLE).with(Type.BALL_LARGE).build());
					data.setPower(1);
					firework.setFireworkMeta(data);
				}
			}, (20 * (i + 1)));

		}
	}

	public void hitFireworksPlayer(Player p) {

		Firework firework = p.getWorld().spawn(p.getLocation().add(0,2,0), Firework.class);
		FireworkMeta data = (FireworkMeta) firework.getFireworkMeta();

		data.addEffects(FireworkEffect.builder().withColor(Color.YELLOW).with(Type.BALL).trail(false).build());
		data.setPower(0);
		firework.setFireworkMeta(data);
		firework.isSilent();
		firework.detonate();
		((CraftFirework)firework).getHandle().expectedLifespan = 1;
	}

	public void addSlownessPotion(Player p, Integer time, Integer x) {
		if (p.getWorld().getName().equalsIgnoreCase(gameWorld) && !p.hasPotionEffect(PotionEffectType.SLOW)) {
			p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, time, x));
		}
	}
	public void addSpeedPotion(Player p, Integer time, Integer x) {
		if (p.getWorld().getName().equalsIgnoreCase(gameWorld) && !p.hasPotionEffect(PotionEffectType.SPEED)) {
			p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, time, x));
		}
	}

	//Stop player removing helmet
	@EventHandler
	public void onInventoryClick (InventoryClickEvent e) {

		Player p = (Player) e.getWhoClicked();

		if (p.getWorld().getName().equalsIgnoreCase(gameWorld) && e.getSlotType() == InventoryType.SlotType.ARMOR) {
			e.setCancelled(true);
		}
	}

	public void randomDrops(){
		if (!gameOpen && powerupsAllow){
			//Find the spawn of the map
			World  w = Bukkit.getServer().getWorld(getConfig().getString("Maps." + mapGameNumber + ".World"));
			int x = getConfig().getInt("maps." + mapGameNumber + ".X");
			int y = getConfig().getInt("maps." + mapGameNumber + ".Y");
			int z = getConfig().getInt("maps." + mapGameNumber + ".Z");
			final Location loc = new Location ((World) w, x, y, z);

			//Randomly drop items over the area

			Random rand = new Random();
			loc.setX(rand.nextInt((x + powerUpSpread) + 1 -(x - powerUpSpread)) +(x-powerUpSpread)); 
			loc.setY(y + 20);
			loc.setZ(rand.nextInt((z + powerUpSpread) + 1 -(z - powerUpSpread)) +(z-powerUpSpread)); 

			//Delay the drop
			//TODO add randomizer between items
			Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable(){
				public void run() 
				{
					if (!gameOpen){
						Random rand = new Random();
						int howManyDrops = 3;
						int  n = rand.nextInt(howManyDrops) + 1;
						String type = "Powerup";

						if (n == 0) {
							ItemStack item = new ItemStack(Material.COOKIE, 1);
							ItemMeta itemmeta = item.getItemMeta();
							itemmeta.setDisplayName(ChatColor.GREEN + "Speed Boost");
							ArrayList<String> lore = new ArrayList<String>();
							lore.add("Speed Boost for a little while");
							itemmeta.setLore(lore);
							item.setItemMeta(itemmeta);
							w.dropItemNaturally(loc,item);
							type = "Speed Boost";
						}
						if (n == 1) {
							ItemStack item = new ItemStack(Material.CLAY_BALL, 1);
							ItemMeta itemmeta = item.getItemMeta();
							itemmeta.setDisplayName(ChatColor.GREEN + "launcher");
							ArrayList<String> lore = new ArrayList<String>();
							lore.add("Launch yourself!");
							itemmeta.setLore(lore);
							item.setItemMeta(itemmeta);
							w.dropItemNaturally(loc,item);
							type = "Launcher";
						}
						if (n == 2) {
							ItemStack item = new ItemStack(Material.TNT, 1);
							ItemMeta itemmeta = item.getItemMeta();
							itemmeta.setDisplayName(ChatColor.GREEN + "BOMB");
							ArrayList<String> lore = new ArrayList<String>();
							lore.add("Allahu Akbar!");
							itemmeta.setLore(lore);
							item.setItemMeta(itemmeta);
							w.dropItemNaturally(loc,item);
							type = "Bomb";

						}

						if (debug)Bukkit.broadcast( type + " dropped at "+ loc.getBlockX()+ loc.getBlockY() + loc.getBlockY(), "minigame.mod");
						randomDrops(); //repeat
					}
				}
			}, randomTime); //Delay time
		}
	}

	//SIGN CODE
	public void saveSignLoc(Player p, Block bloc, String type){
		//Save Sign location so it can be updated
		getConfig().set(type + ".World", bloc.getLocation().getWorld().getName());
		getConfig().set(type + ".X", bloc.getLocation().getBlockX());
		getConfig().set(type + ".Y", bloc.getLocation().getBlockY());
		getConfig().set(type + ".Z", bloc.getLocation().getBlockZ());
		p.sendMessage(type.replace(".", "") + " sign has been set!");
		saveConfig();
		reloadConfig();
	}

	public void autoSignUpdater(String type, String message1, String message2) {	
		if (getConfig().get(type + ".World") != null) {
			World  w = Bukkit.getServer().getWorld(getConfig().getString((type + ".World")));
			int x = getConfig().getInt(type + ".X");
			int y = getConfig().getInt(type + ".Y");
			int z = getConfig().getInt(type + ".Z");
			Block bloc = w.getBlockAt(x,y,z);
			//make sure a sign is that that block location
			if (bloc.getType() == Material.SIGN_POST || bloc.getType() == Material.WALL_SIGN || (bloc.getType() == Material.SIGN)) {
				Sign sign = (Sign)bloc.getState();
				if (message1 != null){
					sign.setLine(2, message1);
				}
				if (message2 != null){
					sign.setLine(3, message2);
				}
				sign.update(true);
			}
		}
	}

	//MAP CODE
	public void saveMapLoc(Player p, String mapName, String type){
		p.sendMessage("Trying to save map Loc");
		int i;
		//if map number isn't taken
		for (i = 0; i < maxMaps; i++){
			p.sendMessage("For loop " + Integer.toString(i));
			if (getConfig().getString("maps." + i ) == null) {
				p.sendMessage("map number is null");

				//Save map location so it can be updated
				getConfig().set("maps." + i + ".World", p.getLocation().getWorld().getName());
				getConfig().set("maps." + i + ".Name", mapName);
				getConfig().set("maps." + i + ".Type", type);
				getConfig().set("maps." + i + ".X", p.getLocation().getX());
				getConfig().set("maps." + i + ".Y", p.getLocation().getY());
				getConfig().set("maps." + i + ".Z", p.getLocation().getZ());
				getConfig().set("maps." + i + ".Pitch", p.getLocation().getPitch());
				getConfig().set("maps." + i + ".Yaw", p.getLocation().getYaw());
				saveConfig();
				reloadConfig();
				p.sendMessage("Map " + mapName + ". #" + Integer.toString(i) );
				break;
			}

		}
	}

	public void setTeleportLocation(Player p, String type){
		reloadConfig();
		getConfig().set(type + ".World", p.getWorld().getName());
		getConfig().set(type + ".X", p.getLocation().getBlockX());
		getConfig().set(type + ".Y", p.getLocation().getBlockY());
		getConfig().set(type + ".Z", p.getLocation().getBlockZ());
		getConfig().set(type + ".Pitch", p.getLocation().getPitch());
		getConfig().set(type + ".Yaw", p.getLocation().getYaw());
		saveConfig();
		reloadConfig();
		p.sendMessage(ChatColor.GOLD + "The " + type.replace(".", "") + " has been set");
	}	

	@EventHandler
	public void onJoin(PlayerJoinEvent  e){
		Player p = e.getPlayer();
		if (e.getPlayer().getWorld().getName().equals(getConfig().getString("lobby." + ".World"))){
			if (debug)Bukkit.broadcast( "Player " + p.getName() + "  joined in gameworld", "minigame.mod");
			Bukkit.getServer().getScheduler().scheduleSyncDelayedTask((Plugin) this, new Runnable(){
				public void run() 
				{
					if (debug)Bukkit.broadcast( "teleporting " + p.getName() + " back to the return-hub", "minigame.mod");
					teleportToLoc(p,"return-hub.");
				}
			}, 2L);
		}
	}

}

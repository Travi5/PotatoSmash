package me.travi5.potatosmash;

import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

public class CombatListener implements Listener{
	PotatoSmash plugin;
	public CombatListener(PotatoSmash passedPlugin){
		this.plugin = passedPlugin;
	}

	//Remove Arrow when arrow has HIT something. This stops rebounds and bounce of the arrow too!
	@EventHandler
	public void projectileHit(ProjectileHitEvent e){
		if (e.getEntity().getWorld().getName().equals(plugin.gameWorld)){
			if (e.getEntity().getType() == EntityType.ARROW){
				e.getEntity().remove();
			}
		}
	}
	
	//Stop the player from taking damage while in the gameWorld
	@EventHandler
	public void damagePlayer(EntityDamageEvent e){
		if(e.getEntity() instanceof Player){
			//Player p = ((Player) e).getPlayer();
			if (e.getEntity().getWorld().getName().equals(plugin.gameWorld) && plugin.gameWorld != null){
				e.setCancelled(true);
			}
		}
	}

	//If player was hit by arrow
	@EventHandler
	  public void onDamageByPlayer(EntityDamageByEntityEvent e){
		//shot by an arrow
		if(e.getCause() == DamageCause.PROJECTILE) {
			if (e.getDamager().getType() == EntityType.ARROW){
				Arrow a = (Arrow) e.getDamager();
			    if(a.getShooter() instanceof Player && e.getEntity() instanceof Player) {
			        Player shooter = (Player) a.getShooter();
			        Player victim = (Player) e.getEntity();
			        if (shooter.getWorld().getName().equals(plugin.gameWorld) && victim.getWorld().getName().equals(plugin.gameWorld)){
			        	if (shooter != null && victim != null){
				        	//shooter.sendMessage("You shot " + victim.getName());      	
				        	victim.sendMessage(ChatColor.GOLD + shooter.getName() + " shot you");
				        }
				        if (plugin.players.get(victim.getName()).contains("player")){
				        	plugin.addSlownessPotion(victim,40,2);//Make victim walk slow for 2 secs
				        	victim.getWorld().playSound(victim.getLocation(), Sound.BLOCK_ANVIL_HIT,(float) 0.5, 1);
				        }
				        if (plugin.players.get(victim.getName()).contains("tagged")){
				        	plugin.knockBack(shooter,victim,15);
				        	victim.getWorld().playSound(victim.getLocation(), Sound.BLOCK_ANVIL_HIT,(float) 0.5, 1);
				        	
				        }
				    }
			    }
			}
		}
		//if the attacker is "tagged" he's got the potato and passed it to victim
		if (((e.getEntity() instanceof Player)) && ((e.getDamager() instanceof Player))){
	    	Player victim = (Player)e.getEntity();
    		Player attacker = (Player)e.getDamager();
    		//successful potato pass
    		if (plugin.players.get(attacker.getName()) != null
	    			&& plugin.players.get(attacker.getName()).equals("tagged")
	    			&& plugin.players.get(victim.getName()) != null
	    			&& plugin.players.get(victim.getName()).equals("player")
	    			&& attacker.getWorld().getName().equals(plugin.gameWorld)
	    			&& !plugin.gameOpen
	    			&& plugin.gameWorld != null 
	    			&& e.getCause() != DamageCause.PROJECTILE){
    			plugin.playerTagged(victim);
    			plugin.playerUntagged(attacker);
    			plugin.knockBack(attacker,victim,5);
	    		victim.getWorld().playSound(victim.getLocation(), Sound.ENTITY_PLAYER_ATTACK_CRIT, 1, 1);
	    		plugin.messageAllPlayers(ChatColor.GOLD + attacker.getName() + " passed the potato onto " + victim.getName(),attacker.getName());
	    		plugin.hitFireworksPlayer(victim);
	    	}
		}
	    
	}
	

}

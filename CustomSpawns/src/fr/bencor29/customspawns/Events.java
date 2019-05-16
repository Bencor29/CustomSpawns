package fr.bencor29.customspawns;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;

public class Events implements Listener {
	
	CustomSpawns plugin;
	GroupManager gm;
	
	public Events(CustomSpawns plugin) {
		this.plugin = plugin;
		gm = new GroupManager(plugin);
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onDeath(PlayerRespawnEvent event) {
		Group group = gm.getGroupByWorld(event.getPlayer().getWorld());
		if(group == null) return;
		if(group.getSpawn() == null) return;
		if(!group.containWorld(event.getPlayer().getWorld().getName())) return;
		event.setRespawnLocation(group.getSpawn());
	}

}

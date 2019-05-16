package fr.bencor29.customspawns;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class Commands implements CommandExecutor {

	CustomSpawns plugin;
	GroupManager gm;
	
	public Commands(CustomSpawns plugin) {
		this.plugin = plugin;
		gm = new GroupManager(plugin);
	}
	
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(!(sender instanceof Player)) {
			sender.sendMessage("Vous devez etre un joueur !");
			return true;
		}
		final Player player = (Player) sender;
		if(label.equalsIgnoreCase("spawn")) {
			spawnCommand(player);
		} else if(label.equalsIgnoreCase("lobby")) {
			lobbyCommand(player);
		} else if(label.equalsIgnoreCase("setlobby")) {
			setlobbyCommand(player);
		} else if(label.equalsIgnoreCase("csreload")) {
			reloadCommand(player);
		}
		return true;
	}

	public void reloadCommand(Player player) {
		plugin.onReload();
		player.sendMessage("§aPlugin rechargé !");
	}


	public void setlobbyCommand(Player player) {
		Location ploc = player.getLocation();
		World 	world = ploc.getWorld();
		double 	x = ploc.getX(),
				y = ploc.getY(),
				z = ploc.getZ();
		float 	yaw = ploc.getYaw(),
				pitch = ploc.getPitch();
		Location newlobby = new Location(world, x, y, z, yaw, pitch);
		plugin.lobby = newlobby;
		player.sendMessage("§aLe nouveau lobby a été définit !");
		saveLobby(world.getName(), x, y, z, yaw, pitch);
	}


	public void saveLobby(String world, double x, double y, double z, float yaw, float pitch) {
		plugin.genconf.set("lobby.world", world);
		plugin.genconf.set("lobby.x", x);
		plugin.genconf.set("lobby.y", y);
		plugin.genconf.set("lobby.z", z);
		plugin.genconf.set("lobby.yaw", yaw);
		plugin.genconf.set("lobby.pitch", pitch);
		File file = plugin.getConfigFile(0);
		FileConfiguration configs = plugin.getFileConfig(0);
		try {
			configs.save(file);
		} catch (IOException e) {
			plugin.getLogger().log(Level.WARNING, "Impossible de sauvegarder les configs !");
		}
	}


	public void lobbyCommand(final Player player) {
		final Location lobby = plugin.lobby;
		if(lobby == null) {
			player.sendMessage("§cErreur: Il n'y a pas de lobby définit !");
			return;
		}
		int secs = plugin.secs;
		final Location playerloc = player.getLocation();
		if(player.hasPermission("customspawns.notimer")) {
			player.teleport(lobby);
			player.sendMessage("§7Téléportation terminé !");
			return;
		}
		player.sendMessage("§7Téléportation dans 5 secondes, ne bougez pas !");
		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			public void run() {
				int lbx = playerloc.getBlockX(),
					lby = playerloc.getBlockY(),
					lbz = playerloc.getBlockZ();
				String lworld = playerloc.getWorld().getName();
				int nbx = player.getLocation().getBlockX(),
					nby = player.getLocation().getBlockY(),
					nbz = player.getLocation().getBlockZ();
				String nworld = player.getLocation().getWorld().getName();
				if(	lbx != nbx ||
					lby != nby ||
					lbz != nbz ||
					!lworld.equalsIgnoreCase(nworld)
					) {
					player.sendMessage("§cVous avez bougé, veuillez ne pas bouger durant la téléportation.");
					return;
				}
				player.teleport(lobby);
				player.sendMessage("§7Téléportation terminé !");
			}
		}, 20 * secs);
	}

	public void spawnCommand(final Player player) {
		Group group = gm.getGroupByWorld(player.getLocation().getWorld().getName());
		if(group == null) {
			player.sendMessage("§cErreur: Aucun spawn n'est définit pour ce monde !");
			return;
		}
		Location spawn = group.getSpawn();
		if(spawn == null) {
			player.sendMessage("§cErreur: Aucun spawn n'est définit pour ce monde !");
			return;
		}
		final Location tp = gm.getSpawnByNameFromConfig(group.getName());
		plugin.getLogger().log(Level.INFO, "" + tp);
		if(player.hasPermission("essentials.teleport.timer.bypass")) {
			player.teleport(tp);
			player.sendMessage("§7Téléportation terminé !");
			return;
		}
		int secs = plugin.secs;
		final Location playerloc = player.getLocation();
		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			public void run() {
				scheduler(playerloc, player, tp);
			}
		}, 20 * secs);
		player.sendMessage("§7Téléportation dans 5 secondes, ne bouger pas !");
	}
	
	
	public Location getSpawnLocation(Player player) {
		for(Group group : plugin.groups) {
			if(group.containWorld(player.getLocation().getWorld().getName())) {
				return group.getSpawn();
			}
		}
		return null;
	}
	
	public void scheduler(Location playerloc, Player player, Location spawn) {
		try {
			int lbx = playerloc.getBlockX(),
				lby = playerloc.getBlockY(),
				lbz = playerloc.getBlockZ();
			String lworld = playerloc.getWorld().getName();
			int nbx = player.getLocation().getBlockX(),
				nby = player.getLocation().getBlockY(),
				nbz = player.getLocation().getBlockZ();
			String nworld = player.getLocation().getWorld().getName();
			if(	lbx != nbx ||
				lby != nby ||
				lbz != nbz ||
				!lworld.equalsIgnoreCase(nworld)
				) {
				player.sendMessage("§cVous avez bougé, veuillez ne pas bouger durant la téléportation.");
				return;
			}
			player.teleport(spawn);
			player.sendMessage("§7Téléportation terminé !");
		} catch(Exception e) {
			player.sendMessage(
					"World: " + spawn.getWorld().getName() +
					" / X: " + spawn.getBlockX() +
					" / Y: " + spawn.getBlockY() +
					" / Z: " + spawn.getBlockZ());
		}
	}

}

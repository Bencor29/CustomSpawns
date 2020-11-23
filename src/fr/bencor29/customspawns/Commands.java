package fr.bencor29.customspawns;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class Commands implements CommandExecutor {

	private final CustomSpawns plugin;
	private final GroupManager gm;
	
	public Commands(CustomSpawns plugin) {
		this.plugin = plugin;
		gm = GroupManager.getInstance();
	}
	
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(!(sender instanceof Player)) {
			sender.sendMessage("§cYou must be a player.");
			return true;
		}
		final Player player = (Player) sender;
		if(label.equalsIgnoreCase("spawn")) {
			spawnCommand(player);
		} else if(label.equalsIgnoreCase("lobby")) {
			lobbyCommand(player);
		} else if(label.equalsIgnoreCase("setlobby")) {
			setLobbyCommand(player);
		} else if(label.equalsIgnoreCase("csreload")) {
			reloadCommand(player);
		}
		return true;
	}

	public void reloadCommand(Player player) {
		plugin.onReload();
		player.sendMessage("§aPlugin reloaded !");
	}


	public void setLobbyCommand(Player player) {
		Location lobby = Utils.getLocation(player);
		plugin.setLobby(lobby);
		player.sendMessage("§aNew lobby's location defined.");

		saveLobby(
				lobby.getWorld().getName(),
				lobby.getX(),
				lobby.getY(),
				lobby.getZ(),
				lobby.getYaw(),
				lobby.getPitch()
		);
	}


	public void saveLobby(String world, double x, double y, double z, float yaw, float pitch) {
		plugin.getGenconf().set("lobby.world", world);
		plugin.getGenconf().set("lobby.x", x);
		plugin.getGenconf().set("lobby.y", y);
		plugin.getGenconf().set("lobby.z", z);
		plugin.getGenconf().set("lobby.yaw", yaw);
		plugin.getGenconf().set("lobby.pitch", pitch);
		File file = plugin.getConfigFile(0);
		FileConfiguration configs = plugin.getFileConfig(0);
		try {
			configs.save(file);
		} catch (IOException e) {
			plugin.getLogger().log(Level.SEVERE, "Failed to save configs.");
		}
	}


	public void lobbyCommand(final Player player) {
		final Location lobby = plugin.getLobby();
		if(lobby == null) {
			player.sendMessage("§cAucun lobby n'est définit");
			return;
		}
		int secs = plugin.getSecs();
		final Location playerloc = player.getLocation();
		if(player.hasPermission("customspawns.notimer")) {
			player.teleport(lobby);
			player.sendMessage("§7Téléportation terminée !");
			return;
		}
		player.sendMessage("§7Téléportation dans 5 secondes, ne bougez pas !");
		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> scheduler(playerloc, player, lobby), 20 * secs);
	}

	public void spawnCommand(final Player player) {
		Group group = gm.getGroupByWorld(player.getLocation().getWorld().getName());
		if(group == null) {
			player.sendMessage("§cAucun spawn n'est définit pour ce monde !");
			return;
		}
		Location spawn = group.getSpawn();
		if(spawn == null) {
			player.sendMessage("§cAucun spawn n'est définit pour ce monde !");
			return;
		}
		final Location tp = gm.getSpawnByNameFromConfig(group.getName());
		plugin.getLogger().log(Level.INFO, "" + tp);
		if(player.hasPermission("essentials.teleport.timer.bypass")) {
			player.teleport(tp);
			player.sendMessage("§7Téléportation terminée !");
			return;
		}
		int secs = plugin.getSecs();
		final Location playerloc = player.getLocation();
		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> scheduler(playerloc, player, tp), 20 * secs);
		player.sendMessage("§7Téléportation dans 5 secondes, ne bougez pas !");
	}

	public void scheduler(Location playerloc, Player player, Location spawn) {
		try {
			if (checkBeforeTP(playerloc, player, spawn)) return;
			player.sendMessage("§7Téléportation terminée !");
		} catch(Exception e) {
			player.sendMessage(
					"World: " + spawn.getWorld().getName() +
					" / X: " + spawn.getBlockX() +
					" / Y: " + spawn.getBlockY() +
					" / Z: " + spawn.getBlockZ());
		}
	}

	private boolean checkBeforeTP(Location playerloc, Player player, Location spawn) {
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
			return true;
		}
		player.teleport(spawn);
		return false;
	}

}

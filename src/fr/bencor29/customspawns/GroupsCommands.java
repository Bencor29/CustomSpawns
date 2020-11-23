package fr.bencor29.customspawns;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GroupsCommands implements CommandExecutor {

	private final CustomSpawns plugin;
	private final GroupManager gm;
	
	public GroupsCommands(CustomSpawns plugin) {
		this.plugin = plugin;
		gm = GroupManager.getInstance();
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (args.length == 0) {
			commandFail(sender);
			return true;
		}
		if (
				args.length != 3 && 
				!args[0].equalsIgnoreCase("setworldspawn") && 
				!args[0].equalsIgnoreCase("remove") && 
				!args[0].equalsIgnoreCase("listworlds") && 
				!args[0].equalsIgnoreCase("list")) {
			commandFail(sender);
			return true;
		}
		if (
				args.length != 2 && args[0].equalsIgnoreCase("setworldspawn") ||
				args.length != 2 && args[0].equalsIgnoreCase("remove") ||
				args.length != 2 && args[0].equalsIgnoreCase("listworlds") ||
				args.length != 1 && args[0].equalsIgnoreCase("list")) {
			commandFail(sender);
			return true;
		}
		String cmd = args[0];
		if (cmd.equalsIgnoreCase("create")) 				commandCreate(sender, args);
		else if (cmd.equalsIgnoreCase("remove"))			commandRemove(sender, args);
		else if (cmd.equalsIgnoreCase("list"))			commandList(sender);
		else if (cmd.equalsIgnoreCase("listworlds"))		commandListWorlds(sender, args);
		else if (cmd.equalsIgnoreCase("addworld")) 		commandAddWorld(sender, args);
		else if (cmd.equalsIgnoreCase("delworld")) 		commandDelWorld(sender, args);
		else if (cmd.equalsIgnoreCase("setworldspawn")) 	commandSetWorldSpawn(sender, args);
		else commandFail(sender);
		return true;
	}

	public void commandListWorlds(CommandSender sender, String[] args) {
		String name = args[1];
		if (gm.getGroupByName(name) == null) {
			Utils.sendMessage(sender, "§cThat group doesn't exists.");
			return;
		}
		Group group = gm.getGroupByName(name);
		Utils.sendMessage(sender, "Worlds in group:");
		for(String world : group.getWorlds()) {
			if (sender instanceof Player) {
				sender.sendMessage("§7 - " + world);
			} else {
				plugin.log(Level.INFO, " - " + world);
			}
		}
	}

	public void commandList(CommandSender sender) {
		Utils.sendMessage(sender, "§6§lGroups list:");
		for(Group group : gm.getGroupsFromFile()) {
			try {
				Utils.sendMessage(
						sender,
						String.format(
								" §7- §e§l%s§7 -> Worlds: %d / Spawn world: %s",
								group.getName(), group.getWorlds().size(), group.getSpawn().getWorld().getName()
						)
				);
			} catch (Exception e) {
				Utils.sendMessage(
						sender,
						String.format(
								" §7- §e§l%s§7 -> Worlds: %d / Spawn world: §cInvalid world",
								group.getName(), group.getWorlds().size()
						)
				);
			}
		}
	}

	public void commandRemove(CommandSender sender, String[] args) {
		String name = args[1];
		if (gm.getGroupByName(name) == null) {
			Utils.sendMessage(sender, "§cThat group doesn't exists.");
			return;
		}

		int id = gm.getGroupIdByName(name) - 1;
		plugin.getGroups().remove(id);
		plugin.getGrpconf().set("groups." + name, null);

		try {
			plugin.getGrpconf().save(plugin.getConfigFile(1));
		} catch (IOException e) {
			Utils.sendMessage(sender, "§cFailed to save group.");
			return;
		}

		Utils.sendMessage(sender, "§aGroup removed.");
	}

	private void commandFail(CommandSender sender) {
		String[] help = new String[] {
				"§cUsage:",
				"§7 §o /group §elist",
				"§7 §o /group §ecreate <group> <spawn_world>",
				"§7 §o /group §eremove <group>",
				"§7 §o /group §eaddworld <group> <world>",
				"§7 §o /group §edelworld <group> <world>",
				"§7 §o /group §elistworlds <group>",
				"§7 §o /group §esetworldspawn <group>"
		};

		Utils.sendMessage(sender, help);
	}

	public void commandSetWorldSpawn(CommandSender sender, String[] args) {
		if (!(sender instanceof Player)) {
			Utils.sendMessage(sender, "§cOnly players can execute this command.");
			return;
		}
		Player player = (Player) sender;
		String name = args[1];
		int id = gm.getGroupIdByName(name);
		if (id == 0) {
			Utils.sendMessage(player, "§cThat group doesn't exists.");
			return;
		}
		id--;
		Group group = plugin.getGroups().get(id);
		group.setSpawn(Utils.getLocation(player));
		plugin.getGroups().set(id, group);
		gm.saveGroup(group);
		Utils.sendMessage(player, "§aGroup successfully modified.");
	}

	private void commandDelWorld(CommandSender sender, String[] args) {
		String worldname = args[2];
		String name = args[1];
		if (!gm.checkAddWorld(worldname)) {
			Utils.sendMessage(sender, "§cThat world isn't in any group.");
			return;
		}

		int id = gm.getGroupIdByWorld(worldname);
		if (id == 0) {
			Utils.sendMessage(sender, "§cThat world isn't in any group.");
			return;
		}
		id--;
		if (!gm.getGroupByName(name).containWorld(worldname)) {
			Utils.sendMessage(sender, "§cThat world isn't in that group.");
			return;
		}
		plugin.getGroups().remove(id);
		Utils.sendMessage(sender, "§aWorld removed from group.");
	}

	public void commandAddWorld(CommandSender sender, String[] args) {
		String name = args[1];
		String worldname = args[2];
		if (gm.checkAddWorld(worldname)) {
			Utils.sendMessage(sender, "§cThat world is already in a group.");
			return;
		}
		
		try {
			World world = plugin.getServer().getWorld(worldname);
			if (world == null) {
				Utils.sendMessage(sender, "§cThat world doesn't exists.");
				return;
			}
		} catch(Exception e) {
			Utils.sendMessage(sender, "§cThat world doesn't exists.");
			return;
		}
		
		int id = gm.getGroupIdByName(name);
		if (id == 0) {
			Utils.sendMessage(sender, "§cThat group doesn't exists.");
			return;
		}
		id--;
		Group group = plugin.getGroups().get(id);
		group.addWorld(worldname);
		plugin.getGroups().set(id, group);
		gm.saveGroup(group);
		Utils.sendMessage(sender, "§aWorld added.");
	}

	public void commandCreate(CommandSender sender, String[] args) {
		String name = args[1];
		String worldname = args[2];
		if (gm.checkCreate(name, worldname)) {
			Utils.sendMessage(sender, "§cThat group/world is already used.");
			return;
		}
		try {
			World world = plugin.getServer().getWorld(worldname);
			if (world == null) {
				Utils.sendMessage(sender, "§cThat group doesn't exists.");
				return;
			}
			Location spawn = new Location(world, 0, 0, 0, 0, 0);
			ArrayList<String> worlds = new ArrayList<>();
			worlds.add(world.getName());
			Group group = new Group(name, worlds, spawn);
			plugin.getGroups().add(group);
			gm.saveGroup(group);
		} catch(Exception e) {
			Utils.sendMessage(sender, "§cThat group doesn't exists.");
			return;
		}

		Utils.sendMessage(sender, "§aGroup created.");
	}
}

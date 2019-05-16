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

	CustomSpawns plugin;
	GroupManager gm;
	String usage;
	
	public GroupsCommands(CustomSpawns plugin) {
		this.plugin = plugin;
		gm = new GroupManager(plugin);
		usage = "Usage: /group [list | create <group> <world_spawn> | remove <group> | addworld <group> <world> | delworld <group> <world> | listworlds <group> | setworldspawn <group>]";
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(args.length == 0) {
			sender.sendMessage("§c" + usage);
			return true;
		}
		if(
				args.length != 3 && 
				!args[0].equalsIgnoreCase("setworldspawn") && 
				!args[0].equalsIgnoreCase("remove") && 
				!args[0].equalsIgnoreCase("listworlds") && 
				!args[0].equalsIgnoreCase("list")) {
			if(sender instanceof Player) {
				commandFail(sender, args);
				return true;
			} else {
				commandFail(sender, args);
				return true;
			}
		}
		if(
				args.length != 2 && args[0].equalsIgnoreCase("setworldspawn") ||
				args.length != 2 && args[0].equalsIgnoreCase("remove") ||
				args.length != 2 && args[0].equalsIgnoreCase("listworlds") ||
				args.length != 1 && args[0].equalsIgnoreCase("list")) {
			if(sender instanceof Player) {
				commandFail(sender, args);
				return true;
			} else {
				commandFail(sender, args);
				return true;
			}
		}
		String cmd = args[0];
		if(cmd.equalsIgnoreCase("create")) 				commandCreate(sender, args);
		else if(cmd.equalsIgnoreCase("remove"))			commandRemove(sender, args);
		else if(cmd.equalsIgnoreCase("list"))			commandList(sender, args);
		else if(cmd.equalsIgnoreCase("listworlds"))		commandListWorlds(sender, args);
		else if(cmd.equalsIgnoreCase("addworld")) 		commandAddWorld(sender, args);
		else if(cmd.equalsIgnoreCase("delworld")) 		commandDelWorld(sender, args);
		else if(cmd.equalsIgnoreCase("setworldspawn")) 	commandSetWorldSpawn(sender, args);
		else commandFail(sender, args);
		return true;
	}

	public void commandListWorlds(CommandSender sender, String[] args) {
		String name = args[1];
		if(gm.getGroupByName(name) == null) {
			if(sender instanceof Player) {
				sender.sendMessage("§cCe groupe n'existe pas !");
				return;
			} else {
				plugin.log(Level.INFO, "Ce groupe n'existe pas !");
				return;
			}
		}
		Group group = gm.getGroupByName(name);
		if(sender instanceof Player) {
			sender.sendMessage("§6§lListe des mondes du groupe:");
		} else {
			plugin.log(Level.INFO, "Liste des mondes du groupe:");
		}
		for(String world : group.getWorlds()) {
			if(sender instanceof Player) {
				sender.sendMessage("§7 - " + world);
			} else {
				plugin.log(Level.INFO, " - " + world);
			}
		}
	}

	public void commandList(CommandSender sender, String[] args) {
		if(sender instanceof Player) {
			sender.sendMessage("§6§lListe des groupes:");
		} else {
			plugin.log(Level.INFO, "Liste des groupe:");
		}
		for(Group group : gm.getGroupsFromFile()) {
			try {
				if(sender instanceof Player) {
					sender.sendMessage(" §7- §e§l" + group.getName() + "§7 -> Mondes: " + group.getWorlds().size() + " / Monde de spawn: " + group.getSpawn().getWorld().getName());
				} else {
					plugin.log(Level.INFO, " - " + group.getName() + " -> Mondes: " + group.getWorlds().size() + " / Monde de spawn: " + group.getSpawn().getWorld().getName());
				}
			} catch (Exception e) {
				if(sender instanceof Player) {
					sender.sendMessage("§7 - ERROR");
					return;
				} else {
					plugin.log(Level.INFO, " - ERROR");
					return;
				}
			}
		}
	}

	public void commandRemove(CommandSender sender, String[] args) {
		String name = args[1];
		if(gm.getGroupByName(name) == null) {
			if(sender instanceof Player) {
				sender.sendMessage("§cCe groupe n'existe pas !");
				return;
			} else {
				plugin.log(Level.INFO, "Ce groupe n'existe pas !");
				return;
			}
		}
		int id = gm.getGroupIdByName(name);
		id--;
		plugin.groups.remove(id);
		plugin.grpconf.set("groups." + name, null);
		try {
			plugin.grpconf.save(plugin.getConfigFile(1));
		} catch (IOException e) {
			if(sender instanceof Player) {
				sender.sendMessage("§cImpossible de sauvegarder les groupes !");
				return;
			} else {
				plugin.log(Level.INFO, "Impossible de sauvegarder les groupes !");
				return;
			}
		}
		if(sender instanceof Player) {
			sender.sendMessage("§aGroupe supprimé avec succès !");
			return;
		} else {
			plugin.log(Level.INFO, "Groupe supprima avec succes !");
			return;
		}
	}

	private void commandFail(CommandSender sender, String[] args) {
		if(sender instanceof Player) {
			sender.sendMessage("§c" + usage);
			return;
		} else {
			plugin.log(Level.INFO, usage);
			return;
		}
	}

	public void commandSetWorldSpawn(CommandSender sender, String[] args) {
		if(!(sender instanceof Player)) {
			plugin.log(Level.INFO, "Vous devez etre un joueur pour executer cette commande !");
			return;
		}
		Player player = (Player) sender;
		Location playerloc = player.getLocation();
		World world 	= player.getWorld();
		double 	x 		= playerloc.getX(),
				y 		= playerloc.getY(),
				z 		= playerloc.getZ();
		float 	yaw 	= playerloc.getYaw(),
				pitch 	= playerloc.getPitch();
		Location spawn = new Location(world, x, y, z, yaw, pitch);
		String name = args[1];
		int id = gm.getGroupIdByName(name);
		if(id == 0) {
			player.sendMessage("§cCe groupe n'existe pas !");
			return;
		}
		id--;
		Group group = plugin.groups.get(id);
		group.setSpawn(spawn);
		plugin.groups.set(id, group);
		gm.saveGroup(group);
		player.sendMessage("§aGroupe modifié avec succès !");
	}

	private void commandDelWorld(CommandSender sender, String[] args) {
		String worldname = args[2];
		String name = args[1];
		if(!gm.checkAddWorld(worldname)) {
			if(sender instanceof Player) {
				sender.sendMessage("§cErreur: Le monde choisit n'est pas utilisé !");
				return;
			} else {
				plugin.log(Level.INFO, "Erreur: Le monde choisit n'est pas utilisé !");
				return;
			}
		}
		int id = gm.getGroupIdByWorld(worldname);
		if(id == 0) {
			if(sender instanceof Player) {
				sender.sendMessage("§cErreur: Le monde choisit n'est pas utilisé !");
				return;
			} else {
				plugin.log(Level.INFO, "Erreur: Le monde choisit n'est pas utilisé !");
				return;
			}
		}
		id--;
		if(!gm.getGroupByName(name).containWorld(worldname)) {
			if(sender instanceof Player) {
				sender.sendMessage("§cErreur: Le monde choisit n'est pas utilisé dans ce groupe !");
				return;
			} else {
				plugin.log(Level.INFO, "Erreur: Le monde choisit n'est pas utilisé dans ce groupe !");
				return;
			}
		}
		plugin.groups.remove(id);
		if(sender instanceof Player) {
			sender.sendMessage("§aLe monde a été supprimé !");
			return;
		} else {
			plugin.log(Level.INFO, "Le monde a ete supprime !");
			return;
		}
	}

	public void commandAddWorld(CommandSender sender, String[] args) {
		String name = args[1];
		String worldname = args[2];
		if(gm.checkAddWorld(worldname)) {
			if(sender instanceof Player) {
				sender.sendMessage("§cErreur: Le monde choisit est déjà utilisé !");
				return;
			} else {
				plugin.log(Level.INFO, "Erreur: Le monde choisit est déjà utilisé !");
				return;
			}
		}
		
		try {
			World world = plugin.getServer().getWorld(worldname);
			if(world == null) {
				if(sender instanceof Player) {
					sender.sendMessage("§cErreur: Ce monde n'existe pas !");
					return;
				} else {
					plugin.log(Level.INFO, "Erreur: Ce monde n'existe pas !");
					return;
				}
			}
		} catch(Exception e) {
			if(sender instanceof Player) {
				sender.sendMessage("§cErreur: Ce monde n'existe pas !");
				return;
			} else {
				plugin.log(Level.INFO, "Erreur: Ce monde n'existe pas !");
				return;
			}
		}
		
		int id = gm.getGroupIdByName(name);
		if(id == 0) {
			if(sender instanceof Player) {
				sender.sendMessage("§cErreur: Ce groupe n'existe pas !");
				return;
			} else {
				plugin.log(Level.INFO, "Erreur: Ce groupe n'existe pas !");
				return;
			}
		}
		id--;
		Group group = plugin.groups.get(id);
		group.addWorld(worldname);
		plugin.groups.set(id, group);
		gm.saveGroup(group);
		if(sender instanceof Player) {
			sender.sendMessage("§aLe monde a été ajouté !");
			return;
		} else {
			plugin.log(Level.INFO, "Le monde a ete ajoute !");
			return;
		}
	}

	public void commandCreate(CommandSender sender, String[] args) {
		String name = args[1];
		String worldname = args[2];
		if(gm.checkCreate(name, worldname)) {
			if(sender instanceof Player) {
				sender.sendMessage("§cErreur: Le groupe/monde choisit est déjà utilisé !");
				return;
			} else {
				plugin.log(Level.INFO, "Erreur: Le groupe/monde choisit est déjà utilisé !");
				return;
			}
		}
		try {
			World world = plugin.getServer().getWorld(worldname);
			if(world == null) {
				if(sender instanceof Player) {
					sender.sendMessage("§cErreur: Le monde '" + worldname + "' n'existe pas !");
					return;
				} else {
					plugin.log(Level.INFO, "Erreur: Le monde '" + worldname + "' n'existe pas !");
					return;
				}
			}
			Location spawn = new Location(world, 0, 0, 0, 0, 0);
			ArrayList<String> worlds = new ArrayList<String>();
			worlds.add(world.getName());
			Group group = new Group(name, worlds, spawn);
			plugin.groups.add(group);
			gm.saveGroup(group);
		} catch(Exception e) {
			if(sender instanceof Player) {
				sender.sendMessage("§cErreur: Le monde '" + worldname + "' n'existe pas !");
				return;
			} else {
				plugin.log(Level.INFO, "Erreur: Le monde '" + worldname + "' n'existe pas !");
				return;
			}
		}
		if(sender instanceof Player) {
			sender.sendMessage("§aLe groupe a été créer !");
			return;
		} else {
			plugin.log(Level.INFO, "Le groupe a ete creer !");
			return;
		}
		
	}

}

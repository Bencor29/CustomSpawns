package fr.bencor29.customspawns;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;

import org.bukkit.Location;
import org.bukkit.World;

public class GroupManager {

	CustomSpawns plugin;
	
	public GroupManager(CustomSpawns plugin) {
		this.plugin = plugin;
	}
	
	public void saveGroup(Group group) {
		String name = group.getName();
		ArrayList<String> worlds = group.getWorlds();
		Location spawn = group.getSpawn();
		String w = spawn.getWorld().getName();
		String path = "groups." + name + ".";
		String loc = path + "spawn.";
		plugin.grpconf.set(path + "worlds", worlds);
		plugin.grpconf.set(loc + "world", w);
		plugin.grpconf.set(loc + "x", spawn.getX());
		plugin.grpconf.set(loc + "y", spawn.getY());
		plugin.grpconf.set(loc + "z", spawn.getZ());
		plugin.grpconf.set(loc + "yaw", spawn.getYaw());
		plugin.grpconf.set(loc + "pitch", spawn.getPitch());
		File file = new File(plugin.getDataFolder(), "groups.yml");
		try {
			plugin.grpconf.save(file);
		} catch (IOException e) {
			plugin.getLogger().log(Level.INFO, "Impossible de sauvegarder les configs !");
		}
	}
	
	public int getGroupIdByWorld(String worldname) {
		for(int i = 0; i < plugin.groups.size(); i++) {
			if(plugin.groups.get(i).containWorld(worldname)) return i + 1;
		}
		return 0;
	}
	
	public Group getGroupByWorld(String worldname) {
		for(int i = 0; i < plugin.groups.size(); i++) {
			if(plugin.groups.get(i).containWorld(worldname)) return plugin.groups.get(i);
		}
		return null;
	}
	
	public Group getGroupByWorld(World world) {
		return getGroupByWorld(world.getName());
	}
	
	public int getGroupIdByWorld(World world) {
		return getGroupIdByWorld(world.getName());
	}
	
	public Group getGroupByName(String name) {
		for(Group group : plugin.groups) {
			if(group.getName().equalsIgnoreCase(name))
				return group;
		}
		return null;
	}
	
	public int getGroupIdByName(String name) {
		for(int i = 0; i < plugin.groups.size(); i++) {
			if(plugin.groups.get(i).getName().equalsIgnoreCase(name)) {
				return i + 1;
			}
		}
		return 0;
	}
	
	public boolean checkAddWorld(String worldname) {
		for(Group group : plugin.groups)
			if(group.containWorld(worldname)) return true;
		return false;
	}
	
	public boolean checkCreate(String name, String worldname) {
		for(Group group : plugin.groups) {
			if(group.getName().equalsIgnoreCase(name)) 	return true;
			if(group.containWorld(worldname)) 			return true;
		}
		return false;
	}

	public ArrayList<Group> getGroupsFromFile() {
		ArrayList<Group> groups = new ArrayList<Group>();
		String gpath = "groups.", path = "", loc = "";
		for(String name : plugin.grpconf.getConfigurationSection("groups").getKeys(false)) {
			try {
				path = gpath + name + ".";
				loc = path + "spawn.";
				@SuppressWarnings("unchecked")
				ArrayList<String> worlds = (ArrayList<String>) plugin.grpconf.getList(path + "worlds");
				World world = plugin.getServer().getWorld(plugin.grpconf.getString(loc + "world"));
				double 	x 		= plugin.grpconf.getDouble(loc + "x"),
						y 		= plugin.grpconf.getDouble(loc + "y"),
						z 		= plugin.grpconf.getDouble(loc + "z");
				float 	yaw 	= plugin.grpconf.getLong(loc + "yaw"),
						pitch 	= plugin.grpconf.getLong(loc + "pitch");
				Location spawn = new Location(world, x, y, z, yaw, pitch);
				Group group = new Group(name, worlds, spawn);
				groups.add(group);
			} catch(Exception e) {
				plugin.log(Level.WARNING, "Cannot load group: " + name);
			}
		}
		return groups;
	}

	public Location getSpawnByNameFromConfig(String name) {
		String path = "groups." + name + ".";
		String loc = path + "spawn.";
		World 	world 	= plugin.getServer().getWorld(plugin.grpconf.getString(loc + "world"));
		double 	x 		= plugin.grpconf.getDouble(loc + "x"),
				y 		= plugin.grpconf.getDouble(loc + "y"),
				z 		= plugin.grpconf.getDouble(loc + "z");
		float 	yaw 	= plugin.grpconf.getLong(loc + "yaw"),
				pitch 	= plugin.grpconf.getLong(loc + "pitch");
		return new Location(world, x, y, z, yaw, pitch);
	}
	
}

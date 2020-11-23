package fr.bencor29.customspawns;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;

import org.bukkit.Location;
import org.bukkit.World;

public class GroupManager {

	private static GroupManager singleton;

	public static GroupManager getInstance() {
		return singleton;
	}

	public static void initSingleton(CustomSpawns plugin) {
		if (singleton != null) {
			return;
		}

		singleton = new GroupManager(plugin);
	}


	private final CustomSpawns plugin;

	private GroupManager(CustomSpawns plugin) {
		this.plugin = plugin;
	}
	
	public void saveGroup(Group group) {
		String name = group.getName();
		ArrayList<String> worlds = group.getWorlds();
		Location spawn = group.getSpawn();
		String w = spawn.getWorld().getName();
		String path = "groups." + name + ".";
		String loc = path + "spawn.";
		plugin.getGrpconf().set(path + "worlds", worlds);
		plugin.getGrpconf().set(loc + "world", w);
		plugin.getGrpconf().set(loc + "x", spawn.getX());
		plugin.getGrpconf().set(loc + "y", spawn.getY());
		plugin.getGrpconf().set(loc + "z", spawn.getZ());
		plugin.getGrpconf().set(loc + "yaw", spawn.getYaw());
		plugin.getGrpconf().set(loc + "pitch", spawn.getPitch());
		File file = new File(plugin.getDataFolder(), "groups.yml");
		try {
			plugin.getGrpconf().save(file);
		} catch (IOException e) {
			plugin.getLogger().log(Level.INFO, "Cannot save configs.");
		}
	}
	
	public int getGroupIdByWorld(String worldname) {
		for(int i = 0; i < plugin.getGroups().size(); i++) {
			if(plugin.getGroups().get(i).containWorld(worldname)) return i + 1;
		}
		return 0;
	}
	
	public Group getGroupByWorld(String worldname) {
		for(int i = 0; i < plugin.getGroups().size(); i++) {
			if(plugin.getGroups().get(i).containWorld(worldname)) return plugin.getGroups().get(i);
		}
		return null;
	}
	
	public Group getGroupByWorld(World world) {
		return getGroupByWorld(world.getName());
	}

	public Group getGroupByName(String name) {
		for(Group group : plugin.getGroups()) {
			if(group.getName().equalsIgnoreCase(name))
				return group;
		}
		return null;
	}
	
	public int getGroupIdByName(String name) {
		for(int i = 0; i < plugin.getGroups().size(); i++) {
			if(plugin.getGroups().get(i).getName().equalsIgnoreCase(name)) {
				return i + 1;
			}
		}
		return 0;
	}
	
	public boolean checkAddWorld(String worldname) {
		for(Group group : plugin.getGroups())
			if(group.containWorld(worldname)) return true;
		return false;
	}
	
	public boolean checkCreate(String name, String worldname) {
		for(Group group : plugin.getGroups()) {
			if(group.getName().equalsIgnoreCase(name)) 	return true;
			if(group.containWorld(worldname)) 			return true;
		}
		return false;
	}

	public ArrayList<Group> getGroupsFromFile() {
		ArrayList<Group> groups = new ArrayList<Group>();
		String gpath = "groups.", path = "", loc = "";
		for(String name : plugin.getGrpconf().getConfigurationSection("groups").getKeys(false)) {
			try {
				path = gpath + name + ".";
				loc = path + "spawn.";
				@SuppressWarnings("unchecked")
				ArrayList<String> worlds = (ArrayList<String>) plugin.getGrpconf().getList(path + "worlds");
				World world = plugin.getServer().getWorld(plugin.getGrpconf().getString(loc + "world"));
				double 	x 		= plugin.getGrpconf().getDouble(loc + "x"),
						y 		= plugin.getGrpconf().getDouble(loc + "y"),
						z 		= plugin.getGrpconf().getDouble(loc + "z");
				float 	yaw 	= plugin.getGrpconf().getLong(loc + "yaw"),
						pitch 	= plugin.getGrpconf().getLong(loc + "pitch");
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
		World 	world 	= plugin.getServer().getWorld(plugin.getGrpconf().getString(loc + "world"));
		double 	x 		= plugin.getGrpconf().getDouble(loc + "x"),
				y 		= plugin.getGrpconf().getDouble(loc + "y"),
				z 		= plugin.getGrpconf().getDouble(loc + "z");
		float 	yaw 	= plugin.getGrpconf().getLong(loc + "yaw"),
				pitch 	= plugin.getGrpconf().getLong(loc + "pitch");
		return new Location(world, x, y, z, yaw, pitch);
	}
	
}

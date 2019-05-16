package fr.bencor29.customspawns;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class CustomSpawns extends JavaPlugin {
	
	FileConfiguration genconf;
	FileConfiguration grpconf;
	
	ArrayList<Group> groups = new ArrayList<Group>();
	int secs;
	Location lobby;
	
	
	public void onEnable() {
		
		String[] commands = {"spawn", "lobby", "setlobby", "csreload"};
		for(String cmd : commands) {
			getCommand(cmd).setExecutor(new Commands(this));
			log(Level.INFO, "Loading command: " + cmd);
		}
		getCommand("group").setExecutor(new GroupsCommands(this));
		log(Level.INFO, "Loading command: group");
		
		loadConfig();
		
		log(Level.INFO, "Registering events");
		getServer().getPluginManager().registerEvents(new Events(this), this);
	}
	
	public void onDisable() {
		//grpconf.set("", arg1);
	}
	
	public void loadConfig() {
		
		log(Level.INFO, "Loading configurations is starting.");
		
		File folder = getDataFolder();
		File config = new File(getDataFolder(), "config.yml");
		File groups = new File(getDataFolder(), "groups.yml");
		
		if(!folder.exists()) folder.mkdirs();
		if(folder.isFile()) {
			folder.renameTo(new File(folder.getName() + ".broken"));
			folder = new File("plugins/CustomSpawns/");
			folder.mkdirs();
		}
		
		if(!config.exists()) {
			saveResource("config.yml", true);
		}
		
		if(config.isDirectory()) {
			String temp_name = config.getName();
			config.renameTo(new File(temp_name + ".broken"));
			config = new File(getDataFolder(), "config.yml");
			saveResource("config.yml", true);
		}

		if(!groups.exists()) {
			saveResource("groups.yml", true);
		}
		
		if(groups.isDirectory()) {
			String temp_name = groups.getName();
			groups.renameTo(new File(temp_name + ".broken"));
			groups = new File(getDataFolder(), "groups.yml");
			saveResource("groups.yml", true);
		}
		
		genconf = YamlConfiguration.loadConfiguration(config);
		grpconf = YamlConfiguration.loadConfiguration(groups);
		
		secs = genconf.getInt("seconds");
		log(Level.INFO, "Loading configurations finished.");
		
		log(Level.INFO, "Loading lobby is starting.");
		lobby = getLobby();
		log(Level.INFO, "Loading lobby finished.");
		
		log(Level.INFO, "Loading groups starting.");
		getGroups();
		log(Level.INFO, "Loading groups finished.");
	}
	
	public void onReload() {
		loadConfig();
	}
	

	public Location getLobby() {
		try {
			try {
				World world = getServer().getWorld(genconf.getString("lobby.world"));
				if(world == null) {
					log(Level.WARNING, "Attetion: Le monde du lobby n'est pas correct !");
					return null;
				}
				double 	x = genconf.getDouble("lobby.x"),
						y = genconf.getDouble("lobby.y"),
						z = genconf.getDouble("lobby.z");
				float 	yaw = genconf.getLong("lobby.yaw"),
						pitch = genconf.getLong("lobby.pitch");
				return new Location(world, x, y, z, yaw, pitch);
			} catch(Exception e) {
				log(Level.WARNING, "Attetion: Le monde du lobby n'est pas correct !");
				return null;
			}
		} catch(Exception e) {
			log(Level.WARNING, "Attetion: Le lobby n'est pas d�finit !");
			return null;
		}
	}

	public File getConfigFile(int i) {
		if(i == 0) {
			return new File("plugins/CustomSpawns/config.yml");
		} else if(i == 1) {
			return new File("plugins/CustomSpawns/groups.yml");
		} else {
			return null;
		}
	}
	
	public FileConfiguration getFileConfig(int i) {
		if(i == 0) {
			return genconf;
		} else if(i == 1) {
			return grpconf;
		} else {
			return null;
		}
	}

	public void getGroups() {
		try {
			for(String name : grpconf.getConfigurationSection("groups").getKeys(false)) {
				String path = "groups." + name + ".";
				String loc = path + "spawn.";
				try {
					World world = getServer().getWorld(grpconf.getString(loc + "world"));
					double 	x 		= grpconf.getDouble(loc + "x"),
							y 		= grpconf.getDouble(loc + "y"),
							z 		= grpconf.getDouble(loc + "z");
					float 	yaw 	= grpconf.getLong(loc + "yaw"),
							pitch 	= grpconf.getLong(loc + "pitch");
					Location spawn 	= new Location(world, x, y, z, yaw, pitch);
					List<?> ttworlds = grpconf.getList(path + "worlds");
					ArrayList<String> tworlds = new ArrayList<String>();
					for(Object tworld : ttworlds) {
						tworlds.add(tworld.toString());
					}
					ArrayList<String> worlds = tworlds;
					Group group = new Group(name, worlds, spawn);
					groups.add(group);
					log(Level.INFO, "Loading group " + group.getName() + " / worlds: " + worlds + " / Spawn: " + spawn);
				} catch (Exception e) {}
			}
		} catch(Exception e) {}
	}
	
	public void log(Level level, String msg) {
		getLogger().log(level, msg);
	}
}
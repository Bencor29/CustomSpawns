package fr.bencor29.customspawns;

import java.util.ArrayList;

import org.bukkit.Location;

public class Group {

	String name;
	ArrayList<String> worlds;
	Location spawn;
	
	public Group(String name, ArrayList<String> worlds, Location spawn) {
		this.name = name;
		this.worlds = worlds;
		this.spawn = spawn;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void addWorld(String world) {
		worlds.add(world);
	}
	
	public void setSpawn(Location spawn) {
		this.spawn = spawn;
	}
	
	public void setWorlds(ArrayList<String> worlds) {
		this.worlds = worlds;
	}
	
	public String getName() {
		return name;
	}
	
	public ArrayList<String> getWorlds() {
		return worlds;
	}
	
	public Location getSpawn() {
		return spawn;
	}
	
	public boolean containWorld(String world) {
		return worlds.contains(world);
	}
	
}

package fr.bencor29.customspawns;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.logging.Level;

public class Utils {

    public static Location getLocation(Player player) {
        Location ploc = player.getLocation();
        World world = ploc.getWorld();

        double 	x = ploc.getX(),
                y = ploc.getY(),
                z = ploc.getZ();

        float 	yaw = ploc.getYaw(),
                pitch = ploc.getPitch();

        return new Location(world, x, y, z, yaw, pitch);
    }

    public static void sendMessage(CommandSender sender, String message) {
        if (sender instanceof Player) {
            sender.sendMessage(message);
        } else {
            CustomSpawns.getInstance().log(Level.INFO, message);
        }
    }

    public static void sendMessage(CommandSender sender, String[] messages) {
        for (String message : messages) {
            sendMessage(sender, message);
        }
    }

}

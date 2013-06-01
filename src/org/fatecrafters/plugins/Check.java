package org.fatecrafters.plugins;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.fatecrafters.plugins.WTUtil;
import org.fatecrafters.plugins.WorldTime;

public class Check implements Runnable {

	private final WorldTime plugin;

	public Check(WorldTime plugin) {
		this.plugin = plugin;
	}

	private String locWorld;
	private String timerExpiredMessage;
	private double locX;
	private double locY;
	private double locZ;
	private int cooldown;

	@SuppressWarnings("deprecation")
	@Override
	public void run() {
		for (Player p : Bukkit.getOnlinePlayers()) {
			final String name = p.getName();
			if (p.hasPermission("worldtimer.bypass") || !WTUtil.inEnabledWorld.get(name).booleanValue())
				continue;					
			final String worldname = p.getWorld().getName();

			plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new Runnable() {
				public void run() {
					locWorld = plugin.getConfig().getString("Worlds."+worldname+".locationOnExpire."+"world");
					locX = plugin.getConfig().getDouble("Worlds."+worldname+".locationOnExpire."+"x");
					locY = plugin.getConfig().getDouble("Worlds."+worldname+".locationOnExpire."+"y");
					locZ = plugin.getConfig().getDouble("Worlds."+worldname+".locationOnExpire."+"z");
					timerExpiredMessage = plugin.getConfig().getString("Worlds."+worldname+".timerExpiredMessage");
					cooldown = plugin.getConfig().getInt("Worlds."+worldname+".cooldown");
				}
			}, 0L);

			Long timer = WTUtil.data.get(name+":"+worldname);
			if (timer == null || System.currentTimeMillis() <= timer)
				continue;
			p.teleport(new Location(plugin.getServer().getWorld(locWorld), locX, locY, locZ));
			p.sendMessage(ChatColor.translateAlternateColorCodes('&', timerExpiredMessage));
			
			plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new Runnable() {
				public void run() {
					if (cooldown > 0) {
						WTUtil.cooldowns.put(name+":"+worldname, System.currentTimeMillis() + cooldown*1000);
					}
					WTUtil.data.remove(name+":"+worldname);
					WTUtil.timestamps.remove(name+":"+worldname);
				}
			}, 0L);
		}
	}

}

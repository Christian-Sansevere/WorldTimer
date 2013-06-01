package org.fatecrafters.plugins;

import java.io.File;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import org.fatecrafters.plugins.Check;

public class WorldTime extends JavaPlugin {

	@Override
	public void onEnable() {
		WTUtil.setPlugin(this);
		File filedir = new File(getDataFolder()+File.separator+"data");
		if (!filedir.exists()) {
			filedir.mkdirs();
		}
		WTUtil.readData("data.yml", WTUtil.data);
		WTUtil.readData("cooldowns.yml", WTUtil.cooldowns);
		WTUtil.readData("timestamps.yml", WTUtil.timestamps);
		WTUtil.readData("cooldownTimestamps.yml", WTUtil.cooldownTimestamps);
		saveDefaultConfig();
		getServer().getPluginManager().registerEvents(new WTListener(this), this);
		getServer().getScheduler().scheduleSyncRepeatingTask(this, new Check(this), 0L, getConfig().getLong("checkIfExpiredEvery")*20);
		getLogger().info("[WorldTimer] WorldTimer has been enabled.");
	}

	@Override
	public void onDisable() {
		HandlerList.unregisterAll(this);
		WTUtil.writeData("data.yml", WTUtil.data);
		WTUtil.writeData("cooldowns.yml", WTUtil.cooldowns);
		WTUtil.writeData("timestamps.yml", WTUtil.timestamps);
		WTUtil.writeData("cooldownTimestamps.yml", WTUtil.cooldownTimestamps);
		getServer().getPluginManager().disablePlugin(this);
		getLogger().info("[WorldTimer] WorldTimer has been disabled.");
	}

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(cmd.getName().equalsIgnoreCase("worldtimer")) {
			if (args.length >= 1) {
				if (args[0].equalsIgnoreCase("reload")) {
					if (!sender.hasPermission("worldtimer.reload")) {
						sender.sendMessage(ChatColor.GRAY + "You do not have permission.");
						return false;
					}
					Long first = System.currentTimeMillis();
					reloadConfig();
					sender.sendMessage(ChatColor.GRAY + "Config reloaded.");
					sender.sendMessage(ChatColor.GRAY + "Took " + ChatColor.YELLOW + (System.currentTimeMillis() - first) + "ms" + ChatColor.GRAY + ".");
					return true;
				}
			}
		}
		return false;
	}

}

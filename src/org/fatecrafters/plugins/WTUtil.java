package org.fatecrafters.plugins;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashMap;

import org.bukkit.configuration.file.FileConfiguration;

public class WTUtil {

	public static HashMap<String,Long> data = new HashMap<String, Long>();
	public static HashMap<String,Long> cooldowns = new HashMap<String, Long>();
	public static HashMap<String,Long> timestamps = new HashMap<String, Long>();
	public static HashMap<String,Long> cooldownTimestamps = new HashMap<String, Long>();
	public static HashMap<String,Boolean> inEnabledWorld = new HashMap<String, Boolean>();

	private static WorldTime plugin;
	public static void setPlugin(WorldTime plugin) {
		WTUtil.plugin = plugin;
	}
	public static FileConfiguration getConfig() {
		return plugin.getConfig();
	}


	public static boolean checkIfInEnabledWorld(String w) {
		Collection<String> ConfigWorlds = plugin.getConfig().getConfigurationSection("Worlds").getKeys(false);
		for (String world : ConfigWorlds) {
			if (w.equalsIgnoreCase(world)) {
				return true;
			}
		}
		return false;
	}

	public static boolean checkIfExpired(String playerName, String world) {
		Long timer = WTUtil.data.get(playerName+":"+world);
		if (timer == null) {
			return false;
		} else if (System.currentTimeMillis() >= timer) {
			return true;
		}
		return false;
	}

	public static boolean checkIfOnCooldown(String playerName, String world) {
		Long cooldown = WTUtil.cooldowns.get(playerName+":"+world);
		if (cooldown == null) {
			return false;
		} else if (System.currentTimeMillis() < cooldown) {
			return true;
		}
		return false;
	}

	@SuppressWarnings("deprecation")
	public static void logoutTimestamp(final String playerName) {
		plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new Runnable() {
			public void run() {
				Collection<String> ConfigWorlds = getConfig().getConfigurationSection("Worlds").getKeys(false);
				for (String world : ConfigWorlds) {
					Long timer = WTUtil.data.get(playerName+":"+world);
					Long timestamp = WTUtil.timestamps.get(playerName+":"+world);
					if (!plugin.getConfig().getBoolean("Worlds."+world+".continueCooldownOnLogout")) {
						Long cooldown = WTUtil.cooldowns.get(playerName+":"+world);
						if (cooldown != null && cooldown > System.currentTimeMillis()) {
							WTUtil.cooldownTimestamps.put(playerName+":"+world, cooldown - System.currentTimeMillis());
							WTUtil.cooldowns.remove(playerName+":"+world);
						} else {
							WTUtil.cooldowns.remove(playerName+":"+world);
						}
					}
					if (timer != null && timer > System.currentTimeMillis()) {
						WTUtil.timestamps.put(playerName+":"+world, timer - System.currentTimeMillis());
						WTUtil.data.remove(playerName+":"+world);
					} else if (timestamp != null) {
						WTUtil.timestamps.put(playerName+":"+world, timestamp);
						WTUtil.data.remove(playerName+":"+world);	
					} else {
						WTUtil.timestamps.remove(playerName+":"+world);
						WTUtil.data.remove(playerName+":"+world);
					}
				}
			}
		}, 0L);
	}

	@SuppressWarnings("deprecation")
	public static void loginTimestamp(final String playerName) {
		plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new Runnable() {
			public void run() {
				Collection<String> ConfigWorlds = getConfig().getConfigurationSection("Worlds").getKeys(false);
				for (String world : ConfigWorlds) {
					Long timestamp = WTUtil.timestamps.get(playerName+":"+world);
					if (timestamp != null) {
						WTUtil.data.put(playerName+":"+world, System.currentTimeMillis() + timestamp);
						WTUtil.timestamps.remove(playerName+":"+world);
					}
					Long cooldownTimestamp = WTUtil.cooldownTimestamps.get(playerName+":"+world);
					if (cooldownTimestamp != null) {
						WTUtil.cooldowns.put(playerName+":"+world, cooldownTimestamp + System.currentTimeMillis());
						WTUtil.cooldownTimestamps.remove(playerName+":"+world);
					}
				}
			}
		}, 0L);
	}

	public static void changeWorldTimestamp(String playerName) {
		Collection<String> ConfigWorlds = getConfig().getConfigurationSection("Worlds").getKeys(false);
		for (String world : ConfigWorlds) {
			Long timer = WTUtil.data.get(playerName+":"+world);
			if (timer != null) {
				WTUtil.timestamps.put(playerName+":"+world, (timer - System.currentTimeMillis()));
			}
		}
	}

	public static void readData(String filename, HashMap<String, Long> hashmap) {
		File datafile = new File(plugin.getDataFolder()+File.separator+"data"+File.separator+filename);
		if (!datafile.exists()) {
			try {
				datafile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			try	{
				BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(datafile)));
				String l;
				while((l = br.readLine()) != null)
				{
					String[] args = l.split("[,]", 2);
					if(args.length != 2)continue;
					String p = args[0].replaceAll(" ", "");
					String b = args[1].replaceAll(" ", "");
					Long time = Long.parseLong(b);
					hashmap.put(p, time);
				}
				br.close();
			} catch (IOException e){
				plugin.getLogger().severe("[WorldTime] " + filename +" has an error!");
			}
		}
	}

	public static void writeData(String filename, HashMap<String, Long> hashmap) {
		File datafile = new File(plugin.getDataFolder()+File.separator+"data"+File.separator+filename);
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(datafile));
			PrintWriter writer = new PrintWriter(datafile);
			writer.print("");
			writer.flush();
			writer.close();
			for(String datastring : hashmap.keySet()) {
				bw.write(datastring + "," + hashmap.get(datastring));
				bw.newLine();
			}
			bw.flush();
			bw.close();
		} catch (IOException e){
			plugin.getLogger().severe("[WorldTime] " + filename +" has an error!");
		}
	}

}

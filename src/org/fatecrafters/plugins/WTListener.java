package org.fatecrafters.plugins;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class WTListener implements Listener {

	private final WorldTime plugin;

	public WTListener(WorldTime plugin) {
		this.plugin = plugin;
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onPlayerTeleportEvent(PlayerTeleportEvent e) {
		String toWorld = e.getTo().getWorld().getName();
		String p = e.getPlayer().getName();
		if (!e.getPlayer().hasPermission("worldtimer.bypass")) {
			if (WTUtil.checkIfInEnabledWorld(toWorld)) {
				if (WTUtil.checkIfOnCooldown(p, toWorld)) {
					String cooldownmsg = WTUtil.getConfig().getString("Worlds."+toWorld+".onCooldownMessage").replace("$cooldown", ""+((WTUtil.cooldowns.get(p+":"+toWorld) - System.currentTimeMillis()) / 1000 / 60));
					e.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', cooldownmsg));
					e.setCancelled(true);
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onPlayerPortalEvent(PlayerPortalEvent e) {
		String toWorld = e.getTo().getWorld().getName();
		String p = e.getPlayer().getName();
		if (!e.getPlayer().hasPermission("worldtimer.bypass")) {
			if (WTUtil.checkIfInEnabledWorld(toWorld)) {
				if (WTUtil.checkIfOnCooldown(p, toWorld)) {
					String cooldownmsg = WTUtil.getConfig().getString("Worlds."+toWorld+".onCooldownMessage").replace("$cooldown", ""+((WTUtil.cooldowns.get(p+":"+toWorld) - System.currentTimeMillis()) / 1000 / 60));
					e.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', cooldownmsg));
					e.setCancelled(true);
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onPlayerChangeWorld(PlayerChangedWorldEvent e) {
		Player player = e.getPlayer();
		if (!player.hasPermission("worldtimer.bypass")) {
			String p = player.getName();
			String world = player.getWorld().getName();
			changedWorldTask(player, p, world);
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onLogOut(PlayerQuitEvent e) {
		Player p = e.getPlayer();
		if (!p.hasPermission("worldtimer.bypass")) {
			WTUtil.logoutTimestamp(p.getName());
			WTUtil.inEnabledWorld.remove(p.getName());
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onJoin(PlayerJoinEvent e) {
		Player p = e.getPlayer();
		if (!p.hasPermission("worldtimer.bypass")) {
			String pname = p.getName();
			String world = p.getWorld().getName();
			joinTask(p, world, pname);
		}
	}

	@SuppressWarnings("deprecation")
	private void joinTask(final Player p, final String world, final String pname) {
		plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new Runnable() {
			public void run() {
				WTUtil.loginTimestamp(pname);
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				if (WTUtil.checkIfInEnabledWorld(world)) {
					if (WTUtil.data.get(pname+":"+world) == null) {
						WTUtil.data.put(pname+":"+world, System.currentTimeMillis() + WTUtil.getConfig().getLong("Worlds."+world+".timer")*1000);
						p.sendMessage(ChatColor.translateAlternateColorCodes('&', WTUtil.getConfig().getString("Worlds."+world+".timerAddedMessage")));
					}
					WTUtil.inEnabledWorld.put(pname, true);
				} else {
					WTUtil.inEnabledWorld.put(pname, false);
				}
				if (WTUtil.cooldowns.get(pname+":"+world) != null && WTUtil.cooldowns.get(pname+":"+world) >= System.currentTimeMillis()) {
					WTUtil.cooldowns.remove(pname+":"+world);
				}
			}
		}, 0L);
	}
	
	@SuppressWarnings("deprecation")
	private void changedWorldTask(final Player player, final String p, final String world) {
		plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new Runnable() {
			public void run() {
				if (WTUtil.checkIfInEnabledWorld(world)) {
					Long timestamp = WTUtil.timestamps.get(p+":"+world);
					if (timestamp != null) {
						WTUtil.data.put(p+":"+world, (System.currentTimeMillis() + timestamp));
						WTUtil.inEnabledWorld.put(p, true);
						player.sendMessage(ChatColor.translateAlternateColorCodes('&', WTUtil.getConfig().getString("Worlds."+world+".returnMessage").replace("$time", ""+((WTUtil.data.get(p+":"+world) - System.currentTimeMillis()) / 1000 / 60))));
					} else {
						WTUtil.data.put(p+":"+world, System.currentTimeMillis() + WTUtil.getConfig().getLong("Worlds."+world+".timer")*1000);
						WTUtil.inEnabledWorld.put(p, true);
						player.sendMessage(ChatColor.translateAlternateColorCodes('&', WTUtil.getConfig().getString("Worlds."+world+".timerAddedMessage")));
					}
				} else {
					WTUtil.changeWorldTimestamp(p);
					WTUtil.inEnabledWorld.put(p, false);
				}
			}
		}, 0L);
	}

}

package org.fatecrafters.plugins;

import org.bukkit.ChatColor;
import org.bukkit.World;
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

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onPlayerTeleportEvent(PlayerTeleportEvent e) {
		World toWorld = e.getTo().getWorld();
		Player p = e.getPlayer();
		if (!p.hasPermission("worldtimer."+toWorld.getName()+".bypass")) {
			if (WTUtil.checkIfInEnabledWorld(toWorld)) {
				if (WTUtil.getConfig().getInt("Worlds."+toWorld.getName()+".cooldown") > 0) {
					if (WTUtil.checkIfOnCooldown(p.getName(), toWorld)) {
						String cooldownmsg = WTUtil.getConfig().getString("Worlds."+toWorld.getName()+".onCooldownMessage").replace("$cooldown", ""+((WTUtil.cooldowns.get(p.getName()+":"+toWorld.getName()) - System.currentTimeMillis()) / 1000 / 60));
						p.sendMessage(ChatColor.translateAlternateColorCodes('&', cooldownmsg));
						e.setCancelled(true);
					}
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onPlayerPortalEvent(PlayerPortalEvent e) {
		String toWorld = e.getTo().getWorld().getName();
		Player p = e.getPlayer();
		if (!p.hasPermission("worldtimer."+toWorld+".bypass")) {
			if (WTUtil.checkIfInEnabledWorld(p.getWorld())) {
				if (WTUtil.checkIfOnCooldown(p.getName(), p.getWorld())) {
					String cooldownmsg = WTUtil.getConfig().getString("Worlds."+toWorld+".onCooldownMessage").replace("$cooldown", ""+((WTUtil.cooldowns.get(p.getName()+":"+toWorld) - System.currentTimeMillis()) / 1000 / 60));
					p.sendMessage(ChatColor.translateAlternateColorCodes('&', cooldownmsg));
					e.setCancelled(true);
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onPlayerChangeWorld(PlayerChangedWorldEvent e) {
		Player player = e.getPlayer();
		String p = player.getName();
		String world = player.getWorld().getName();
		if (!player.hasPermission("worldtimer."+world+".bypass")) {
			if (WTUtil.checkIfInEnabledWorld(player.getWorld())) {
				Long timestamp = WTUtil.timestamps.get(p+":"+world);
				if (timestamp != null) {
					WTUtil.data.put(p+":"+world, (System.currentTimeMillis() + timestamp));
					WTUtil.inEnabledWorld.put(p, true);
					player.sendMessage(ChatColor.translateAlternateColorCodes('&', WTUtil.getConfig().getString("Worlds."+world+".returnMessage").replace("$time", ""+((WTUtil.data.get(p+":"+world) - System.currentTimeMillis()) / 1000 / 60))));
				} else  {
					WTUtil.data.put(p+":"+world, System.currentTimeMillis() + WTUtil.getConfig().getLong("Worlds."+world+".timer")*1000);
					WTUtil.inEnabledWorld.put(p, true);
					player.sendMessage(ChatColor.translateAlternateColorCodes('&', WTUtil.getConfig().getString("Worlds."+world+".timerAddedMessage")));
				}
			} else {
				WTUtil.changeWorldTimestamp(p);
				WTUtil.inEnabledWorld.put(p, false);
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onLogOut(PlayerQuitEvent e) {
		Player p = e.getPlayer();
		WTUtil.logoutTimestamp(p.getName());
		WTUtil.inEnabledWorld.remove(p.getName());
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onJoin(PlayerJoinEvent e) {
		Player p = e.getPlayer();
		World world = p.getWorld();
		WTUtil.loginTimestamp(p.getName());
		if (!p.hasPermission("worldtimer."+world.getName()+".bypass")) {
			if (WTUtil.checkIfInEnabledWorld(world)) {
				if (WTUtil.data.get(p.getName()+":"+world.getName()) == null) {
					WTUtil.data.put(p.getName()+":"+world.getName(), System.currentTimeMillis() + WTUtil.getConfig().getLong("Worlds."+world.getName()+".timer")*1000);
					p.sendMessage(ChatColor.translateAlternateColorCodes('&', WTUtil.getConfig().getString("Worlds."+world.getName()+".timerAddedMessage")));
				}
				WTUtil.inEnabledWorld.put(p.getName(), true);
			} else {
				WTUtil.inEnabledWorld.put(p.getName(), false);
			}
		}
	}

}

package me.nashplugz.spectrab;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class SpectraListener implements Listener {
    private final SpectraBroadcast plugin;

    public SpectraListener(SpectraBroadcast plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        plugin.broadcastMessage("Welcome " + event.getPlayer().getName() + " to the server!");
    }
}
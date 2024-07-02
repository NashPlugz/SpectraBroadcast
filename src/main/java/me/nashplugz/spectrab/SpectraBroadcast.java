package me.nashplugz.spectrab;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SpectraBroadcast extends JavaPlugin {
    private final List<BukkitTask> scheduledTasks = new ArrayList<>();
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private String prefix;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadPrefix();
        scheduleBroadcasts();
        getLogger().info("SpectraBroadcast has been enabled!");
        getServer().getPluginManager().registerEvents(new SpectraListener(this), this);
    }

    @Override
    public void onDisable() {
        getLogger().info("SpectraBroadcast has been disabled!");
        cancelScheduledTasks();
    }

    private void loadPrefix() {
        FileConfiguration config = getConfig();
        prefix = config.getString("prefix", "<bold>[Broadcast]</bold>");
    }

    private void scheduleBroadcasts() {
        cancelScheduledTasks();
        FileConfiguration config = getConfig();
        List<Map<?, ?>> broadcasts = config.getMapList("broadcasts");

        for (Map<?, ?> broadcast : broadcasts) {
            String intervalString = (String) broadcast.get("interval");
            String message = (String) broadcast.get("message");
            long interval = parseInterval(intervalString);

            BukkitTask task = new BukkitRunnable() {
                @Override
                public void run() {
                    broadcastMessage(message);
                }
            }.runTaskTimer(this, interval * 20L, interval * 20L);

            scheduledTasks.add(task);
        }
    }

    private void cancelScheduledTasks() {
        for (BukkitTask task : scheduledTasks) {
            if (task != null) {
                task.cancel();
            }
        }
        scheduledTasks.clear();
    }

    public void broadcastMessage(String message) {
        Component prefixComponent = miniMessage.deserialize(prefix);
        Component messageComponent = miniMessage.deserialize(message);
        Component combined = prefixComponent.append(Component.space()).append(messageComponent);

        getServer().getOnlinePlayers().forEach(player -> {
            player.sendMessage(combined);
        });
    }

    private long parseInterval(String intervalString) {
        Pattern pattern = Pattern.compile("^(\\d+)([smh])$");
        Matcher matcher = pattern.matcher(intervalString);
        if (matcher.matches()) {
            int value = Integer.parseInt(matcher.group(1));
            char unit = matcher.group(2).charAt(0);
            switch (unit) {
                case 's':
                    return value;
                case 'm':
                    return value * 60;
                case 'h':
                    return value * 3600;
                default:
                    throw new IllegalArgumentException("Invalid interval unit: " + unit);
            }
        } else {
            throw new IllegalArgumentException("Invalid interval format: " + intervalString);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("spectrareload")) {
            if (sender.hasPermission("spectrabroadcast.reload")) {
                reloadConfig();
                loadPrefix();
                scheduleBroadcasts();
                sendReloadMessage(sender);
                getLogger().log(Level.INFO, "SpectraBroadcast configuration reloaded by " + sender.getName());
            } else {
                sender.sendMessage("You do not have permission to use this command.");
            }
            return true;
        }
        return false;
    }

    private void sendReloadMessage(CommandSender sender) {
        String reloadMessage = "Reloaded";
        Component prefixComponent = miniMessage.deserialize(prefix);
        Component messageComponent = miniMessage.deserialize(reloadMessage);
        Component combined = prefixComponent.append(Component.space()).append(messageComponent);

        sender.sendMessage(combined);
    }
}
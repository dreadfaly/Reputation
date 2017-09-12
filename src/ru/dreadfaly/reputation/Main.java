package ru.dreadfaly.reputation;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class Main extends JavaPlugin implements Listener{

    public Manager data = new Manager("data.db");

    public void onEnable() {
        File config = new File(getDataFolder() + File.separator + "config.yml"); // config
        if(!config.exists()) {
            getLogger().info("Creating new config..."); // сообщение в консоль
            getConfig().options().copyDefaults(true);
            saveDefaultConfig(); // сохранение конфига
        }
        getCommand("rep").setExecutor(new Reputation(this));
        getCommand("reputation").setExecutor(new Reputation(this));
        getCommand("changerep").setExecutor(new ChangeReputationAdmin(this));
        getCommand("changereputation").setExecutor(new ChangeReputationAdmin(this));
        getServer().getPluginManager().registerEvents(this, this);
    }

    public void onDisable() {
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        String playerString = player.getName();
        if (data.read(playerString) == null) {
            data.write(playerString, 0);
            player.sendMessage(ChatColor.DARK_AQUA + "[Reputation] " + ChatColor.GRAY +"You have " + 0 + " reputation points.");
        } else {
            int point = (int) data.read(playerString);
            if (point>=1) player.sendMessage(ChatColor.DARK_AQUA + "[Reputation] " + ChatColor.GREEN +"You have " + point + " reputation points.");
            if (point<=-1) player.sendMessage(ChatColor.DARK_AQUA + "[Reputation] " + ChatColor.RED +"You have " + point + " reputation points.");
            else if (point==0) player.sendMessage(ChatColor.DARK_AQUA + "[Reputation] " + ChatColor.GRAY +"You have " + point + " reputation points.");
        }
    }
}
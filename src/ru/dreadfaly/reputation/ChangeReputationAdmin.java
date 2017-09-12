package ru.dreadfaly.reputation;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ChangeReputationAdmin implements CommandExecutor {
    private Main plugin;

    public ChangeReputationAdmin(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length != 1 && args.length != 2) { // проверка аргументов
            sender.sendMessage(ChatColor.RED + "Use command: /changerep <player> <reputation points>");
            return true;
        }
        String player = args[0];
        int points = Integer.parseInt(args[1]);
        if (!(sender instanceof Player)) { // проверка является ли отправитель игроком
            sender.sendMessage("[Reputation] Sorry, you console!"); // если нет, то пишется это сообщение.
            return true;
        } else {
            if (!sender.hasPermission("reputation.admin")) { // проверка есть ли у отправителя права
                sender.sendMessage(ChatColor.DARK_AQUA + "[Reputation] " + ChatColor.RED + "You dont permissions.");
                return true;
            }
            if (plugin.data.read(player) == null) { // существует ли игрок в базе данных
                plugin.data.write(player, points); // если нет, то заносят его в неё и новое значение
                sender.sendMessage(ChatColor.DARK_AQUA + "[Reputation] " + ChatColor.GREEN + "You have changed the player's points " + player + " on: " + points);
                return true;
            } else {
                plugin.data.write(player, points);
                sender.sendMessage(ChatColor.DARK_AQUA + "[Reputation] " + ChatColor.GREEN + "You have changed the player's points " + player + " on: " + points);
                return true;
            }
        }
    }
}
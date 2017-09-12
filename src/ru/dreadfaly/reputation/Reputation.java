package ru.dreadfaly.reputation;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;


public class Reputation implements CommandExecutor {
    private Main plugin;
    private String thmycvm = "thmycvm"; // through how much you can vote more

    public Reputation(Main plugin) {
        this.plugin = plugin;
    }
    private HashMap<Player, Long> checkTime = new HashMap<Player, Long>();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length != 1 && args.length != 2) { // проверка аргументов
            sender.sendMessage(ChatColor.RED + "Use command: /rep <player> <+/->");
            return true;
        }
        String player = args[0]; // кому будет идти начисление баллов репутации (текстовый игрок)
        if (args.length == 1) {
            if (!sender.hasPermission("reputation.info")){ // проверка разрешения
                sender.sendMessage(ChatColor.DARK_AQUA + "[Reputation] " + ChatColor.RED + "You dont permissions.");
                return true;
            }
            if (plugin.data.read(player) == null) { // проверка существования игрока в базе данных
                sender.sendMessage(ChatColor.DARK_AQUA + "[Reputation] " + ChatColor.GRAY + "The player " + player + " has 0 reputation points.");
                plugin.data.write(player, 0); // вносит игрока в базу данных
                return true;
            }
            int repPlayer = (int) plugin.data.read(player); // переменная, получаемая очки репутации игрока
            if (repPlayer==0){ // проверка на 0 очков репутации игрока
                sender.sendMessage(ChatColor.DARK_AQUA + "[Reputation] " + ChatColor.GRAY +"The player " + player + " has " + repPlayer + " reputation points.");
                return true;
            }
            String gold = "gold"; // строка, которая используется конфигом для назначения золотого статуса.
            if (!plugin.getConfig().contains(gold)) { // существует ли в конфиге строка gold
                plugin.getConfig().set(gold, 1000); // если не существует, то внести её с значением 1000.
            }
            int goldConfig = plugin.getConfig().getInt(gold); // получение значение из строки gold в конфиге.
            if (repPlayer>=goldConfig){ // проверка кол-во очков для выдачи золотого статуса.
                sender.sendMessage(ChatColor.DARK_AQUA + "[Reputation] " + ChatColor.GOLD + "[GOLD] " + ChatColor.GOLD + "The player " + player + " has " + repPlayer + " reputation points.");
                return true;
            }
            if (repPlayer>=1) { // если количество репутации было больше/равно 1, то выводило зеленое сообщение.
                sender.sendMessage(ChatColor.DARK_AQUA + "[Reputation] " + ChatColor.GREEN + "The player " + player + " has " + repPlayer + " reputation points.");
                return true;
            } else { // если проверка дает false, то выводится красное сообщение.
                sender.sendMessage(ChatColor.DARK_AQUA + "[Reputation] " + ChatColor.RED + "The player " + player + " has " + repPlayer + " reputation points.");
                return true;
            }
        }

        Player playerObject = Bukkit.getServer().getPlayer(player); // создание физического игрока
        if (playerObject == null) { // проверка наличие игрока на сервере.
            sender.sendMessage(ChatColor.DARK_AQUA + "[Reputation] " + ChatColor.GRAY + "Player " + player + " is offline.");
            return true;
        }

        if (sender == playerObject) { // проверяем причастность сендера к игроку, чтобы не дать самому себе голос.
            sender.sendMessage(ChatColor.DARK_AQUA + "[Reputation] " + ChatColor.RED + "You can not vote for yourself.");
            return true;
        }

        switch (args[1]) {
            case "-":
                voteNegatively(sender, player);
                return true;
            case "+":
                votePositively(sender, player);
                return true;
            default:
                return false;
        }
    }

    private boolean voteNegatively(CommandSender sender, String target) { // target - игрок, которому хотят изменить репутацию.
        if (!plugin.getConfig().contains(thmycvm)) { // проверка существования в конфиге thmycvm
            plugin.getConfig().set(thmycvm, 60); // вносит стандартное количество (60 минут)
        }

        int thmycvmInt = plugin.getConfig().getInt(thmycvm); // получение значения из конфига.
        long timeMil = thmycvmInt * 60000; // перевод часов в миллисекунды

        if (!(sender instanceof  Player)) { // проверка является ли отправитель игроком
            sender.sendMessage("[Reputation] Sorry, you console!"); // если нет, то пишется это сообщение.
        } else {
            if (!sender.hasPermission("reputation.give")){ // проверка есть ли у отправителя права
                sender.sendMessage(ChatColor.DARK_AQUA + "[Reputation] " + ChatColor.RED + "You dont permissions.");
                return true;
            }
            Player player = (Player) sender; // сделать из отправителя физического игрока.
            long timeNow = System.currentTimeMillis(); // текущее время

            if (!checkTime.containsKey(player) || timeNow - checkTime.get(player) > timeMil) { // отсутствует ли игрок в HashMap / прошло ли время заданное в конфиге.
                checkTime.put(player, timeNow); // заносит в HashMap игрока и его текущее время
                if (plugin.data.read(target) == null) { // существует ли игрок в базе данных
                    plugin.data.write(target, -1); // если нет, то заносят его в неё и новое значение
                    checkTime.put(player, timeNow); // заносят в HashMap игрока и его текущее время
                    plugin.saveConfig(); // сохраняет конфиг
                    sender.sendMessage(ChatColor.DARK_AQUA + "[Reputation] " + ChatColor.RED + "You voted for the player " + target + " negatively.");
                    return true;
                }
                int repPlayer = (int) plugin.data.read(target); // получаем количество репутации игрока
                sender.sendMessage(ChatColor.DARK_AQUA + "[Reputation] " + ChatColor.RED + "You voted for the player " + target + " negatively.");
                repPlayer--; // изменяет репутацию игрока
                plugin.data.write(target, repPlayer); // заносит в базу данных репутацию игрока
                plugin.saveConfig(); // сохраняет конфиг
                return true;
            } else {
                if (timeNow - checkTime.get(player) < timeMil){ // проверка прошло ли время..
                    long c = timeMil -(timeNow - checkTime.get(player));
                    int f = (int) (c / 60000); // действия, чтобы получить переменную с оставшимся временем.
                    sender.sendMessage(ChatColor.DARK_AQUA + "[Reputation] " + ChatColor.RED + "You voted for someone in the last hour. Left: " + f + " min");
                } else {
                    sender.sendMessage(ChatColor.DARK_AQUA + "[Reputation] " + ChatColor.RED + "We have registered you in the reputation system. Try to vote in an hour.");
                    checkTime.put(player, timeNow); // заносит игрока в базу данных, если ничего выше не подошло.
                }
            }
        }
        return true;
    }

    private boolean votePositively(CommandSender sender, String target) { // target - игрок, которому хотят изменить репутацию.
        if (!plugin.getConfig().contains(thmycvm)) { // проверка существования в конфиге thmycvm
            plugin.getConfig().set(thmycvm, 60); // вносит стандартное количество (60 минут)
        }

        int thmycvmInt = plugin.getConfig().getInt(thmycvm); // получение значения из конфига.
        long timeMil = thmycvmInt * 60000; // перевод часов в миллисекунды

        if (!(sender instanceof  Player)) { // проверка является ли отправитель игроком
            sender.sendMessage("[Reputation] Sorry, you console!"); // если нет, то пишется это сообщение.
        } else {
            if (!sender.hasPermission("reputation.give")){ // проверка есть ли у отправителя права
                sender.sendMessage(ChatColor.DARK_AQUA + "[Reputation] " + ChatColor.RED + "You dont permissions.");
                return true;
            }
            Player player = (Player) sender; // сделать из отправителя физического игрока.
            long timeNow = System.currentTimeMillis(); // текущее время

            if (!checkTime.containsKey(player) || timeNow - checkTime.get(player) > timeMil) { // отсутствует ли игрок в HashMap / прошло ли время заданное в конфиге.
                checkTime.put(player, timeNow); // заносит в HashMap игрока и его текущее время
                if (plugin.data.read(target) == null) { // существует ли игрок в базе данных
                    plugin.data.write(target, 1); // если нет, то заносят его в неё и новое значение
                    checkTime.put(player, timeNow); // заносят в HashMap игрока и его текущее время
                    plugin.saveConfig(); // сохраняет конфиг
                    sender.sendMessage(ChatColor.DARK_AQUA + "[Reputation] " + ChatColor.RED + "You voted for the player " + target + " positively.");
                    return true;
                }
                int repPlayer = (int) plugin.data.read(target); // получаем количество репутации игрока
                sender.sendMessage(ChatColor.DARK_AQUA + "[Reputation] " + ChatColor.RED + "You voted for the player " + target + " positively.");
                repPlayer++; // изменяет репутацию игрока
                plugin.data.write(target, repPlayer); // заносит в базу данных репутацию игрока
                plugin.saveConfig(); // сохраняет конфиг
                return true;
            } else {
                if (timeNow - checkTime.get(player) < timeMil){ // проверка прошло ли время..
                    long c = timeMil -(timeNow - checkTime.get(player));
                    int f = (int) (c / 60000); // действия, чтобы получить переменную с оставшимся временем.
                    sender.sendMessage(ChatColor.DARK_AQUA + "[Reputation] " + ChatColor.RED + "You voted for someone in the last hour. Left: " + f + " min");
                } else {
                    sender.sendMessage(ChatColor.DARK_AQUA + "[Reputation] " + ChatColor.RED + "We have registered you in the reputation system. Try to vote in an hour.");
                    checkTime.put(player, timeNow); // заносит игрока в базу данных, если ничего выше не подошло.
                }
            }
        }
        return true;
    }
}

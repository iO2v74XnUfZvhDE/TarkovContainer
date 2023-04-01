package me.io2.tarkovcontainer;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.StringUtil;
import org.ipvp.canvas.MenuFunctionListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public final class TarkovContainer extends JavaPlugin {
    private static JavaPlugin plugin;
    public static JavaPlugin getPlugin() {
        return plugin;
    }
    public static final List<String> itemBlackList = new ArrayList<>();

    private static boolean isStarting = false;

    @Override
    public void onEnable() {
        plugin = this;
        isStarting = true;
        // Plugin startup logic
        getLogger().info("Hi!");
        Bukkit.getPluginManager().registerEvents(new MenuFunctionListener(), plugin);
        Bukkit.getPluginManager().registerEvents(new ContainerListener(), plugin);

        saveDefaultConfig();
        Thread thread = new Thread(() -> {
            try {
                while (isStarting) {
                    getLogger().info("Saving data...");
                    ContainerListener.containerDataHashMap.forEach((uuid, containerData) -> {
                        int stackPos = 0;
                        for (ItemStack stack : containerData.stacks) {
                            TarkovContainer.getPlugin().getConfig().set(uuid + "." + stackPos, stack);
                            stackPos += 1;
                        }

                        boolean contains = ContainerListener.yamiotiPlayerList.contains(uuid);
                        TarkovContainer.getPlugin().getConfig().set(uuid + ".yamioti", contains);
                    });

                    saveConfig();
                    Thread.sleep(60000);
                }
            } catch (InterruptedException ignored) {
            }
        });
        thread.start();

        itemBlackList.clear();
        itemBlackList.addAll(getConfig().getStringList("blacklist"));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("setyamioti")) {
            if (args.length == 0) {
                sender.sendMessage("§cUsage: setyamioti <playername> (yes/no)");
                return false;
            } else {
                Player player = Bukkit.getPlayer(args[0]);
                if (player != null) {
                    if (args.length > 1) {
                        if (args[1].equalsIgnoreCase("on")) {
                            if (ContainerListener.yamiotiPlayerList.contains(player.getUniqueId())) {
                                sender.sendMessage("プレイヤー" + player.getName() + "は闇落ちしています。");
                            } else {
                                ContainerListener.yamiotiPlayerList.add(player.getUniqueId());
                                sender.sendMessage("プレイヤー" + player.getName() + "は闇落ちしました。");
                            }
                        } else if (args[1].equalsIgnoreCase("off")) {
                            ContainerListener.yamiotiPlayerList.remove(player.getUniqueId());
                            sender.sendMessage("プレイヤー" + player.getName() + "は闇落ちではなくなりました。");
                        } else {
                            if (ContainerListener.yamiotiPlayerList.contains(player.getUniqueId())) {
                                sender.sendMessage("プレイヤー" + player.getName() + "は闇落ちしています。");
                            } else {
                                sender.sendMessage("プレイヤー" + player.getName() + "は闇落ちしていません。");
                            }
                        }
                    } else {
                        if (ContainerListener.yamiotiPlayerList.contains(player.getUniqueId())) {
                            sender.sendMessage("プレイヤー" + player.getName() + "は闇落ちしています。");
                        } else {
                            sender.sendMessage("プレイヤー" + player.getName() + "は闇落ちしていません。");
                        }
                    }
                    return true;
                } else {
                    sender.sendMessage("Player not found.");
                    return false;
                }
            }
        }

        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> result = new ArrayList<>();

        if (command.getName().equalsIgnoreCase("setyamioti")) {
            if (args.length > 1) {
                StringUtil.copyPartialMatches(args[1], Arrays.asList("on", "off"), result);
                return result;
            } else if (args.length == 1) {
                Collection<? extends Player> onlinePlayers = Bukkit.getOnlinePlayers();
                List<String> lists = new ArrayList<>();
                for (Player onlinePlayer : onlinePlayers) {
                    lists.add(onlinePlayer.getName());
                }
                StringUtil.copyPartialMatches(args[0], lists, result);
                return result;
            }
        }
        return new ArrayList<>();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        isStarting = false;
        ContainerListener.containerDataHashMap.forEach((uuid, containerData) -> {
            int stackPos = 0;
            for (ItemStack stack : containerData.stacks) {
                TarkovContainer.getPlugin().getConfig().set(uuid + "." + stackPos, stack);
                stackPos += 1;
            }

            boolean contains = ContainerListener.yamiotiPlayerList.contains(uuid);
            TarkovContainer.getPlugin().getConfig().set(uuid + ".yamioti", contains);
        });

        saveConfig();
    }
}

package com.steamsworld.plugins.ratio.command;

import com.google.common.collect.Maps;
import com.steamsworld.plugins.ratio.RatioPlugin;
import com.steamsworld.plugins.ratio.ratio.Ratio;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author Steamworks (Steamworks#1127)
 * Sunday 16 2022 (10:25 PM)
 * Ratio (com.steamsworld.plugins.ratio.command)
 */
@SuppressWarnings("NullableProblems")
public class RatioCommand implements TabExecutor {

    private final RatioPlugin plugin;
    private final Map<UUID, Long> cooldowns = Maps.newHashMap();
    private boolean shouldCooldown;

    public RatioCommand(RatioPlugin plugin) {
        this.plugin = plugin;
        this.shouldCooldown = plugin.getConfig().getBoolean("cooldown.enabled");

        Objects.requireNonNull(plugin.getCommand("ratio")).setExecutor(this);
        Objects.requireNonNull(plugin.getCommand("ratio")).setTabCompleter(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        plugin.getLogger().info(Arrays.toString(args));

        if(!(sender instanceof Player)) {
            sender.sendMessage(plugin.getMessage("non-player"));
            return false;
        }


        Player player = (Player) sender;

        if(args.length == 0) {
            player.sendMessage(plugin.getMessage("incorrect-usage"));
            return false;
        }

        if(args.length == 3 && args[0].equalsIgnoreCase("vote")) {
            // ratio vote Steamworks true
            Player target = Bukkit.getPlayer(args[1]);
            if(target == null || !target.isOnline()) {
                sender.sendMessage(plugin.getMessage("invalid-player").replace("{name}", args[1]));
                return false;
            }

            if(!plugin.getRatioHandler().hasPendingRatio(player)) {
                player.sendMessage(plugin.getMessage("no-ratio"));
                return false;
            }

            Ratio ratio = plugin.getRatioHandler().get(player);

            if(ratio.alreadyVoted(player.getUniqueId())) {
                player.sendMessage(plugin.getMessage("already-voted"));
                return true;
            }

            if(!(Arrays.asList("true", "false").contains(args[2].toLowerCase()))) {
                player.sendMessage(plugin.getMessage("incorrect-usage"));
                return true;
            }

            boolean ratiod = Boolean.parseBoolean(args[2]);
            player.sendMessage(plugin.getMessage("ratio-vote").replace("{name}", target.getName()));
            plugin.getRatioHandler().increment(
                    ratio,
                    player,
                    Ratio.RatioVote.valueOf(ratiod ? "POSITIVE" : "NEGATIVE")
            );

            return true;
        }

        if(cooldowns.containsKey(player.getUniqueId()) && System.currentTimeMillis() < cooldowns.get(player.getUniqueId())) {
            player.sendMessage(plugin.getMessage("cooldown").replace("{time}", millisToRoundedTime(cooldowns.get(player.getUniqueId()) - System.currentTimeMillis())));
            return false;
        }

        if(args.length != 1) {
            if(args[0].equalsIgnoreCase("vote")) {
                player.sendMessage(plugin.getMessage("incorrect-usage"));
                return false;
            }

            return false;
        }

        if(plugin.getRatioHandler().existsCreatedBy(player)) {
            player.sendMessage(plugin.getMessage("pending-ratio"));
            return false;
        }

        if(plugin.getConfig().getBoolean("permission.enabled") && !player.hasPermission(plugin.getConfig().getString("permission.node", "ratio.permission"))) {
            player.sendMessage(plugin.getMessage("no-permission"));
            return false;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if(target == null || !target.isOnline()) {
            player.sendMessage(plugin.getMessage("invalid-player").replace("{name}", args[0]));
            return false;
        }

        if(plugin.getRatioHandler().hasPendingRatio(target)) {
            player.sendMessage(plugin.getMessage("target-exists").replace("{name}", args[0]));
            return false;
        }

        Arrays.asList(
                new ComponentBuilder(ChatColor.translateAlternateColorCodes('&', plugin.getMessage("ratio-header")).replace("{player}", target.getName()))
                        .create(),
                new ComponentBuilder(ChatColor.GREEN + "" + ChatColor.BOLD + "[RATIO]")
                        .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, String.format("/ratio vote %s true", target.getName())))
                        .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(ChatColor.GREEN + "" + ChatColor.BOLD + "RATIO! Ratio this mineman.")))
                        .create(),
                new ComponentBuilder("" + ChatColor.RED + "" + ChatColor.BOLD + "[BE KIND]")
                        .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, String.format("/ratio vote %s false", target.getName())))
                        .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(ChatColor.RED + "" + ChatColor.BOLD + "BE KIND! Respect this mineman.")))
                        .create()
        ).forEach((component) -> player.spigot().sendMessage(component));

        Ratio ratio = plugin.getRatioHandler().add(player, target);
        player.sendMessage(plugin.getMessage("ratio-sent").replace("{name}", target.getName()));
        Bukkit.getServer().getScheduler().runTaskLater(plugin, () -> {
            int positive = ratio.getPositive(), negative = ratio.getNegative();
            plugin.getRatioHandler().remove(ratio);

            String message = (positive == 0 && negative == 0) ? "no-voters" : (positive > negative) ? "successfully-ratiod" : "failed-ratiod";
            Bukkit.broadcastMessage(plugin.getMessage(message).replace("{player}", target.getName()).replace("{players}", "" + positive));
        }, plugin.getConfig().getInt("end-after") * 20L);

        if(shouldCooldown)
            cooldowns.put(player.getUniqueId(), System.currentTimeMillis() + (TimeUnit.SECONDS.toMillis(plugin.getConfig().getInt("cooldown.time"))));
        return true;
    }

    private String millisToRoundedTime(long millis) {
        millis += 1L;

        long seconds = millis / 1000L;
        long minutes = seconds / 60L;
        long hours = minutes / 60L;
        long days = hours / 24L;
        long weeks = days / 7L;
        long months = weeks / 4L;
        long years = months / 12L;

        if(years > 0)
            return years + " year" + (years == 1 ? "" : "s");
        else if(months > 0)
            return months + " month" + (months == 1 ? "" : "s");
        else if(weeks > 0)
            return weeks + " week" + (weeks == 1 ? "" : "s");
        else if(days > 0)
            return days + " day" + (days == 1 ? "" : "s");
        else if(hours > 0)
            return hours + " hour" + (hours == 1 ? "" : "s");
        else if(minutes > 0)
            return minutes + " minute" + (minutes == 1 ? "" : "s");
        else
            return seconds + " second" + (seconds == 1 ? "" : "s");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        return args.length == 0 ? Bukkit.getServer().getOnlinePlayers().stream().map(HumanEntity::getName).collect(Collectors.toList()) : Collections.emptyList();
    }
}

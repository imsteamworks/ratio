package com.steamsworld.plugins.ratio.api;

import com.steamsworld.plugins.ratio.ratio.Ratio;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

/**
 * @author Steamworks (Steamworks#1127)
 * Sunday 16 2022 (10:20 PM)
 * Ratio (com.steamsworld.plugins.ratio.api)
 */
@Getter
public class PlayerRatioEvent extends PlayerEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Ratio ratio;
    private final Ratio.RatioVote vote;

    public PlayerRatioEvent(Player player, Ratio ratio, Ratio.RatioVote vote) {
        super(player);

        this.ratio = ratio;
        this.vote = vote;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}

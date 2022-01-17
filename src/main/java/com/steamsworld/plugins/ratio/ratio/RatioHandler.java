package com.steamsworld.plugins.ratio.ratio;

import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Steamworks (Steamworks#1127)
 * Sunday 16 2022 (10:15 PM)
 * Ratio (com.steamsworld.plugins.ratio.ratio)
 *
 * This class handlers all ratios.
 */
public class RatioHandler {

    private final List<Ratio> ratios = new ArrayList<>();

    public Ratio add(Player player, Player target) {
        Ratio ratio = new Ratio(player, target);
        ratios.add(ratio);
        return ratio;
    }

    public boolean existsCreatedBy(Player player) {
        return ratios.stream().anyMatch(ratio -> ratio.getCreatedBy().equals(player));
    }

    public boolean hasPendingRatio(Player player) {
        return ratios.stream().anyMatch(ratio -> ratio.getTarget().equals(player));
    }

    public Ratio get(Player player) {
        return ratios.stream().filter(ratio -> ratio.getTarget().equals(player)).findFirst().orElse(null);
    }

    public void remove(Ratio ratio) {
        ratio.getVotes().clear();
        ratios.remove(ratio);
    }

    public void increment(Ratio ratio, Player player, Ratio.RatioVote vote) {
        switch(vote) {
            case POSITIVE:
                ratio.addVoter(player.getUniqueId(), vote);
                ratio.setPositive(ratio.getPositive() + 1);
                return;
            case NEGATIVE:
                ratio.addVoter(player.getUniqueId(), vote);
                ratio.setNegative(ratio.getNegative() + 1);
                return;
            default:
                throw new IllegalArgumentException("The provided 'RatioVote' is not valid.");
        }
    }

}

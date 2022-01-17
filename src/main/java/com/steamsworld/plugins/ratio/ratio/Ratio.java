package com.steamsworld.plugins.ratio.ratio;

import com.google.common.collect.Maps;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;

/**
 * @author Steamworks (Steamworks#1127)
 * Sunday 16 2022 (10:13 PM)
 * Ratio (com.steamsworld.plugins.ratio.object)
 *
 * This class represents a Ratio.
 */
@Getter
@Setter
public class Ratio {

    private final Player createdBy, target;
    private final Map<UUID, RatioVote> votes = Maps.newHashMap();
    private int positive = 0, negative = 0;

    public Ratio(Player createdBy, Player target) {
        this.createdBy = createdBy;
        this.target = target;
    }

    public static enum RatioVote {
        POSITIVE,
        NEGATIVE
    }

    public void addVoter(UUID uuid, RatioVote vote) {
        votes.put(uuid, vote);
    }

    public boolean alreadyVoted(UUID uuid) {
        return votes.containsKey(uuid);
    }

}

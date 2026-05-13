package dev.tsc.epgrab;

import net.minecraft.client.MinecraftClient;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
import net.minecraft.scoreboard.ScoreboardEntry;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.Team;
import net.minecraft.text.Text;

import java.util.Locale;

final class DungeonContext {
    private DungeonContext() {
    }

    static boolean isTrackedContext(MinecraftClient client) {
        if (client == null || client.world == null || client.player == null) {
            return false;
        }

        Scoreboard scoreboard = client.world.getScoreboard();
        ScoreboardObjective sidebar = scoreboard.getObjectiveForSlot(ScoreboardDisplaySlot.SIDEBAR);
        if (sidebar != null && objectiveMatches(scoreboard, sidebar)) {
            return true;
        }

        for (ScoreboardObjective objective : scoreboard.getObjectives()) {
            if (objectiveMatches(scoreboard, objective)) {
                return true;
            }
        }

        for (Team team : scoreboard.getTeams()) {
            if (teamMatches(team)) {
                return true;
            }
        }

        return false;
    }

    private static boolean objectiveMatches(Scoreboard scoreboard, ScoreboardObjective objective) {
        if (containsDungeonMarker(objective.getName()) || containsDungeonMarker(objective.getDisplayName().getString())) {
            return true;
        }

        for (ScoreboardEntry entry : scoreboard.getScoreboardEntries(objective)) {
            if (containsDungeonMarker(entry.owner())
                || containsDungeonMarker(entry.name().getString())
                || (entry.display() != null && containsDungeonMarker(entry.display().getString()))) {
                return true;
            }
        }

        return false;
    }

    private static boolean teamMatches(Team team) {
        String prefix = textOf(team.getPrefix());
        String suffix = textOf(team.getSuffix());
        return containsDungeonMarker(team.getName())
            || containsDungeonMarker(textOf(team.getDisplayName()))
            || containsDungeonMarker(prefix)
            || containsDungeonMarker(suffix)
            || containsDungeonMarker(prefix + suffix);
    }

    private static String textOf(Text text) {
        return text == null ? "" : text.getString();
    }

    private static boolean containsDungeonMarker(String text) {
        String normalized = normalize(text);
        String compact = normalized.replace(" ", "");
        return normalized.contains("dungeon hub")
            || normalized.contains("the catacombs")
            || compact.contains("dungeonhub")
            || compact.contains("thecatacombs");
    }

    private static String normalize(String text) {
        return text
            .replaceAll("§.", "")
            .toLowerCase(Locale.ROOT)
            .replaceAll("[^a-z ]", " ")
            .replaceAll("\\s+", " ")
            .trim();
    }
}

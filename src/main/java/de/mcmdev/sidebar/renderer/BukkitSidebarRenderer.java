/*
 * MIT License
 *
 * Copyright (c) 2021 MCMDEV
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package de.mcmdev.sidebar.renderer;

import de.mcmdev.sidebar.Sidebar;
import net.kyori.adventure.text.Component;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BukkitSidebarRenderer implements SidebarRenderer {

    private static final String[] COLOR_CODES = Arrays
            .stream(ChatColor.values())
            .map(Object::toString)
            .toArray(String[]::new);

    private final String rendererId = Integer.toHexString(ThreadLocalRandom.current().nextInt());
    private final Pattern pattern = Pattern.compile(rendererId + "_(\\d\\d?)");

    @Override
    public void add(Player player, Sidebar sidebar) {
        Scoreboard scoreboard = player.getScoreboard();
        Objective objective = scoreboard.registerNewObjective("sidebar", "", sidebar.getTitle().apply(player));
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
    }

    @Override
    public void remove(Player player, Sidebar sidebar) {
        //Nothing for now
    }

    @Override
    public void render(Sidebar sidebar) {
        if (sidebar.getTitle() == null || sidebar.getLineFunction() == null) return;
        //I don't want this!
        for (Player viewer : sidebar.getViewers()) {
            List<Component> lines = List.copyOf(sidebar.getLineFunction().apply(viewer));

            cleanup(viewer, lines.size());
            for (int i = 0; i < lines.size(); i++) {
                renderLine(viewer, i, lines.get(i));
            }
        }
    }

    private void renderLine(Player player, int index, Component line) {
        Scoreboard scoreboard = player.getScoreboard();

        Objective objective = player.getScoreboard().getObjective("sidebar");
        if (objective == null) return;

        Team team = scoreboard.getTeam(rendererId + "_" + index);

        if (team == null) {
            team = scoreboard.registerNewTeam(rendererId + "_" + index);
        }

        String entry = COLOR_CODES[index];
        if (!team.hasEntry(entry)) {
            team.addEntry(entry);
        }

        team.prefix(line);

        objective.getScore(entry).setScore(16 - index);
    }

    private void cleanup(Player player, int size) {
        Scoreboard scoreboard = player.getScoreboard();

        for (Team team : scoreboard.getTeams()) {
            String name = team.getName();
            Matcher matcher = pattern.matcher(name);
            if(!matcher.matches()) continue;
            if(Integer.parseInt(matcher.group(1)) <= size) continue;
            for (String entry : team.getEntries()) {
                scoreboard.resetScores(entry);
            }
            team.unregister();
        }
    }
}

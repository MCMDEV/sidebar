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

package de.mcmdev.sidebar;

import com.google.common.collect.ImmutableSet;
import de.mcmdev.sidebar.renderer.SidebarRenderer;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

public class Sidebar {

    private final Set<Player> viewers = new HashSet<>();
    private final SidebarRenderer sidebarRenderer;
    private Function<Player, Component> title;
    private Function<Player, Collection<Component>> lineFunction;

    public Sidebar(SidebarRenderer sidebarRenderer) {
        this.sidebarRenderer = sidebarRenderer;
    }

    public void add(Player player) {
        viewers.add(player);
        sidebarRenderer.add(player, this);
    }

    public void add(Player... players) {
        for (Player player : players) {
            add(player);
        }
    }

    public Set<Player> getViewers() {
        return ImmutableSet.copyOf(viewers);
    }

    public Function<Player, Component> getTitle() {
        return title;
    }

    public Function<Player, Collection<Component>> getLineFunction() {
        return lineFunction;
    }

    public void remove(Player player) {
        viewers.remove(player);
        sidebarRenderer.remove(player, this);
    }

    public void remove(Player... players) {
        for (Player player : players) {
            remove(player);
        }
    }

    public void title(Function<Player, Component> title) {
        this.title = title;
    }

    public void lines(Function<Player, Collection<Component>> lineFunction) {
        this.lineFunction = lineFunction;
    }

    public void render() {
        this.sidebarRenderer.render(this);
    }
}

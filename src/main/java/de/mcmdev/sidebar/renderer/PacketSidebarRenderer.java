package de.mcmdev.sidebar.renderer;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.AdventureComponentConverter;
import de.mcmdev.sidebar.Sidebar;
import net.kyori.adventure.text.Component;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

public class PacketSidebarRenderer implements SidebarRenderer {

    private final boolean zeroScores;

    public PacketSidebarRenderer() {
        this(false);
    }

    public PacketSidebarRenderer(boolean zeroScores) {
        this.zeroScores = zeroScores;
    }

    private static final String[] COLOR_CODES = Arrays
            .stream(ChatColor.values())
            .map(Object::toString)
            .toArray(String[]::new);

    private final String rendererId = Integer.toHexString(ThreadLocalRandom.current().nextInt());
    private final List<PacketContainer> teamPackets = IntStream.range(0, 16)
            .mapToObj(this::createTeamPacket)
            .toList();

    @Override
    public void add(Player player, Sidebar sidebar) {
        teamPackets.forEach(packetContainer -> {
            ProtocolLibrary.getProtocolManager().sendServerPacket(player, packetContainer);
        });

        PacketContainer objectivePacket = new PacketContainer(PacketType.Play.Server.SCOREBOARD_OBJECTIVE);
        objectivePacket.getModifier().writeDefaults();
        objectivePacket.getStrings().write(0, "sidebar");
        objectivePacket.getChatComponents().write(0, AdventureComponentConverter.fromComponent(sidebar.getTitle().apply(player)));
        objectivePacket.getIntegers().write(0, 0);

        PacketContainer sidebarPacket = new PacketContainer(PacketType.Play.Server.SCOREBOARD_DISPLAY_OBJECTIVE);
        sidebarPacket.getIntegers().write(0, 1);
        sidebarPacket.getStrings().write(0, "sidebar");

        ProtocolLibrary.getProtocolManager().sendServerPacket(player, objectivePacket);
        ProtocolLibrary.getProtocolManager().sendServerPacket(player, sidebarPacket);
    }

    @Override
    public void remove(Player player, Sidebar sidebar) {
        PacketContainer objectivePacket = new PacketContainer(PacketType.Play.Server.SCOREBOARD_OBJECTIVE);
        objectivePacket.getModifier().writeDefaults();
        objectivePacket.getStrings().write(0, "sidebar");
        objectivePacket.getIntegers().write(0, 1);
        ProtocolLibrary.getProtocolManager().sendServerPacket(player, objectivePacket);
    }

    @Override
    public void render(Sidebar sidebar) {
        if(sidebar.getTitle() == null || sidebar.getLineFunction() == null) return;
        for (Player viewer : sidebar.getViewers()) {
            List<Component> lines = new ArrayList<>(sidebar.getLineFunction().apply(viewer));

            renderTitle(viewer, sidebar.getTitle().apply(viewer));
            for (int i = 0; i < lines.size(); i++) {
                renderLine(viewer, i, lines.get(i));
            }
        }
    }

    private void renderTitle(Player player, Component title)    {
        PacketContainer objectivePacket = new PacketContainer(PacketType.Play.Server.SCOREBOARD_OBJECTIVE);
        objectivePacket.getModifier().writeDefaults();
        objectivePacket.getStrings().write(0, "sidebar");
        objectivePacket.getChatComponents().write(0, AdventureComponentConverter.fromComponent(title));
        objectivePacket.getIntegers().write(0, 2);
        ProtocolLibrary.getProtocolManager().sendServerPacket(player, objectivePacket);
    }

    private void renderLine(Player player, int index, Component line)   {
        PacketContainer teamPacket = new PacketContainer(PacketType.Play.Server.SCOREBOARD_TEAM);
        teamPacket.getModifier().writeDefaults();
        teamPacket.getStrings().write(0, this.rendererId + "_" + index);
        teamPacket.getIntegers().write(0, 2);
        teamPacket.getOptionalStructures().read(0).ifPresent(internalStructure -> {
            internalStructure.getEnumModifier(ChatColor.class, MinecraftReflection.getMinecraftClass("EnumChatFormat")).write(0, ChatColor.WHITE);
            internalStructure.getChatComponents().write(1, AdventureComponentConverter.fromComponent(line));
            internalStructure.getChatComponents().write(2, AdventureComponentConverter.fromComponent(Component.empty()));
            teamPacket.getOptionalStructures().write(0, Optional.of(internalStructure));
        });

        PacketContainer scorePacket = new PacketContainer(PacketType.Play.Server.SCOREBOARD_SCORE);
        scorePacket.getModifier().writeDefaults();
        scorePacket.getStrings().write(0, COLOR_CODES[index]);
        scorePacket.getStrings().write(1, "sidebar");
        scorePacket.getIntegers().write(0, zeroScores ? 0 : 16 - index);

        ProtocolLibrary.getProtocolManager().sendServerPacket(player, teamPacket);
        ProtocolLibrary.getProtocolManager().sendServerPacket(player, scorePacket);
    }

    private PacketContainer createTeamPacket(int index) {
        PacketContainer packetContainer = new PacketContainer(PacketType.Play.Server.SCOREBOARD_TEAM);
        packetContainer.getModifier().writeDefaults();
        packetContainer.getStrings().write(0, rendererId + "_" + index);
        packetContainer.getIntegers().write(0, 0);
        packetContainer.getSpecificModifier(Collection.class).writeSafely(0, Collections.singletonList(COLOR_CODES[index]));
        packetContainer.getOptionalStructures().read(0).ifPresent(internalStructure -> {
            internalStructure.getEnumModifier(ChatColor.class, MinecraftReflection.getMinecraftClass("EnumChatFormat")).write(0, ChatColor.WHITE);
            internalStructure.getChatComponents().write(1, AdventureComponentConverter.fromComponent(Component.empty()));
            internalStructure.getChatComponents().write(2, AdventureComponentConverter.fromComponent(Component.empty()));
            packetContainer.getOptionalStructures().write(0, Optional.of(internalStructure));
        });
        return packetContainer;
    }


}

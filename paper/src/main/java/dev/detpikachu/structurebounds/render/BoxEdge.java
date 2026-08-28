package dev.detpikachu.structurebounds.render;

import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public record BoxEdge(int entityId, ClientboundAddEntityPacket addPacket, ClientboundSetEntityDataPacket dataPacket) {}

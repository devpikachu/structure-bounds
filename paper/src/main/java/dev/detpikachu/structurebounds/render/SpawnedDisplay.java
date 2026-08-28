package dev.detpikachu.structurebounds.render;

import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.world.entity.Display;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.ApiStatus;

import java.util.Objects;

@ApiStatus.Internal
public record SpawnedDisplay(
        int entityId, ClientboundAddEntityPacket addPacket, ClientboundSetEntityDataPacket dataPacket) {

    private static final float NO_ROTATION = 0.0f;
    private static final int NO_ENTITY_DATA = 0;

    public static SpawnedDisplay of(Display display) {
        final var position = display.position();
        final var addPacket = new ClientboundAddEntityPacket(
                display.getId(),
                display.getUUID(),
                position.x,
                position.y,
                position.z,
                NO_ROTATION,
                NO_ROTATION,
                display.getType(),
                NO_ENTITY_DATA,
                Vec3.ZERO,
                NO_ROTATION);
        final var dataPacket = new ClientboundSetEntityDataPacket(
                display.getId(), Objects.requireNonNull(display.getEntityData().getNonDefaultValues()));

        return new SpawnedDisplay(display.getId(), addPacket, dataPacket);
    }
}

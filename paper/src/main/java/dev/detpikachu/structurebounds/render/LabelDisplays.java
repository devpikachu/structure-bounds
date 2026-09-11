package dev.detpikachu.structurebounds.render;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Brightness;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public final class LabelDisplays {

    private static final double LEGIBILITY_RADIUS = 40.0;
    private static final double LEGIBILITY_RADIUS_SQUARED = LEGIBILITY_RADIUS * LEGIBILITY_RADIUS;

    private static final float BLOCKS_PER_VIEW_RANGE = 64.0f;
    private static final float VIEW_RANGE = (float) (LEGIBILITY_RADIUS / BLOCKS_PER_VIEW_RANGE);

    private static final float LIFT = 0.5f;

    private static final byte STYLE_FLAGS = Display.TextDisplay.FLAG_SEE_THROUGH;

    public static boolean isLegible(BoundingBox bounds, Vec3 position) {
        return anchor(bounds).distanceToSqr(position) <= LEGIBILITY_RADIUS_SQUARED;
    }

    public static SpawnedDisplay build(ServerLevel level, LabelKey key) {
        final var anchor = anchor(key.bounds());
        // #if MC_26_2
        // $$ final var display = new Display.TextDisplay(
        // $$         net.minecraft.world.entity.EntityTypes.TEXT_DISPLAY, level);
        // #else
        final var display = new Display.TextDisplay(EntityType.TEXT_DISPLAY, level);
        // #endif

        display.setPos(anchor.x, anchor.y, anchor.z);
        display.setText(Component.literal(key.text()));
        display.setFlags(STYLE_FLAGS);
        display.setBillboardConstraints(Display.BillboardConstraints.CENTER);
        display.setBrightnessOverride(Brightness.FULL_BRIGHT);
        display.setViewRange(VIEW_RANGE);

        return SpawnedDisplay.of(display);
    }

    private static Vec3 anchor(BoundingBox bounds) {
        return new Vec3(
                bounds.minX() + bounds.getXSpan() / 2.0,
                bounds.maxY() + 1 + LIFT,
                bounds.minZ() + bounds.getZSpan() / 2.0);
    }
}

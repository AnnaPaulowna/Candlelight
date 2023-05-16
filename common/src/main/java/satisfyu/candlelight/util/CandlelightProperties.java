package satisfyu.candlelight.util;

import net.minecraft.world.level.block.state.properties.EnumProperty;
import satisfyu.candlelight.block.LineConnectingType;

public class CandlelightProperties {
    public static final EnumProperty<LineConnectingType> LINE_CONNECTING_TYPE;

    static {
        LINE_CONNECTING_TYPE = EnumProperty.create("type", LineConnectingType.class);
    }
}

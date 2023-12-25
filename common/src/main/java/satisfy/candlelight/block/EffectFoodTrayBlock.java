package satisfy.candlelight.block;

import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import satisfy.candlelight.util.CandlelightGeneralUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class EffectFoodTrayBlock extends EffectFoodBlock {

    public EffectFoodTrayBlock(Properties settings, int maxBites, FoodProperties foodComponent) {
        super(settings, maxBites, foodComponent);
    }

    private static final Supplier<VoxelShape> voxelShapeSupplier = () -> {
        VoxelShape shape = Shapes.empty();
        shape = Shapes.joinUnoptimized(shape, Shapes.box(0.25, 0, 0.15625, 0.75, 0.0625, 0.84375), BooleanOp.OR);
        shape = Shapes.joinUnoptimized(shape, Shapes.box(0.71875, 0.0625, 0.125, 0.78125, 0.1875, 0.875), BooleanOp.OR);
        shape = Shapes.joinUnoptimized(shape, Shapes.box(0.21875, 0.0625, 0.125, 0.28125, 0.1875, 0.875), BooleanOp.OR);
        shape = Shapes.joinUnoptimized(shape, Shapes.box(0.28125, 0.0625, 0.8125, 0.71875, 0.1875, 0.875), BooleanOp.OR);
        shape = Shapes.joinUnoptimized(shape, Shapes.box(0.28125, 0.0625, 0.125, 0.71875, 0.1875, 0.1875), BooleanOp.OR);
        return shape;
    };

    public static final Map<Direction, VoxelShape> SHAPE = Util.make(new HashMap<>(), map -> {
        for (Direction direction : Direction.Plane.HORIZONTAL.stream().toList()) {
            map.put(direction, CandlelightGeneralUtil.rotateShape(Direction.NORTH, direction, voxelShapeSupplier.get()));
        }
    });

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return SHAPE.get(state.getValue(FACING));
    }
}

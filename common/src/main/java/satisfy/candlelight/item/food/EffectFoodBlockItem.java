package satisfy.candlelight.item.food;

import com.mojang.datafixers.util.Pair;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import satisfy.candlelight.block.EffectFoodBlock;
import satisfy.candlelight.item.food.EffectFood;
import satisfy.candlelight.item.food.EffectFoodHelper;

import java.util.List;

import static satisfy.candlelight.block.EffectFoodBlock.BITES;

public class EffectFoodBlockItem extends BlockItem implements EffectFood {

    private final int foodStages;

    public EffectFoodBlockItem(Block block, Properties settings, int foodStages) {
        super(block, settings);
        this.foodStages = foodStages;
    }

    @Nullable
    @Override
    protected BlockState getPlacementState(BlockPlaceContext context) {
        BlockState blockState = this.getBlock().getStateForPlacement(context);
        return blockState != null && this.canPlace(context, blockState) ? blockState.getBlock() instanceof EffectFoodBlock ? blockState.setValue(BITES , EffectFoodHelper.getStage(context.getItemInHand())) : blockState : null;
    }

    @Override
    public @NotNull ItemStack finishUsingItem(ItemStack stack, Level world, LivingEntity user) {
        if (!world.isClientSide) {
            List<Pair<MobEffectInstance, Float>> effects = EffectFoodHelper.getEffects(stack);
            for (Pair<MobEffectInstance, Float> effect : effects) {
                if (effect.getFirst() != null && world.random.nextFloat() < effect.getSecond()) {
                    user.addEffect(new MobEffectInstance(effect.getFirst()));
                }
            }
        }

        int slot = -1;
        Inventory playerInventory = null;
        if (user instanceof Player player && !player.isCreative()) {
            playerInventory = player.getInventory();
            slot = playerInventory.findSlotMatchingUnusedItem(stack);
        }
        ItemStack returnStack =  super.finishUsingItem(stack, world, user);
        int stage = EffectFoodHelper.getStage(stack);
        if (playerInventory != null && stage < this.foodStages) {
            ItemStack itemStack = EffectFoodHelper.setStage(new ItemStack(this), stage + 1);
            if (playerInventory.getItem(slot).isEmpty()) {
                playerInventory.add(slot, itemStack);
            }
            else {
                slot = playerInventory.getSlotWithRemainingSpace(itemStack);
                playerInventory.add(slot, itemStack);
            }
        }
        return returnStack;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag context) {
        tooltip.add(Component.translatable("tooltip.candlelight.canbeplaced").withStyle(ChatFormatting.ITALIC, ChatFormatting.GRAY));
        EffectFoodHelper.getTooltip(stack, tooltip);
    }
}

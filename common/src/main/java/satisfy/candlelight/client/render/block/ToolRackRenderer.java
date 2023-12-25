package satisfy.candlelight.client.render.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import de.cristelknight.doapi.client.render.block.storage.StorageTypeRenderer;
import de.cristelknight.doapi.common.block.entity.StorageBlockEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import satisfy.candlelight.client.ClientUtil;
import satisfy.candlelight.registry.ObjectRegistry;

@Environment(EnvType.CLIENT)
public class ToolRackRenderer implements StorageTypeRenderer {
    @Override
    public void render(StorageBlockEntity entity, PoseStack matrices, MultiBufferSource vertexConsumers, NonNullList<ItemStack> itemStacks) {
        for(int i = 0; i < itemStacks.size(); i++) {
            ItemStack stack = itemStacks.get(i);

            if (!stack.isEmpty()) {
                matrices.pushPose();
                Item item = stack.getItem();
                double translate = (i + 1) * (1D / 3);

                if (item instanceof Item && item.equals(ObjectRegistry.COOKING_PAN_ITEM.get())) {
                    matrices.translate(translate - (2D / 3), 0.6f, 0.38f);
                    matrices.scale(0.5f, 0.5f, 0.5f);
                    matrices.mulPose(Vector3f.ZN.rotationDegrees(135f));
                    matrices.mulPose(Vector3f.YN.rotationDegrees(0f));
                    ClientUtil.renderItem(stack, matrices, vertexConsumers, entity);
                } else {
                    matrices.translate(translate - (2D / 3), 0.6f, 0.38f);
                    matrices.scale(0.6f, 0.6f, 0.6f);
                    matrices.mulPose(Vector3f.ZN.rotationDegrees(135f));
                    matrices.mulPose(Vector3f.YN.rotationDegrees(0f));
                    ClientUtil.renderItem(stack, matrices, vertexConsumers, entity);
                }
                matrices.popPose();
            }
        }
    }
}
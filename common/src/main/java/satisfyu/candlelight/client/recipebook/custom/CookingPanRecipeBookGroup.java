package satisfyu.candlelight.client.recipebook.custom;

import com.google.common.collect.ImmutableList;
import de.cristelknight.doapi.client.recipebook.IRecipeBookGroup;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import satisfyu.candlelight.item.food.EffectFoodItem;
import satisfyu.candlelight.registry.ObjectRegistry;

import java.util.Arrays;
import java.util.List;

@Environment(EnvType.CLIENT)
public enum CookingPanRecipeBookGroup implements IRecipeBookGroup {
    SEARCH(new ItemStack(Items.COMPASS)),
    EFFECT(new ItemStack(Items.POTION)),
    MISC(new ItemStack(ObjectRegistry.DOUGH.get())),
    BIG(new ItemStack(ObjectRegistry.BEEF_WELLINGTON.get()));

    public static final List<IRecipeBookGroup> PAN_GROUPS = ImmutableList.of(SEARCH, MISC, EFFECT, BIG);

    private final List<ItemStack> icons;

    CookingPanRecipeBookGroup(ItemStack... entries) {
        this.icons = ImmutableList.copyOf(entries);
    }

    public boolean fitRecipe(Recipe<? extends Container> recipe, RegistryAccess registryAccess) {
        return switch (this) {
            case SEARCH -> true;
            case MISC ->
                    recipe.getIngredients().stream().noneMatch((ingredient) -> ingredient.test(Items.POTION.getDefaultInstance())) && recipe.getIngredients().stream().noneMatch((ingredient) -> Arrays.stream(ingredient.getItems()).anyMatch(itemStack -> itemStack.getItem() instanceof EffectFoodItem));
            case EFFECT ->
                    recipe.getIngredients().stream().anyMatch((ingredient) -> ingredient.test(Items.POTION.getDefaultInstance()));
            case BIG ->
                    recipe.getIngredients().stream().anyMatch((ingredient) -> Arrays.stream(ingredient.getItems()).anyMatch(itemStack -> itemStack.getItem() instanceof EffectFoodItem));
        };
    }

    @Override
    public List<ItemStack> getIcons() {
        return this.icons;
    }

}

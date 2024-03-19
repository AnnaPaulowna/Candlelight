package satisfy.candlelight.recipe;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import satisfy.candlelight.registry.RecipeTypeRegistry;
import satisfy.candlelight.util.GeneralUtil;

public class CookingPanRecipe implements Recipe<Container> {
    final ResourceLocation id;
    private final NonNullList<Ingredient> inputs;
    private final ItemStack container;
    private final ItemStack output;

    public CookingPanRecipe(ResourceLocation id, NonNullList<Ingredient> inputs, ItemStack container, ItemStack output) {
        this.id = id;
        this.inputs = inputs;
        this.container = container;
        this.output = output;
    }

    @Override
    public boolean matches(Container inventory, Level world) {
        return GeneralUtil.matchesRecipe(inventory, this.inputs, 0, 6);
    }

    @Override
    public @NotNull ItemStack assemble(Container container, RegistryAccess registryAccess) {
        return ItemStack.EMPTY;
    }
    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public @NotNull ItemStack getResultItem(RegistryAccess registryAccess) {
        return this.output.copy();
    }
    @Override
    public @NotNull ResourceLocation getId() {
        return this.id;
    }

    public ItemStack getContainer() {
        return this.container;
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return RecipeTypeRegistry.COOKING_PAN_RECIPE_SERIALIZER.get();
    }

    @Override
    public @NotNull RecipeType<?> getType() {
        return RecipeTypeRegistry.COOKING_PAN_RECIPE_TYPE.get();
    }

    @Override
    public @NotNull NonNullList<Ingredient> getIngredients() {
        return this.inputs;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    public static class Serializer implements RecipeSerializer<CookingPanRecipe> {

        @Override
        public @NotNull CookingPanRecipe fromJson(ResourceLocation id, JsonObject json) {
            final var ingredients = GeneralUtil.deserializeIngredients(GsonHelper.getAsJsonArray(json, "ingredients"));
            if (ingredients.isEmpty()) {
                throw new JsonParseException("No ingredients for CookingPan Recipe");
            } else if (ingredients.size() > 6) {
                throw new JsonParseException("Too many ingredients for CookingPan Recipe");
            } else {
                return new CookingPanRecipe(id, ingredients,  ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "container")), ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "result")));
            }
        }

        @Override
        public @NotNull CookingPanRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
            final var ingredients  = NonNullList.withSize(buf.readVarInt(), Ingredient.EMPTY);
            ingredients.replaceAll(ignored -> Ingredient.fromNetwork(buf));
            return new CookingPanRecipe(id, ingredients, buf.readItem(), buf.readItem());
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, CookingPanRecipe recipe) {
            buf.writeVarInt(recipe.inputs.size());
            recipe.inputs.forEach(entry -> entry.toNetwork(buf));
            buf.writeItem(recipe.getContainer());
            buf.writeItem(recipe.output);
        }
    }
    
}
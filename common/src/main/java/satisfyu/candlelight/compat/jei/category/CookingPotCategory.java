package satisfyu.candlelight.compat.jei.category;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.crafting.Ingredient;
import satisfyu.candlelight.Candlelight;
import satisfyu.candlelight.client.gui.CookingPanGui;
import satisfyu.candlelight.client.gui.CookingPotGui;
import satisfyu.candlelight.compat.jei.CandlelightJEIPlugin;
import satisfyu.candlelight.entity.CookingPotBlockEntity;
import satisfyu.candlelight.recipe.CookingPotRecipe;
import satisfyu.candlelight.registry.ObjectRegistry;

public class CookingPotCategory implements IRecipeCategory<CookingPotRecipe> {
    public static final RecipeType<CookingPotRecipe> COOKING_POT = RecipeType.create(Candlelight.MOD_ID, "pot_cooking", CookingPotRecipe.class);
    public static final int WIDTH = 124;
    public static final int HEIGHT = 60;
    public static final int WIDTH_OF = 26;
    public static final int HEIGHT_OF = 13;
    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawable burnIcon;
    private final IDrawableAnimated arrow;
    private final Component localizedName;

    public CookingPotCategory(IGuiHelper helper) {
        this.background = helper.createDrawable(CookingPotGui.BACKGROUND, WIDTH_OF, HEIGHT_OF, WIDTH, HEIGHT);
        this.arrow = helper.drawableBuilder(CookingPotGui.BACKGROUND, 178, 15, 23, 30)
                .buildAnimated(CookingPotBlockEntity.MAX_COOKING_TIME, IDrawableAnimated.StartDirection.LEFT, false);
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, ObjectRegistry.COOKING_POT.get().asItem().getDefaultInstance());
        this.burnIcon = helper.createDrawable(CookingPanGui.BACKGROUND, 176, 0, 17, 15);
        this.localizedName = Component.translatable("rei.candlelight.cooking_pot_category");
    }


    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, CookingPotRecipe recipe, IFocusGroup focuses) {

        // Wine input
        NonNullList<Ingredient> ingredients = recipe.getIngredients();
        int s = ingredients.size();

        builder.addSlot(RecipeIngredientRole.INPUT, 95 - WIDTH_OF, 55 - HEIGHT_OF).addItemStack(recipe.getContainer());

        for (int row = 0; row < 2; row++) {
            for (int slot = 0; slot < 3; slot++) {
                int current = slot + row + (row * 2);
                if(s - 1 < current) break;
                CandlelightJEIPlugin.addSlot(builder,30 + (slot * 18) - WIDTH_OF, 17 + (row * 18) - HEIGHT_OF, ingredients.get(current));
            }
        }

        // Output
        builder.addSlot(RecipeIngredientRole.OUTPUT, 124 - WIDTH_OF,  28 - HEIGHT_OF).addItemStack(recipe.getResultItem(Minecraft.getInstance().level.registryAccess()));
    }

    @Override
    public void draw(CookingPotRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        arrow.draw(guiGraphics, CookingPotGui.ARROW_X - WIDTH_OF, CookingPotGui.ARROW_Y - HEIGHT_OF);
        burnIcon.draw(guiGraphics, 124 - WIDTH_OF, 56 - HEIGHT_OF);
    }

    @Override
    public RecipeType<CookingPotRecipe> getRecipeType() {
        return COOKING_POT;
    }

    @Override
    public Component getTitle() {
        return this.localizedName;
    }

    @Override
    public IDrawable getBackground() {
        return this.background;
    }

    @Override
    public IDrawable getIcon() {
        return this.icon;
    }
}

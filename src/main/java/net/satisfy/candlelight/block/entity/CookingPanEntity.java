package net.satisfy.candlelight.block.entity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.satisfy.candlelight.block.CookingPanBlock;
import net.satisfy.candlelight.client.gui.handler.CookingPanScreenHandler;
import net.satisfy.candlelight.item.EffectFood;
import net.satisfy.candlelight.item.EffectFoodHelper;
import net.satisfy.candlelight.recipe.CookingPanRecipe;
import net.satisfy.candlelight.registry.CandlelightEntityTypes;
import net.satisfy.candlelight.registry.RecipeTypes;
import net.satisfy.candlelight.util.CandlelightTags;
import org.jetbrains.annotations.Nullable;

public class CookingPanEntity extends BlockEntity implements BlockEntityTicker<CookingPanEntity>, Inventory, NamedScreenHandlerFactory {

	private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(MAX_CAPACITY, ItemStack.EMPTY);
	private static final int MAX_CAPACITY = 8;
	public static final int MAX_COOKING_TIME = 600; // Time in ticks (30s)
	private int cookingTime;
	public static final int BOTTLE_INPUT_SLOT = 6;
	public static final int OUTPUT_SLOT = 7;
	private static final int INGREDIENTS_AREA = 2 * 3;

	private boolean isBeingBurned;

	private final PropertyDelegate delegate;

	public CookingPanEntity(BlockPos pos, BlockState state) {
		super(CandlelightEntityTypes.COOKING_PAN_BLOCK_ENTITY, pos, state);
		this.delegate = new PropertyDelegate() {
			@Override
			public int get(int index) {
				return switch (index) {
					case 0 -> CookingPanEntity.this.cookingTime;
					case 1 -> CookingPanEntity.this.isBeingBurned ? 1 : 0;
					default -> 0;
				};
			}

			@Override
			public void set(int index, int value) {
				switch (index) {
					case 0 -> CookingPanEntity.this.cookingTime = value;
					case 1 -> CookingPanEntity.this.isBeingBurned = value != 0;
				}
			}

			@Override
			public int size() {
				return 2;
			}
		};
	}
	
	@Override
	public void readNbt(NbtCompound nbt) {
		super.readNbt(nbt);
			Inventories.readNbt(nbt, this.inventory);
			this.cookingTime = nbt.getInt("CookingTime");
	}
	
	@Override
	protected void writeNbt(NbtCompound nbt) {
		super.writeNbt(nbt);
		Inventories.writeNbt(nbt, this.inventory);
		nbt.putInt("CookingTime", this.cookingTime);
	}
	
	public boolean isBeingBurned() {
		if (world == null)
			throw new NullPointerException("Null world invoked");
		final BlockState belowState = world.getBlockState(getPos().down());
		final var optionalList = Registry.BLOCK.getEntryList(CandlelightTags.ALLOWS_COOKING_ON_PAN);
		final var entryList = optionalList.orElse(null);
		if (entryList == null) {
			return false;
		} else if (!entryList.contains(belowState.getBlock().getRegistryEntry())) {
			return false;
		} else
			return belowState.get(Properties.LIT);
	}
	
	private boolean canCraft(Recipe<?> recipe) {
		if (recipe == null || recipe.getOutput().isEmpty()) {
			return false;
		}
		if(recipe instanceof CookingPanRecipe cookingPanRecipe){
			if (!this.getStack(BOTTLE_INPUT_SLOT).isOf(cookingPanRecipe.getContainer().getItem())) {
				return false;
			} else if (this.getStack(OUTPUT_SLOT).isEmpty()) {
				return true;
			} else {
				final ItemStack recipeOutput = generateOutputItem(recipe);
				final ItemStack outputSlotStack = this.getStack(OUTPUT_SLOT);
				final int outputSlotCount = outputSlotStack.getCount();
				if (!outputSlotStack.isItemEqualIgnoreDamage(recipeOutput)) {
					return false;
				} else if (outputSlotCount < this.getMaxCountPerStack() && outputSlotCount < outputSlotStack.getMaxCount()) {
					return true;
				} else {
					return outputSlotCount < recipeOutput.getMaxCount();
				}
			}
		}
		return false;
	}
	
	private void craft(Recipe<?> recipe) {
		if (!canCraft(recipe)) {
			return;
		}
		final ItemStack recipeOutput = generateOutputItem(recipe);
		final ItemStack outputSlotStack = this.getStack(OUTPUT_SLOT);
		if (outputSlotStack.isEmpty()) {
			setStack(OUTPUT_SLOT, recipeOutput);
		} else if (outputSlotStack.isOf(recipeOutput.getItem())) {
			outputSlotStack.increment(recipeOutput.getCount());
		}
		final DefaultedList<Ingredient> ingredients = recipe.getIngredients();
		// each slot can only be used once because in canMake we only checked if decrement by 1 still retains the recipe
		// otherwise recipes can break when an ingredient is used multiple times
		boolean[] slotUsed = new boolean[INGREDIENTS_AREA];
		for (int i = 0; i < recipe.getIngredients().size(); i++) {
			Ingredient ingredient = ingredients.get(i);
			// Looks for the best slot to take it from
			final ItemStack bestSlot = this.getStack(i);
			if (ingredient.test(bestSlot) && !slotUsed[i]) {
				slotUsed[i] = true;
				bestSlot.decrement(1);
			} else {
				// check all slots in search of the ingredient
				for (int j = 0; j < INGREDIENTS_AREA; j++) {
					ItemStack stack = this.getStack(j);
					if (ingredient.test(stack) && !slotUsed[j]) {
						slotUsed[j] = true;
						stack.decrement(1);
					}
				}
			}
		}
		this.getStack(BOTTLE_INPUT_SLOT).decrement(1);
	}

	private ItemStack generateOutputItem(Recipe<?> recipe) {
		ItemStack outputStack = recipe.getOutput();

		if (!(outputStack.getItem() instanceof EffectFood)) {
			return outputStack;
		}

		for (Ingredient ingredient : recipe.getIngredients()) {
			for (int j = 0; j < 6; j++) {
				ItemStack stack = this.getStack(j);
				if (ingredient.test(stack)) {
					EffectFoodHelper.getEffects(stack).forEach(effect -> EffectFoodHelper.addEffect(outputStack, effect));
					break;
				}
			}
		}
		return outputStack;
	}

	@Override
	public void tick(World world, BlockPos pos, BlockState state, CookingPanEntity blockEntity) {
		if (world.isClient()) {
			return;
		}
		this.isBeingBurned = isBeingBurned();
		if (!this.isBeingBurned){
			if(state.get(CookingPanBlock.LIT)) {
				world.setBlockState(pos, state.with(CookingPanBlock.LIT, false), Block.NOTIFY_ALL);
			}
			return;
		}
		Recipe<?> recipe = world.getRecipeManager().getFirstMatch(RecipeTypes.COOKING_PAN_RECIPE_TYPE, this, world).orElse(null);

		boolean canCraft = canCraft(recipe);
		if (canCraft) {
			this.cookingTime++;
			if (this.cookingTime >= MAX_COOKING_TIME) {
				this.cookingTime = 0;
				craft(recipe);
			}
		} else if (!canCraft(recipe)) {
			this.cookingTime = 0;
		}
		if (canCraft) {
			world.setBlockState(pos, this.getCachedState().getBlock().getDefaultState().with(CookingPanBlock.COOKING, true).with(CookingPanBlock.LIT, true), Block.NOTIFY_ALL);
		} else if (state.get(CookingPanBlock.COOKING)) {
			world.setBlockState(pos, this.getCachedState().getBlock().getDefaultState().with(CookingPanBlock.COOKING, false).with(CookingPanBlock.LIT, true), Block.NOTIFY_ALL);
		}
		else if(state.get(CookingPanBlock.LIT) != isBeingBurned){
			world.setBlockState(pos, state.with(CookingPanBlock.LIT, isBeingBurned), Block.NOTIFY_ALL);
		}
	}


	
	@Override
	public int size() {
		return inventory.size();
	}
	
	@Override
	public boolean isEmpty() {
		return inventory.stream().allMatch(ItemStack::isEmpty);
	}
	
	@Override
	public ItemStack getStack(int slot) {
		return this.inventory.get(slot);
	}
	
	@Override
	public ItemStack removeStack(int slot, int amount) {
		return Inventories.splitStack(this.inventory, slot, amount);
	}
	
	@Override
	public ItemStack removeStack(int slot) {
		return Inventories.removeStack(this.inventory, slot);
	}
	
	@Override
	public void setStack(int slot, ItemStack stack) {
		this.inventory.set(slot, stack);
		if (stack.getCount() > this.getMaxCountPerStack()) {
			stack.setCount(this.getMaxCountPerStack());
		}
		this.markDirty();
	}


	@Override
	public boolean canPlayerUse(PlayerEntity player) {
		if (this.world.getBlockEntity(this.pos) != this) {
			return false;
		} else {
			return player.squaredDistanceTo((double) this.pos.getX() + 0.5, (double) this.pos.getY() + 0.5, (double) this.pos.getZ() + 0.5) <= 64.0;
		}
	}

	@Override
	public void clear() {
		inventory.clear();
	}
	
	@Override
	public Text getDisplayName() {
		return Text.translatable(this.getCachedState().getBlock().getTranslationKey());
	}
	
	@Nullable
	@Override
	public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
		return new CookingPanScreenHandler(syncId, inv, this, this.delegate);
	}
}



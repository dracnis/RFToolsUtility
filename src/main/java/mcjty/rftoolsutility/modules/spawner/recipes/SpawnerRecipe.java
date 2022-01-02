package mcjty.rftoolsutility.modules.spawner.recipes;

import mcjty.rftoolsutility.modules.spawner.SpawnerModule;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;

public class SpawnerRecipe implements Recipe<Container> {

    private final ResourceLocation id;
    private final SpawnerRecipes.MobSpawnAmount item1;
    private final SpawnerRecipes.MobSpawnAmount item2;
    private final SpawnerRecipes.MobSpawnAmount item3;
    private final int spawnRf;
    private final ResourceLocation entity;

    public SpawnerRecipe(ResourceLocation id, SpawnerRecipes.MobSpawnAmount item1, SpawnerRecipes.MobSpawnAmount item2, SpawnerRecipes.MobSpawnAmount item3, int spawnRf,
                         ResourceLocation entity) {
        this.id = id;
        this.item1 = item1;
        this.item2 = item2;
        this.item3 = item3;
        this.spawnRf = spawnRf;
        this.entity = entity;
    }

    public SpawnerRecipes.MobSpawnAmount getItem1() {
        return item1;
    }

    public SpawnerRecipes.MobSpawnAmount getItem2() {
        return item2;
    }

    public SpawnerRecipes.MobSpawnAmount getItem3() {
        return item3;
    }

    public int getSpawnRf() {
        return spawnRf;
    }

    public ResourceLocation getEntity() {
        return entity;
    }

    @Override
    public boolean matches(@Nonnull Container inv, @Nonnull Level worldIn) {
        return false;
    }

    @Nonnull
    @Override
    public ItemStack assemble(@Nonnull Container inv) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return false;
    }

    @Nonnull
    @Override
    public ItemStack getResultItem() {
        return ItemStack.EMPTY;
    }

    @Nonnull
    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Nonnull
    @Override
    public RecipeSerializer<?> getSerializer() {
        return SpawnerModule.SPAWNER_SERIALIZER.get();
    }

    @Nonnull
    @Override
    public RecipeType<?> getType() {
        return SpawnerModule.SPAWNER_RECIPE_TYPE;
    }
}

/*
 * Work under Copyright. Licensed under the EUPL.
 * See the project README.md and LICENSE.txt for more information.
 *
 */

package net.dries007.tfc.api.types;

import java.util.Random;
import javax.annotation.Nonnull;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.gen.structure.template.TemplateManager;
import net.minecraftforge.registries.IForgeRegistryEntry;

import net.dries007.tfc.api.ITreeGenerator;

public class Tree extends IForgeRegistryEntry.Impl<Tree>
{

    public final int dominance;
    private final float minTemp;
    private final float maxTemp;
    private final float minRain;
    private final float maxRain;
    private final float minDensity;
    private final float maxDensity;

    /**
     * The path part of the resource location, used for assigning block names
     */
    public final String name;
    public final int maxGrowthRadius;
    public final int maxHeight;
    public final int maxDecayDistance;
    public final boolean isConifer;
    public final float minGrowthTime;
    // Used when growing a tree
    private final ITreeGenerator gen;

    /**
     * This is a registry object that will create a number of things:
     * 1. Wood logs, planks, and leaf blocks, and all the respective variants
     * 2. A Tree object to be used in TFC world gen
     *
     * Addon mods that want to add trees should subscribe to the registry event for this class
     * They also must put (in their mod) the required resources in /assets/tfc/...
     *
     * When using this class, use the provided Builder to create your trees. This will require all the default values, as well as
     * provide optional values
     *
     * @param name    the ResourceLocation registry name of this tree
     * @param gen     the generator that should be called to generate this tree, both during world gen and when growing from a sapling
     * @param minTemp min temperature
     * @param maxTemp max temperature
     * @param minRain min rainfall
     * @param maxRain max rainfall
     * @param dominance how much this tree is chosen over other trees. Range 0 <> 5 with 0 being less common, 5 more common
     * @param maxGrowthRadius used to check growth conditions
     * @param maxHeight used to check growth conditions
     * @param isConifer todo
     * @param minGrowthTime the amount of time (in in-game days) that this tree requires to grow
     */
    private Tree(@Nonnull ResourceLocation name, @Nonnull ITreeGenerator gen,
                 float minTemp, float maxTemp, float minRain, float maxRain, float minDensity, float maxDensity, int dominance,
                 int maxGrowthRadius, int maxHeight, int maxDecayDistance, boolean isConifer, float minGrowthTime)
    {
        this.minTemp = minTemp;
        this.maxTemp = maxTemp;
        this.minRain = minRain;
        this.maxRain = maxRain;
        this.dominance = dominance;
        this.maxGrowthRadius = maxGrowthRadius;
        this.maxHeight = maxHeight;
        this.maxDecayDistance = maxDecayDistance;
        this.isConifer = isConifer;
        this.minGrowthTime = minGrowthTime;
        this.minDensity = minDensity;
        this.maxDensity = maxDensity;

        this.gen = gen;
        this.name = name.getResourcePath().toLowerCase();
        setRegistryName(name);
    }

    public void makeTreeWithoutChecking(TemplateManager manager, World world, BlockPos pos, Random rand)
    {
        gen.generateTree(manager, world, pos, this, rand);
    }
    public void makeTree(TemplateManager manager, World world, BlockPos pos, Random rand)
    {
        if (gen.canGenerateTree(world, pos, this))
            makeTreeWithoutChecking(manager, world, pos, rand);
    }

    public void makeTree(World world, BlockPos pos, Random rand)
    {
        if (world.isRemote) return;
        final TemplateManager manager = ((WorldServer) world).getStructureTemplateManager();
        makeTree(manager, world, pos, rand);
    }

    public boolean isValidLocation(float temp, float rain, float density)
    {
        return minTemp <= temp && maxTemp >= temp && minRain <= rain && maxRain >= rain && density >= minDensity && density <= maxDensity;
    }

    public static class Builder
    {
        private float minTemp;
        private float maxTemp;
        private float minRain;
        private float maxRain;
        private float minDensity;
        private float maxDensity;
        private int dominance;
        private int maxHeight;
        private int maxGrowthRadius;
        private int maxDecayDistance;
        private boolean isConifer;
        private float minGrowthTime;
        private ITreeGenerator gen;
        private ResourceLocation name;

        public Builder(@Nonnull ResourceLocation name, float minRain, float maxRain, float minTemp, float maxTemp, int dominance, @Nonnull ITreeGenerator gen)
        {
            this.minTemp = minTemp; // required values
            this.maxTemp = maxTemp;
            this.minRain = minRain;
            this.maxRain = maxRain;
            this.dominance = dominance;
            this.name = name;
            this.gen = gen;
            this.maxGrowthRadius = 2; // default values
            this.maxHeight = 6;
            this.maxDecayDistance = 4;
            this.isConifer = false;
            this.minGrowthTime = 7;
            this.minDensity = 0.1f;
            this.maxDensity = 2f;
        }

        public Builder setRadius(int maxGrowthRadius)
        {
            this.maxGrowthRadius = maxGrowthRadius;
            return this;
        }

        public Builder setDecayDist(int maxDecayDistance)
        {
            this.maxDecayDistance = maxDecayDistance;
            return this;
        }

        public Builder setConifer()
        {
            isConifer = true;
            return this;
        }

        public Builder setHeight(int maxHeight)
        {
            this.maxHeight = maxHeight;
            return this;
        }

        public Builder setGrowthTime(float growthTime)
        {
            this.minGrowthTime = growthTime;
            return this;
        }

        public Builder setDensity(float min, float max)
        {
            this.minDensity = min;
            this.maxDensity = max;
            return this;
        }

        public Tree build()
        {
            return new Tree(name, gen, minTemp, maxTemp, minRain, maxRain, minDensity, maxDensity, dominance, maxGrowthRadius, maxHeight, maxDecayDistance, isConifer, minGrowthTime);
        }
    }
}

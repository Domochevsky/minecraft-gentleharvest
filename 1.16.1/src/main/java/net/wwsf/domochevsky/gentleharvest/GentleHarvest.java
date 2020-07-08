package net.wwsf.domochevsky.gentleharvest;

import java.util.Collections;

// Used for debugging
// import org.apache.logging.log4j.LogManager;
// import org.apache.logging.log4j.Logger;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.CocoaBlock;
import net.minecraft.block.CropsBlock;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.state.IntegerProperty;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod("harvestchevsky")
public class GentleHarvest {
    // Directly reference a log4j logger.
    // Logger LOGGER = LogManager.getLogger();

    public GentleHarvest() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
    }

    private void setup(final FMLCommonSetupEvent event) {
        MinecraftForge.EVENT_BUS.register(new GentleHarvest());
    }

    @SubscribeEvent
	public void onPlayerInteract(RightClickBlock event) {
        if (event.getWorld().isRemote()) { return; } 						// Not doing this on client side. (Seems to be fired on server side only by default already.)
		if (event.getPlayer() == null) { return; }						// Who...?
        if (event.getPlayer().isCreative()) { return; }	                // Not interfering with creative mode
        if (event.getPlayer().isSpectator()) { return; }                // Not doing anything for spectator accounts
        if (event.getHand() != Hand.MAIN_HAND) { return; }				// Only for the main hand
        if (event.getPlayer().isSneaking()) { return; }	                // Not interfering like this.

        BlockState block = event.getWorld().getBlockState(event.getPos());
        
        if (block == null) { return; }      // ...The block doesn't exist?
        if (!(block.getBlock() instanceof CropsBlock)) { return; }    // Only interested in crops

        CropsBlock crop = (CropsBlock) block.getBlock();

        if (crop == null) { return; }   // ...Just as a precaution

        ItemStack seedStack = crop.getItem(event.getWorld(), event.getPos(), block);

        if (seedStack == null || seedStack.getItem() == null) { return; }	// Crops by default seem to return an itemstack of their seeds. If they don't have any we won't try to harvest it

        int fortune = 0;

        if (event.getPlayer() != null) {
			ItemStack stack = event.getPlayer().getHeldItemMainhand();
			fortune = EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE, stack);
        }
        
        IntegerProperty age = crop.getAgeProperty();

        boolean cancelled = this.tryDropAndResetBlock(event.getWorld(), event.getPos(), age, crop, fortune);
        
        if (cancelled) { event.setCanceled(true); }
        
    }

    @SubscribeEvent
	public void onBlockBreak(BreakEvent event) {
        if (event.getWorld().isRemote()) { return; } 						// Not doing this on client side. (Seems to be fired on server side only by default already.)
		if (event.getPlayer() == null) { return; }						// Who...?
        if (event.getPlayer().isCreative()) { return; }	                // Not interfering with creative mode
        if (event.getPlayer().isSpectator()) { return; }                // Not doing anything for spectator accounts
        if (event.getPlayer().isSneaking()) { return; }	                // Not interfering like this.

        BlockState block = event.getWorld().getBlockState(event.getPos());
        
        if (block == null) { return; }      // ...The block doesn't exist?
		if (!(block.getBlock() instanceof CocoaBlock)) { return; } // Not what we're looking for

        CocoaBlock cocoa = (CocoaBlock) block.getBlock();
        
        if (cocoa == null) { return; }    // ...Just as a precaution
        
		int fortune = 0;

		if (event.getPlayer() != null) {
			ItemStack stack = event.getPlayer().getHeldItemMainhand();
			fortune = EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE, stack);
		}

		boolean cancelled = this.tryDropAndResetBlock((World)event.getWorld(), event.getPos(), cocoa.AGE, cocoa, fortune);

		if (cancelled) { event.setCanceled(true); }
	}

    // Only does so if the block has reached max age
	// Returns true if we succeeded and the event should be cancelled.
	private boolean tryDropAndResetBlock(World world, BlockPos pos, IntegerProperty ageProperty, Block block, int fortune) {
		BlockState blockState = world.getBlockState(pos);

		if (blockState == null) { return false; }

        int maxAge = Collections.max(ageProperty.getAllowedValues());
        int age = 0;    // Initialise age as zero

        try {   // To fix issue https://github.com/Domochevsky/minecraft-gentleharvest/issues/2
            age = blockState.get(ageProperty);
        } catch (IllegalArgumentException e) {
            // LOGGER.info(e.getMessage());
            return false;
        }
        

		if (age < maxAge) { return false; } // Not yet.

        // block.spawnDrops(world, pos, blockState, fortune);e
        Block.spawnDrops(blockState, world, pos);

		// Step 2, reset the crop's age
		world.setBlockState(pos, blockState.with(ageProperty, 0)); // Should set the block it back to default, modifying only that particular property

		return true;
	}

}

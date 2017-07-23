package net.wwsf.domochevsky.gentleharvest;

import java.util.Collections;

import net.minecraft.block.Block;
import net.minecraft.block.BlockBeetroot;
import net.minecraft.block.BlockCocoa;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.init.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class Event_Listener 
{
	@SubscribeEvent
	public void onPlayerInteract(RightClickBlock event)
	{
		if (event.getWorld().isRemote) { return; } 								// Not doing this on client side. (Seems to be fired on server side only by default already.)
		if (event.getEntityPlayer() == null) { return; }						// Who...?
		if (event.getEntityPlayer().capabilities.isCreativeMode) { return; }	// Not interfering with creative mode
		if (event.getHand() != EnumHand.MAIN_HAND) { return; }					// Only for the main hand

		if (event.getEntityPlayer().isSneaking()) { return; }	// Not interfering like this.

		IBlockState block = event.getWorld().getBlockState(event.getPos());

		if (block == null) { return; }// Nevermind...?

		if (!(block.getBlock() instanceof BlockCrops)) { return; }	// Not what I'm looking for.

		BlockCrops crop = (BlockCrops) block.getBlock();

		if (crop == null) { return; }	// ...how?

		ItemStack seedStack = crop.getItem(event.getWorld(), event.getPos(), block);

		if (seedStack == null || seedStack.getItem() == null) { return; }	// Crops by default seem to return an itemstack of their seeds. If they don't have any we won't try to harvest it.

		int fortune = 0;

		if (event.getEntityPlayer() != null)
		{
			ItemStack stack = event.getEntityPlayer().getHeldItemMainhand();
			fortune = EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE, stack);
		}
		
		PropertyInteger age;
		
		if (block.getBlock() instanceof BlockBeetroot)
		{
			// This is a beetroot, which for some reason doesn't use the same age property as the other crops.
			BlockBeetroot root = (BlockBeetroot) block.getBlock();
			
			age = root.BEETROOT_AGE;
		}
		else
		{
			// A regular crop
			age = crop.AGE;
		}

		boolean cancelled = this.tryDropAndResetBlock(event.getWorld(), event.getPos(), age, crop, fortune);

		if (cancelled) { event.setCanceled(true); }

		// SFX
		event.getEntityPlayer().swingArm(EnumHand.MAIN_HAND);
	}


	@SubscribeEvent
	public void onBlockBreak(BreakEvent event)
	{
		// Looking for cocoa blocks, too
		if (event.getWorld().isRemote) { return; } // Not doing this on client side. (Seems to be fired on server side only by default already.)
		if (event.getPlayer() != null && event.getPlayer().capabilities.isCreativeMode) { return; } // Not interfering with creative mode

		if (event.getPlayer().isSneaking()) { return; }	// Not interfering like this.

		IBlockState block = event.getWorld().getBlockState(event.getPos());

		//System.out.println("[GENTLE HARVEST] 1");

		if (block == null) { return; } // Nevermind...?
		if (!(block.getBlock() instanceof BlockCocoa)) { return; } // Not what we're looking for

		// Alright, making it happen

		BlockCocoa cocoa = (BlockCocoa) block.getBlock();

		if (cocoa == null) { return; }	// Um. How?

		int fortune = 0;

		if (event.getPlayer() != null)
		{
			ItemStack stack = event.getPlayer().getHeldItemMainhand();	// Do we need anything other than the main hand fortune?
			fortune = EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE, stack);
		}

		boolean cancelled = this.tryDropAndResetBlock(event.getWorld(), event.getPos(), cocoa.AGE, cocoa, fortune);

		if (cancelled) { event.setCanceled(true); }
	}


	// Only does so if the block has reached max age
	// Returns true if we succeeded and the event should be cancelled.
	private boolean tryDropAndResetBlock(World world, BlockPos pos, IProperty<Integer> property, Block block, int fortune)
	{
		IBlockState blockState = world.getBlockState(pos);

		if (blockState == null) { return false; }

		int maxAge = Collections.max(property.getAllowedValues());
		int age = blockState.getValue(property);

		if (age < maxAge) { return false; } // Not yet.

		block.dropBlockAsItem(world, pos, blockState, fortune);

		// Step 2, reset the crop's age
		world.setBlockState(pos, blockState.withProperty(property, 0)); // Should set the block it back to default, modifying only that particular property

		return true;
	}
}

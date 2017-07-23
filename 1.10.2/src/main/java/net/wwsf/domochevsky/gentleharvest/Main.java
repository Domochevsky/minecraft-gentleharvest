package net.wwsf.domochevsky.gentleharvest;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = "harvestchevsky", name = "Gentle Harvest", version = "b9", acceptableRemoteVersions = "*", canBeDeactivated = true, acceptedMinecraftVersions="[1.9,1.10.2,1.11,1.11.2,1.12)")
public class Main
{
	@EventHandler
	public void preInit(FMLPreInitializationEvent event)
	{
		MinecraftForge.EVENT_BUS.register(new Event_Listener()); // The key to what we're doing here. Reacting to a particular event.
	}
}

/*
b1
	- Initial Release

b2
	- Updated to be compatible with Forge 1811

b3
	- Updated to be compatible with Forge 1854
	- Changed the harvesting to a right click, now that this event is functional in Forge. A left click punches the crop out as normal.

b4
	- Added a line to ensure that the mod is only required on server-side

b5
- The right-click event should now only be fired once (right hand) instead of twice (right and left hand)
- Ignoring crouching players now. Ideally this will allow other mod-added crops with their own right-click functions to do their thing.

b6
- Did some minor adjustments, to be compatible with Forge build 1907.
- Not harvesting a plant that returns nothing from getItem() anymore. Should prevent harvesting of thing that aren't meant to be replanted.
  getItem() is a bit of a workaround, since it by default returns an itemstack of getSeed(), which is what I actually want. (Who made that protected?)

b7
- Changed the allowed Minecraft Versions to 1.9.* (Was previously 1.9 EXACTLY.) to make it officially compatible with 1.9.4.

b8
- Updated to 1.10.*
- Added compatibility for beetroot. So that should be harvestable now too. (For some reason they're using a different age property than every other crops.)
b9
- Updated to be compatible with Forge 2316

 */

package net.wwsf.domochevsky.gentleharvest;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = "harvestchevsky", name = "Gentle Harvest", version = "b1", acceptableRemoteVersions = "*", canBeDeactivated = true)
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
	- 1.11.2 forge update 2421


 */

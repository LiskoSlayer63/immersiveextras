package lizcraft.immersiveextras.common;

import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.WorldTickEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class CommonEventHandler{
	@SubscribeEvent
	public void onSave(WorldEvent.Save event)
	{
		if(!event.getWorld().isClientSide())
		{
			//IPSaveData.markInstanceAsDirty();
		}
	}
	
	@SubscribeEvent
	public void onUnload(WorldEvent.Unload event)
	{
		if(!event.getWorld().isClientSide())
		{
			//IPSaveData.markInstanceAsDirty();
		}
	}
	
	@SubscribeEvent
	public void worldTickServer(WorldTickEvent event)
	{
		if(event.phase == Phase.END)
		{
			//handleLubricatingMachines(event.world);
		}
	}
}
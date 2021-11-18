package lizcraft.immersiveextras.common;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import lizcraft.immersiveextras.ImmersiveExtras;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class CommonProxy{
	@SuppressWarnings("unused")
	private static final Logger log = LogManager.getLogger(ImmersiveExtras.MODID + "/CommonProxy");
	
	/** Fired at {@link FMLCommonSetupEvent} */
	public void setup() {}
	
	public void registerContainersAndScreens()
	{
		
	}
	
	public void preInit() {}
	
	public void preInitEnd() {}
	
	public void init() {}
	
	public void postInit() {}
	
	/** Fired at {@link FMLLoadCompleteEvent} */
	public void completed() {}
	
	public void serverAboutToStart() {}
	
	public void serverStarting() {}
	
	public void serverStarted() {}
	
	public void openTileScreen(ResourceLocation guiId, TileEntity tileEntity) {}
	
	public World getClientWorld()
	{
		return null;
	}
	
	public PlayerEntity getClientPlayer()
	{
		return null;
	}
}
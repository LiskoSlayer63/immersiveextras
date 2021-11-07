package lizcraft.immersiveextras;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import lizcraft.immersiveextras.common.CommonProxy;
import lizcraft.immersiveextras.common.IExtrasContent;
import lizcraft.immersiveextras.common.IExtrasTileTypes;
import lizcraft.immersiveextras.common.IExtrasNetworkUtils;
import lizcraft.immersiveextras.client.ClientProxy;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.event.server.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(ImmersiveExtras.MODID)
public class ImmersiveExtras
{
	public static final String MODID = "immersiveextras";
	public static final Logger LOGGER = LogManager.getLogger(MODID);

	public static ImmersiveExtras INSTANCE;
	
	public static CommonProxy proxy = DistExecutor.safeRunForDist(() -> ClientProxy::new, () -> CommonProxy::new);
	
	public ImmersiveExtras() 
	{
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::loadComplete);
		
		MinecraftForge.EVENT_BUS.addListener(this::serverStarting);
		MinecraftForge.EVENT_BUS.addListener(this::serverAboutToStart);
		MinecraftForge.EVENT_BUS.addListener(this::serverStarted);
		
		IExtrasContent.populate();
		
		IExtrasTileTypes.REGISTER.register(FMLJavaModLoadingContext.get().getModEventBus());
		
		proxy.registerContainersAndScreens();
	}
	
	public void setup(FMLCommonSetupEvent event)
	{
		proxy.setup();
		
		// ---------------------------
		
		proxy.preInit();
		
		IExtrasContent.preInit();
		
		proxy.preInitEnd();
		
		// ---------------------------
		
		IExtrasContent.init();
		IExtrasNetworkUtils.init();
		
		proxy.init();
		
		// ---------------------------
		
		proxy.postInit();
	}
	
	public void loadComplete(FMLLoadCompleteEvent event)
	{
		proxy.completed();
	}
	
	public void serverAboutToStart(FMLServerAboutToStartEvent event)
	{
		proxy.serverAboutToStart();
	}
	
	public void serverStarting(FMLServerStartingEvent event)
	{
		proxy.serverStarting();
	}
	
	public void serverStarted(FMLServerStartedEvent event)
	{
		proxy.serverStarted();
	}	
}
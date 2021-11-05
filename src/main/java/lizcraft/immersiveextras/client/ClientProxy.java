package lizcraft.immersiveextras.client;

import lizcraft.immersiveextras.common.CommonProxy;
import lizcraft.immersiveextras.common.IExtrasContent;
import lizcraft.immersiveextras.common.blocks.AdvancedComparatorTileEntity;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

//import blusunrize.lib.manual.ManualEntry;
//import blusunrize.lib.manual.Tree.InnerNode;
import lizcraft.immersiveextras.ImmersiveExtras;
import lizcraft.immersiveextras.client.gui.AdvancedComparatorScreen;
import net.minecraftforge.fml.common.Mod;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
//import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = ImmersiveExtras.MODID)
public class ClientProxy extends CommonProxy
{
	@SuppressWarnings("unused")
	private static final Logger log = LogManager.getLogger(ImmersiveExtras.MODID + "/ClientProxy");
	public static final String CAT_IEXTRAS = "iextras";
	
	@Override
	public void setup() {}
	
	@Override
	public void registerContainersAndScreens()
	{
		super.registerContainersAndScreens();
	}
	
	@Override
	public void completed()
	{
		//setupManualPages();
	}
	
	@Override
	public void preInit() {}
	
	@Override
	public void preInitEnd() {}
	
	@Override
	public void init()
	{
		//ClientRegistry.bindTileEntityRenderer(IPTileTypes.AUTOLUBE.get(), AutoLubricatorRenderer::new);
	}
	
	@Override
	public void openTileScreen(ResourceLocation guiId, TileEntity tileEntity)
	{
		if(guiId == IExtrasContent.GUIID_AdvancedComparator && tileEntity instanceof AdvancedComparatorTileEntity)
			Minecraft.getInstance().setScreen(new AdvancedComparatorScreen((AdvancedComparatorTileEntity)tileEntity, tileEntity.getBlockState().getBlock().getName()));
	}
	
	/** ImmersiveExtras's Manual Category */
	//private static InnerNode<ResourceLocation, ManualEntry> IEXTRAS_CATEGORY;
	
	@Override
	public World getClientWorld()
	{
		return Minecraft.getInstance().level;
	}
	
	@Override
	public PlayerEntity getClientPlayer()
	{
		return Minecraft.getInstance().player;
	}
}
package lizcraft.immersiveextras.common;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import lizcraft.immersiveextras.ImmersiveExtras;
import lizcraft.immersiveextras.common.blocks.AdvancedComparatorBlock;
import lizcraft.immersiveextras.common.blocks.RedstoneThresholderBlock;
import net.minecraft.util.ResourceLocation;

public class IExtrasContent 
{
	public static final Logger log = LogManager.getLogger(ImmersiveExtras.MODID + "/Content");
	
	public static final ResourceLocation GUIID_AdvancedComparator = new ResourceLocation(ImmersiveExtras.MODID, "advanced_comparator");
	public static final ResourceLocation GUIID_RedstoneThresholder = new ResourceLocation(ImmersiveExtras.MODID, "redstone_thresholder");
	
	public static AdvancedComparatorBlock advancedComparator;
	public static RedstoneThresholderBlock redstoneThresholder;
	
	public static void populate()
	{
		advancedComparator = new AdvancedComparatorBlock();
		redstoneThresholder = new RedstoneThresholderBlock();
	}
	
	public static void preInit() {}
	
	public static void init() {}
}

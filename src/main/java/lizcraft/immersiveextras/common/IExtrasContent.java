package lizcraft.immersiveextras.common;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import lizcraft.immersiveextras.ImmersiveExtras;
import lizcraft.immersiveextras.common.blocks.AdvancedComparatorBlock;
import net.minecraft.util.ResourceLocation;

public class IExtrasContent 
{
	public static final Logger log = LogManager.getLogger(ImmersiveExtras.MODID + "/Content");
	
	public static final ResourceLocation GUIID_AdvancedComparator = new ResourceLocation(ImmersiveExtras.MODID, "advanced_comparator");
	
	public static AdvancedComparatorBlock advancedComparator;
	
	public static void populate()
	{
		advancedComparator = new AdvancedComparatorBlock();
	}
	
	public static void preInit() {}
	
	public static void init() {}
}

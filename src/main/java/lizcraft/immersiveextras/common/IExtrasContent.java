package lizcraft.immersiveextras.common;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import lizcraft.immersiveextras.ImmersiveExtras;
import lizcraft.immersiveextras.common.blocks.AdvancedComparatorBlock;
import lizcraft.immersiveextras.common.blocks.RedstoneChannelSwitcherBlock;
import lizcraft.immersiveextras.common.blocks.RedstonePulseCounterBlock;
import lizcraft.immersiveextras.common.blocks.RedstonePulseGeneratorBlock;
import lizcraft.immersiveextras.common.blocks.RedstoneThresholderBlock;
import net.minecraft.util.ResourceLocation;

public class IExtrasContent 
{
	public static final Logger log = LogManager.getLogger(ImmersiveExtras.MODID + "/Content");
	
	public static final ResourceLocation GUIID_AdvancedComparator = new ResourceLocation(ImmersiveExtras.MODID, "advanced_comparator");
	public static final ResourceLocation GUIID_RedstoneThresholder = new ResourceLocation(ImmersiveExtras.MODID, "redstone_thresholder");
	public static final ResourceLocation GUIID_RedstonePulseCounter = new ResourceLocation(ImmersiveExtras.MODID, "redstone_pulsecounter");
	public static final ResourceLocation GUIID_RedstonePulseGenerator = new ResourceLocation(ImmersiveExtras.MODID, "redstone_pulsegenerator");
	public static final ResourceLocation GUIID_RedstoneChannelSwitcher = new ResourceLocation(ImmersiveExtras.MODID, "redstone_channelswitcher");
	
	public static AdvancedComparatorBlock advancedComparator;
	public static RedstoneThresholderBlock redstoneThresholder;
	public static RedstonePulseCounterBlock redstonePulseCounter;
	public static RedstonePulseGeneratorBlock redstonePulseGenerator;
	public static RedstoneChannelSwitcherBlock redstoneChannelSwitcher;
	
	public static void populate()
	{
		advancedComparator = new AdvancedComparatorBlock();
		redstoneThresholder = new RedstoneThresholderBlock();
		redstonePulseCounter = new RedstonePulseCounterBlock();
		redstonePulseGenerator = new RedstonePulseGeneratorBlock();
		redstoneChannelSwitcher = new RedstoneChannelSwitcherBlock();
	}
	
	public static void preInit() {}
	
	public static void init() {}
	
	public static ResourceLocation makeGuiTextureLocation(String name)
	{
		return new ResourceLocation(ImmersiveExtras.MODID, "textures/gui/" + name  + ".png");
	}
	
	public static ResourceLocation getResourceLocation(String name)
	{
		return new ResourceLocation(ImmersiveExtras.MODID, name);
	}
}

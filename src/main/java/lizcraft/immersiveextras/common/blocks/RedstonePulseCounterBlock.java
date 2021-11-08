package lizcraft.immersiveextras.common.blocks;

import lizcraft.immersiveextras.common.IExtrasTileTypes;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.block.material.PushReaction;
import net.minecraftforge.common.ToolType;

public class RedstonePulseCounterBlock extends IExtrasTileBlockBase<RedstonePulseCounterTileEntity>
{
private static final Material material = new Material(MaterialColor.METAL, false, false, true, true, false, false, PushReaction.BLOCK);
	
	public RedstonePulseCounterBlock() 
	{
		super("redstone_pulsecounter", IExtrasTileTypes.REDSTONE_PULSECOUNTER,
				Block.Properties.of(material)
				.strength(5.0F, 6.0F)
				.harvestTool(ToolType.PICKAXE)
				.sound(SoundType.METAL)
				.isRedstoneConductor((s, r, p) -> false));
	}
}

package lizcraft.immersiveextras.common.blocks;

import java.util.function.Supplier;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.common.blocks.IETileProviderBlock;
import lizcraft.immersiveextras.ImmersiveExtras;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;

public class IExtrasTileBlockBase<T extends TileEntity> extends IETileProviderBlock<T>
{
	public IExtrasTileBlockBase(String name, Supplier<TileEntityType<T>> tileType, Properties blockProps) 
	{
		super(name, tileType, blockProps);
	}
	
	@Override
	public ResourceLocation createRegistryName()
	{
		return new ResourceLocation(ImmersiveExtras.MODID, name);
	}

	
	@Override
	protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder)
	{
		super.createBlockStateDefinition(builder);
		builder.add(IEProperties.FACING_ALL);
	}
}

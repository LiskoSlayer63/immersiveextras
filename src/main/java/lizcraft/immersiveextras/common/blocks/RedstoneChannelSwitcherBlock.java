package lizcraft.immersiveextras.common.blocks;

import blusunrize.immersiveengineering.api.IEProperties;
import lizcraft.immersiveextras.common.IExtrasTileTypes;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.block.material.PushReaction;
import net.minecraft.state.StateContainer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.common.ToolType;

public class RedstoneChannelSwitcherBlock extends IExtrasTileBlockBase<RedstoneChannelSwitcherTileEntity>
{
	private static final Material material = new Material(MaterialColor.METAL, false, false, true, true, false, false, PushReaction.BLOCK);
    
	private static final VoxelShape shape_north = Block.box(0D, 0D, 5D, 16D, 16D, 16D);
	private static final VoxelShape shape_south = Block.box(0D, 0D, 0D, 16D, 16D, 11D);
	private static final VoxelShape shape_east = Block.box(0D, 0D, 0D, 11D, 16D, 16D);
	private static final VoxelShape shape_west = Block.box(5D, 0D, 0D, 16D, 16D, 16D);
	private static final VoxelShape shape_down = Block.box(0D, 5D, 0D, 16D, 16D, 16D);
	private static final VoxelShape shape_up = Block.box(0D, 0D, 0D, 16D, 11D, 16D);
	
	public RedstoneChannelSwitcherBlock() 
	{
		super("redstone_channelswitcher", IExtrasTileTypes.REDSTONE_CHANNELSWITCHER,
				Block.Properties.of(material)
				.strength(5.0F, 6.0F)
				.harvestTool(ToolType.PICKAXE)
				.sound(SoundType.METAL)
				.isRedstoneConductor((s, r, p) -> false));
	}
	
	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) 
	{
		switch((Direction)state.getValue(IEProperties.FACING_ALL))
		{
		case NORTH:
			return shape_north;
		case EAST:
			return shape_east;
		case SOUTH:
			return shape_south;
		case WEST:
			return shape_west;
		case DOWN:
			return shape_down;
		case UP:
		default:
			return shape_up;
		}
	}
	
	@Override
	protected BlockState getInitDefaultState()
	{
		BlockState ret = super.getInitDefaultState();
		
		ret.setValue(ROTATION, BlockRotation.DEG_0);
		
		return ret;
	}
	
	@Override 
	protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder)
	{
		super.createBlockStateDefinition(builder);
		builder.add(ROTATION);
	}
}

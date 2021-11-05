package lizcraft.immersiveextras.common.blocks;

import javax.annotation.Nonnull;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.utils.SafeChunkUtils;
import blusunrize.immersiveengineering.api.wires.ConnectionPoint;
import blusunrize.immersiveengineering.api.wires.GlobalWireNetwork;
import blusunrize.immersiveengineering.api.wires.redstone.CapabilityRedstoneNetwork;
import blusunrize.immersiveengineering.api.wires.redstone.CapabilityRedstoneNetwork.RedstoneBundleConnection;
import blusunrize.immersiveengineering.api.wires.redstone.RedstoneNetworkHandler;
import blusunrize.immersiveengineering.common.blocks.IEBaseTileEntity;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IStateBasedDirectional;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IComparatorOverride;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IScrewdriverInteraction;
import lizcraft.immersiveextras.ImmersiveExtras;
import lizcraft.immersiveextras.common.IExtrasContent;
import lizcraft.immersiveextras.common.IExtrasTileTypes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DyeColor;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.Property;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

public class AdvancedComparatorTileEntity extends IEBaseTileEntity implements ITickableTileEntity, IStateBasedDirectional, IScrewdriverInteraction, IComparatorOverride
{
	public static enum ComparatorMode 
	{
		MEDIAN,
		SPLIT
	}
	
	private final LazyOptional<RedstoneBundleConnection> redstoneCap = registerConstantCap(
			new RedstoneBundleConnection()
			{
				@Override
				public void updateInput(byte[] signals, ConnectionPoint cp, Direction side)
				{
					if(!level.isClientSide && SafeChunkUtils.isChunkSafe(level, worldPosition))
					{
						if (comparatorMode == ComparatorMode.SPLIT)
						{
							for (int i = 0; i < redstoneValues.size(); i++)
								if (redstoneColors.get(i))
									signals[i] = (byte)getMaxRSInput();
							
							rsDirty = false;
						}
					}
				}
				
				@Override
				public void onChange(ConnectionPoint cp, RedstoneNetworkHandler handler, Direction side)
				{
					if(!level.isClientSide && SafeChunkUtils.isChunkSafe(level, worldPosition))
					{
						if (comparatorMode == ComparatorMode.MEDIAN)
						{
							for (int i = 0; i < redstoneValues.size(); i++)
								if (redstoneColors.get(i))
									redstoneValues.set(i, handler.getValue(i));
							
							setChanged();
							markContainingBlockForUpdate(getBlockState());
							level.updateNeighborsAt(worldPosition, getBlockState().getBlock());
						}
					}
				}
			}
	);
	
	public NonNullList<Boolean> redstoneColors = NonNullList.withSize(DyeColor.values().length, false);
	public NonNullList<Byte> redstoneValues = NonNullList.withSize(DyeColor.values().length, (byte)0);
	public ComparatorMode comparatorMode = ComparatorMode.MEDIAN;
	
	protected GlobalWireNetwork globalNet;
	protected boolean rsDirty = false;
	
	public AdvancedComparatorTileEntity() 
	{
		super(IExtrasTileTypes.ADVANCED_COMPARATOR.get());
	}
	
	protected int calculateRedstone()
	{
		int value = 0;
		int count = 0;
		
		for (int i = 0; i < this.redstoneColors.size(); i++)
		{
			if (this.redstoneColors.get(i))
			{
				value += this.redstoneValues.get(i);
				count++;
			}
		}
		
		return count == 0 || value == 0 ? 0 : value / count;
	}
	
	protected void updateAfterConfigure()
	{
		setChanged();
		
		try 
		{
			globalNet.getLocalNet(worldPosition.relative(getFacing()))
				.getHandler(RedstoneNetworkHandler.ID, RedstoneNetworkHandler.class)
				.updateValues();
		} 
		finally {}
		
		this.markContainingBlockForUpdate(null);
		level.blockEvent(getBlockPos(), this.getBlockState().getBlock(), 254, 0);
	}
	
	protected boolean isRSInput()
	{
		return this.comparatorMode == ComparatorMode.SPLIT;
	}
	
	@Override
	public void tick()
	{
		if (hasLevel() && !level.isClientSide && rsDirty)
		{	
			try 
			{
				globalNet.getLocalNet(worldPosition.relative(getFacing()))
					.getHandler(RedstoneNetworkHandler.ID, RedstoneNetworkHandler.class)
					.updateValues();
			} 
			finally {}
		}
	}
	
	@Override
	public Property<Direction> getFacingProperty()
	{
		return IEProperties.FACING_ALL;
	}

	@Override
	public PlacementLimitation getFacingLimitation()
	{
		return PlacementLimitation.PISTON_LIKE;
	}

	@Override
	public boolean mirrorFacingOnPlacement(LivingEntity placer)
	{
		return true;
	}

	@Override
	public void readCustomNBT(CompoundNBT nbt, boolean descPacket) 
	{
		this.comparatorMode = ComparatorMode.values()[nbt.getByte("comparatorMode")];
		
		byte[] redstoneConfig = nbt.getByteArray("redstoneColors");
		
		for(int i = 0; i < redstoneConfig.length; i++)
			this.redstoneColors.set(i, redstoneConfig[i] != 0);
	}

	@Override
	public void writeCustomNBT(CompoundNBT nbt, boolean descPacket) 
	{
		nbt.putByte("comparatorMode", (byte)this.comparatorMode.ordinal());
		
		byte[] redstoneConfig = new byte[this.redstoneColors.size()];
		
		for(int i = 0; i < redstoneConfig.length; i++)
			redstoneConfig[i] = (byte)(this.redstoneColors.get(i) ? 1 : 0);
		
		nbt.putByteArray("redstoneColors", redstoneConfig);
	}
	
	@Override
	public void receiveMessageFromClient(CompoundNBT message)
	{
		if(message.contains("comparatorMode"))
			this.comparatorMode = ComparatorMode.values()[message.getByte("comparatorMode")];
		
		if(message.contains("redstoneColor") && message.contains("redstoneValue"))
			this.redstoneColors.set(message.getInt("redstoneColor"), message.getBoolean("redstoneValue"));
		
		updateAfterConfigure();
	}
	
	@Nonnull
	@Override
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, Direction facing)
	{
		if(capability == CapabilityRedstoneNetwork.REDSTONE_BUNDLE_CONNECTION && facing == getFacing())
			return redstoneCap.cast();
		
		return super.getCapability(capability, facing);
	}
	
	@Override
	public int getRSInput(@Nonnull Direction side)
	{
		return this.isRSInput() && side != getFacing().getOpposite() ? super.getRSInput(side) : 0;
	}
	
	@Override
	public boolean isRSPowered()
	{
		return false;
	}

	@Override
	public ActionResultType screwdriverUseSide(Direction arg0, PlayerEntity arg1, Hand arg2, Vector3d arg3) 
	{
		if(level.isClientSide)
			ImmersiveExtras.proxy.openTileScreen(IExtrasContent.GUIID_AdvancedComparator, this);
		
		return ActionResultType.SUCCESS;
	}
	
	@Override
	public void onNeighborBlockChange(BlockPos otherPos)
	{
		int oldRSIn = getMaxRSInput();
		
		super.onNeighborBlockChange(otherPos);
		
		if(isRSInput() && oldRSIn != getMaxRSInput())
			rsDirty = true;
	}
	
	@Override
	public void setLevelAndPosition(@Nonnull World worldIn, @Nonnull BlockPos pos)
	{
		super.setLevelAndPosition(worldIn, pos);
		
		globalNet = GlobalWireNetwork.getNetwork(worldIn);
	}
	
	@Override
	public boolean canHammerRotate(Direction side, Vector3d hit, LivingEntity entity)
	{
		return false;
	}

	@Override
	public boolean canRotate(Direction axis)
	{
		return false;
	}

	@Override
	public int getComparatorInputOverride() 
	{
		return calculateRedstone();
	}
}

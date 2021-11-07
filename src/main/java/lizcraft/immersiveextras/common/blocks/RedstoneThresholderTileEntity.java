package lizcraft.immersiveextras.common.blocks;

import javax.annotation.Nonnull;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.common.blocks.IEBaseTileEntity;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IRedstoneOutput;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IScrewdriverInteraction;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IStateBasedDirectional;
import lizcraft.immersiveextras.ImmersiveExtras;
import lizcraft.immersiveextras.common.IExtrasContent;
import lizcraft.immersiveextras.common.IExtrasTileTypes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.Property;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;

public class RedstoneThresholderTileEntity extends IEBaseTileEntity implements ITickableTileEntity, IStateBasedDirectional, IScrewdriverInteraction, IRedstoneOutput
{
	public static enum ThresholdMode 
	{
		UPPER,
		LOWER
	}
	
	public ThresholdMode thresholdMode = ThresholdMode.UPPER;
	public int thresholdValue = 1;
	
	private Direction oldFacing;
	
	public RedstoneThresholderTileEntity() 
	{
		super(IExtrasTileTypes.REDSTONE_THRESHOLDER.get());
		
		this.oldFacing = getFacing();
	}
	
	protected boolean isThresholdReached(int input)
	{
		if (this.thresholdMode == ThresholdMode.LOWER)
			return input < this.thresholdValue;
		else
			return input >= this.thresholdValue;
	}
	
	protected int calculateRedstone()
	{
		int input = getMaxRSInput();
		
		return isThresholdReached(input) ? input : 0;
	}
	
	protected void updateAfterConfigure()
	{
		setChanged();
		
		this.markContainingBlockForUpdate(null);
		level.blockEvent(getBlockPos(), this.getBlockState().getBlock(), 254, 0);
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
	public void tick() 
	{
		if (oldFacing != getFacing())
		{
			oldFacing = getFacing();
			
			updateAfterConfigure();
		}
	}

	@Override
	public void readCustomNBT(CompoundNBT nbt, boolean descPacket) 
	{
		this.thresholdMode = ThresholdMode.values()[nbt.getByte("thresholdMode")];
		this.thresholdValue = nbt.getByte("thresholdValue");
	}

	@Override
	public void writeCustomNBT(CompoundNBT nbt, boolean descPacket) 
	{
		nbt.putByte("thresholdMode", (byte)this.thresholdMode.ordinal());
		nbt.putByte("thresholdValue", (byte)this.thresholdValue);
	}
	
	@Override
	public void receiveMessageFromClient(CompoundNBT message)
	{
		ImmersiveExtras.LOGGER.info("Message received!");
		
		if(message.contains("thresholdMode"))
			this.thresholdMode = ThresholdMode.values()[message.getByte("thresholdMode")];
		
		if(message.contains("thresholdValue"))
			this.thresholdValue = message.getByte("thresholdValue");
		
		
		updateAfterConfigure();
	}
	
	@Override
	public boolean canConnectRedstone(Direction from) 
	{
		return from == getFacing() || from == getFacing().getOpposite();
	}
	
	@Override
	public int getRSInput(@Nonnull Direction side)
	{
		return side == getFacing() ? super.getRSInput(side) : 0;
	}

	@Override
	public int getStrongRSOutput(Direction from) 
	{
		return from == getFacing() ? calculateRedstone() : 0;
	}

	@Override
	public int getWeakRSOutput(Direction from) 
	{
		return from == getFacing() ? calculateRedstone() : 0;
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
			ImmersiveExtras.proxy.openTileScreen(IExtrasContent.GUIID_RedstoneThresholder, this);
		
		return ActionResultType.SUCCESS;
	}
	
	@Override
	public boolean canHammerRotate(Direction side, Vector3d hit, LivingEntity entity)
	{
		return true;
	}

	@Override
	public boolean canRotate(Direction axis)
	{
		return true;
	}
	
	@Override
	public void onNeighborBlockChange(BlockPos otherPos)
	{
		int oldRSIn = getMaxRSInput();
		
		super.onNeighborBlockChange(otherPos);
		
		if(oldRSIn != getMaxRSInput())
			this.markContainingBlockForUpdate(null);
	}
}

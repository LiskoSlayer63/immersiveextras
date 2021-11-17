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
import net.minecraft.util.math.vector.Vector3d;

public class RedstonePulseGeneratorTileEntity extends IEBaseTileEntity implements ITickableTileEntity, IStateBasedDirectional, IScrewdriverInteraction, IRedstoneOutput
{
	public static enum ControlSource
	{
		INTERNAL,
		EXTERNAL
	}
	
	private Direction oldFacing;
	
	private boolean isUp = false;
	private int ticksFromLastChange = 0;
	
	public ControlSource speedSource = ControlSource.INTERNAL;
	public int pulseSpeed = 1;

	public RedstonePulseGeneratorTileEntity()
	{
		super(IExtrasTileTypes.REDSTONE_PULSEGENERATOR.get());

		this.oldFacing = getFacing();
	}
	
	public int getPulseWidth(int speed)
	{
		return interpolateDuration(43, 5, speed / 15f) / 2 * 2;
	}
	
	private int interpolateDuration(int min, int max, float multi)
	{
		return (int)Math.floor(min + ((max - min) * multi));
	}
	
	private void doPulseLogic(int power)
	{
		boolean oldStatus = isUp;
		
		if (power <= 0)
		{
			isUp = false;
			ticksFromLastChange = 0;
		}
		else
		{
			int speed = this.speedSource == ControlSource.INTERNAL ? pulseSpeed : power;
			int stateDuration = getPulseWidth(speed) / 2;
			
			ticksFromLastChange++;
			
			if (ticksFromLastChange >= stateDuration)
			{
				isUp = !isUp;
				ticksFromLastChange = 0;
			}
		}
		
		if (isUp != oldStatus)
			updateAfterConfigure();
	}
	
	protected void updateAfterConfigure()
	{
		setChanged();
		
		this.markContainingBlockForUpdate(null);
		level.blockEvent(getBlockPos(), this.getBlockState().getBlock(), 254, 0);
	}

	@Override
	public void tick()
	{
		int power = getMaxRSInput();
		
		doPulseLogic(power);
		
		if (oldFacing != getFacing())
		{
			oldFacing = getFacing();

			updateAfterConfigure();
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
		return placer.isShiftKeyDown();
	}

	@Override
	public boolean canConnectRedstone(Direction arg0) 
	{
		return true;
	}

	@Override
	public int getRSInput(@Nonnull Direction side)
	{
		return side != this.getFacing() ? super.getRSInput(side) : 0;
	}

	@Override
	public int getStrongRSOutput(Direction from) 
	{
		return from.getOpposite() == this.getFacing() && isUp ? 15 : 0;
	}

	@Override
	public int getWeakRSOutput(Direction from)
	{
		return from.getOpposite() == this.getFacing() && isUp ? 15 : 0;
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
			ImmersiveExtras.proxy.openTileScreen(IExtrasContent.GUIID_RedstonePulseGenerator, this);
		
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
	public void readCustomNBT(CompoundNBT nbt, boolean descPacket) 
	{
		this.speedSource = ControlSource.values()[nbt.getByte("speedSource")];
		this.pulseSpeed = nbt.getByte("pulseSpeed");
	}

	@Override
	public void writeCustomNBT(CompoundNBT nbt, boolean descPacket) 
	{
		nbt.putByte("speedSource", (byte)this.speedSource.ordinal());
		nbt.putByte("pulseSpeed", (byte)this.pulseSpeed);
	}
	
	@Override
	public void receiveMessageFromClient(CompoundNBT message)
	{
		if(message.contains("speedSource"))
			this.speedSource = ControlSource.values()[message.getByte("speedSource")];
		
		if(message.contains("pulseSpeed"))
			this.pulseSpeed = message.getByte("pulseSpeed");
		
		updateAfterConfigure();
	}

}

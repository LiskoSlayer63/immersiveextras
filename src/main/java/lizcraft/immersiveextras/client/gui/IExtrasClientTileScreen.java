package lizcraft.immersiveextras.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;

import blusunrize.immersiveengineering.client.gui.ClientTileScreen;
import blusunrize.immersiveengineering.client.gui.IEContainerScreen;
import blusunrize.immersiveengineering.common.blocks.IEBaseTileEntity;
import blusunrize.immersiveengineering.common.network.MessageTileSync;
import lizcraft.immersiveextras.ImmersiveExtras;
import lizcraft.immersiveextras.common.IExtrasContent;
import lizcraft.immersiveextras.common.IExtrasNetworkUtils.NetworkHandler;
import net.minecraft.client.util.InputMappings;
import net.minecraft.client.util.InputMappings.Input;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import static blusunrize.immersiveengineering.client.ClientUtils.mc;

public abstract class IExtrasClientTileScreen<T extends IEBaseTileEntity> extends ClientTileScreen<T>
{
	protected static final ResourceLocation TEXTURE = IExtrasContent.makeGuiTextureLocation("redstone_buttons");
	protected static final ResourceLocation TEXTURE_IE = IEContainerScreen.makeTextureLocation("redstone_configuration");
	
	protected final String registryName;
	
	public IExtrasClientTileScreen(String name, T tileEntity, ITextComponent title) 
	{
		super(tileEntity, title);
		this.registryName = name;
	}
	
	protected void sendConfig(CompoundNBT message)
	{
		NetworkHandler.sendToServer(new MessageTileSync(this.tileEntity, message));
	}

	protected ITextComponent getTranslationComponent(String key, Object... args)
	{
		return new TranslationTextComponent("gui." + ImmersiveExtras.MODID + "." + this.registryName + "." + key, args);
	}
	
	protected void sendConfig(String key, boolean value)
	{
		CompoundNBT message = new CompoundNBT();
		message.putBoolean(key, value);
		sendConfig(message);
	}
	
	protected void sendConfig(String key, int value)
	{
		sendConfig(key, (byte)value);
	}
	
	protected void sendConfig(String key, byte value)
	{
		CompoundNBT message = new CompoundNBT();
		message.putByte(key, value);
		sendConfig(message);
	}
	
	@Override
	public void init()
	{
		super.init();
		
		mc().keyboardHandler.setSendRepeatsToGui(true);
	}
	
	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers)
	{
		Input mouseKey = InputMappings.getKey(keyCode, scanCode);
		
		if(mc().options.keyInventory.isActiveAndMatches(mouseKey))
		{
			this.onClose();
			return true;
		}
		return super.keyPressed(keyCode, scanCode, modifiers);
	}
	
	@Override
	protected void drawGuiContainerBackgroundLayer(MatrixStack transform, int mouseX, int mouseY, float partialTick)
	{

	}
}

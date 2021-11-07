package lizcraft.immersiveextras.client.gui;

import blusunrize.immersiveengineering.api.client.TextUtils;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.gui.ClientTileScreen;
import blusunrize.immersiveengineering.client.gui.IEContainerScreen;
import blusunrize.immersiveengineering.client.gui.elements.GuiButtonBoolean;
import blusunrize.immersiveengineering.client.gui.elements.GuiButtonState;
import blusunrize.immersiveengineering.common.network.MessageTileSync;
import lizcraft.immersiveextras.ImmersiveExtras;
import lizcraft.immersiveextras.common.blocks.RedstoneThresholderTileEntity.ThresholdMode;
import lizcraft.immersiveextras.common.IExtrasNetworkUtils.NetworkHandler;
import lizcraft.immersiveextras.common.blocks.RedstoneThresholderTileEntity;
import net.minecraft.client.util.InputMappings;
import net.minecraft.client.util.InputMappings.Input;
import net.minecraft.item.DyeColor;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.client.gui.GuiUtils;

import static blusunrize.immersiveengineering.client.ClientUtils.mc;

import java.util.ArrayList;
import java.util.function.Consumer;

import com.mojang.blaze3d.matrix.MatrixStack;

public class RedstoneThresholderScreen extends ClientTileScreen<RedstoneThresholderTileEntity> 
{
	public RedstoneThresholderScreen(RedstoneThresholderTileEntity tileEntity, ITextComponent title) 
	{
		super(tileEntity, title);
		
		this.xSize = 100;
		this.ySize = 120;
	}

	private static final ResourceLocation TEXTURE = IEContainerScreen.makeTextureLocation("redstone_configuration");

	private GuiButtonState<ThresholdMode> buttonMode;
	private GuiButtonBoolean[] powerButtons;
	
	@Override
	public void init()
	{
		super.init();
		mc().keyboardHandler.setSendRepeatsToGui(true);
		
		this.buttons.clear();

		buttonMode = new GuiButtonState<ThresholdMode>(guiLeft+41, guiTop+20, 18, 18, new StringTextComponent(""), new ThresholdMode[] { ThresholdMode.UPPER, ThresholdMode.LOWER },
				tileEntity.thresholdMode.ordinal(), TEXTURE, 176, 0, 1,
				btn -> sendConfig("thresholdMode", btn.getNextState().ordinal())
		);
		
		this.addButton(buttonMode);

		powerButtons = new GuiButtonBoolean[15];
		for(int i = 0; i < powerButtons.length; i++)
		{
			final int power = i + 1;
			powerButtons[i] = buildPowerButton(powerButtons, guiLeft+16+(i%5*14), guiTop+44+(i/5*14),
					power <= tileEntity.thresholdValue, power, btn -> sendConfig("thresholdValue", power));
			this.addButton(powerButtons[i]);
		}
	}
	
	public void sendConfig(String key, int value)
	{
		CompoundNBT message = new CompoundNBT();
		message.putByte(key, (byte)value);
		NetworkHandler.sendToServer(new MessageTileSync(tileEntity, message));
	}
	
	@Override
	protected void drawGuiContainerBackgroundLayer(MatrixStack transform, int mouseX, int mouseY, float partialTick)
	{

	}

	@Override
	protected void drawGuiContainerForegroundLayer(MatrixStack transform, int mouseX, int mouseY, float partialTick)
	{
		ArrayList<ITextComponent> tooltip = new ArrayList<>();

		if(buttonMode.isHovered())
		{
			tooltip.add(new TranslationTextComponent("gui." + ImmersiveExtras.MODID + ".redstone_thresholder.mode.title"));
			tooltip.add(TextUtils.applyFormat(
					new TranslationTextComponent("gui." + ImmersiveExtras.MODID + ".redstone_thresholder.mode." + buttonMode.getState().name() + ".name"),
					TextFormatting.GRAY
			));
			tooltip.add(TextUtils.applyFormat(
					new TranslationTextComponent("gui." + ImmersiveExtras.MODID + ".redstone_thresholder.mode." + buttonMode.getState().name() + ".desc"),
					TextFormatting.GRAY, TextFormatting.ITALIC
			));
		}

		for(int i = 0; i < powerButtons.length; i++)
			if(powerButtons[i].isHovered())
			{
				tooltip.add(new TranslationTextComponent("gui." + ImmersiveExtras.MODID + ".redstone_thresholder.power.title"));
				tooltip.add(TextUtils.applyFormat(
						new TranslationTextComponent("gui." + ImmersiveExtras.MODID + ".redstone_thresholder.power.desc", i + 1),
						TextFormatting.GRAY
				));
			}

		if(!tooltip.isEmpty())
			GuiUtils.drawHoveringText(transform, tooltip, mouseX, mouseY, width, height, -1, font);
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

	public static GuiButtonBoolean buildPowerButton(GuiButtonBoolean[] buttons, int posX, int posY, boolean active, int power, Consumer<GuiButtonBoolean> onClick)
	{
		return new GuiButtonBoolean(posX, posY, 12, 12, "", active,
				TEXTURE, 194, 0, 1,
				btn -> {
					onClick.accept((GuiButtonBoolean)btn);
					for (int i = 0; i < buttons.length; i++)
						buttons[i].setStateByInt(i < power ? 1 : 0);
					btn.setStateByInt(0);
				})
		{
			@Override
			protected boolean isValidClickButton(int button)
			{
				return true;
			}

			@Override
			public void render(MatrixStack transform, int mouseX, int mouseY, float partialTicks)
			{
				super.render(transform, mouseX, mouseY, partialTicks);
				if(this.visible)
				{
					int col = DyeColor.GREEN.getColorValue();
					if(!getState())
						col = ClientUtils.getDarkenedTextColour(col);
					col = 0xff000000|col;
					this.fillGradient(transform, x+3, y+3, x+9, y+9, col, col);
				}
			}
		};
	}
}

package lizcraft.immersiveextras.client.gui;

import java.util.ArrayList;
import java.util.function.Consumer;

import com.mojang.blaze3d.matrix.MatrixStack;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.IEEnums.IOSideConfig;
import blusunrize.immersiveengineering.api.client.TextUtils;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.gui.elements.GuiButtonBoolean;
import blusunrize.immersiveengineering.client.gui.elements.GuiButtonState;
import lizcraft.immersiveextras.common.blocks.RedstoneChannelSwitcherTileEntity;
import lizcraft.immersiveextras.common.blocks.RedstoneChannelSwitcherTileEntity.ControlMode;
import net.minecraft.item.DyeColor;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.client.gui.GuiUtils;

public class RedstoneChannelSwitcherScreen extends IExtrasClientTileScreen<RedstoneChannelSwitcherTileEntity> 
{
	public RedstoneChannelSwitcherScreen(RedstoneChannelSwitcherTileEntity tileEntity, ITextComponent title) 
	{
		super("redstone_channelswitcher", tileEntity, title);
		
		this.xSize = 100;
		this.ySize = 120;
	}

	private GuiButtonState<IOSideConfig> buttonIO;
	private GuiButtonState<ControlMode> buttonControl;
	private GuiButtonBoolean[] colorButtons;
	
	@Override
	public void init()
	{
		super.init();
		
		this.buttons.clear();

		buttonIO = new GuiButtonState<>(guiLeft+31, guiTop+20, 18, 18, new StringTextComponent(""), new IOSideConfig[] { IOSideConfig.INPUT, IOSideConfig.OUTPUT },
				tileEntity.ioMode.ordinal()-1, TEXTURE_IE, 176, 0, 1,
				btn -> sendConfig("ioMode", btn.getNextState().ordinal())
		);
		
		this.addButton(buttonIO);

		buttonControl = new GuiButtonState<ControlMode>(guiLeft+51, guiTop+20, 18, 18, new StringTextComponent(""), new ControlMode[] { ControlMode.INTERNAL, ControlMode.EXTERNAL },
				tileEntity.controlMode.ordinal(), TEXTURE, 18, 0, 1,
				btn -> sendConfig("controlMode", btn.getNextState().ordinal())
		);
		
		this.addButton(buttonControl);

		colorButtons = new GuiButtonBoolean[16];
		for(int i = 0; i < colorButtons.length; i++)
		{
			final DyeColor color = DyeColor.byId(i);
			final int channel = i;
			colorButtons[i] = buildColorButton(colorButtons, tileEntity, guiLeft+22+(i%4*14), guiTop+44+(i/4*14),
					i == tileEntity.getChannel(), color, btn -> sendConfig("redstoneChannel", channel));
			this.addButton(colorButtons[i]);
		}
	}
	
	@Override
	protected void drawGuiContainerForegroundLayer(MatrixStack transform, int mouseX, int mouseY, float partialTick)
	{
		ArrayList<ITextComponent> tooltip = new ArrayList<>();

		if(buttonIO.isHovered())
		{
			tooltip.add(getTranslationComponent("ioMode.title"));
			tooltip.add(TextUtils.applyFormat(
					getTranslationComponent("ioMode." + buttonIO.getState().name() + ".name"),
					TextFormatting.GRAY
			));
			tooltip.add(TextUtils.applyFormat(
					getTranslationComponent("ioMode." + buttonIO.getState().name() + ".desc"),
					TextFormatting.GRAY, TextFormatting.ITALIC
			));
		}

		if(buttonControl.isHovered())
		{
			tooltip.add(getTranslationComponent("controlMode.title"));
			tooltip.add(TextUtils.applyFormat(
					getTranslationComponent("controlMode." + buttonControl.getState().name() + ".name"),
					TextFormatting.GRAY
			));
			tooltip.add(TextUtils.applyFormat(
					getTranslationComponent("controlMode." + buttonControl.getState().name() + ".desc"),
					TextFormatting.GRAY, TextFormatting.ITALIC
			));
		}

		for(int i = 0; i < colorButtons.length; i++)
			if(colorButtons[i].isHovered())
			{
				tooltip.add(new TranslationTextComponent(Lib.GUI_CONFIG+"redstone_color"));
				tooltip.add(TextUtils.applyFormat(
						new TranslationTextComponent("color.minecraft."+DyeColor.byId(i).getName()),
						TextFormatting.GRAY
				));
			}

		if(!tooltip.isEmpty())
			GuiUtils.drawHoveringText(transform, tooltip, mouseX, mouseY, width, height, -1, font);
	}
	
	public static GuiButtonBoolean buildColorButton(GuiButtonBoolean[] buttons, RedstoneChannelSwitcherTileEntity tileEntity, int posX, int posY, boolean active, DyeColor color, Consumer<GuiButtonBoolean> onClick)
	{
		return new GuiButtonBoolean(posX, posY, 12, 12, "", active,
				TEXTURE_IE, 194, 0, 1,
				btn -> {
					if(btn.getNextState())
						onClick.accept((GuiButtonBoolean)btn);
					for(int j = 0; j < buttons.length; j++)
						if(j!=color.ordinal())
							buttons[j].setStateByInt(0);
				})
		{
			@Override
			protected boolean isValidClickButton(int button)
			{
				return tileEntity.controlMode == ControlMode.INTERNAL;
			}

			@Override
			public void render(MatrixStack transform, int mouseX, int mouseY, float partialTicks)
			{
				if (tileEntity.controlMode == ControlMode.EXTERNAL)
					for(int j = 0; j < buttons.length; j++)
						buttons[j].setStateByInt(j == tileEntity.getChannel() ? 1 : 0);
				
				super.render(transform, mouseX, mouseY, partialTicks);
				
				if(this.visible)
				{
					int col = color.getColorValue();
					if(!getState())
						col = ClientUtils.getDarkenedTextColour(col);
					col = 0xff000000|col;
					this.fillGradient(transform, x+3, y+3, x+9, y+9, col, col);
				}
			}
		};
	}
}

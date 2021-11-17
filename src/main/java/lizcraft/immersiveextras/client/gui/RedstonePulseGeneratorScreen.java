package lizcraft.immersiveextras.client.gui;

import java.util.ArrayList;
import java.util.function.Consumer;

import com.mojang.blaze3d.matrix.MatrixStack;

import blusunrize.immersiveengineering.api.client.TextUtils;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.gui.elements.GuiButtonBoolean;
import blusunrize.immersiveengineering.client.gui.elements.GuiButtonState;
import lizcraft.immersiveextras.common.blocks.RedstonePulseGeneratorTileEntity;
import lizcraft.immersiveextras.common.blocks.RedstonePulseGeneratorTileEntity.ControlSource;
import net.minecraft.item.DyeColor;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.client.gui.GuiUtils;

public class RedstonePulseGeneratorScreen  extends IExtrasClientTileScreen<RedstonePulseGeneratorTileEntity> 
{
	public RedstonePulseGeneratorScreen(RedstonePulseGeneratorTileEntity tileEntity, ITextComponent title) 
	{
		super("redstone_pulsegenerator", tileEntity, title);
		
		this.xSize = 100;
		this.ySize = 120;
	}

	private GuiButtonState<ControlSource> buttonSource;
	private GuiButtonBoolean[] powerButtons;
	
	@Override
	public void init()
	{
		super.init();
		
		this.buttons.clear();

		buttonSource = new GuiButtonState<ControlSource>(guiLeft+41, guiTop+20, 18, 18, new StringTextComponent(""), new ControlSource[] { ControlSource.INTERNAL, ControlSource.EXTERNAL },
				tileEntity.speedSource.ordinal(), TEXTURE, 18, 0, 1,
				btn -> sendConfig("speedSource", btn.getNextState().ordinal())
		);
		
		this.addButton(buttonSource);

		powerButtons = new GuiButtonBoolean[15];
		for(int i = 0; i < powerButtons.length; i++)
		{
			final int power = i + 1;
			powerButtons[i] = buildPowerButton(powerButtons, tileEntity, guiLeft+16+(i%5*14), guiTop+44+(i/5*14),
					power <= tileEntity.pulseSpeed, power, btn -> sendConfig("pulseSpeed", power));
			this.addButton(powerButtons[i]);
		}
	}

	@Override
	protected void drawGuiContainerForegroundLayer(MatrixStack transform, int mouseX, int mouseY, float partialTick)
	{
		ArrayList<ITextComponent> tooltip = new ArrayList<>();

		if(buttonSource.isHovered())
		{
			tooltip.add(getTranslationComponent("source.title"));
			tooltip.add(TextUtils.applyFormat(
					getTranslationComponent("source." + buttonSource.getState().name() + ".name"),
					TextFormatting.GRAY
			));
			tooltip.add(TextUtils.applyFormat(
					getTranslationComponent("source." + buttonSource.getState().name() + ".desc"),
					TextFormatting.GRAY, TextFormatting.ITALIC
			));
		}

		for(int i = 0; i < powerButtons.length; i++)
			if(powerButtons[i].isHovered())
			{
				tooltip.add(getTranslationComponent("speed.title"));
				tooltip.add(TextUtils.applyFormat(
						getTranslationComponent("speed.desc", tileEntity.getPulseWidth(i + 1)),
						TextFormatting.GRAY
				));
			}

		if(!tooltip.isEmpty())
			GuiUtils.drawHoveringText(transform, tooltip, mouseX, mouseY, width, height, -1, font);
	}
	
	public static GuiButtonBoolean buildPowerButton(GuiButtonBoolean[] buttons, RedstonePulseGeneratorTileEntity tileEntity, int posX, int posY, boolean active, int power, Consumer<GuiButtonBoolean> onClick)
	{
		return new GuiButtonBoolean(posX, posY, 12, 12, "", active,
				TEXTURE_IE, 194, 0, 1,
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
				return tileEntity.speedSource == ControlSource.INTERNAL;
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

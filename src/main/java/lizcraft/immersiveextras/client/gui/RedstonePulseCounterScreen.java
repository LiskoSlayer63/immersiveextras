package lizcraft.immersiveextras.client.gui;

import java.util.ArrayList;
import java.util.function.Consumer;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import com.mojang.blaze3d.systems.RenderSystem;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.client.TextUtils;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.gui.elements.GuiButtonBoolean;
import blusunrize.immersiveengineering.client.gui.elements.GuiButtonIE;
import blusunrize.immersiveengineering.client.gui.elements.GuiButtonState;
import lizcraft.immersiveextras.common.blocks.RedstonePulseCounterTileEntity;
import lizcraft.immersiveextras.common.blocks.RedstonePulseCounterTileEntity.CountDirection;
import lizcraft.immersiveextras.common.blocks.RedstonePulseCounterTileEntity.CountEdge;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.item.DyeColor;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.client.gui.GuiUtils;

public class RedstonePulseCounterScreen extends IExtrasClientTileScreen<RedstonePulseCounterTileEntity> 
{
	public RedstonePulseCounterScreen(RedstonePulseCounterTileEntity tileEntity, ITextComponent title) 
	{
		super("redstone_pulsecounter", tileEntity, title);
		
		this.xSize = 100;
		this.ySize = 120;
	}

	private GuiButtonState<CountDirection> buttonDirection;
	private GuiButtonState<CountEdge> buttonEdge;
	private GuiButtonBoolean buttonLoop;
	private GuiButtonIE buttonReset;
	private GuiButtonBoolean[] powerButtons;
	
	@Override
	public void init()
	{
		super.init();
		
		this.buttons.clear();
		
		buttonDirection = new GuiButtonState<CountDirection>(guiLeft+20, guiTop+20, 18, 18, new StringTextComponent(""), new CountDirection[] { CountDirection.UP, CountDirection.DOWN },
				tileEntity.countDirection.ordinal(), TEXTURE, 36, 0, 1,
				btn -> sendConfig("countDirection", btn.getNextState().ordinal())
		);

		buttonEdge = new GuiButtonState<CountEdge>(guiLeft+41, guiTop+20, 18, 18, new StringTextComponent(""), new CountEdge[] { CountEdge.RISING, CountEdge.FALLING },
				tileEntity.countEdge.ordinal(), TEXTURE, 54, 0, 1,
				btn -> sendConfig("countEdge", btn.getNextState().ordinal())
		);
		
		buttonLoop = new GuiButtonBoolean(guiLeft+62, guiTop+20, 18, 18, "", 
				tileEntity.loopEnd, TEXTURE, 72, 0, 1, 
				btn -> sendConfig("loopEnd", btn.getNextState() ? 1 : 0)
		);
		
		this.addButton(buttonDirection);
		this.addButton(buttonEdge);
		this.addButton(buttonLoop);
		
		powerButtons = new GuiButtonBoolean[15];
		for(int i = 0; i < powerButtons.length; i++)
		{
			final int limit = i + 1;
			powerButtons[i] = buildPowerButton(powerButtons, guiLeft+16+(i%5*14), guiTop+44+(i/5*14),
					limit <= tileEntity.countLimit, limit, btn -> sendConfig("countLimit", limit));
			this.addButton(powerButtons[i]);
		}
		
		buttonReset = buildResetButton(guiLeft+41, guiTop+96, (btn) -> sendConfig("countReset", true));
		
		this.addButton(buttonReset);
	}

	@Override
	protected void drawGuiContainerForegroundLayer(MatrixStack transform, int mouseX, int mouseY, float partialTick)
	{
		ArrayList<ITextComponent> tooltip = new ArrayList<>();

		if(buttonDirection.isHovered())
		{
			tooltip.add(getTranslationComponent("directionMode.title"));
			tooltip.add(TextUtils.applyFormat(
					getTranslationComponent("directionMode." + buttonDirection.getState().name() + ".name"),
					TextFormatting.GRAY
			));
			tooltip.add(TextUtils.applyFormat(
					getTranslationComponent("directionMode." + buttonDirection.getState().name() + ".desc"),
					TextFormatting.GRAY, TextFormatting.ITALIC
			));
		}

		if(buttonEdge.isHovered())
		{
			tooltip.add(getTranslationComponent("edgeMode.title"));
			tooltip.add(TextUtils.applyFormat(
					getTranslationComponent("edgeMode." + buttonEdge.getState().name() + ".name"),
					TextFormatting.GRAY
			));
			tooltip.add(TextUtils.applyFormat(
					getTranslationComponent("edgeMode." + buttonEdge.getState().name() + ".desc"),
					TextFormatting.GRAY, TextFormatting.ITALIC
			));
		}

		if(buttonLoop.isHovered())
		{
			tooltip.add(getTranslationComponent("loopMode.title"));
			tooltip.add(TextUtils.applyFormat(
					getTranslationComponent("loopMode." + (buttonLoop.getState() ? "ENABLED" : "DISABLED") + ".name"),
					TextFormatting.GRAY
			));
		}
		
		for(int i = 0; i < powerButtons.length; i++)
			if(powerButtons[i].isHovered())
			{
				tooltip.add(getTranslationComponent("power.title"));
				tooltip.add(TextUtils.applyFormat(
						getTranslationComponent("power.desc", i + 1),
						TextFormatting.GRAY
				));
			}
		
		if(buttonReset.isHovered())
		{
			tooltip.add(getTranslationComponent("reset.title"));
			tooltip.add(TextUtils.applyFormat(
					getTranslationComponent("reset.desc"),
					TextFormatting.GRAY
			));
		}

		if(!tooltip.isEmpty())
			GuiUtils.drawHoveringText(transform, tooltip, mouseX, mouseY, width, height, -1, font);
	}
	
	protected GuiButtonBoolean buildPowerButton(GuiButtonBoolean[] buttons, int posX, int posY, boolean active, int power, Consumer<GuiButtonBoolean> onClick)
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
				return true;
			}

			@Override
			public void render(MatrixStack transform, int mouseX, int mouseY, float partialTicks)
			{
				super.render(transform, mouseX, mouseY, partialTicks);
				if(this.visible)
				{
					int col = power <= tileEntity.countedPulses ? DyeColor.LIME.getColorValue() : DyeColor.GREEN.getColorValue();
					if(!getState())
						col = ClientUtils.getDarkenedTextColour(col);
					col = 0xff000000|col;
					this.fillGradient(transform, x+3, y+3, x+9, y+9, col, col);
				}
			}
		};
	}
	
	public static GuiButtonIE buildResetButton(int x, int y, Consumer<GuiButtonIE> onClick)
	{
		return new GuiButtonIE(x, y, 18, 18, ITextComponent.nullToEmpty(""), TEXTURE, 90, 18, (btn) -> onClick.accept(btn))
		{
			private int[] hoverOffset;
			private boolean isClicking = false;
			private boolean isPressing = false;
			
			private boolean isPressable(double mouseX, double mouseY)
			{
				return this.active&&this.visible&&mouseX >= this.x&&mouseY >= this.y&&mouseX < this.x+this.width&&mouseY < this.y+this.height;
			}
			
			@Override
			public GuiButtonIE setHoverOffset(int x, int y)
			{
				this.hoverOffset = new int[]{x, y};
				return this;
			}
			
			@Override
			public void render(MatrixStack transform, int mouseX, int mouseY, float partialTicks)
			{
				if(this.visible)
				{
					Minecraft mc = Minecraft.getInstance();
					ClientUtils.bindTexture(texture);
					FontRenderer fontrenderer = mc.font;
					this.isHovered = isPressable(mouseX, mouseY);
					this.isPressing = this.isHovered && this.isClicking;
					RenderSystem.enableBlend();
					RenderSystem.blendFuncSeparate(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ONE, DestFactor.ZERO);
					RenderSystem.blendFunc(770, 771);
					if(hoverOffset!=null&&this.isHovered&&this.isPressing)
						this.blit(transform, x, y, texU+hoverOffset[1], texV+hoverOffset[0], width, height);
					else if(hoverOffset!=null&&this.isHovered)
						this.blit(transform, x, y, texU+hoverOffset[0], texV+hoverOffset[1], width, height);
					else
						this.blit(transform, x, y, texU, texV, width, height);
					if(!getMessage().getString().isEmpty())
					{
						int txtCol = 0xE0E0E0;
						if(!this.active)
							txtCol = 0xA0A0A0;
						else if(this.isHovered)
							txtCol = Lib.COLOUR_I_ImmersiveOrange;
						drawCenteredString(transform, fontrenderer, getMessage(), this.x+this.width/2, this.y+(this.height-8)/2, txtCol);
					}
				}
			}
			
			@Override
			public boolean mouseClicked(double mouseX, double mouseY, int arg2)
			{
				isClicking = super.mouseClicked(mouseX, mouseY, arg2);
				return isClicking;
			}
			
			@Override
			public boolean mouseReleased(double mouseX, double mouseY, int arg2)
			{
				isClicking = !super.mouseReleased(mouseX, mouseY, arg2);
				return !isClicking;
			}
		}.setHoverOffset(-18, 0);
	}
}
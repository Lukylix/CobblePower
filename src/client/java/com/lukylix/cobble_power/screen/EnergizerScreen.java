package com.lukylix.cobble_power.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class EnergizerScreen extends AbstractContainerScreen<EnergizerScreenHandler> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath("cobble_power", "textures/gui/container/energizer_gui.png");

    public EnergizerScreen(EnergizerScreenHandler handler, Inventory inventory, Component title) {
        super(handler, inventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        super.renderBackground(graphics, mouseX, mouseY, delta);
        super.render(graphics, mouseX, mouseY, delta);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float delta, int mouseX, int mouseY) {
        int ledPosX = 176;
        int ledPosY = 0;
        int ledOffsetX = 70;
        int ledOffsetY = 53;
        int ledWidth = 36;
        int ledHeight = 18;
        // Draw GUI texture
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        RenderSystem.enableBlend();
        if (menu.isChargingItem()) {
            graphics.blit(TEXTURE, leftPos + ledOffsetX, topPos + ledOffsetY, ledPosX, ledPosY, ledWidth, ledHeight);
        } else {
            graphics.blit(TEXTURE, leftPos + ledOffsetX, topPos + ledOffsetY, ledPosX, ledPosY + ledHeight, ledWidth, ledHeight);
        }
        RenderSystem.disableBlend();
    }

}

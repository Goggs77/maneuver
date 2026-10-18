package com.goggs.maneuver.client;

import com.goggs.maneuver.config.DamperSettings;
import com.goggs.maneuver.net.SetDamperPayload;
import com.goggs.maneuver.screen.DamperScreenHandler;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import java.util.Locale;

public final class DamperScreen extends AbstractContainerScreen<DamperScreenHandler> {
    private EditBox radius;
    private EditBox strength;
    private boolean active;
    private boolean showRange;
    private boolean affectCreative;
    private Component error = Component.empty();

    public DamperScreen(DamperScreenHandler menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 300, 244);
        this.active = menu.getSettings().active();
        this.showRange = menu.getSettings().showRange();
        this.affectCreative = menu.getSettings().affectCreative();
    }

    @Override
    protected void init() {
        super.init();
        DamperSettings settings = menu.getSettings();
        int left = leftPos + 20;

        radius = new EditBox(font, left + 92, topPos + 42, 54, 18, Component.empty());
        radius.setMaxLength(3);
        radius.setValue(Integer.toString(settings.radius()));
        addRenderableWidget(radius);

        strength = new EditBox(font, left + 92, topPos + 70, 54, 18, Component.empty());
        strength.setMaxLength(6);
        strength.setValue(String.format(Locale.ROOT, "%.1f", settings.strength() * 100.0f));
        addRenderableWidget(strength);

        addRenderableWidget(Button.builder(activeLabel(), button -> {
            active = !active;
            button.setMessage(activeLabel());
        }).bounds(left, topPos + 108, 120, 20).build());

        addRenderableWidget(Button.builder(rangeLabel(), button -> {
            showRange = !showRange;
            button.setMessage(rangeLabel());
        }).bounds(left + 128, topPos + 108, 120, 20).build());

        addRenderableWidget(Button.builder(creativeLabel(), button -> {
            affectCreative = !affectCreative;
            button.setMessage(creativeLabel());
        }).bounds(left, topPos + 136, 120, 20).build());

        addRenderableWidget(Button.builder(
                Component.translatable("gui.maneuver.apply"),
                button -> apply()
        ).bounds(left + 128, topPos + 136, 76, 20).build());

        addRenderableWidget(Button.builder(
                Component.translatable("gui.done"),
                button -> onClose()
        ).bounds(left + 212, topPos + 136, 60, 20).build());
    }

    private Component activeLabel() {
        return Component.translatable(active ? "gui.maneuver.active" : "gui.maneuver.inactive");
    }

    private Component rangeLabel() {
        return Component.translatable(showRange ? "gui.maneuver.range_on" : "gui.maneuver.range_off");
    }

    private Component creativeLabel() {
        return Component.translatable(
                affectCreative ? "gui.maneuver.creative_on" : "gui.maneuver.creative_off"
        );
    }

    private void apply() {
        try {
            int parsedRadius = (int) Math.round(Double.parseDouble(radius.getValue().trim()));
            float parsedPercent = Float.parseFloat(strength.getValue().trim());
            DamperSettings settings = new DamperSettings(
                    parsedRadius,
                    parsedPercent / 100.0f,
                    active,
                    showRange,
                    affectCreative
            );
            menu.setClientSettings(settings);
            ClientPlayNetworking.send(new SetDamperPayload(menu.getPos(), settings));
            error = Component.empty();
        } catch (NumberFormatException exception) {
            error = Component.translatable("gui.maneuver.invalid_number");
        }
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        int border = 0xFF9D7DF0;
        graphics.fill(leftPos - 1, topPos - 1, leftPos + imageWidth + 1, topPos + imageHeight + 1, border);
        graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xF00E1321);
        graphics.fill(leftPos + 10, topPos + 30, leftPos + imageWidth - 10, topPos + 90, 0x80241D3E);
    }

    @Override
    public void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        graphics.text(font, title, 16, 12, 0xFFF4ECFF, false);
        graphics.text(font,
                Component.translatable("gui.maneuver.radius"),
                20, 47, 0xFFE9DFFF, false);
        graphics.text(font,
                Component.translatable("gui.maneuver.strength"),
                20, 75, 0xFFE9DFFF, false);

        int infoY = 164;
        infoY = drawWrapped(graphics,
                Component.translatable("gui.maneuver.radius_help", DamperSettings.MAX_RADIUS),
                20, infoY, imageWidth - 40, 0xFFC9B9EA);
        infoY = drawWrapped(graphics,
                Component.translatable("gui.maneuver.damper_help"),
                20, infoY, imageWidth - 40, 0xFF9E8DBA);
        if (!error.getString().isEmpty()) {
            drawWrapped(graphics, error, 20, infoY, imageWidth - 40, 0xFFFF7777);
        }
    }

    private int drawWrapped(
            GuiGraphicsExtractor graphics,
            Component text,
            int textX,
            int textY,
            int width,
            int color
    ) {
        for (var line : font.split(text, width)) {
            graphics.text(font, line, textX, textY, color, false);
            textY += font.lineHeight + 1;
        }
        return textY + 2;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}

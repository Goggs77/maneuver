package com.goggs.maneuver.client;

import com.goggs.maneuver.config.GravityFieldSettings;
import com.goggs.maneuver.net.SetGravityFieldPayload;
import com.goggs.maneuver.physics.FieldBounds;
import com.goggs.maneuver.physics.GravityVector;
import com.goggs.maneuver.screen.GravityFieldScreenHandler;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public final class GravityFieldScreen extends AbstractContainerScreen<GravityFieldScreenHandler> {
    private EditBox offsetX;
    private EditBox offsetY;
    private EditBox offsetZ;
    private EditBox sizeX;
    private EditBox sizeY;
    private EditBox sizeZ;
    private EditBox gravityX;
    private EditBox gravityY;
    private EditBox gravityZ;
    private boolean active;
    private boolean showRange;
    private boolean affectCreative;
    private Component error = Component.empty();

    public GravityFieldScreen(
            GravityFieldScreenHandler menu,
            Inventory inventory,
            Component title
    ) {
        super(menu, inventory, title, 350, 276);
        this.active = menu.getSettings().active();
        this.showRange = menu.getSettings().showRange();
        this.affectCreative = menu.getSettings().affectCreative();
    }

    @Override
    protected void init() {
        super.init();
        GravityFieldSettings settings = menu.getSettings();
        FieldBounds bounds = settings.bounds();
        GravityVector gravity = settings.gravity();

        int left = leftPos + 20;
        int inputX = left + 70;
        int yOffset = topPos + 40;
        int ySize = topPos + 68;
        int yGravity = topPos + 108;

        offsetX = addNumberField(inputX, yOffset, bounds.offsetX());
        offsetY = addNumberField(inputX + 62, yOffset, bounds.offsetY());
        offsetZ = addNumberField(inputX + 124, yOffset, bounds.offsetZ());
        sizeX = addNumberField(inputX, ySize, bounds.sizeX());
        sizeY = addNumberField(inputX + 62, ySize, bounds.sizeY());
        sizeZ = addNumberField(inputX + 124, ySize, bounds.sizeZ());
        gravityX = addNumberField(inputX, yGravity, gravity.x());
        gravityY = addNumberField(inputX + 62, yGravity, gravity.y());
        gravityZ = addNumberField(inputX + 124, yGravity, gravity.z());

        addRenderableWidget(Button.builder(activeLabel(), button -> {
            active = !active;
            button.setMessage(activeLabel());
        }).bounds(left, topPos + 148, 132, 20).build());

        addRenderableWidget(Button.builder(rangeLabel(), button -> {
            showRange = !showRange;
            button.setMessage(rangeLabel());
        }).bounds(left + 140, topPos + 148, 132, 20).build());

        addRenderableWidget(Button.builder(creativeLabel(), button -> {
            affectCreative = !affectCreative;
            button.setMessage(creativeLabel());
        }).bounds(left, topPos + 174, 132, 20).build());

        addRenderableWidget(Button.builder(
                Component.translatable("gui.maneuver.apply"),
                button -> apply()
        ).bounds(left + 140, topPos + 174, 92, 20).build());

        addRenderableWidget(Button.builder(
                Component.translatable("gui.done"),
                button -> onClose()
        ).bounds(left + 240, topPos + 174, 70, 20).build());
    }

    private EditBox addNumberField(int fieldX, int fieldY, float value) {
        EditBox field = new EditBox(
                font,
                fieldX,
                fieldY,
                54,
                18,
                Component.empty()
        );
        field.setMaxLength(12);
        field.setValue(Float.toString(value));
        addRenderableWidget(field);
        return field;
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
            FieldBounds bounds = new FieldBounds(
                    parseFloat(offsetX),
                    parseFloat(offsetY),
                    parseFloat(offsetZ),
                    parseFloat(sizeX),
                    parseFloat(sizeY),
                    parseFloat(sizeZ)
            );
            GravityVector gravity = new GravityVector(
                    parseFloat(gravityX),
                    parseFloat(gravityY),
                    parseFloat(gravityZ)
            );
            GravityFieldSettings settings = new GravityFieldSettings(
                    active,
                    showRange,
                    affectCreative,
                    bounds,
                    gravity
            );
            menu.setClientSettings(settings);
            ClientPlayNetworking.send(new SetGravityFieldPayload(menu.getPos(), settings));
            error = Component.empty();
        } catch (NumberFormatException exception) {
            error = Component.translatable("gui.maneuver.invalid_number");
        }
    }

    private static float parseFloat(EditBox field) {
        return Float.parseFloat(field.getValue().trim());
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        int border = 0xFFA88BFF;
        graphics.fill(leftPos - 1, topPos - 1, leftPos + imageWidth + 1, topPos + imageHeight + 1, border);
        graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xF0140D20);
        graphics.fill(leftPos + 10, topPos + 30, leftPos + imageWidth - 10, topPos + 138, 0x80251936);
    }

    @Override
    public void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        graphics.text(font, title, 16, 12, 0xFFF4ECFF, false);
        drawRowLabel(graphics, "gui.maneuver.offset", 20, 45);
        drawRowLabel(graphics, "gui.maneuver.size", 20, 73);
        drawRowLabel(graphics, "gui.maneuver.gravity", 20, 113);

        drawAxis(graphics, 98, 30);

        int infoY = 196;
        infoY = drawWrapped(graphics,
                Component.translatable("gui.maneuver.gravity_help"),
                20, infoY, imageWidth - 40, 0xFFC9B9EA);
        infoY = drawWrapped(graphics,
                Component.translatable("gui.maneuver.field_bounds_help",
                        FieldBounds.MIN_OFFSET, FieldBounds.MAX_OFFSET,
                        FieldBounds.MIN_SIZE, FieldBounds.MAX_SIZE),
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

    private void drawRowLabel(GuiGraphicsExtractor graphics, String translationKey, int labelX, int labelY) {
        graphics.text(font, Component.translatable(translationKey), labelX, labelY, 0xFFE9DFFF, false);
    }

    private void drawAxis(GuiGraphicsExtractor graphics, int axisX, int axisY) {
        graphics.text(font, Component.literal("X"), axisX + 19, axisY, 0xFFAF9BE8, false);
        graphics.text(font, Component.literal("Y"), axisX + 81, axisY, 0xFFAF9BE8, false);
        graphics.text(font, Component.literal("Z"), axisX + 143, axisY, 0xFFAF9BE8, false);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}


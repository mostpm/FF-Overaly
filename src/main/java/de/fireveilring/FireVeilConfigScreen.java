package de.fireveilring;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.function.DoubleConsumer;

/** Einstellungsmenue: /fireveil */
public class FireVeilConfigScreen extends Screen {
    private final Screen parent;
    private int previewY;

    public FireVeilConfigScreen(Screen parent) {
        super(Component.literal("Fire Veil Ring"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        FireVeilConfig cfg = FireVeilConfig.get();
        int w = 200;
        int x = this.width / 2 - w / 2;
        int y = this.height / 2 - 92;

        this.addRenderableWidget(Button.builder(toggleText(cfg.enabled), btn -> {
            cfg.enabled = !cfg.enabled;
            btn.setMessage(toggleText(cfg.enabled));
        }).bounds(x, y, w, 20).build());

        y += 28;
        this.addRenderableWidget(new ValueSlider(x, y, w, 20, "Dicke", 0.02, 0.50, cfg.thickness,
                v -> cfg.thickness = Math.round(v * 100.0) / 100.0, 2));

        y += 28;
        this.addRenderableWidget(new ValueSlider(x, y, w, 20, "Rot", 0, 255, cfg.red,
                v -> cfg.red = (int) Math.round(v), 0));
        y += 24;
        this.addRenderableWidget(new ValueSlider(x, y, w, 20, "Gruen", 0, 255, cfg.green,
                v -> cfg.green = (int) Math.round(v), 0));
        y += 24;
        this.addRenderableWidget(new ValueSlider(x, y, w, 20, "Blau", 0, 255, cfg.blue,
                v -> cfg.blue = (int) Math.round(v), 0));

        y += 28;
        this.previewY = y;

        y += 22;
        this.addRenderableWidget(Button.builder(Component.literal("Fertig"), btn -> this.onClose())
                .bounds(x, y, w, 20).build());
    }

    private static Component toggleText(boolean on) {
        return Component.literal("Ring: " + (on ? "AN" : "AUS"));
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);
        String title = "Fire Veil Ring";
        graphics.text(this.font, title, this.width / 2 - this.font.width(title) / 2,
                this.height / 2 - 92 - 16, 0xFFFFFFFF, true);
        String preview = "|||||||||||||||  Vorschau  |||||||||||||||";
        graphics.text(this.font, preview, this.width / 2 - this.font.width(preview) / 2,
                this.previewY, 0xFF000000 | FireVeilConfig.get().rgb(), true);
    }

    @Override
    public void onClose() {
        FireVeilConfig.save();
        this.minecraft.setScreen(this.parent);
    }

    /** Schieberegler mit Beschriftung "Name: Wert". */
    private static final class ValueSlider extends AbstractSliderButton {
        private final String label;
        private final double min;
        private final double max;
        private final DoubleConsumer onChange;
        private final int decimals;

        ValueSlider(int x, int y, int w, int h, String label, double min, double max,
                    double current, DoubleConsumer onChange, int decimals) {
            super(x, y, w, h, Component.empty(), (current - min) / (max - min));
            this.label = label;
            this.min = min;
            this.max = max;
            this.onChange = onChange;
            this.decimals = decimals;
            updateMessage();
        }

        private double real() {
            return min + this.value * (max - min);
        }

        @Override
        protected void updateMessage() {
            this.setMessage(Component.literal(label + ": " + String.format("%." + decimals + "f", real())));
        }

        @Override
        protected void applyValue() {
            onChange.accept(real());
        }
    }
}

package one.oth3r.sit.screen;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public class TextureButtonWidget extends ButtonWidget {
    //todo gray support
    protected final Identifier texture;
    protected final int textureWidth;
    protected final int textureHeight;
    protected final boolean tooltip;

    TextureButtonWidget(int width, int height, Text message, int textureWidth, int textureHeight, Identifier texture, ButtonWidget.PressAction onPress, @Nullable ButtonWidget.NarrationSupplier narrationSupplier, boolean tooltip) {
        super(0, 0, width, height, message, onPress, narrationSupplier == null ? DEFAULT_NARRATION_SUPPLIER : narrationSupplier);
        this.textureWidth = textureWidth;
        this.textureHeight = textureHeight;
        this.texture = texture;
        this.tooltip = tooltip;
        if (tooltip) setTooltip(Tooltip.of(message));
    }

    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        super.renderWidget(context, mouseX, mouseY, delta);
        int x = this.getX() + this.getWidth() / 2 - this.textureWidth / 2;
        int y = this.getY() + this.getHeight() / 2 - this.textureHeight / 2;
        context.drawGuiTexture(this.texture, x, y, this.textureWidth, this.textureHeight);
    }

    @Override
    public void drawMessage(DrawContext context, TextRenderer textRenderer, int color) {
        if (!this.tooltip) super.drawMessage(context, textRenderer, color);
    }


    public static class Builder {
        private final Text text;
        private final ButtonWidget.PressAction onPress;
        private final boolean hideText;
        private int width = 150;
        private int height = 20;
        @Nullable
        private Identifier texture;
        private int textureWidth;
        private int textureHeight;
        @Nullable
        ButtonWidget.NarrationSupplier narrationSupplier;

        public Builder(Text text, ButtonWidget.PressAction onPress, boolean hideText) {
            this.text = text;
            this.onPress = onPress;
            this.hideText = hideText;
        }

        public Builder dimensions(int width, int height) {
            this.width = width;
            this.height = height;
            return this;
        }

        public Builder texture(Identifier texture, int width, int height) {
            this.texture = texture;
            this.textureWidth = width;
            this.textureHeight = height;
            return this;
        }

        public Builder narration(ButtonWidget.NarrationSupplier narrationSupplier) {
            this.narrationSupplier = narrationSupplier;
            return this;
        }

        public TextureButtonWidget build() {
            if (this.texture == null) {
                throw new IllegalStateException("Sprite not set");
            }
            return new TextureButtonWidget(width,height,text,textureWidth,textureHeight,texture,onPress,narrationSupplier,hideText);
        }
    }
}

package one.oth3r.sit.screen;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.function.Supplier;

public class ClickableImageWidget extends ButtonWidget {
    private final Identifier image;

    public ClickableImageWidget(int x, int y, int width, int height, Tooltip tooltip, Identifier image, ButtonWidget.PressAction onPress) {
        super(x, y, width, height, Text.empty(), onPress, Supplier::get);
        this.image = image;
        this.setTooltip(tooltip);
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        context.drawTexture(RenderLayer::getGuiTextured, image,
                this.getX(), this.getY(), 0.0f, 0.0f, this.getWidth(), this.getHeight(), this.getWidth(), this.getHeight());
    }

}

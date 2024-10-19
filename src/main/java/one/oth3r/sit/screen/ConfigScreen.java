package one.oth3r.sit.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ConfirmLinkScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import one.oth3r.sit.file.FileData;
import one.oth3r.sit.utl.Data;

import java.net.URI;

public class ConfigScreen extends Screen {
    protected final Screen parent;

    public ConfigScreen(Screen parent) {
        super(Text.translatable("sit!.screen.config"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int startY = this.height / 4 + 48;
        int spacing = 36;
        TextureButtonWidget serverConfigButton = this.addDrawableChild(new TextureButtonWidget.Builder(Text.translatable("config.server"),
                (button) -> client.setScreen(new UnderConstructionScreen(this, FileData.getServerConfig())), false)
                .dimensions(250,30).texture(Identifier.of(Data.MOD_ID, "server_button"), 246, 26).build());
        serverConfigButton.setPosition(this.width / 2 - (serverConfigButton.getWidth()/2), startY);

        TextureButtonWidget sittingConfigButton = this.addDrawableChild(new TextureButtonWidget.Builder(Text.translatable("config.sitting"),
                (button) -> client.setScreen(new UnderConstructionScreen(this, FileData.getSittingConfig())), false)
                .dimensions(250,30).texture(Identifier.of(Data.MOD_ID, "sitting_button"), 246, 26).build());
        sittingConfigButton.setPosition(this.width / 2 - (sittingConfigButton.getWidth()/2), startY+36);


        TextureButtonWidget issuesButton = this.addDrawableChild(new TextureButtonWidget.Builder(Text.translatable("sit!.gui.button.issues"),
                        ConfirmLinkScreen.opening(this, URI.create("https://github.com/Oth3r/Sit/issues")), true)
                .dimensions(20,20).texture(Identifier.of(Data.MOD_ID, "issues"), 15, 15).build());
        issuesButton.setPosition(this.width / 2 - 125, startY + 72 + 12);


        this.addDrawableChild(ButtonWidget.builder(Text.translatable("sit!.gui.button.website"),
                ConfirmLinkScreen.opening(this, URI.create("https://modrinth.com/mod/sit!"))
        ).dimensions(this.width / 2 - 100, startY + 72 + 12, 98, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.translatable("gui.done"), (button) -> {
            close();
        }).dimensions(this.width / 2 + 2, startY + 72 + 12, 98, 20).build());

        TextureButtonWidget donateButton = this.addDrawableChild(new TextureButtonWidget.Builder(Text.translatable("sit!.gui.button.donate"),
                ConfirmLinkScreen.opening(this, URI.create("https://Ko-fi.com/oth3r")), true)
                .dimensions(20,20).texture(Identifier.of(Data.MOD_ID, "donate"), 15, 15).build());
        donateButton.setPosition(this.width / 2 + 105, startY + 72 + 12);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        // todo fade in like the title screen on first load?
        renderBanner(context,width/2  - 64,this.height / 4 -38,1);
    }

    @Override
    public void close() {
        this.client.setScreen(parent);
    }

    private void renderBanner(DrawContext context, int x, int y, float alpha) {
        context.setShaderColor(1.0f, 1.0f, 1.0f, alpha);
        RenderSystem.enableBlend();

        context.drawTexture(Identifier.of(Data.MOD_ID, "textures/gui/banner.png"), x, y, 0.0f, 0.0f, 128, 72, 128, 72);

        context.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.disableBlend();
    }
}

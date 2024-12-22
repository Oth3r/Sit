package one.oth3r.sit.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.terraformersmc.modmenu.gui.ModMenuOptionsScreen;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ConfirmLinkScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import one.oth3r.sit.file.FileData;
import one.oth3r.sit.utl.Data;

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
                .dimensions(250,30).texture(Identifier.of(Data.MOD_ID, "textures/gui/sprites/server_button.png"), 246, 26).build());
        serverConfigButton.setPosition(this.width / 2 - (serverConfigButton.getWidth()/2), startY);

        TextureButtonWidget sittingConfigButton = this.addDrawableChild(new TextureButtonWidget.Builder(Text.translatable("config.sitting"),
                (button) -> client.setScreen(new UnderConstructionScreen(this, FileData.getSittingConfig())), false)
                .dimensions(250,30).texture(Identifier.of(Data.MOD_ID, "textures/gui/sprites/sitting_button.png"), 246, 26).build());
        sittingConfigButton.setPosition(this.width / 2 - (sittingConfigButton.getWidth()/2), startY+36);


        TextureButtonWidget issuesButton = this.addDrawableChild(new TextureButtonWidget.Builder(Text.translatable("sit!.gui.button.issues"),
                        ConfirmLinkScreen.opening("https://github.com/Oth3r/Sit/issues",this,true), true)
                .dimensions(20,20).texture(Identifier.of(Data.MOD_ID, "textures/gui/sprites/issues.png"), 15, 15).build());
        issuesButton.setPosition(this.width / 2 - 125, startY + 72 + 12);


        this.addDrawableChild(ButtonWidget.builder(Text.translatable("sit!.gui.button.website"),
                ConfirmLinkScreen.opening("https://modrinth.com/mod/sit!",this,true)
        ).dimensions(this.width / 2 - 100, startY + 72 + 12, 98, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.translatable("gui.done"), (button) -> {
            close();
        }).dimensions(this.width / 2 + 2, startY + 72 + 12, 98, 20).build());

        TextureButtonWidget donateButton = this.addDrawableChild(new TextureButtonWidget.Builder(Text.translatable("sit!.gui.button.donate"),
                ConfirmLinkScreen.opening("https://Ko-fi.com/oth3r",this,true), true)
                .dimensions(20,20).texture(Identifier.of(Data.MOD_ID, "textures/gui/sprites/donate.png"), 15, 15).build());
        donateButton.setPosition(this.width / 2 + 105, startY + 72 + 12);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // render the background because you have to in this version
        this.renderBackground(context);
        // todo fade in like the title screen on first load?
        renderBanner(context,width/2  - 64,this.height / 4 -38,1);

        super.render(context, mouseX, mouseY, delta);


    }

    @Override
    public void close() {
        this.client.setScreen(parent);
    }

    private void renderBanner(DrawContext context, int x, int y, float alpha) {
        RenderSystem.enableBlend();

        context.drawTexture(Identifier.of(Data.MOD_ID, "textures/gui/banner.png"),
                x, y, 0.0f, 0.0f, 128, 72, 128, 72);

        RenderSystem.disableBlend();
    }
}

package one.oth3r.sit.screen;

import net.minecraft.client.gui.screen.ConfirmLinkScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import one.oth3r.sit.file.CustomFile;
import one.oth3r.sit.file.SittingConfig;
import one.oth3r.sit.utl.Data;
import one.oth3r.sit.utl.Utl;

import java.net.URI;
import java.nio.file.Paths;

public class UnderConstructionScreen<T extends CustomFile<T>> extends Screen {
    protected final Screen parent;
    protected T file;

    public UnderConstructionScreen(Screen parent, T file) {
        super(Text.translatable("sit!.screen.config"));
        this.parent = parent;
        this.file = file;
    }

    @Override
    protected void init() {
        int startY = this.height / 5-4;
        ButtonWidget foxPNG = this.addDrawableChild(new ClickableImageWidget(70,70,140,140, Tooltip.of(Text.of("Art by @bunnestbun")),
                Identifier.of(Data.MOD_ID, "textures/gui/fox.png"), ConfirmLinkScreen.opening(this, URI.create("https://www.instagram.com/bunnestbun/"))));
        foxPNG.setPosition(this.width / 2 - (foxPNG.getWidth()/2), startY-35);

        ButtonWidget openFileButton = this.addDrawableChild(new ButtonWidget.Builder(Text.translatable("sit!.gui.button.file"),
                (button) -> Util.getOperatingSystem().open(this.file.getFile()))
                .dimensions(0, 0, 118 ,20).build());
        openFileButton.setPosition(this.width / 2 - 70, startY+110);

        TextureButtonWidget folderButton = this.addDrawableChild(new TextureButtonWidget.Builder(Text.translatable("sit!.gui.button.folder"),
                (button) -> Util.getOperatingSystem().open(Paths.get(this.file.getFile().getParent())), true)
                .dimensions(20,20).texture(Identifier.of(Data.MOD_ID, "folder"), 15, 15).build());
        folderButton.setPosition(this.width / 2 + 50, startY + 110);

        TextureButtonWidget resetButton = this.addDrawableChild(new TextureButtonWidget.Builder(Text.translatable("sit!.gui.button.reset"),
                (button) -> {
                    this.file.reset();
                    this.file.save();
                }, true)
                .dimensions(20,20).texture(Identifier.of(Data.MOD_ID, "reset_file"), 15, 15).build());
        resetButton.setPosition(this.width / 2 -70, startY + 135);

        ButtonWidget revertButton = this.addDrawableChild(new ButtonWidget.Builder(Text.translatable("sit!.gui.button.revert"),
                (button) -> this.file.save())
                .dimensions(0, 0, 118,20).build());
        revertButton.setPosition(this.width / 2 - 48, startY+135);


        ButtonWidget saveExitButton = this.addDrawableChild(new ButtonWidget.Builder(Text.translatable("sit!.gui.button.save"),
                (button) -> {
                    this.file.load();
                    this.file.save();

                    // send the settings to the server if editing the sitting file and on a supported server
                    if (this.file instanceof SittingConfig && Data.isSupportedServer()) {
                        Utl.sendSettingsPackets();
                    }

                    this.client.setScreen(parent);
                })
                .dimensions(0, 0, 140,20).build());
        saveExitButton.setPosition(this.width / 2 - 70, startY+168);
    }

    @Override
    public void close() {
        this.client.setScreen(parent);
    }
}

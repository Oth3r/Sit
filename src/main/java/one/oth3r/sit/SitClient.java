package one.oth3r.sit;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.Identifier;
import one.oth3r.otterlib.client.screen.ConfigScreen;
import one.oth3r.otterlib.client.screen.utl.CustomImage;
import one.oth3r.otterlib.client.screen.utl.SimpleButton;
import one.oth3r.sit.file.FileData;
import one.oth3r.sit.utl.Chat;
import one.oth3r.sit.utl.Data;
import one.oth3r.sit.utl.Events;

import java.net.URI;
import java.util.List;

public class SitClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        Data.setClient(true);
        Events.registerClient();
    }

    public static Screen getConfigScreen(Screen parent) {
        return new ConfigScreen(parent, Chat.lang("sit!.screen.config"),
                new CustomImage(Identifier.of(Data.MOD_ID, "textures/gui/banner.png"),128, 72),
                List.of(
                        SimpleButton.Templates.fileEditor(Chat.lang("config.server"), FileData.getServerConfig(), new CustomImage(Identifier.of(Data.MOD_ID, "server_button"),246,26)).build(),
                        SimpleButton.Templates.fileEditor(Chat.lang("config.sitting"), FileData.getSittingConfig(), new CustomImage(Identifier.of(Data.MOD_ID, "sitting_button"), 246, 26)).build()
                ),
                List.of(
                        SimpleButton.Templates.warning(Chat.lang("sit!.gui.button.issues")).openLink("https://github.com/Oth3r/Sit/issues").build(),
                        new SimpleButton.Builder(Chat.lang("sit!.gui.button.website")).openLink("https://modrinth.com/mod/sit!").build(),
                        SimpleButton.Templates.done(Chat.lang("gui.done")).build(),
                        SimpleButton.Templates.donate(Chat.lang("sit!.gui.button.donate")).openLink(URI.create("https://ko-fi.com/oth3r")).build()
                ));
    }
}

package one.oth3r.sit.mixin;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ReloadCommand;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Formatting;
import one.oth3r.sit.file.FileData;
import one.oth3r.sit.utl.Data;
import one.oth3r.sit.utl.Utl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ReloadCommand.class)
public class ReloadCommandMixin {
    @Inject(at = @At("TAIL"), method = "register")
    private static void register(CommandDispatcher<ServerCommandSource> dispatcher, CallbackInfo ci) {
        FileData.loadFiles();

        // make sure the server isn't null
        MinecraftServer server = Data.getServer();
        if (server == null || server.getPlayerManager() == null) return;

        // send a reloaded message to all players with permissions
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
             if (player.isCreativeLevelTwoOp()) {
                 player.sendMessage(Utl.messageTag().append(Utl.lang("sit!.chat.reloaded").formatted(Formatting.GREEN)));
             }
        }
    }
}
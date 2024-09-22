package one.oth3r.sit.mixin;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.ReloadCommand;
import net.minecraft.server.command.ServerCommandSource;
import one.oth3r.sit.file.FileData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ReloadCommand.class)
public class ReloadCommandMixin {
    @Inject(at = @At("TAIL"), method = "register")
    private static void register(CommandDispatcher<ServerCommandSource> dispatcher, CallbackInfo ci) {
        FileData.loadFiles();
    }
}

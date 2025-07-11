package one.oth3r.sit.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import one.oth3r.sit.utl.Chat;
import one.oth3r.sit.utl.Data;
import one.oth3r.sit.utl.Logic;
import one.oth3r.sit.utl.Utl;

import java.awt.*;
import java.util.concurrent.CompletableFuture;

public class SitCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("sit")
                .requires((commandSource) -> commandSource.hasPermissionLevel(0))
                .executes((context2) -> command(context2.getSource(), context2.getInput()))
                .then(CommandManager.argument("args", StringArgumentType.string())
                        .requires((commandSource) -> commandSource.hasPermissionLevel(2))
                        .suggests(SitCommand::getSuggestions)
                        .executes((context2) -> command(context2.getSource(), context2.getInput()))));
    }

    public static CompletableFuture<Suggestions> getSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {
        builder.suggest("reload");
        builder.suggest("purgeChairEntities");
        return builder.buildFuture();
    }

    private static int command(ServerCommandSource source, String arg) {
        ServerPlayerEntity player = source.getPlayer();
        // trim all the arguments before the command (for commands like /execute)
        int index = arg.indexOf("sit");
        // trims the words before the text
        if (index != -1) arg = arg.substring(index).trim();

        String[] args = arg.split(" ");
        // if command string starts with sit, remove it
        if (args[0].equalsIgnoreCase("sit"))
            args = arg.replaceFirst("sit ", "").split(" ");

        // if console
        if (player == null) {
            if (args[0].equalsIgnoreCase("reload")) {
                Logic.reload();
                Data.LOGGER.info(Chat.lang("sit!.chat.reloaded").toString());
            }
            return 1;
        }

        // player

        if (args[0].equalsIgnoreCase("sit")) {
            // if the player can't sit where they're looking, try to sit below
            if (!Logic.sitLooking(player)) {
                BlockPos pos = player.getBlockPos();

                if (!(player.getY() - ((int) player.getY()) > 0.00)) {
                    pos = pos.add(0, -1, 0);
                }

                // if already sitting, ignore
                if (Data.getSitEntity(player) != null) return 1;

                // try to make the player sit
                Logic.sit(player, pos, null);
            }
        }

        if (args[0].equalsIgnoreCase("reload")) {
            Logic.reload();
            player.sendMessage(Chat.tag().append(Chat.lang("sit!.chat.reloaded").color(Color.GREEN)).b());
        }

        if (args[0].equalsIgnoreCase("purgeChairEntities")) Utl.Entity.purge(player,true);
        return 1;
    }
}

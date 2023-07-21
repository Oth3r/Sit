package one.oth3r.sit;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class SitCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("sit")
                .requires((commandSource) -> commandSource.hasPermissionLevel(2))
                .executes((context2) -> command(context2.getSource(), context2.getInput()))
                .then(CommandManager.argument("args", StringArgumentType.string())
                        .suggests(SitCommand::getSuggestions)
                        .executes((context2) -> command(context2.getSource(), context2.getInput()))));
    }
    public static CompletableFuture<Suggestions> getSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {
        ServerPlayerEntity player = Objects.requireNonNull(context.getSource().getPlayer());
        String[] args = context.getInput().split(" ");
        builder.suggest("reload");
        builder.suggest("purgeChairEntities");
        return builder.buildFuture();
    }
    private static int command(ServerCommandSource source, String arg) {
        ServerPlayerEntity player = source.getPlayer();
        if (player == null) return 1;
        //trim all the arguments before the command
        String keyword = "sit";
        int index = Integer.MAX_VALUE;
        if (arg.contains(keyword)) index = arg.indexOf(keyword);
        //trims the words before the text
        if (index != Integer.MAX_VALUE) arg = arg.substring(index).trim();
        String[] args = arg.split(" ");
        if (args[0].equalsIgnoreCase("sit"))
            args = arg.replaceFirst("sit ", "").split(" ");

        if (args[0].equalsIgnoreCase("reload")) {
            config.load();
            player.sendMessage(Sit.lang("key.sit.command.reloaded").styled(style -> style.withColor(TextColor.fromFormatting(Formatting.GREEN))));
        }
        if (args[0].equalsIgnoreCase("purgeChairEntities")) {
            String cmd = "kill @e[type=minecraft:text_display,name=\""+Sit.ENTITY_NAME+"\"]";
            try {
                ParseResults<ServerCommandSource> parse =
                        Sit.commandManager.getDispatcher().parse(cmd, player.getCommandSource());
                Sit.commandManager.getDispatcher().execute(parse);
                player.sendMessage(Sit.lang("key.sit.command.purged"));
            } catch (CommandSyntaxException e) {
                player.sendMessage(Sit.lang("key.sit.command.purged"));
                e.printStackTrace();
            }
        }
        return 1;
    }
}

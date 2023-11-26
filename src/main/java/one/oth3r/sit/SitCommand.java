package one.oth3r.sit;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

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
        //trim all the arguments before the command
        String keyword = "sit";
        int index = Integer.MAX_VALUE;
        if (arg.contains(keyword)) index = arg.indexOf(keyword);
        //trims the words before the text
        if (index != Integer.MAX_VALUE) arg = arg.substring(index).trim();
        String[] args = arg.split(" ");
        if (args[0].equalsIgnoreCase("sit"))
            args = arg.replaceFirst("sit ", "").split(" ");
        // if console
        if (player == null) {
            if (args[0].equalsIgnoreCase("reload")) {
                config.load();
                Sit.LOGGER.info(Sit.lang("key.sit.command.reloaded").getString());
            }
            return 1;
        }
        if (args[0].equalsIgnoreCase("sit")) {
            BlockPos pos = player.getBlockPos();
            if (!(player.getY() -((int) player.getY()) > 0.00)) {
                pos = pos.add(0,-1,0);
            }
            World world = player.getWorld();
            // if already sitting, ignore
            if (Events.entities.containsKey(player)) return 1;
            // make entity first to check the blocks
            DisplayEntity.TextDisplayEntity entity = new DisplayEntity.TextDisplayEntity(EntityType.TEXT_DISPLAY,player.getServerWorld());
            Events.setEntity(pos,world,entity);
            if (Events.checkBlocks(pos,world,Events.isAboveBlockheight(entity))) {
                player.getServerWorld().spawnEntity(entity);
                player.startRiding(entity);
                Events.entities.put(player,entity);
                return 1;
            }
        }
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

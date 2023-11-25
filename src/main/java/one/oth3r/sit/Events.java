package one.oth3r.sit;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.block.*;
import net.minecraft.block.enums.BlockHalf;
import net.minecraft.block.enums.SlabType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import one.oth3r.sit.Utl.HandType;

import java.util.*;

public class Events {
    private static int tick;
    public static HashMap<ServerPlayerEntity, Entity> entities = new HashMap<>();
    public static HashMap<ServerPlayerEntity, Integer> checkPlayers = new HashMap<>();
    public static boolean checkLogic(ServerPlayerEntity player) {
        ArrayList<UseAction> food = new ArrayList<>();
        food.add(UseAction.EAT);
        food.add(UseAction.DRINK);
        ArrayList<UseAction> notUsable = new ArrayList<>(food);
        notUsable.add(UseAction.NONE);
        HashMap<HandType, ItemStack> itemMap = new HashMap<>();
        itemMap.put(HandType.main,player.getMainHandStack());
        itemMap.put(HandType.off,player.getOffHandStack());
        // if sneaking cant sit
        if (player.isSneaking()) return false;
        // for both hands
        for (HandType type:HandType.values()) {
            ItemStack targetStack = itemMap.get(type);
            // if req is empty and the item isn't empty, false
            if (Utl.getReq(player,type).equals(config.HandRequirement.empty) && !targetStack.isEmpty()) return false;
            // if req is restrictive
            if (Utl.getReq(player,type).equals(config.HandRequirement.restrictive)) {
                // if item is in blacklist, false
                if (checkList(Utl.getList(player,type,"blacklist"),targetStack)) return false;
                // if item is NOT in whitelist
                if (!checkList(Utl.getList(player,type,"whitelist"),targetStack)) {
                    // if block is restricted and items is block, false, ect
                    if (Utl.getBool(player,type,"block") && (targetStack.getItem() instanceof BlockItem)) return false;
                    if (Utl.getBool(player,type,"food") && food.contains(targetStack.getUseAction())) return false;
                    if (Utl.getBool(player,type,"usable") && !notUsable.contains(targetStack.getUseAction())) return false;
                }
            }
        }
        // else true
        return true;
    }
    public static boolean checkList(List<String> list, ItemStack itemStack) {
        String itemID = Registries.ITEM.getId(itemStack.getItem()).toString();
        return list.contains(itemID);
    }
    public static HashMap<String,HashMap<String,Object>> getCustomBlocks() {
        HashMap<String,HashMap<String,Object>> map = new HashMap<>();
        int i = 1;
        for (String s:config.customBlocks) {
            String[] split = s.split("\\|");
            HashMap<String,Object> data = new HashMap<>();
            data.put("block",split[0]);
            data.put("height",split[1]);
            data.put("hitbox",split[2]);
            if (split.length==4) data.put("state",split[3]);
            map.put(String.valueOf(i),data);
            i++;
        }
        return map;
    }
    public static boolean isSitSafe(Block block) {
        // check if the block is sit safe (like a sign in the way)
        return block instanceof WallSignBlock || block instanceof TrapdoorBlock ||
                block instanceof WallBannerBlock || block instanceof AirBlock;
    }
    public static boolean checkBlocks(BlockPos pos, World world, boolean isAbove) {
        BlockState blockState = world.getBlockState(pos);
        Block block = blockState.getBlock();
        // make sure the block above the chair is safe
        if (!isSitSafe(world.getBlockState(pos.add(0,1,0)).getBlock())) return false;
        // if the player is above the block, (taller) check the next block above
        if (isAbove && !isSitSafe(world.getBlockState(pos.add(0,2,0)).getBlock())) return false;
        //if there's already an entity at the block location or one above it
        for (Entity entity:entities.values()) if (entity.getBlockPos().equals(pos) || entity.getBlockPos().add(0,1,0).equals(pos)) return false;

        // return for the 4 default types
        if (block instanceof StairsBlock && config.stairsOn) return blockState.get(StairsBlock.HALF) == BlockHalf.BOTTOM;
        if (block instanceof SlabBlock && config.slabsOn) return blockState.get(SlabBlock.TYPE) == SlabType.BOTTOM;
        if (block instanceof CarpetBlock && config.carpetsOn) return true;
        if (blockState.isFullCube(world,pos.add(0,1,0)) && config.fullBlocksOn) return true;
        // custom checker
        if (config.customOn && config.customBlocks.size() != 0) {
            for (HashMap<String,Object> map:getCustomBlocks().values()) {
                String blockID = Registries.BLOCK.getId(block).toString();
                if (map.get("block").equals(blockID)) {
                    if (!map.containsKey("state")) return true;
                    String[] states = ((String) map.get("state")).split(",\\s*");
                    boolean matching = true;
                    for (String state:states) {
                        if (state.charAt(0) == '!') {
                            if (blockState.toString().contains(state.substring(1))) matching = false;
                        } else if (!blockState.toString().contains(state)) matching = false;
                    }
                    return matching;
                }
            }
        }
        return false;
    }
    public static boolean isAboveBlockheight(Entity entity) {
        return entity.getPitch()<0;
    }
    public static void setEntity(BlockPos pos, World world, Entity entity) {
        Block block = world.getBlockState(pos).getBlock();
        entity.setCustomName(Text.of(Sit.ENTITY_NAME));
        entity.setCustomNameVisible(false);
        double hitBoxY = 0.5;
        entity.updatePositionAndAngles(pos.getX() + 0.5, pos.getY()+.47, pos.getZ() + 0.5, 0, 0);
        entity.setInvulnerable(true);
        if (block instanceof StairsBlock) {
            entity.updatePositionAndAngles(pos.getX() + 0.5, pos.getY()+.27, pos.getZ() + 0.5, 0, 0);
            hitBoxY = 2;
        }
        if (block instanceof SlabBlock) {
            entity.updatePositionAndAngles(pos.getX() + 0.5, pos.getY()+.27, pos.getZ() + 0.5, 0, 0);
            hitBoxY = 1;
        }
        if (block instanceof CarpetBlock) {
            entity.updatePositionAndAngles(pos.getX() + 0.5, pos.getY()-.17, pos.getZ() + 0.5, 0, 0);
            hitBoxY = 0.125;
        }
        if (world.getBlockState(pos).isFullCube(world,pos.add(0,1,0))) {
            entity.updatePositionAndAngles(pos.getX() + 0.5, pos.getY()+.78, pos.getZ() + 0.5, 0, 0);
            hitBoxY = 2;
        }
        if (config.customOn && config.customBlocks.size() != 0) {
            for (HashMap<String,Object> map:getCustomBlocks().values()) {
                String blockID = Registries.BLOCK.getId(block).toString();
                if (map.get("block").equals(blockID)) {
                    double input = Math.max(Math.min(Double.parseDouble((String) map.get("height")),1),0);
                    entity.updatePositionAndAngles(pos.getX() + 0.5, pos.getY()+input-.22, pos.getZ() + 0.5, 0, 0);
                    hitBoxY = Double.parseDouble((String) map.get("hitbox"));
                }
            }
        }
        //1.20.2 mounting pos change (shifts everything down by .25)
        entity.updatePositionAndAngles(entity.getX(),entity.getY()+.25,entity.getZ(),0,0);
        entity.setBoundingBox(Box.of(Vec3d.of(pos),1.5,hitBoxY,1.5));
        //change pitch based on if player is sitting below block height or not
        if (entity.getY() <= pos.getY()+.35+.25) entity.setPitch(90);
        else entity.setPitch(-90);
    }
    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(minecraftServer -> minecraftServer.execute(Events::cleanUp));
        // PLAYER JOIN
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.player;
            checkPlayers.put(player,2);
            // put server settings in the player settings
            Sit.playerSettings.put(player,Utl.getHandSettings());
        });
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            ServerPlayerEntity player = handler.player;
            if (entities.containsKey(player)) {
                if (!config.keepActive) {
                    player.dismountVehicle();
                    entities.get(player).setRemoved(Entity.RemovalReason.DISCARDED);
                }
                entities.remove(player);
            }
            checkPlayers.remove(player);
            Sit.playerSettings.remove(player);
        });
        ServerLifecycleEvents.SERVER_STARTED.register(s -> {
            Sit.server = s;
            Sit.commandManager = s.getCommandManager();
            UseBlockCallback.EVENT.register((pl, world, hand, hitResult) -> {
                ServerPlayerEntity player = Sit.server.getPlayerManager().getPlayer(pl.getUuid());
                if (player == null) return ActionResult.PASS;
                if (hand == net.minecraft.util.Hand.MAIN_HAND && hitResult.getType() == HitResult.Type.BLOCK) {
                    BlockPos pos = hitResult.getBlockPos();
                    if (!checkLogic(player)) return ActionResult.PASS;
                    if (checkBlocks(pos,world)) {
                        if (entities.containsKey(player)) {
                            if (!config.sitWhileSeated) return ActionResult.PASS;
                            entities.get(player).setRemoved(Entity.RemovalReason.DISCARDED);
                            entities.remove(player);
                        }
                        //interactions entity to make the hitbox?
                        DisplayEntity.TextDisplayEntity entity = new DisplayEntity.TextDisplayEntity(EntityType.TEXT_DISPLAY,player.getServerWorld());
                        setEntity(pos,world,entity);
                        player.getServerWorld().spawnEntity(entity);
                        player.startRiding(entity);
                        entities.put(player,entity);
                        return ActionResult.CONSUME;
                    }
                }
                return ActionResult.PASS;
            });
        });
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> SitCommand.register(dispatcher));
    }
    public static void cleanUp() {
        tick++;
        if (tick >= 5) {
            tick = 0;
            Iterator<Map.Entry<ServerPlayerEntity, Entity>> entityLoop = entities.entrySet().iterator();
            while (entityLoop.hasNext()) {
                Map.Entry<ServerPlayerEntity, Entity> entry = entityLoop.next();
                ServerPlayerEntity player = entry.getKey();
                Entity entity = entry.getValue();
                if (player.getVehicle() == null || !player.getVehicle().equals(entity)) {
                    entity.setRemoved(Entity.RemovalReason.DISCARDED);
                    entityLoop.remove();
                } else {
                    BlockPos pos = new BlockPos(entity.getBlockX(),(int) Math.floor(player.getY()),entity.getBlockZ());
                    if (entity.getPitch() == 90) pos = new BlockPos(entity.getBlockX(),(int) Math.ceil(player.getY()),entity.getBlockZ());
                    BlockState blockState = player.getWorld().getBlockState(pos);
                    if (blockState.isAir()) {
                        player.teleport(player.getX(),player.getBlockY()+1,player.getZ());
                        entity.setRemoved(Entity.RemovalReason.DISCARDED);
                        entityLoop.remove();
                    }
                }
            }
            Iterator<Map.Entry<ServerPlayerEntity, Integer>> playerCheckLoop = checkPlayers.entrySet().iterator();
            while (playerCheckLoop.hasNext()) {
                Map.Entry<ServerPlayerEntity, Integer> entry = playerCheckLoop.next();
                ServerPlayerEntity player = entry.getKey();
                int i = entry.getValue();
                checkPlayers.put(player,i-1);
                if (i<0) {
                    playerCheckLoop.remove();
                    continue;
                }
                if (player.getVehicle() != null) {
                    Entity entity = player.getVehicle();
                    if (entity.getName().getString().equals(Sit.ENTITY_NAME)) {
                        setEntity(player.getBlockPos().add(0,1,0),player.getServerWorld(),entity);
                        entities.put(player,entity);
                        playerCheckLoop.remove();
                    }
                }
            }
        }
    }
}

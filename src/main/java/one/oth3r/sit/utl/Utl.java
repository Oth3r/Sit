package one.oth3r.sit.utl;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.google.gson.stream.MalformedJsonException;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.block.*;
import net.minecraft.block.enums.BlockHalf;
import net.minecraft.block.enums.SlabType;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.consume.UseAction;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import one.oth3r.otterlib.chat.CTxT;
import one.oth3r.sit.file.*;
import one.oth3r.sit.packet.SitPayloads;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class Utl {

    /**
     * check if a block is obstructed (no collision)
     * @return true if not obstructed
     */
    public static boolean isNotObstructed(World world, BlockPos blockPos) {
        // get the block state at the blockPos
        BlockState state = world.getBlockState(blockPos);
        // make sure it doesn't have a collision
        return state.getCollisionShape(world,blockPos).isEmpty();
    }

    /**
     * checks the list of sit entities and sees if any of them are occupying the block pos
     */
    public static boolean isNotOccupied(BlockPos pos) {
        return Data.getSitEntities().values().stream().noneMatch(entity -> entity.getBlockPos().equals(pos));
    }

    public static final double HALF_BLOCK = 0.5;
    public static final double CARPET = 0.062;

    /**
     * checks if the provided itemstack is a valid one for the provided filter
     * @param filter the filter
     * @param itemStack itemstack to check
     * @return if true, the item isn't filtered out
     */
    public static boolean checkItem(HandSetting.Filter filter, ItemStack itemStack) {
        // default to true if there's nothing
        if (itemStack.isEmpty()) return true;

        boolean TRUE = true, FALSE = false;
        if (filter.isInverted()) {
            TRUE = false;
            FALSE = true;
        }

        boolean itemcheck = filter.getCustomItems().checkItem(itemStack);

        // iif the item passes the checks, return true
        if (itemcheck) return TRUE;

        // if none of the custom were met, try the default conditions

        // get the use actions for the filters
        ArrayList<UseAction> food = new ArrayList<>();
        food.add(UseAction.EAT);
        food.add(UseAction.DRINK);
        ArrayList<UseAction> notUsable = new ArrayList<>(food);
        notUsable.add(UseAction.NONE);

        HandSetting.Filter.Presets presets = filter.getPresets();

        // try the default conditions
        if (presets.isBlock() && itemStack.getItem() instanceof BlockItem) return TRUE;
        if (presets.isFood() && food.contains(itemStack.getUseAction())) return TRUE;
        if (presets.isUsable() && !notUsable.contains(itemStack.getUseAction())) return TRUE;

        // if nothing else is met, the item is filtered out
        return FALSE;
    }

    /**
     * get a block ID (eg. minecraft:air) from a blockstate. (it is easier with a block, but we are mostly working with block states
     * @return the block ID (minecraft:air)
     */
    public static String getBlockID(BlockState blockState) {
        return Registries.BLOCK.getId(blockState.getBlock()).toString();
    }

    /**
     * gets the sitting height for the provided blockstate, via memory loaded config from Data
     * @param player the player to
     * @param blockPos the pos of the block
     * @param hit nullable, for the player interaction check
     * @return null if not a valid block
     */
    public static Double getSittingHeight(ServerPlayerEntity player, BlockPos blockPos, @Nullable BlockHitResult hit) {
        ServerWorld serverWorld = player.getWorld();
        ServerConfig config = FileData.getServerConfig();
        BlockState blockState = serverWorld.getBlockState(blockPos);
        Block block = blockState.getBlock();

        // make sure that the block that is being sit on has no interaction when hand sitting
        if (hit != null && blockIsInList(config.getInteractionBlocks(), blockState)) {
            return null;
        }

        // only if custom is enabled
        if (config.isCustomEnabled()) {
            // if the block is on the blacklist, false
            if (blockIsInList(config.getBlacklistedBlocks(),blockState)) return null;

            for (SittingBlock sittingBlock : config.getSittingBlocks()) {
                // if the block is valid, true
                if (sittingBlock.isValid(blockState)) return sittingBlock.getSittingHeight();
            }
        }

        // add the default block types and check for them
        if (block instanceof StairsBlock
                && config.getPresetBlocks().isStairs()
                && blockState.get(StairsBlock.HALF) == BlockHalf.BOTTOM) return HALF_BLOCK;
        if (config.getPresetBlocks().isSlabs()
            && block instanceof SlabBlock
            && blockState.get(SlabBlock.TYPE) == SlabType.BOTTOM) return HALF_BLOCK;
        if (config.getPresetBlocks().isCarpets()
            && block instanceof CarpetBlock) return CARPET;
        if (config.getPresetBlocks().isFullBlocks()
                // make sure the block is a full cube
                && blockState.isFullCube(player.getWorld(),blockPos)) return 1.0;

        // at the end, return false
        return null;
    }

    /**
     * checks if a blockstate is in the list provided
     * @return
     */
    public static boolean blockIsInList(ArrayList<CustomBlock> blockList, BlockState blockState) {
        return blockList.stream().anyMatch(c -> c.isValid(blockState));
    }

    public static class Entity {
        /**
         * the customizable y height of the entity, as some versions have different sitting heights on the entity
         */
        public static final double Y_ADJUSTMENT = 0;

        /**
         * checks if the entity's block is still there, & is valid
         */
        public static boolean isValid(ServerPlayerEntity player, @NotNull DisplayEntity.TextDisplayEntity entity) {
            BlockPos blockPos = getBlockPos(entity);
            // get the blockstate
            BlockState blockState = player.getWorld().getBlockState(blockPos);
            // check if the block is still there & the block is a valid sit block (by checking if there is a sit height for the block)
            return !blockState.isAir() && getSittingHeight(player,blockPos,null) != null;
        }

        /**
         * gets the bound block pos of the sit entity
         */
        public static BlockPos getBlockPos(DisplayEntity.TextDisplayEntity entity) {
            // the entity Y level, adjusted
            // the adjustment - is the opposite of the offset applied in Entity.create()
            int entityBlockY = (int) (Math.floor(entity.getY() + (Y_ADJUSTMENT*-1)));
            // get the block pos
            BlockPos pos = new BlockPos(entity.getBlockX(),entityBlockY,entity.getBlockZ());
            // if above the block, subtract 1
            if (isAboveBlockHeight(entity)) {
                pos = pos.add(0,-1,0);
            }

            return pos;
        }

        /**
         * using the entity's pitch, figure out if the player is above the block height or not
         */
        public static boolean isAboveBlockHeight(DisplayEntity.TextDisplayEntity entity) {
            return entity.getPitch() > 0;
        }

        /**
         * creates the sit entity from the pos & sit height provided
         * @param world the world to make the entity in
         * @param blockPos the pos of the entity
         * @param sitHeight the height for the entity to be at
         * @return the entity at the correct height and position
         */
        public static DisplayEntity.TextDisplayEntity create(World world, BlockPos blockPos, double sitHeight) {
            DisplayEntity.TextDisplayEntity entity = new DisplayEntity.TextDisplayEntity(EntityType.TEXT_DISPLAY,world);

            // entity flags
            entity.setCustomName(Text.of(Data.ENTITY_NAME));
            entity.setCustomNameVisible(false);
            entity.setInvulnerable(true);
            entity.setInvisible(true);

            // get the entities y level
            double entityY = blockPos.getY();
            entityY += sitHeight;

            // set the entities position
            entity.updatePosition(blockPos.getX()+.5, entityY, blockPos.getZ()+.5);

            // change pitch based on if player is sitting below block height or not (full block height only)
            if (entity.getY() == blockPos.getY() + 1) entity.setPitch(90); // below
            else entity.setPitch(-90); // above

            // adjusting the entity height after doing the main calculations, for correct player visuals
            entity.updatePosition(entity.getX(),entityY+Y_ADJUSTMENT,entity.getZ());

            return entity;
        }

        /**
         * removes the entity from the entity map and world, dismounting any passengers
         */
        public static void remove(DisplayEntity.TextDisplayEntity entity) {
            // dismount everyone
            entity.removeAllPassengers();
            // remove the entity
            entity.setRemoved(net.minecraft.entity.Entity.RemovalReason.DISCARDED);
            // remove the entity from the data set if exists
            Data.removeSitEntity(entity);
        }

        /**
         * spawns the entity and make the player sit on it
         */
        public static void spawnSit(ServerPlayerEntity player, DisplayEntity.TextDisplayEntity entity) {
            Data.setSpawnList(player, entity);
        }

        /**
         * removes all sit entities loaded on the server
         */
        public static void purge(ServerPlayerEntity player, boolean message) {
            /// FYI it cant purge an entity from a disconnected player or unloaded chunks

            // get a list of sit entities
            List<? extends DisplayEntity.TextDisplayEntity> list = player.getWorld()
                    .getEntitiesByType(TypeFilter.instanceOf(DisplayEntity.TextDisplayEntity.class),
                            entity -> entity.getName().getString().equals(Data.ENTITY_NAME));

            // amount of sit entities purged
            int count = 0;

            // remove each one & count
            for (DisplayEntity.TextDisplayEntity entity : list) {
                remove(entity);
                count++;
            }

            // send a message if needed
            if (message) {
                player.sendMessage(Chat.tag()
                        .append(Chat.lang("sit!.chat.purged",
                                Chat.lang("sit!.chat.purged.total",count).color(Color.gray).b()
                        ).color(Color.GREEN)).b());
            }
        }
    }

    /**
     * sends the settings packets to the server, if client & in game
     */
    public static void sendSettingsPackets() {
        if (Data.isClient() && Data.isInGame() &&
                ClientPlayNetworking.canSend(SitPayloads.SettingsPayload.ID)) {
            ClientPlayNetworking.send(new SitPayloads.SettingsPayload(Utl.getGson().toJson(FileData.getSittingConfig())));
        }
    }

    /**
     * gets a Gson with the LenientTypeAdapter
     */
    public static Gson getGson() {
        return new GsonBuilder()
                .disableHtmlEscaping()
                .setPrettyPrinting()
                .registerTypeAdapterFactory(new LenientTypeAdapterFactory())
                .create();
    }

    /**
     * the LenientTypeAdapter, doesn't throw anything when reading a weird JSON entry, good for human entered JSONs
     */
    @SuppressWarnings("unchecked")
    public static class LenientTypeAdapterFactory implements TypeAdapterFactory {
        public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
            final TypeAdapter<T> delegate = gson.getDelegateAdapter(this, type);

            // Check if the type is a List, then run the custom list type adapter
            if (List.class.isAssignableFrom(type.getRawType())) {
                Type elementType = ((ParameterizedType) type.getType()).getActualTypeArguments()[0];
                TypeAdapter<?> elementAdapter = gson.getAdapter(TypeToken.get(elementType));
                // the custom adapter
                return (TypeAdapter<T>) new RemoveNullListTypeAdapter<>(elementAdapter);
            }

            return new TypeAdapter<>() {
                // normal writer
                public void write(JsonWriter out, T value) throws IOException {
                    delegate.write(out, value);
                }
                // custom reader
                public T read(JsonReader in) throws IOException {
                    try {
                        //Try to read value using default TypeAdapter
                        return delegate.read(in);
                    } catch (JsonSyntaxException | MalformedJsonException e) {
                        // don't throw anything if there's a weird JSON, just return null
                        in.skipValue();
                        return null;
                    }
                }
            };
        }
    }

    /**
     * type adapter that doesnt allow null / bad entries
     */
    private static class RemoveNullListTypeAdapter<E> extends TypeAdapter<List<E>> {
        private final TypeAdapter<E> elementAdapter;

        RemoveNullListTypeAdapter(TypeAdapter<E> elementAdapter) {
            this.elementAdapter = elementAdapter;
        }

        @Override
        public void write(JsonWriter out, List<E> value) throws IOException {
            out.beginArray();
            for (E element : value) {
                elementAdapter.write(out, element);
            }
            out.endArray();
        }

        @Override
        public List<E> read(JsonReader in) throws IOException {
            List<E> list = new ArrayList<>();
            in.beginArray();
            while (in.hasNext()) {
                try {
                    E element = elementAdapter.read(in);
                    // skip null entry
                    if (element == null) continue;
                    list.add(element);
                } catch (Exception e) {
                    // skip invalid entry
                    in.skipValue();
                }
            }
            in.endArray();
            return list;
        }
    }

    public static BlockPos getBlockPosPlayerIsLookingAt(ServerWorld world, PlayerEntity player, double range) {
        // pos, adjusted to player eye level
        Vec3d rayStart = player.getPos().add(0, player.getEyeHeight(player.getPose()), 0);
        // extend ray by the range
        Vec3d rayEnd = rayStart.add(player.getRotationVector().multiply(range));

        BlockHitResult hitResult = world.raycast(new RaycastContext(rayStart, rayEnd, RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, ShapeContext.absent()));

        if (hitResult.getType() == HitResult.Type.BLOCK) {
            return hitResult.getBlockPos();
        }

        return new BlockPos(player.getBlockPos());
    }

    public static double getPlayerReach(PlayerEntity player) {
        // use the BLOCK_INTERACTION_RANGE attribute if available
        if (player.getAttributeInstance(EntityAttributes.BLOCK_INTERACTION_RANGE) != null) {
            return player.getAttributeValue(EntityAttributes.BLOCK_INTERACTION_RANGE);
        }
        // fallback to 5
        return 5;
    }
}

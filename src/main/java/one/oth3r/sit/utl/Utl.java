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
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.UseAction;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import one.oth3r.sit.file.*;
import one.oth3r.sit.packet.SitPayloads;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Utl {

    /**
     * check if a block is obstructed (no collision / custom list)
     * @return true if not obstructed
     */
    public static boolean isNotObstructed(World world, BlockPos blockPos) {
        // get the block state at the blockPos
        BlockState state = world.getBlockState(blockPos);
        // make sure it doesn't have a collision
        return state.getCollisionShape(world,blockPos).isEmpty();
    }

    public static class Num {

        public static boolean isInt(String string) {
            try {
                Integer.parseInt(string);
            } catch (NumberFormatException nfe) {
                return false;
            }
            return true;
        }

        public static Integer toInt(String s) {
            // return an int no matter what
            try {
                return Integer.parseInt(s);
            } catch (NumberFormatException e) {
                try {
                    return (int) Double.parseDouble(s);
                } catch (NumberFormatException e2) {
                    return 0;
                }
            }
        }

        public static boolean isNum(String s) {
            // checks if int or a double
            try {
                Integer.parseInt(s);
                return true;
            } catch (NumberFormatException e1) {
                try {
                    Double.parseDouble(s);
                    return true;
                } catch (NumberFormatException e2) {
                    return false;
                }
            }
        }
    }

    public static final double HALF_BLOCK = 0.49;
    public static final double CARPET = 0.05;

    /**
     * checks if the provided itemstack is a valid one for the provided filter
     * @param filter the filter
     * @param itemStack itemstack to check
     * @return if true, the item isn't filtered out
     */
    public static boolean checkItem(HandSetting.Filter filter, ItemStack itemStack) {
        // default to true if theres nothing
        if (itemStack.isEmpty()) return true;

        String itemId = Registries.ITEM.getId(itemStack.getItem()).toString();
        // check the custom item ids
        for (String id : filter.getCustomItems()) {
            // if there is a match for the NOT(!) item, its filtered, false
            if (id.startsWith("!") && id.substring(1).equalsIgnoreCase(itemId)) return false;
            // if there is a match for the item, return true immediately
            if (id.equalsIgnoreCase(itemId)) return true;
        }
        // check the custom item tags
        for (String tag : filter.getCustomTags()) {
            // substring to remove # and if needed, !
            // if there is a math for the NOT(!) tag, return false
            if (tag.startsWith("!") && itemStack.isIn(TagKey.of(Registries.ITEM.getKey(), Identifier.of(tag.substring(2))))) return false;
            // if there is a match, return true
            if (itemStack.isIn(TagKey.of(Registries.ITEM.getKey(), Identifier.of(tag.substring(1))))) return true;
        }

        // if none of the custom were met, try the default conditions

        // get the use actions for the filters
        ArrayList<UseAction> food = new ArrayList<>();
        food.add(UseAction.EAT);
        food.add(UseAction.DRINK);
        ArrayList<UseAction> notUsable = new ArrayList<>(food);
        notUsable.add(UseAction.NONE);

        // try the default conditions
        if (filter.isBlock() && itemStack.getItem() instanceof BlockItem) return true;
        if (filter.isFood() && food.contains(itemStack.getUseAction())) return true;
        if (filter.isUsable() && !notUsable.contains(itemStack.getUseAction())) return true;

        // if nothing else is met, the item is filtered out
        return false;
    }

    /**
     * get a block ID (namespace, minecraft:air) from a blockstate. (it is easier with a block, but we are mostly working with block states
     * @return the block ID (minecraft:air)
     */
    public static String getBlockID(BlockState blockState) {
        return Registries.BLOCK.getId(blockState.getBlock()).toString();
    }

    /**
     * gets the sitting height for the provided blockstate, via memory loaded config from Data
     * @param blockState the state of the block
     * @param player the player to
     * @param blockPos the pos of the block
     * @param hit nullable, for the player interaction check
     * @return null if not a valid block
     */
    public static Double getSittingHeight(BlockState blockState, ServerPlayerEntity player, BlockPos blockPos, @Nullable BlockHitResult hit) {
        ServerConfig config = FileData.getServerConfig();
        Block block = blockState.getBlock();

        // only if custom is enabled
        if (config.isCustomEnabled()) {
            // if the block is on the blacklist, false
            if (config.getBlacklistedBlocks().contains(getBlockID(blockState))) return null;

            for (CustomBlock customBlock : config.getCustomBlocks()) {
                // if the block is valid, true
                if (customBlock.isValid(blockState)) return customBlock.getSittingHeight();
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
                && blockState.isFullCube(player.getWorld(),blockPos)
                // make sure there isn't an action for the block IF the hit isn't null (manual command / no right click check)
                && (hit == null || !blockState.onUse(player.getWorld(), player, hit).isAccepted())) return 1.0;

        // at the end, return false
        return null;
    }

    public static class Entity {

        /**
         * checks if the entity's block is still there, & is valid
         */
        public static boolean isValid(ServerPlayerEntity player, @NotNull DisplayEntity.TextDisplayEntity entity) {
            BlockPos blockPos = getBlockPos(entity);
            // get the blockstate
            BlockState blockState = player.getWorld().getBlockState(blockPos);
            // check if the block is still there & the block is a valid sit block (by checking if there is a sit height for the block)
            return !blockState.isAir() && getSittingHeight(blockState,player,blockPos,null) != null;
        }

        /**
         * gets the bound block pos of the sit entity
         */
        public static BlockPos getBlockPos(DisplayEntity.TextDisplayEntity entity) {
            // get the block pos
            BlockPos pos = new BlockPos(entity.getBlockX(),entity.getBlockY(),entity.getBlockZ());
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

            entity.setCustomName(Text.of(Data.ENTITY_NAME));
            entity.setCustomNameVisible(false);
            entity.setInvulnerable(true);
            entity.setInvisible(true);

            entity.updatePositionAndAngles(blockPos.getX()+.5, blockPos.getY()+sitHeight, blockPos.getZ()+.5, 0, 0);

    //        // 1.20.2 mounting pos change (shifts everything down by .25)
    //        double oneTwentyTwo = .25;
    //        entity.updatePositionAndAngles(entity.getX(),entity.getY()+oneTwentyTwo,entity.getZ(),0,0);

            // change pitch based on if player is sitting below block height or not (full block height only)
            if (entity.getY() == blockPos.getY() + 1) entity.setPitch(90); // below
            else entity.setPitch(-90); // above

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
            FileData.removeSitEntity(entity);
        }

        /**
         * spawns the entity and make the player sit on it
         */
        public static void spawnSit(ServerPlayerEntity player, DisplayEntity.TextDisplayEntity entity) {
            player.getServerWorld().spawnEntity(entity);
            player.startRiding(entity);
            // add the entity to the list
            FileData.addSitEntity(player, entity);
        }

        /**
         * removes all sit entities loaded on the server
         */
        public static void purge(ServerPlayerEntity player, boolean message) {
            // todo test if it can purge an entity from a disconnected player or unloaded chunks
            // get a list of sit entities
            List<? extends DisplayEntity.TextDisplayEntity> list = player.getServerWorld()
                    .getEntitiesByType(TypeFilter.instanceOf(DisplayEntity.TextDisplayEntity.class),
                            entity -> entity.getName().getString().equals(Data.ENTITY_NAME));

            // remove each one
            for (DisplayEntity.TextDisplayEntity entity : list) {
                remove(entity);
            }

            // send a message if needed
            if (message) {
                // todo maybe a count for the message for debuging
                player.sendMessage(Utl.lang("msg.purged"));
            }
        }
    }

    /**
     * gets a MutableText using the language key, if on server, using the custom lang reader
     */
    public static MutableText lang(String key, Object... args) {
        if (Data.isClient()) return Text.translatable(key, args);
        else return LangReader.of(key, args).getTxT();
    }

    public static class Enum {

        public static <T extends java.lang.Enum<T>> T get(Object enumString, Class<T> enumType) {
            return get(enumString,enumType,enumType.getEnumConstants()[0]);
        }
        /**
         * gets an enum from a string without returning null
         * @param enumString the string of the enum
         * @param enumType the class of enums
         * @param defaultEnum the enum to return if a match isn't found
         * @return an enum, if there isn't a match, it returns the first enum
         */
        public static <T extends java.lang.Enum<T>> T get(Object enumString, Class<T> enumType, T defaultEnum) {
            T[] values = enumType.getEnumConstants();
            for (T all : values) {
                // check if there is a match for any of the enum names
                if (enumString.toString().equals(all.name())) return all;
            }
            // if there's no match return the first entry
            return defaultEnum;
        }
    }

    // todo call when editing a config on the client
    /**
     * sends the settings packets to the server, if client & in game
     */
    public static void sendSettingsPackets() {
        if (Data.isClient() && Data.isInGame()) {
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
    public static class LenientTypeAdapterFactory implements TypeAdapterFactory {
        public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
            final TypeAdapter<T> delegate = gson.getDelegateAdapter(this, type);

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
}

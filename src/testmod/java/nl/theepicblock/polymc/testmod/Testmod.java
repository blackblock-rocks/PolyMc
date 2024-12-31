package nl.theepicblock.polymc.testmod;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.object.builder.v1.block.type.BlockSetTypeBuilder;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.block.*;
import net.minecraft.component.type.FoodComponents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.equipment.ArmorMaterial;
import net.minecraft.item.equipment.EquipmentAssetKeys;
import net.minecraft.item.equipment.EquipmentType;
import net.minecraft.potion.Potion;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ColorCode;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.List;
import java.util.function.Function;

import static nl.theepicblock.polymc.testmod.YellowStatusEffect.YELLOW;

public class Testmod implements ModInitializer {
    private static final String MODID = "polymc-testmod";

    public static final BlockSetType TEST_IRON_BLOCKSET = new BlockSetTypeBuilder()
            .soundGroup(BlockSoundGroup.BONE)
            .openableByHand(false)
            .register(id("test_iron"));
    public static final BlockSetType TEST_WOOD_BLOCKSET = new BlockSetTypeBuilder()
            .soundGroup(BlockSoundGroup.WOOL)
            .openableByHand(true)
            .register(id("test_wood"));

    public static final Item TEST_ITEM = registerItem(id("test_item"), settings -> new TestItem(settings.maxCount(6).rarity(Rarity.EPIC)));
    public static final Item TEST_FOOD = registerItem(id("test_food"), settings -> new Item(settings.food(FoodComponents.COOKED_CHICKEN)));
    public static final ArmorMaterial TEST_MATERIAL = new ArmorMaterial(5, Util.make(new EnumMap<>(EquipmentType.class), (map) -> {
        map.put(EquipmentType.BOOTS, 1);
        map.put(EquipmentType.LEGGINGS, 2);
        map.put(EquipmentType.CHESTPLATE, 3);
        map.put(EquipmentType.HELMET, 1);
        map.put(EquipmentType.BODY, 3);
    }), 5, SoundEvents.AMBIENT_BASALT_DELTAS_ADDITIONS, 3, 4, TagKey.of(RegistryKeys.ITEM, id("empty")), RegistryKey.of(EquipmentAssetKeys.REGISTRY_KEY, id("armor_material")));
    public static final Item TELMET = registerItem(id("test_helmet"), settings -> new ArmorItem(TEST_MATERIAL, EquipmentType.HELMET, settings));
    public static final Item TESTPLATE =  registerItem(id("test_chestplate"), settings -> new ArmorItem(TEST_MATERIAL, EquipmentType.CHESTPLATE, settings));
    public static final Item TEGGINGS =  registerItem(id("test_leggings"), settings -> new ArmorItem(TEST_MATERIAL, EquipmentType.LEGGINGS, settings));
    public static final Item TOOTS =  registerItem(id("test_boots"), settings -> new ArmorItem(TEST_MATERIAL, EquipmentType.BOOTS, settings));

    public static final Block TEST_BLOCK = registerBlock(id("test_block"), AbstractBlock.Settings.create(), TestBlock::new);
    public static final Block TEST_STAIRS = registerBlock(id("test_stairs"), AbstractBlock.Settings.create(), settings -> new TestStairsBlock(TEST_BLOCK.getDefaultState(), settings));
    public static final Block TEST_SLAB = registerBlock(id("test_slab"), AbstractBlock.Settings.create(), TestSlabBlock::new);
    public static final Block TEST_DOOR = registerBlock(id("test_door"), Block.Settings.copy(Blocks.OAK_DOOR), settings -> new DoorBlock(TEST_WOOD_BLOCKSET, settings));
    public static final Block TEST_IRON_DOOR = registerBlock(id("test_iron_door"), Block.Settings.copy(Blocks.OAK_DOOR), settings -> new DoorBlock(TEST_IRON_BLOCKSET, settings));
    public static final Block TEST_TRAP_DOOR = registerBlock(id("test_trapdoor"), Block.Settings.copy(Blocks.OAK_TRAPDOOR), settings -> new TrapdoorBlock(TEST_WOOD_BLOCKSET, settings));
    public static final Block TEST_IRON_TRAP_DOOR = registerBlock(id("test_iron_trapdoor"), Block.Settings.copy(Blocks.OAK_TRAPDOOR), settings -> new TrapdoorBlock(TEST_IRON_BLOCKSET, settings));
    public static final Block TEST_BLOCK_GLOWING = registerBlock(id("test_block_glowing"), Block.Settings.create().luminance(x -> 9), Block::new);
    public static final Block TEST_BLOCK_WIZARD = registerBlock(id("test_block_wizard"), Block.Settings.create(), settings -> new ColoredFallingBlock(new ColorCode(0), settings));

    public static final EntityType<? extends LivingEntity> TEST_ENTITY_DIRECT = registerEntity(id("test_entity_direct"), FabricEntityTypeBuilder.create().entityFactory(CreeperEntity::new).trackRangeChunks(4).dimensions(EntityDimensions.fixed(0.5f, 0.5f)));
    public static final EntityType<? extends LivingEntity> TEST_ENTITY_EXTEND_DIRECT = registerEntity(id("test_entity_extend_direct"), FabricEntityTypeBuilder.create().entityFactory(TestExtendDirectEntity::new).trackRangeChunks(4).dimensions(EntityDimensions.fixed(0.5f, 0.5f)));
    public static final EntityType<? extends LivingEntity> TEST_ENTITY_EXTEND_MOB = registerEntity(id("test_entity_extend_mob"), FabricEntityTypeBuilder.create().entityFactory(TestExtendMobEntity::new).trackRangeChunks(4).dimensions(EntityDimensions.fixed(0.5f, 0.5f)));
    public static final EntityType<? extends LivingEntity> TEST_ENTITY_EXTEND_GOLEM = registerEntity(id("test_entity_extend_golem"),FabricEntityTypeBuilder.create().entityFactory(TestExtendGolemEntity::new).trackRangeChunks(4).dimensions(EntityDimensions.fixed(0.5f, 0.5f)));
    public static final EntityType<? extends LivingEntity> TEST_ENTITY_LIVING = registerEntity(id("test_entity_extend_living"),FabricEntityTypeBuilder.create().entityFactory(TestLivingEntity::new).trackRangeChunks(4).dimensions(EntityDimensions.fixed(0.5f, 0.5f)));
    public static final EntityType<?> TEST_ENTITY_OTHER = registerEntity(id("test_entity_other"),FabricEntityTypeBuilder.create().entityFactory(TestOtherEntity::new).trackRangeChunks(4).dimensions(EntityDimensions.fixed(0.5f, 0.5f)));
    public static final EntityType<?> TEST_FLYING_WAXED_WEATHERED_CUT_COPPER_STAIRS_ENTITY = registerEntity(id("test_flying_waxed_weathered_cut_copper_stairs"), FabricEntityTypeBuilder.create().entityFactory(TestFlyingWaxedWeatheredCutCopperStairs::new).trackRangeChunks(4).dimensions(EntityDimensions.fixed(0.5f, 0.5f)));

    public static final RegistryEntry<StatusEffect> TEST_EFFECT = Registry.registerReference(Registries.STATUS_EFFECT, id("yellow_effect"), new YellowStatusEffect(StatusEffectCategory.HARMFUL, YELLOW));
    public static final Potion TEST_POTION_TYPE = Registry.register(Registries.POTION, id("yellow_potion"), new Potion("yellow_potion", new StatusEffectInstance(TEST_EFFECT, 9600)));

    @Override
    public void onInitialize() {
        FabricDefaultAttributeRegistry.register(TEST_ENTITY_DIRECT, CreeperEntity.createCreeperAttributes());
        FabricDefaultAttributeRegistry.register(TEST_ENTITY_EXTEND_DIRECT, CreeperEntity.createCreeperAttributes());

        FabricDefaultAttributeRegistry.register(TEST_ENTITY_EXTEND_MOB, MobEntity.createMobAttributes());

        FabricDefaultAttributeRegistry.register(TEST_ENTITY_EXTEND_GOLEM, MobEntity.createMobAttributes());

        FabricDefaultAttributeRegistry.register(TEST_ENTITY_LIVING, LivingEntity.createLivingAttributes());

        CommandRegistrationCallback.EVENT.register(TestCommands::register);

        var e = Registries.POTION.getRawId(TEST_POTION_TYPE);
        System.out.println("qwertgyuwgdyuyqw "+e);
        e = Registries.STATUS_EFFECT.getRawId(TEST_EFFECT.value());
        System.out.println("eeeeeeeeeeeeeeee "+e);
    }

    public static void debugSend(@Nullable PlayerEntity playerEntity, String text) {
        if (playerEntity != null) playerEntity.sendMessage(Text.literal(text), false);
    }

    private static <T extends Entity> EntityType<T> registerEntity(Identifier id, FabricEntityTypeBuilder<T> builder) {
        return Registry.register(Registries.ENTITY_TYPE, id, builder.build(RegistryKey.of(RegistryKeys.ENTITY_TYPE, id)));
    }


    private static <T extends Item> T registerItem(Identifier id, Function<Item.Settings, T> block) {
        var entry = block.apply(new Item.Settings().registryKey(RegistryKey.of(RegistryKeys.ITEM, id)));
        Registry.register(Registries.ITEM, id, entry);
        return entry;
    }

    private static <T extends Block> T registerBlock(Identifier id, AbstractBlock.Settings settings, Function<AbstractBlock.Settings, T> block) {
        var entry = block.apply(settings.registryKey(RegistryKey.of(RegistryKeys.BLOCK, id)));
        Registry.register(Registries.BLOCK, id, entry);
        Registry.register(Registries.ITEM, id, new BlockItem(entry, new Item.Settings().registryKey(RegistryKey.of(RegistryKeys.ITEM, id))));
        return entry;
    }

    private static Identifier id(String path) {
        return Identifier.of(MODID, path);
    }
}

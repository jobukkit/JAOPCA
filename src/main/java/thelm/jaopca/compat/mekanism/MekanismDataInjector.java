package thelm.jaopca.compat.mekanism;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Supplier;

import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.MultimapBuilder;

import mekanism.api.MekanismAPI;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.slurry.Slurry;
import net.minecraft.resources.ResourcePackType;
import net.minecraft.tags.Tag;
import net.minecraft.util.ResourceLocation;
import thelm.jaopca.api.config.IDynamicSpecConfig;
import thelm.jaopca.api.resources.IInMemoryResourcePack;
import thelm.jaopca.data.DataCollector;

public class MekanismDataInjector {

	private MekanismDataInjector() {}

	public static final Set<ResourceLocation> GAS_TAG_BLACKLIST = new TreeSet<>();
	public static final Set<ResourceLocation> INFUSE_TYPE_TAG_BLACKLIST = new TreeSet<>();
	public static final Set<ResourceLocation> PIGMENT_TAG_BLACKLIST = new TreeSet<>();
	public static final Set<ResourceLocation> SLURRY_TAG_BLACKLIST = new TreeSet<>();
	private static final ListMultimap<ResourceLocation, Supplier<? extends Gas>> GAS_TAGS_INJECT = MultimapBuilder.treeKeys().arrayListValues().build();
	private static final ListMultimap<ResourceLocation, Supplier<? extends InfuseType>> INFUSE_TYPE_TAGS_INJECT = MultimapBuilder.treeKeys().arrayListValues().build();
	private static final ListMultimap<ResourceLocation, Supplier<? extends Pigment>> PIGMENT_TAGS_INJECT = MultimapBuilder.treeKeys().arrayListValues().build();
	private static final ListMultimap<ResourceLocation, Supplier<? extends Slurry>> SLURRY_TAGS_INJECT = MultimapBuilder.treeKeys().arrayListValues().build();

	public static boolean registerGasTag(ResourceLocation location, Supplier<? extends Gas> gasSupplier) {
		if(GAS_TAG_BLACKLIST.contains(location)) {
			return false;
		}
		Objects.requireNonNull(location);
		Objects.requireNonNull(gasSupplier);
		return GAS_TAGS_INJECT.put(location, gasSupplier);
	}

	public static boolean registerInfuseTypeTag(ResourceLocation location, Supplier<? extends InfuseType> infuseTypeSupplier) {
		if(INFUSE_TYPE_TAG_BLACKLIST.contains(location)) {
			return false;
		}
		Objects.requireNonNull(location);
		Objects.requireNonNull(infuseTypeSupplier);
		return INFUSE_TYPE_TAGS_INJECT.put(location, infuseTypeSupplier);
	}

	public static boolean registerPigmentTag(ResourceLocation location, Supplier<? extends Pigment> pigmentSupplier) {
		if(PIGMENT_TAG_BLACKLIST.contains(location)) {
			return false;
		}
		Objects.requireNonNull(location);
		Objects.requireNonNull(pigmentSupplier);
		return PIGMENT_TAGS_INJECT.put(location, pigmentSupplier);
	}

	public static boolean registerSlurryTag(ResourceLocation location, Supplier<? extends Slurry> slurrySupplier) {
		if(SLURRY_TAG_BLACKLIST.contains(location)) {
			return false;
		}
		Objects.requireNonNull(location);
		Objects.requireNonNull(slurrySupplier);
		return SLURRY_TAGS_INJECT.put(location, slurrySupplier);
	}

	public static Set<ResourceLocation> getInjectGasTags() {
		return GAS_TAGS_INJECT.keySet();
	}

	public static Set<ResourceLocation> getInjectInfuseTypeTags() {
		return INFUSE_TYPE_TAGS_INJECT.keySet();
	}

	public static Set<ResourceLocation> getInjectPigmentTags() {
		return PIGMENT_TAGS_INJECT.keySet();
	}

	public static Set<ResourceLocation> getInjectSlurryTags() {
		return SLURRY_TAGS_INJECT.keySet();
	}

	static void setupConfig(IDynamicSpecConfig config) {
		config.setComment("gasTags", "Configurations related to gas tags.");
		GAS_TAG_BLACKLIST.addAll(Lists.transform(config.getDefinedStringList("gasTags.blacklist", new ArrayList<>(),
				"List of gas tags that should not be added."), ResourceLocation::new));
		DataCollector.getDefinedTags("gases").addAll(Lists.transform(config.getDefinedStringList("gasTags.customDefined", new ArrayList<>(),
				"List of gas tags that should be considered as defined."), ResourceLocation::new));

		config.setComment("infuseTypeTags", "Configurations related to infuse type tags.");
		INFUSE_TYPE_TAG_BLACKLIST.addAll(Lists.transform(config.getDefinedStringList("infuseTypeTags.blacklist", new ArrayList<>(),
				"List of infuse type tags that should not be added."), ResourceLocation::new));
		DataCollector.getDefinedTags("infuseTypes").addAll(Lists.transform(config.getDefinedStringList("infuseTypeTags.customDefined", new ArrayList<>(),
				"List of infuse type tags that should be considered as defined."), ResourceLocation::new));

		config.setComment("pigmentTags", "Configurations related to pigment tags.");
		PIGMENT_TAG_BLACKLIST.addAll(Lists.transform(config.getDefinedStringList("pigmentTags.blacklist", new ArrayList<>(),
				"List of infuse type tags that should not be added."), ResourceLocation::new));
		DataCollector.getDefinedTags("pigments").addAll(Lists.transform(config.getDefinedStringList("pigmentTags.customDefined", new ArrayList<>(),
				"List of infuse type tags that should be considered as defined."), ResourceLocation::new));

		config.setComment("slurryTags", "Configurations related to slurry tags.");
		SLURRY_TAG_BLACKLIST.addAll(Lists.transform(config.getDefinedStringList("slurryTags.blacklist", new ArrayList<>(),
				"List of infuse type tags that should not be added."), ResourceLocation::new));
		DataCollector.getDefinedTags("slurries").addAll(Lists.transform(config.getDefinedStringList("slurryTags.customDefined", new ArrayList<>(),
				"List of infuse type tags that should be considered as defined."), ResourceLocation::new));
	}

	static void putJsons(IInMemoryResourcePack pack) {
		GAS_TAGS_INJECT.asMap().forEach((location, suppliers)->{
			Gas[] gases = suppliers.stream().map(Supplier::get).distinct().filter(Objects::nonNull).toArray(Gas[]::new);
			Tag<Gas> tag = Tag.Builder.<Gas>create().add(gases).build(location);
			pack.putJson(ResourcePackType.SERVER_DATA, new ResourceLocation(location.getNamespace(), "tags/gases/"+location.getPath()+".json"), tag.serialize(MekanismAPI.gasRegistry()::getKey));
		});
		INFUSE_TYPE_TAGS_INJECT.asMap().forEach((location, suppliers)->{
			InfuseType[] entityTypes = suppliers.stream().map(Supplier::get).distinct().filter(Objects::nonNull).toArray(InfuseType[]::new);
			Tag<InfuseType> tag = Tag.Builder.<InfuseType>create().add(entityTypes).build(location);
			pack.putJson(ResourcePackType.SERVER_DATA, new ResourceLocation(location.getNamespace(), "tags/entity_types/"+location.getPath()+".json"), tag.serialize(MekanismAPI.infuseTypeRegistry()::getKey));
		});
		PIGMENT_TAGS_INJECT.asMap().forEach((location, suppliers)->{
			Pigment[] entityTypes = suppliers.stream().map(Supplier::get).distinct().filter(Objects::nonNull).toArray(Pigment[]::new);
			Tag<Pigment> tag = Tag.Builder.<Pigment>create().add(entityTypes).build(location);
			pack.putJson(ResourcePackType.SERVER_DATA, new ResourceLocation(location.getNamespace(), "tags/entity_types/"+location.getPath()+".json"), tag.serialize(MekanismAPI.pigmentRegistry()::getKey));
		});
		SLURRY_TAGS_INJECT.asMap().forEach((location, suppliers)->{
			Slurry[] entityTypes = suppliers.stream().map(Supplier::get).distinct().filter(Objects::nonNull).toArray(Slurry[]::new);
			Tag<Slurry> tag = Tag.Builder.<Slurry>create().add(entityTypes).build(location);
			pack.putJson(ResourcePackType.SERVER_DATA, new ResourceLocation(location.getNamespace(), "tags/entity_types/"+location.getPath()+".json"), tag.serialize(MekanismAPI.slurryRegistry()::getKey));
		});
	}
}

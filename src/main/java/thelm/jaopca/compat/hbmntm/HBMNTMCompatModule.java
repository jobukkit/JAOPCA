package thelm.jaopca.compat.hbmntm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;
import java.util.TreeSet;

import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import thelm.jaopca.api.JAOPCAApi;
import thelm.jaopca.api.config.IDynamicSpecConfig;
import thelm.jaopca.api.helpers.IMiscHelper;
import thelm.jaopca.api.materials.IMaterial;
import thelm.jaopca.api.materials.MaterialType;
import thelm.jaopca.api.modules.IModule;
import thelm.jaopca.api.modules.IModuleData;
import thelm.jaopca.api.modules.JAOPCAModule;
import thelm.jaopca.utils.ApiImpl;
import thelm.jaopca.utils.MiscHelper;

@JAOPCAModule(modDependencies = "hbm")
public class HBMNTMCompatModule implements IModule {

	private static final Set<String> TO_PLATE_BLACKLIST = new TreeSet<>(Arrays.asList(
			"AdvancedAlloy", "Aluminium", "Aluminum", "CMBSteel", "Copper", "Gold", "Iron", "Lead", "Saturnite",
			"Schrabidium", "Steel", "Titanium"));
	private static final Set<String> TO_CRYSTAL_BLACKLIST = new TreeSet<>(Arrays.asList(
			"Diamond", "Emerald", "Lapis"));
	private static Set<String> configAnvilToPlateBlacklist = new TreeSet<>();
	private static Set<String> configToCrystalBlacklist = new TreeSet<>();

	@Override
	public String getName() {
		return "hbm_compat";
	}

	@Override
	public Set<MaterialType> getMaterialTypes() {
		return EnumSet.allOf(MaterialType.class);
	}

	@Override
	public void defineModuleConfig(IModuleData moduleData, IDynamicSpecConfig config) {
		IMiscHelper helper = MiscHelper.INSTANCE;
		helper.caclulateMaterialSet(
				config.getDefinedStringList("recipes.anvilToPlateMaterialBlacklist", new ArrayList<>(),
						helper.configMaterialPredicate(), "The materials that should not have anvil to plate recipes added. (Why are press recipes hardcoded)"),
				configAnvilToPlateBlacklist);
		helper.caclulateMaterialSet(
				config.getDefinedStringList("recipes.toCrystalMaterialBlacklist", new ArrayList<>(),
						helper.configMaterialPredicate(), "The materials that should not have crystallizing to material recipes added."),
				configToCrystalBlacklist);
	}

	@Override
	public void onInit(IModuleData moduleData, FMLInitializationEvent event) {
		JAOPCAApi api = ApiImpl.INSTANCE;
		HBMNTMHelper helper = HBMNTMHelper.INSTANCE;
		IMiscHelper miscHelper = MiscHelper.INSTANCE;
		Set<String> oredict = api.getOredict();
		for(IMaterial material : moduleData.getMaterials()) {
			MaterialType type = material.getType();
			String name = material.getName();
			if(type.isIngot() && !TO_PLATE_BLACKLIST.contains(material.getName()) && !configAnvilToPlateBlacklist.contains(material.getName())) {
				String materialOredict = miscHelper.getOredictName(material.getType().getFormName(), material.getName());
				String plateOredict = miscHelper.getOredictName("plate", material.getName());
				if(oredict.contains(plateOredict)) {
					helper.registerAnvilConstructionRecipe(
							miscHelper.getRecipeKey("hbm.material_to_plate_anvil", material.getName()),
							new Object[] {
									materialOredict, 1,
							}, new Object[] {
									plateOredict, 1, 1F,
							}, 3);
				}
			}
			if(type.isCrystalline() && !TO_CRYSTAL_BLACKLIST.contains(material.getName()) && !configToCrystalBlacklist.contains(material.getName())) {
				String dustOredict = miscHelper.getOredictName("dust", material.getName());
				String materialOredict = miscHelper.getOredictName(material.getType().getFormName(), material.getName());
				if(oredict.contains(dustOredict)) {
					helper.registerCrystallizerRecipe(
							miscHelper.getRecipeKey("hbm.dust_to_material", material.getName()),
							dustOredict, materialOredict, 1);
				}
			}
		}
	}
}

package thelm.jaopca.api.fluids;

import net.minecraft.block.Block;
import net.minecraftforge.fluids.Fluid;
import thelm.jaopca.api.blocks.IBlockProvider;
import thelm.jaopca.api.materialforms.IMaterialFormInfo;

public interface IFluidInfo extends IMaterialFormInfo, IFluidProvider, IBlockProvider {

	IMaterialFormFluid getMaterialFormFluid();

	IMaterialFormFluidBlock getMaterialFormFluidBlock();

	@Override
	default Fluid asFluid() {
		return getMaterialFormFluid().asFluid();
	}

	@Override
	default Block asBlock() {
		return getMaterialFormFluidBlock().asBlock();
	}

	@Override
	default IMaterialFormFluid getMaterialForm() {
		return getMaterialFormFluid();
	}
}

package forestry.arboriculture.blocks.property;

import com.google.common.base.Objects;

import java.util.ArrayList;
import java.util.List;

import forestry.api.arboriculture.IAlleleTreeSpecies;
import forestry.api.genetics.AlleleManager;
import forestry.api.genetics.IAllele;
import forestry.core.blocks.propertys.PropertyAllele;

public class PropertyTree extends PropertyAllele<IAlleleTreeSpecies> {

	public PropertyTree(String name) {
		super(name);
	}

	@Override
	public Class<IAlleleTreeSpecies> getValueClass() {
		return IAlleleTreeSpecies.class;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this).add("name", this.name).add("clazz", IAlleleTreeSpecies.class).add("values", this.getAllowedValues()).toString();
	}

	@Override
	public int hashCode() {
		return 31 * IAlleleTreeSpecies.class.hashCode() + this.name.hashCode();
	}

	@Override
	public List<IAlleleTreeSpecies> getAllowedValues() {
		List<IAlleleTreeSpecies> trees = new ArrayList<>();
		for (IAllele allele : AlleleManager.alleleRegistry.getRegisteredAlleles().values()) {
			if (allele instanceof IAlleleTreeSpecies) {
				trees.add((IAlleleTreeSpecies) allele);
			}
		}
		return trees;
	}

	@Override
	public String getName(IAlleleTreeSpecies value) {
		return value.getModelName().replace("tree", "");
	}
	
}

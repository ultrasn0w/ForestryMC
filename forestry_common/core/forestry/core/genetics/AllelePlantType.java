/*******************************************************************************
 * Copyright (c) 2011-2014 SirSengir.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl-3.0.txt
 * 
 * Various Contributors including, but not limited to:
 * SirSengir (original work), CovertJaguar, Player, Binnie, MysteriousAges
 ******************************************************************************/
package forestry.core.genetics;

import java.util.EnumSet;

import net.minecraftforge.common.EnumPlantType;

import forestry.api.genetics.IAllelePlantType;

public class AllelePlantType extends Allele implements IAllelePlantType {

	private EnumSet<EnumPlantType> types;

	protected AllelePlantType(String uid, EnumPlantType type) {
		this(uid, type, false);
	}

	protected AllelePlantType(String uid, EnumPlantType type, boolean isDominant) {
		this(uid, EnumSet.of(type), isDominant);
	}

	protected AllelePlantType(String uid, EnumSet<EnumPlantType> types, boolean isDominant) {
		super(uid, isDominant);
		this.types = types;
	}

	public EnumSet<EnumPlantType> getPlantTypes() {
		return types;
	}
}

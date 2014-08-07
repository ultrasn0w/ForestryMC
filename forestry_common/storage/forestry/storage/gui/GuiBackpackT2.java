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
package forestry.storage.gui;


import forestry.core.config.Defaults;
import forestry.core.gui.ContainerForestry;

public class GuiBackpackT2 extends GuiBackpack {

	public GuiBackpackT2(ContainerForestry container) {
		super(Defaults.TEXTURE_PATH_GUI + "/backpackT2.png", container);

		xSize = 176;
		ySize = 192;
	}
}

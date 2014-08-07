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
package forestry.core.gui.buttons;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiButtonSmall extends GuiBetterButton {

    public GuiButtonSmall(int i, int x, int y, String s) {
        this(i, x, y, 200, s);
    }

    public GuiButtonSmall(int i, int x, int y, int w, String s) {
        super(i, x, y, w, StandardButtonTextureSets.SMALL_BUTTON, s);
    }

}

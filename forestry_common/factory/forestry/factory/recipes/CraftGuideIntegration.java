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
package forestry.factory.recipes;

import java.lang.reflect.Method;

import forestry.core.proxy.Proxies;

public class CraftGuideIntegration {

	public static void register() {
		registerRecipeProviders();
	}

	private static void registerRecipeProviders() {

		Proxies.log.info("Registering CraftGuide integration.");
		try {
			Class<?> c = Class.forName("uristqwerty.CraftGuide.ReflectionAPI");
			Method m = c.getMethod("registerAPIObject", Object.class);

			Proxies.log.fine("Adding crafting handler custom recipes.");
			m.invoke(null, new CraftGuideCustomRecipes());
			Proxies.log.fine("Adding crafting handler for the carpenter.");
			m.invoke(null, new CraftGuideCarpenter());
			Proxies.log.fine("Adding crafting handler for the thermionic fabricator.");
			m.invoke(null, new CraftGuideFabricator());
			Proxies.log.fine("Adding crafting handler for the centrifuge.");
			m.invoke(null, new CraftGuideCentrifuge());
			Proxies.log.fine("Adding crafting handler for the squeezer.");
			m.invoke(null, new CraftGuideSqueezer());
			Proxies.log.fine("Adding crafting handler for the fermenter.");
			m.invoke(null, new CraftGuideFermenter());
			Proxies.log.fine("Adding crafting handler for the still.");
			m.invoke(null, new CraftGuideStill());
			Proxies.log.fine("Adding crafting handler for the bottler.");
			m.invoke(null, new CraftGuideBottler());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

}

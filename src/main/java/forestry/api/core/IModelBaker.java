/*******************************************************************************
 * Copyright 2011-2014 SirSengir
 * 
 * This work (the API) is licensed under the "MIT" License, see LICENSE.txt for details.
 ******************************************************************************/
package forestry.api.core;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.util.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * A model baker to make custom models
 */
@SideOnly(Side.CLIENT)
public interface IModelBaker {

	void setRenderBoundsFromBlock(@Nonnull Block block);

	void setRenderBounds(double d, double e, double f, double g, double h, double i );

	void setColorIndex(int color);

	void addBlockModel(@Nonnull Block block, @Nullable BlockPos pos, @Nonnull TextureAtlasSprite[] sprites, int colorIndex);
	
	void addBlockModel(@Nonnull Block block, @Nullable BlockPos pos, @Nonnull TextureAtlasSprite sprites, int colorIndex);
	
	void addBakedModel(@Nonnull IBakedModel model);
	
	void addFaceXNeg(@Nonnull TextureAtlasSprite sprite);
	
	void addFaceYNeg(@Nonnull TextureAtlasSprite sprite);

	void addFaceZNeg(@Nonnull TextureAtlasSprite sprite);

	void addFaceYPos(@Nonnull TextureAtlasSprite sprite);

	void addFaceZPos(@Nonnull TextureAtlasSprite sprite);

	void addFaceXPos(@Nonnull TextureAtlasSprite sprite);
	
	void setParticleSprite(@Nullable TextureAtlasSprite particleSprite);
	
	IModelBakerModel bakeModel(boolean flip);
	
}

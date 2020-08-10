/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2014, AlgorithmX2, All rights reserved.
 *
 * Applied Energistics 2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Applied Energistics 2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Applied Energistics 2.  If not, see <http://www.gnu.org/licenses/lgpl>.
 */

package appeng.client.render.spatial;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.ModelBakeSettings;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.util.Identifier;

import appeng.client.render.BasicUnbakedModel;
import appeng.core.AppEng;

public class SpatialPylonModel implements BasicUnbakedModel {

    @Nullable
    @Override
    public BakedModel bake(ModelLoader loader, Function<SpriteIdentifier, Sprite> textureGetter,
            ModelBakeSettings rotationContainer, Identifier modelId) {
        Map<SpatialPylonTextureType, Sprite> textures = new EnumMap<>(SpatialPylonTextureType.class);

        for (SpatialPylonTextureType type : SpatialPylonTextureType.values()) {
            textures.put(type, textureGetter.apply(getTexturePath(type)));
        }

        return new SpatialPylonBakedModel(textures);
    }

    @Override
    public Stream<SpriteIdentifier> getAdditionalTextures() {
        return Arrays.stream(SpatialPylonTextureType.values()).map(SpatialPylonModel::getTexturePath);
    }

    private static SpriteIdentifier getTexturePath(SpatialPylonTextureType type) {
        return new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEX,
                new Identifier(AppEng.MOD_ID, "block/spatial_pylon/" + type.name().toLowerCase(Locale.ROOT)));
    }

}

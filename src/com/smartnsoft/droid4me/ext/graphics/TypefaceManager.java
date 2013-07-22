package com.smartnsoft.droid4me.ext.graphics;

import java.util.HashMap;
import java.util.Map;

import android.content.res.AssetManager;
import android.graphics.Typeface;

/**
 * A helper class for managing Android fonts.
 * 
 * @author Édouard Mercier
 * @since 2013.07.22
 * 
 * @param <T>
 *          a enumerated type that the developer needs to create in order to name and refer to typefaces
 */
public class TypefaceManager<T extends Enum<?>>
{

  private final Map<T, Typeface> typefacesMap = new HashMap<T, Typeface>();

  public final Typeface getTypeface(AssetManager assetManager, T typefaces)
  {
    {
      final Typeface typeface = typefacesMap.get(typefaces);
      if (typeface != null)
      {
        return typeface;
      }
    }

    final String fontPath = getTypefacePath(typefaces);
    final Typeface typeface = Typeface.createFromAsset(assetManager, fontPath);
    typefacesMap.put(typefaces, typeface);
    return typeface;
  }

  public final String getTypefacePath(T typeface)
  {
    return getAssetsFontFolderPathPrefix() + typeface.toString();
  }

  protected String getAssetsFontFolderPathPrefix()
  {
    return "font/";
  }

}

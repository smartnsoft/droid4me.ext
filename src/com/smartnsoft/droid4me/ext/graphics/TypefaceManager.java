package com.smartnsoft.droid4me.ext.graphics;

import java.util.HashMap;
import java.util.Map;

import android.content.res.AssetManager;
import android.graphics.Typeface;

import com.smartnsoft.droid4me.ext.graphics.TypefaceManager.Typefaceable;

/**
 * A helper class for managing Android fonts.
 * 
 * @author Édouard Mercier
 * @since 2013.07.22
 * 
 * @param <T>
 *          a enumerated type that the developer needs to create in order to name and refer to typefaces
 */
public class TypefaceManager<T extends Typefaceable>
{

  public static interface Typefaceable
  {

    Typeface fromNative();
  }

  private final Map<T, Typeface> typefacesMap = new HashMap<T, Typeface>();

  public final Typeface getTypeface(AssetManager assetManager, T typefaces)
  {
    {
      // We first check whether the font is native
      final Typeface typeface = typefaces.fromNative();
      if (typeface != null)
      {
        return typeface;
      }
    }
    {
      final Typeface typeface = typefacesMap.get(typefaces);
      if (typeface != null)
      {
        return typeface;
      }
    }

    final String fontPathPrefix = getAssetsFontFolderPathPrefix();
    final Typeface typeface = Typeface.createFromAsset(assetManager, fontPathPrefix + typefaces.toString());
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
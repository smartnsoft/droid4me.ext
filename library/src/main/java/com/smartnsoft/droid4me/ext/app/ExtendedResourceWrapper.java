/*
 * (C) Copyright 2009-2014 Smart&Soft SAS (http://www.smartnsoft.com/) and contributors.
 *
 * The code hereby is the full property of Smart&Soft, SIREN 444 622 690.
 * 34, boulevard des Italiens - 75009 - Paris - France
 * contact@smartnsoft.com - 00 33 6 79 60 05 49
 *
 * You are not allowed to use the source code or the resulting binary code, nor to modify the source code, without prior permission of the owner.
 * 
 * This library is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * Contributors:
 *     Smart&Soft - initial API and implementation
 */

package com.smartnsoft.droid4me.ext.app;

import java.io.File;

import android.content.res.AssetManager;
import android.content.res.Resources;
import android.content.res.Resources.Theme;

/**
 * A extension point for resources taken from an external .apk files.
 *
 * @author Édouard Mercier
 * @since 2013.08.07
 */
public class ExtendedResourceWrapper
{

  private final AssetManager assets;

  private final Resources resources;

  private final Theme theme;

  public ExtendedResourceWrapper(File apkFilePath, Resources superResources, AssetManager superAssets, Theme superTheme)
  {
    if (apkFilePath != null)
    {
      try
      {
        final AssetManager newAssets = AssetManager.class.newInstance();
        newAssets.getClass().getMethod("addAssetPath", String.class).invoke(newAssets, apkFilePath.getAbsolutePath());
        assets = newAssets;
      }
      catch (Exception exception)
      {
        throw new RuntimeException(exception);
      }

      resources = new Resources(assets, superResources.getDisplayMetrics(), superResources.getConfiguration());
      theme = resources.newTheme();
      theme.setTo(superTheme);
    }
    else
    {
      assets = superAssets;
      resources = superResources;
      theme = superTheme;
    }
  }

  public AssetManager getAssets()
  {
    return assets;
  }

  public Resources getResources()
  {
    return resources;
  }

  public Theme getTheme()
  {
    return theme;
  }

}

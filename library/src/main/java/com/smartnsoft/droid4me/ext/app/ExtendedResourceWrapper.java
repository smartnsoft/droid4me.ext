// The MIT License (MIT)
//
// Copyright (c) 2017 Smart&Soft
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in all
// copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// SOFTWARE.

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

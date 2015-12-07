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
 * A configuration class when loading resources from external .apk files.
 *
 * @author Édouard Mercier
 * @since 2013.08.07
 */
public class ExtendedConfiguration
{

  private static volatile ExtendedConfiguration instance;

  // We accept the "out-of-order writes" case
  public static ExtendedConfiguration getInstance()
  {
    if (instance == null)
    {
      synchronized (ExtendedConfiguration.class)
      {
        if (instance == null)
        {
          instance = new ExtendedConfiguration();
        }
      }
    }
    return instance;
  }

  private File apkFile;

  public void addApkFile(File apkFile)
  {
    this.apkFile = apkFile;
  }

  public ExtendedResourceWrapper get(Resources superResources, AssetManager superAssets, Theme superTheme)
  {
    return new ExtendedResourceWrapper(apkFile, superResources, superAssets, superTheme);
  }

}
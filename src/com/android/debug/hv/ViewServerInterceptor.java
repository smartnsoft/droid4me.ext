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

package com.android.debug.hv;

import android.app.Activity;

import com.smartnsoft.droid4me.app.ActivityController.Interceptor;

/**
 * Enables to integrate the ViewServer component (https://github.com/romainguy/ViewServer), for being able to use the "Hierarchy Viewer" Android tool.
 * 
 * @author Édouard Mercier, Jocelyn Girard, Yannick Platon
 * @since 2013.06.26
 */
public final class ViewServerInterceptor
    implements Interceptor
{

  @Override
  public void onLifeCycleEvent(Activity activity, Object component, InterceptorEvent event)
  {
    if (component != null)
    {
      return;
    }
    if (event == InterceptorEvent.onContentChanged)
    {
      ViewServer.get(activity).addWindow(activity);
    }
    else if (event == InterceptorEvent.onResume)
    {
      ViewServer.get(activity).setFocusedWindow(activity);
    }
    else if (event == InterceptorEvent.onDestroy)
    {
      ViewServer.get(activity).removeWindow(activity);
    }
  }

}

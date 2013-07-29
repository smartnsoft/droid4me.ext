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

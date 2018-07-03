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

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import com.smartnsoft.droid4me.LifeCycle.BusinessObjectUnavailableException;
import com.smartnsoft.droid4me.app.ActivityController;
import com.smartnsoft.droid4me.app.SmartActivity;
import com.smartnsoft.droid4me.app.Smartable;
import com.smartnsoft.droid4me.app.SmartableActivity;
import com.smartnsoft.droid4me.ext.app.ActivityAnnotations.ActivityAnnotation;
import com.smartnsoft.droid4me.ext.app.ActivityAnnotations.FragmentAnnotation;
import com.smartnsoft.droid4me.support.v4.app.SmartFragment;

/**
 * An interceptor which is responsible for handling the {@links ActivityAnnotations} declarations on {@link SmartActivity} and {@link SmartFragment}.
 *
 * @author Jocelyn Girard, Willy Noel
 * @since 2014.04.08
 */
public abstract class ActivityInterceptor<ActivityAggregateClass extends ActivityAggregate<? extends Application>, FragmentAggregateClass extends FragmentAggregate<? extends Application, ? extends Activity>>
    implements ActivityController.Interceptor
{

  public static final class BusinessObjectsContainer
  {

    private static final String BUSINESS_OBJECT_UNAVAILABLE_EXCEPTION = "businessObjectUnavailableException";

    private BusinessObjectUnavailableException exception;

    public void onRestoreInstanceState(Bundle bundle)
    {
      if (bundle != null)
      {
        exception = (BusinessObjectUnavailableException) bundle.getSerializable(BusinessObjectsContainer.BUSINESS_OBJECT_UNAVAILABLE_EXCEPTION);
      }
    }

    public void onSaveInstanceState(Bundle bundle)
    {
      if (exception != null)
      {
        bundle.putSerializable(BusinessObjectsContainer.BUSINESS_OBJECT_UNAVAILABLE_EXCEPTION, exception);
      }
    }
  }

  /**
   * This method should be invoked during the {@link ActivityController.Interceptor#onLifeCycleEvent(Activity, Object, InterceptorEvent)} method, and
   * it will handle everything.
   */
  @SuppressWarnings("unchecked")
  @Override
  public void onLifeCycleEvent(final Activity activity, Object component, InterceptorEvent interceptorEvent)
  {
    if (interceptorEvent == InterceptorEvent.onSuperCreateBefore)
    {
      if (component instanceof Smartable<?>)
      {
        // It's a Fragment
        final Smartable<FragmentAggregateClass> smartableFragment = (Smartable<FragmentAggregateClass>) component;
        final FragmentAnnotation fragmentAnnotation = smartableFragment.getClass().getAnnotation(FragmentAnnotation.class);
        smartableFragment.setAggregate(instantiateFragmentAggregate(smartableFragment, fragmentAnnotation));
      }
      else
      {
        // It's an Activity
        final Smartable<ActivityAggregateClass> smartableActivity = (Smartable<ActivityAggregateClass>) activity;
        final ActivityAnnotation activityAnnotation = activity.getClass().getAnnotation(ActivityAnnotation.class);
        smartableActivity.setAggregate(instantiateActivityAggregate(activity, smartableActivity, activityAnnotation));
      }
    }
    else if (interceptorEvent == InterceptorEvent.onCreate)
    {
      if (component == null && activity instanceof Smartable<?>)
      {
        // This is an Activity
        final Smartable<ActivityAggregateClass> smartableActivity = (Smartable<ActivityAggregateClass>) activity;
        smartableActivity.getAggregate().onCreate();
      }
    }
    else if (interceptorEvent == InterceptorEvent.onCreateDone)
    {
      if (component instanceof Smartable<?>)
      {
        // We handle a Fragment
        final Smartable<FragmentAggregateClass> smartableFragment = (Smartable<FragmentAggregateClass>) component;
        smartableFragment.getAggregate().onCreateDone(activity);
      }
    }
  }

  /**
   * This method is responsible for instantiating an {@link ActivityAggregate} class, which will be defined as a {@link SmartableActivity} aggregate.
   *
   * @param activity
   * @param smartable
   * @param annotation
   * @return the new instance of {@link ActivityAggregate}
   */
  protected abstract ActivityAggregateClass instantiateActivityAggregate(Activity activity,
      Smartable<ActivityAggregateClass> smartable, ActivityAnnotation annotation);

  /**
   * This method is responsible for instantiating an {@link FragmentAggregate} class, which will be defined as a {@link Smartable} fragment aggregate.
   *
   * @param smartableFragment
   * @param fragmentAnnotation
   * @return the new instance of {@link FragmentAggregate}
   */
  protected abstract FragmentAggregateClass instantiateFragmentAggregate(
      Smartable<FragmentAggregateClass> smartableFragment, FragmentAnnotation fragmentAnnotation);

}

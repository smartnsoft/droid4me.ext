package com.smartnsoft.droid4me.ext.app;

import android.app.Activity;
import com.smartnsoft.droid4me.app.ActivityController;
import com.smartnsoft.droid4me.app.SmartApplication;
import com.smartnsoft.droid4me.app.Smartable;
import com.smartnsoft.droid4me.ext.app.ActivityAnnotations.ActivityAnnotation;
import com.smartnsoft.droid4me.ext.app.ActivityAnnotations.FragmentAnnotation;

/**
 * @author Jocelyn Girard, Willy Noel
 * @since 2014.04.08
 */
public abstract class ActivityContainerInterceptor<ActivityAggregateClass extends ActivityAggregate<? extends SmartApplication>, FragmentAggregateClass extends FragmentAggregate<? extends SmartApplication, ? extends Activity>>
    implements ActivityController.Interceptor
{

  protected abstract ActivityAggregateClass instantiateActivityAggregate(Activity activity,
      Smartable<ActivityAggregateClass> smartableActivity, ActivityAnnotation annotation);

  protected abstract FragmentAggregateClass instantiateFragmentAggregate(
      Smartable<FragmentAggregateClass> smartableFragment, FragmentAnnotation fragmentAnnotation);

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
        smartableFragment.getAggregate().onCreateDone();
      }
    }
  }

}

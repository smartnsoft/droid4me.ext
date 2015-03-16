package com.smartnsoft.droid4me.ext.app;

import android.app.Activity;

import com.smartnsoft.droid4me.app.ActivityController;
import com.smartnsoft.droid4me.app.SmartActivity;
import com.smartnsoft.droid4me.app.SmartApplication;
import com.smartnsoft.droid4me.app.SmartFragment;
import com.smartnsoft.droid4me.app.Smartable;
import com.smartnsoft.droid4me.app.SmartableActivity;
import com.smartnsoft.droid4me.ext.app.ActivityAnnotations.ActivityAnnotation;
import com.smartnsoft.droid4me.ext.app.ActivityAnnotations.FragmentAnnotation;

/**
 * An interceptor which is responsible for handling the {@links ActivityAnnotations} declarations on {@link SmartActivity} and {@link SmartFragment}.
 * 
 * @author Jocelyn Girard, Willy Noel
 * @since 2014.04.08
 */
public abstract class ActivityInterceptor<ActivityAggregateClass extends ActivityAggregate<? extends SmartApplication>, FragmentAggregateClass extends FragmentAggregate<? extends SmartApplication, ? extends Activity>>
    implements ActivityController.Interceptor
{

  /**
   * This method is responsible for instantiating an {@link ActivityAggregate} class, which will be defined as a {@link SmartableActivity} aggregate.
   * 
   * @param activity
   * @param smartable
   * @param annotation
   * @return the new instance of {@link ActivityAggregate}
   */
  protected abstract ActivityAggregateClass instantiateActivityAggregate(Activity activity, Smartable<ActivityAggregateClass> smartable,
      ActivityAnnotation annotation);

  /**
   * This method is responsible for instantiating an {@link FragmentAggregate} class, which will be defined as a {@link Smartable} fragment aggregate.
   * 
   * @param smartableFragment
   * @param fragmentAnnotation
   * @return the new instance of {@link FragmentAggregate}
   */
  protected abstract FragmentAggregateClass instantiateFragmentAggregate(Smartable<FragmentAggregateClass> smartableFragment,
      FragmentAnnotation fragmentAnnotation);

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

}

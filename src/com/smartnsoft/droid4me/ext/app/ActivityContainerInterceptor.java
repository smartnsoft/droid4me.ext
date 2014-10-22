package com.smartnsoft.droid4me.ext.app;

import android.app.Activity;

import com.smartnsoft.droid4me.app.ActivityController;
import com.smartnsoft.droid4me.app.SmartApplication;
import com.smartnsoft.droid4me.app.Smartable;
import com.smartnsoft.droid4me.ext.app.ActivityContainerParameter.ActivityParameters;
import com.smartnsoft.droid4me.ext.app.ActivityContainerParameter.FragmentParameters;

/**
 * @author Jocelyn Girard, Willy Noel
 * @since 2014.04.08
 */
public abstract class ActivityContainerInterceptor<ActivityAggregateClass extends ActivityAggregate<? extends SmartApplication>, FragmentAggregateClass extends FragmentAggregate<? extends SmartApplication, ? extends Activity>>
    implements ActivityController.Interceptor
{

  // TODO: shift this to the ActivityParameters annotations
  protected abstract int getActivityContentViewResourceId();

  protected abstract ActivityAggregateClass instantiateActivityAggregate(Activity activity, Smartable<ActivityAggregateClass> smartableActivity,
      ActivityParameters annotation);

  protected abstract FragmentAggregateClass instantiateFragmentAggregate(Smartable<FragmentAggregateClass> smartableFragment);

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
        smartableFragment.setAggregate(instantiateFragmentAggregate(smartableFragment));
      }
      else
      {
        // It's an Activity
        final Smartable<ActivityAggregateClass> smartableActivity = (Smartable<ActivityAggregateClass>) activity;
        smartableActivity.setAggregate(instantiateActivityAggregate(activity, smartableActivity, activity.getClass().getAnnotation(ActivityParameters.class)));
      }
    }
    else if (interceptorEvent == InterceptorEvent.onCreate)
    {
      if (component == null && activity instanceof Smartable<?>)
      {
        // This is an Activity
        final Smartable<?> smartableActivity = (Smartable<?>) activity;
        if (smartableActivity.getAggregate() instanceof ActivityAggregate)
        {
          final Smartable<ActivityAggregate<? extends SmartApplication>> activityContainerSmartable = (Smartable<ActivityAggregate<? extends SmartApplication>>) smartableActivity;
          if (activityContainerSmartable.getAggregate().getActivityParameters() != null)
          {
            activity.setContentView(getActivityContentViewResourceId());
            activityContainerSmartable.getAggregate().openParameterFragment();
            activityContainerSmartable.getAggregate().setActionBarBehavior();
          }
        }
      }
    }
    else if (interceptorEvent == InterceptorEvent.onCreateDone)
    {
      if (component instanceof Smartable<?>)
      {
        // We handle a Fragment
        final Smartable<FragmentAggregateClass> smartableFragment = (Smartable<FragmentAggregateClass>) component;
        final FragmentParameters fragmentParameters = smartableFragment.getClass().getAnnotation(ActivityContainerParameter.FragmentParameters.class);
        // TODO: delegate the handling to the Fragment Aggregate
        if (fragmentParameters != null)
        {
          // We set the "title"
          final int resourceTitle = fragmentParameters.fragmentTitle();
          if (resourceTitle != -1)
          {
            smartableFragment.getAggregate().setActionBarTitle(resourceTitle);
          }
          final int resourceSubTitle = fragmentParameters.fragmentSubTitle();
          if (resourceSubTitle != -1)
          {
            smartableFragment.getAggregate().setActionBarSubTitle(resourceSubTitle);
          }
        }
      }
    }
  }

}

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
public abstract class ActivityContainerInterceptor<ActivityAggregateClass extends ActivityContainerAggregate, FragmentAggregateClass extends FragmentAggregate<? extends SmartApplication, ? extends Activity>>
    implements ActivityController.Interceptor
{

  protected abstract int getActivityContentViewResourceId();

  protected abstract ActivityAggregateClass instanciateActivityAggregate(Activity activity, Smartable<ActivityAggregateClass> smartableActivity,
      ActivityParameters annotation);

  protected abstract FragmentAggregateClass instanciateFragmentAggregate(Smartable<FragmentAggregateClass> smartableFragment);

  @SuppressWarnings("unchecked")
  @Override
  public void onLifeCycleEvent(final Activity activity, Object component, InterceptorEvent interceptorEvent)
  {
    if (interceptorEvent == InterceptorEvent.onSuperCreateBefore)
    {
      if (component != null)
      {
        // It's a Fragment
        final Smartable<FragmentAggregateClass> smartableFragment = (Smartable<FragmentAggregateClass>) component;
        smartableFragment.setAggregate(instanciateFragmentAggregate(smartableFragment));
      }
      else
      {
        // It's an Activity
        final Smartable<ActivityAggregateClass> smartableActivity = (Smartable<ActivityAggregateClass>) activity;
        smartableActivity.setAggregate(instanciateActivityAggregate(activity, smartableActivity, activity.getClass().getAnnotation(ActivityParameters.class)));
      }
    }
    else if (interceptorEvent == InterceptorEvent.onCreate)
    {
      if (component == null && activity instanceof Smartable<?>)
      {
        // This is an Activity
        final Smartable<?> smartable = (Smartable<?>) activity;
        if (smartable.getAggregate() != null && smartable.getAggregate() instanceof ActivityContainerAggregate)
        {
          final Smartable<ActivityContainerAggregate> activityContainerSmartable = (Smartable<ActivityContainerAggregate>) smartable;
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
        if (fragmentParameters != null && activity instanceof Smartable<?>)
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

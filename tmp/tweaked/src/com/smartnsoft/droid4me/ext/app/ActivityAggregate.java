package com.smartnsoft.droid4me.ext.app;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment.SavedState;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.smartnsoft.droid4me.app.SmartApplication;
import com.smartnsoft.droid4me.app.Smartable;
import com.smartnsoft.droid4me.ext.app.ActivityAnnotations.ActionBarBehavior;
import com.smartnsoft.droid4me.ext.app.ActivityAnnotations.ActionBarTitleBehavior;
import com.smartnsoft.droid4me.ext.app.ActivityAnnotations.ActivityAnnotation;
import com.smartnsoft.droid4me.log.Logger;
import com.smartnsoft.droid4me.log.LoggerFactory;
import com.smartnsoft.droid4me.support.v4.app.SmartFragment;

/**
 * @author Jocelyn Girard, Willy Noel
 * @since 2014.04.08
 */
public abstract class ActivityAggregate<SmartApplicationClass extends SmartApplication>
{

  protected final static Logger log = LoggerFactory.getInstance(ActivityAggregate.class);

  protected final Activity activity;

  protected final Smartable<?> smartable;

  private final ActivityAnnotation activityAnnotation;

  protected SmartFragment<?> fragment;

  public ActivityAggregate(Activity activity, Smartable<?> smartable, ActivityAnnotation activityAnnotation)
  {
    this.activity = activity;
    this.smartable = smartable;
    this.activityAnnotation = activityAnnotation;
  }

  @SuppressWarnings("unchecked")
  public SmartApplicationClass getApplication()
  {
    return (SmartApplicationClass) activity.getApplication();
  }

  /**
   * Open the specified fragment, the previous fragment is add to the back stack.
   *
   * @param fragmentClass
   */
  public final void openFragment(Class<? extends SmartFragment<?>> fragmentClass)
  {
    openFragment(fragmentClass, activityAnnotation.fragmentContainerIdentifier(), activityAnnotation.addFragmentToBackStack(), activityAnnotation.fragmentBackStackName(), null, activity.getIntent().getExtras());
  }

  /**
   * Open the specified fragment, the previous fragment is add to the back stack.
   *
   * @param fragmentClass
   */
  public final void openFragment(Class<? extends SmartFragment<?>> fragmentClass, @IdRes int fragmentContainerIdentifer,
      boolean addFragmentToBackStack, @Nullable String fragmentBackStackName)
  {
    openFragment(fragmentClass, fragmentContainerIdentifer, addFragmentToBackStack, fragmentBackStackName, null, activity.getIntent().getExtras());
  }

  /**
   * Open the specified fragment, the previous fragment is add to the back stack.
   *
   * @param fragmentClass
   */
  public final void openFragment(Class<? extends SmartFragment<?>> fragmentClass, SavedState savedState,
      Bundle arguments)
  {
    openFragment(fragmentClass, activityAnnotation.fragmentContainerIdentifier(), activityAnnotation.addFragmentToBackStack(), activityAnnotation.fragmentBackStackName(), null, activity.getIntent().getExtras());
  }

  /**
   * Open the specified fragment, the previous fragment is add to the back stack.
   *
   * @param fragmentClass
   * @param arguments
   */
  public final void openFragment(Class<? extends SmartFragment<?>> fragmentClass, @IdRes int fragmentContainerIdentifer,
      boolean addFragmentToBackStack, @Nullable String fragmentBackStackName, SavedState savedState, Bundle arguments)
  {
    try
    {
      fragment = fragmentClass.newInstance();
      fragment.setArguments(arguments);

      // We (re)set its initial state if necessary
      if (savedState != null)
      {
        fragment.setInitialSavedState(savedState);
      }

      final FragmentTransaction fragmentTransaction = ((FragmentActivity) activity).getSupportFragmentManager().beginTransaction();
      fragmentTransaction.replace(fragmentContainerIdentifer, fragment);

      if (addFragmentToBackStack == true)
      {
        fragmentTransaction.addToBackStack(fragmentBackStackName);
      }

      fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
      fragmentTransaction.commitAllowingStateLoss();
    }
    catch (Exception exception)
    {
      if (log.isErrorEnabled())
      {
        log.error("Unable to instanciate the fragment '" + fragmentClass.getSimpleName() + "'", exception);
      }
    }
  }

  public final SmartFragment<?> getOpenedFragment()
  {
    return fragment;
  }

  public final ActivityAnnotation getActivityAnnotation()
  {
    return activityAnnotation;
  }

  protected void onCreate()
  {
    if (activityAnnotation != null)
    {
      activity.setContentView(activityAnnotation.contentViewIdentifier());
      final int toolbarIdentifier = activityAnnotation.toolbarIdentifier();
      if (toolbarIdentifier > 0 && activity instanceof AppCompatActivity)
      {
        final Toolbar toolbar = (Toolbar) activity.findViewById(toolbarIdentifier);
        ((AppCompatActivity) activity).setSupportActionBar(toolbar);
      }
      setActionBarBehavior();
      if (activity instanceof FragmentActivity)
      {
        final FragmentActivity fragmentActivity = (FragmentActivity) activity;
        fragment = (SmartFragment<?>) fragmentActivity.getSupportFragmentManager().findFragmentById(activityAnnotation.fragmentContainerIdentifier());
        if (fragment == null)
        {
          openParameterFragment();
        }
      }
    }
  }

  protected void openParameterFragment()
  {
    if (activityAnnotation != null)
    {
      if (activityAnnotation.fragmentClass() != AbsSmartFragment.class && activityAnnotation.fragmentContainerIdentifier() != -1)
      {
        openFragment(activityAnnotation.fragmentClass());
      }
    }
  }

  protected abstract Object getActionBar(Activity activity);

  protected void setActionBarBehavior()
  {
    final Object actionBarObject = getActionBar(activity);
    if (actionBarObject instanceof ActionBar)
    {
      final ActionBar actionBar = (ActionBar) actionBarObject;
      final ActionBarTitleBehavior actionBarTitleBehavior = activityAnnotation.actionBarTitleBehavior();
      switch (actionBarTitleBehavior)
      {
      case UseIcon:
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayUseLogoEnabled(false);
        actionBar.setDisplayShowHomeEnabled(true);
        break;

      case UseLogo:
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayUseLogoEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        break;

      default:
      case UseTitle:
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setDisplayUseLogoEnabled(false);
        actionBar.setDisplayShowHomeEnabled(true);
        break;
      }
      final ActionBarBehavior actionBarUpBehavior = activityAnnotation.actionBarUpBehavior();
      switch (actionBarUpBehavior)
      {
      case ShowAsUp:
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        break;

      case ShowAsDrawer:
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(false);
        break;

      default:
      case None:
        actionBar.setHomeButtonEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(false);
        break;
      }
    }
    else if (actionBarObject instanceof android.app.ActionBar)
    {
      final android.app.ActionBar actionBar = (android.app.ActionBar) actionBarObject;
      final ActionBarTitleBehavior actionBarTitleBehavior = activityAnnotation.actionBarTitleBehavior();
      switch (actionBarTitleBehavior)
      {
      case UseIcon:
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayUseLogoEnabled(false);
        actionBar.setDisplayShowHomeEnabled(true);
        break;

      case UseLogo:
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayUseLogoEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        break;

      default:
      case UseTitle:
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setDisplayUseLogoEnabled(false);
        actionBar.setDisplayShowHomeEnabled(true);
        break;
      }
      final ActionBarBehavior actionBarUpBehavior = activityAnnotation.actionBarUpBehavior();
      switch (actionBarUpBehavior)
      {
      case ShowAsUp:
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        break;

      case ShowAsDrawer:
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(false);
        break;

      default:
      case None:
        actionBar.setHomeButtonEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(false);
        break;
      }
    }
  }

}

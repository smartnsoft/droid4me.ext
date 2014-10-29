package com.smartnsoft.droid4me.ext.app;

import android.app.ActionBar;
import android.app.Activity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import com.smartnsoft.droid4me.app.SmartApplication;
import com.smartnsoft.droid4me.app.Smartable;
import com.smartnsoft.droid4me.ext.app.ActivityAnnotations.ActionBarBehavior;
import com.smartnsoft.droid4me.ext.app.ActivityAnnotations.ActionBarTitleBehavior;
import com.smartnsoft.droid4me.ext.app.ActivityAnnotations.ActivityAnnotation;
import com.smartnsoft.droid4me.log.Logger;
import com.smartnsoft.droid4me.log.LoggerFactory;

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

  protected Fragment fragment;

  public ActivityAggregate(Activity activity, Smartable<?> smartable, ActivityAnnotation activityAnnotation)
  {
    this.activity = activity;
    this.smartable = smartable;
    this.activityAnnotation = activityAnnotation;
  }

  public SmartApplicationClass getApplication()
  {
    return (SmartApplicationClass) activity.getApplication();
  }

  /**
   * Open the specified fragment, the previous fragment is add to the back stack.
   * 
   * @param fragmentClass
   */
  public final void openFragment(Class<? extends Fragment> fragmentClass)
  {
    try
    {
      final FragmentTransaction fragmentTransaction = ((FragmentActivity) activity).getSupportFragmentManager().beginTransaction();
      fragment = fragmentClass.newInstance();
      fragmentTransaction.replace(activityAnnotation.fragmentContainerIdentifier(), fragment);
      fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
      fragmentTransaction.commit();
    }
    catch (Exception exception)
    {
      if (log.isErrorEnabled())
      {
        log.error("Unable to instanciate the fragment '" + fragmentClass.getSimpleName() + "'", exception);
      }
    }
  }

  protected void onCreate()
  {
    activity.setContentView(activityAnnotation.contentViewIdentifier());
    setActionBarBehavior();
    openParameterFragment();
  }

  private void openParameterFragment()
  {
    if (activityAnnotation != null && activityAnnotation.fragmentClass() != null)
    {
      openFragment(activityAnnotation.fragmentClass());
    }
  }

  private void setActionBarBehavior()
  {
    try
    {
      getActionBar().setDisplayShowTitleEnabled(false);
      getActionBar().setDisplayUseLogoEnabled(false);
      getActionBar().setDisplayHomeAsUpEnabled(true);
      getActionBar().setDisplayShowHomeEnabled(true);
      getActionBar().setHomeButtonEnabled(true);
      final ActionBarTitleBehavior actionBarTitleBehavior = activityAnnotation.actionBarTitleBehavior();
      if (actionBarTitleBehavior == ActionBarTitleBehavior.UseLogo)
      {
        getActionBar().setDisplayShowTitleEnabled(false);
        getActionBar().setDisplayUseLogoEnabled(true);
      }
      else if (actionBarTitleBehavior == ActionBarTitleBehavior.UseTitle)
      {
        getActionBar().setDisplayShowTitleEnabled(true);
      }
      if (ActionBarBehavior.ShowAsUp == activityAnnotation.actionBarUpBehavior())
      {
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setDisplayShowHomeEnabled(true);
        getActionBar().setIcon(android.R.color.transparent);
      }
    }
    catch (NullPointerException exception)
    {
      // Unable to manupilate the SupportActionBar with NoTitle theme.
      // http://stackoverflow.com/questions/20147921/actionbaractivity-getsupportactionbar-hide-throws-nullpointerexception
      // http://grepcode.com/file/repository.grepcode.com/java/ext/com.google.android/android/4.3_r2.1/android/support/v7/app/ActionBarImplICS.java#302
    }
  }

  private ActionBar getActionBar()
  {
    return activity.getActionBar();
  }

}

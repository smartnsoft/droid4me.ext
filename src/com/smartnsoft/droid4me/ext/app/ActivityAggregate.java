package com.smartnsoft.droid4me.ext.app;

import android.app.ActionBar;
import android.app.Activity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;

import com.smartnsoft.droid4me.app.SmartApplication;
import com.smartnsoft.droid4me.app.Smartable;
import com.smartnsoft.droid4me.ext.app.ActivityContainerParameter.ActivityParameters;
import com.smartnsoft.droid4me.ext.app.ActivityContainerParameter.LogoIconTitleBehavior;
import com.smartnsoft.droid4me.ext.app.ActivityContainerParameter.MenuBehavior;
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

  private final ActivityParameters activityParameters;

  protected Fragment fragment;

  public ActivityAggregate(Activity activity, Smartable<?> smartable, ActivityParameters activityParameters)
  {
    this.activity = activity;
    this.smartable = smartable;
    this.activityParameters = activityParameters;
  }

  public SmartApplicationClass getApplication()
  {
    return (SmartApplicationClass) getApplication();
  }

  // TODO: think twice before exposing that method!
  public final Fragment getFragment()
  {
    return fragment;
  }

  // TODO: think twice before exposing that method!
  public final ActivityParameters getActivityParameters()
  {
    return activityParameters;
  }

  public void openParameterFragment()
  {
    if (activityParameters != null && activityParameters.fragmentClass() != null)
    {
      openFragment(activityParameters.fragmentClass());
    }
  }

  /**
   * Allows to specify the resource identifier for its container.
   * 
   * @return fragmentContainerIdentifier
   */
  // TODO: turn this into an annotation
  protected abstract int getFragmentContainerIdentifier();

  /**
   * Open the specified fragment, the previous fragment is add to the back stack.
   * 
   * @param fragmentClass
   */
  protected final void openFragment(Class<? extends Fragment> fragmentClass)
  {
    try
    {
      final FragmentTransaction fragmentTransaction = ((FragmentActivity) activity).getSupportFragmentManager().beginTransaction();
      fragment = fragmentClass.newInstance();
      fragmentTransaction.replace(getFragmentContainerIdentifier(), fragment);
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

  /**
   * Set the behavior of the action bar
   */
  protected void setActionBarBehavior()
  {
    try
    {
      getActionBar().setDisplayShowTitleEnabled(false);
      getActionBar().setDisplayUseLogoEnabled(false);
      getActionBar().setDisplayHomeAsUpEnabled(true);
      getActionBar().setDisplayShowHomeEnabled(true);
      getActionBar().setHomeButtonEnabled(true);
      final LogoIconTitleBehavior logoIconTitleBehavior = activityParameters.logoIconTitleBehavior();
      if (logoIconTitleBehavior == LogoIconTitleBehavior.UseLogo)
      {
        getActionBar().setDisplayShowTitleEnabled(false);
        getActionBar().setDisplayUseLogoEnabled(true);
      }
      else if (logoIconTitleBehavior == LogoIconTitleBehavior.UseTitle)
      {
        getActionBar().setDisplayShowTitleEnabled(true);
      }
      if (MenuBehavior.ShowAsUp == activityParameters.menuBehavior())
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

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
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment.SavedState;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

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
public abstract class ActivityAggregate<SmartApplicationClass extends Application>
    implements FragmentManager.OnBackStackChangedListener
{

  public enum FragmentTransactionType
  {
    Add, Replace
  }

  protected final static Logger log = LoggerFactory.getInstance(ActivityAggregate.class);

  protected final Activity activity;

  protected final Smartable<?> smartable;

  protected SmartFragment<?> fragment;

  private final ActivityAnnotation activityAnnotation;

  private SmartFragment<?> lastBackstackFragment;

  private int lastBackstackCount;

  public ActivityAggregate(Activity activity, Smartable<?> smartable, ActivityAnnotation activityAnnotation)
  {
    this.activity = activity;
    this.smartable = smartable;
    this.activityAnnotation = activityAnnotation;

    if (this.activity instanceof FragmentActivity)
    {
      final FragmentActivity fragmentActivity = (FragmentActivity) this.activity;
      fragmentActivity.getSupportFragmentManager().addOnBackStackChangedListener(this);
    }
  }

  @Override
  public void onBackStackChanged()
  {
    if (this.activity instanceof FragmentActivity)
    {
      final FragmentManager fragmentManager = ((FragmentActivity) this.activity).getSupportFragmentManager();
      final int newCount = fragmentManager.getBackStackEntryCount();

      // Fragment just restored from backstack
      if (newCount < lastBackstackCount)
      {
        fragment = lastBackstackFragment;
      }

      // Save the new backstack count
      lastBackstackCount = newCount;
      // Save the new (last) backstack fragment
      if (newCount > 1)
      {
        final String tag = fragmentManager.getBackStackEntryAt(newCount - 2).getName();
        if (tag != null)
        {
          lastBackstackFragment = (SmartFragment) fragmentManager.findFragmentByTag(tag);
        }
      }
    }
  }

  @SuppressWarnings("unchecked")
  public SmartApplicationClass getApplication()
  {
    return (SmartApplicationClass) activity.getApplication();
  }

  /**
   * Replaces the current fragment by the specified fragment one.
   * Reads the activity annotation in order to add it or not to the backstack.
   * The fragment is opened with the extras of the activity as its arguments.
   *
   * @param fragmentClass the fragment to open
   */
  public void replaceFragment(Class<? extends SmartFragment<?>> fragmentClass)
  {
    addOrReplaceFragment(fragmentClass, activityAnnotation.fragmentContainerIdentifier(),
        activityAnnotation.addFragmentToBackStack(), activityAnnotation.fragmentBackStackName(), null,
        activity.getIntent().getExtras(), FragmentTransactionType.Replace);
  }

  /**
   * Replaces the current fragment by the specified fragment one.
   * The fragment is opened with the extras of the activity as its arguments.
   *
   * @param fragmentClass              the fragment to open
   * @param fragmentContainerIdentifer the identifier of the container whose fragment is to be replaced.
   * @param addFragmentToBackStack     indicates wether the fragment should be added to the backstack
   * @param fragmentBackStackName      the name of the fragment into the backstack if it should added
   */
  public final void replaceFragment(Class<? extends SmartFragment<?>> fragmentClass,
      @IdRes int fragmentContainerIdentifer,
      boolean addFragmentToBackStack, @Nullable String fragmentBackStackName)
  {
    addOrReplaceFragment(fragmentClass, fragmentContainerIdentifer, addFragmentToBackStack, fragmentBackStackName, null,
        activity.getIntent().getExtras(), FragmentTransactionType.Replace);
  }

  /**
   * Replaces the current fragment by the specified fragment one.
   * Reads the activity annotation in order to add it or not to the backstack.
   *
   * @param fragmentClass the fragment to open
   * @param savedState    the initial saved state of the fragment
   * @param arguments     the arguments of the fragment
   */
  public final void replaceFragment(Class<? extends SmartFragment<?>> fragmentClass, SavedState savedState,
      Bundle arguments)
  {
    addOrReplaceFragment(fragmentClass, activityAnnotation.fragmentContainerIdentifier(),
        activityAnnotation.addFragmentToBackStack(), activityAnnotation.fragmentBackStackName(), savedState,
        arguments, FragmentTransactionType.Replace);
  }

  /**
   * Adds or replaces the current fragment by the specified fragment one.
   *
   * @param fragmentClass              the fragment to open
   * @param fragmentContainerIdentifer the identifier of the container whose fragment is to be replaced.
   * @param addFragmentToBackStack     indicates wether the fragment should be added to the backstack
   * @param fragmentBackStackName      the name of the fragment into the backstack if it should added
   * @param savedState                 the initial saved state of the fragment
   * @param arguments                  the arguments of the fragment
   */
  public final void addOrReplaceFragment(Class<? extends SmartFragment<?>> fragmentClass,
      @IdRes int fragmentContainerIdentifer,
      boolean addFragmentToBackStack, @Nullable String fragmentBackStackName, SavedState savedState, Bundle arguments,
      FragmentTransactionType fragmentTransactionType)
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

      if (fragmentTransactionType == FragmentTransactionType.Replace)
      {
        fragmentTransaction.replace(fragmentContainerIdentifer, fragment,
            addFragmentToBackStack == true ? fragmentBackStackName : null);
      }
      else
      {
        fragmentTransaction.add(fragmentContainerIdentifer, fragment,
            addFragmentToBackStack == true ? fragmentBackStackName : null);
      }

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
        fragment = (SmartFragment<?>) fragmentActivity.getSupportFragmentManager().findFragmentById(
            activityAnnotation.fragmentContainerIdentifier());
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
        replaceFragment(activityAnnotation.fragmentClass());
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

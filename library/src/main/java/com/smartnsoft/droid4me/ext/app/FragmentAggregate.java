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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.support.annotation.IdRes;
import android.support.v4.app.Fragment.SavedState;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;

import com.smartnsoft.droid4me.app.SmartApplication;
import com.smartnsoft.droid4me.ext.app.ActivityAnnotations.FragmentAnnotation;
import com.smartnsoft.droid4me.log.Logger;
import com.smartnsoft.droid4me.log.LoggerFactory;
import com.smartnsoft.droid4me.support.v4.app.SmartFragment;

/**
 * @author Jocelyn Girard, Willy Noel
 * @since 2014.10.14
 */
public abstract class FragmentAggregate<SmartApplicationClass extends SmartApplication, SmartActivityClass extends Activity>
{

  public interface OnBackPressedListener
  {

    boolean onBackPressed();

  }

  protected final static Logger log = LoggerFactory.getInstance(FragmentAggregate.class);

  protected final Fragment fragment;

  protected final android.support.v4.app.Fragment supportFragment;

  private final FragmentAnnotation fragmentAnnotation;

  public FragmentAggregate(Object fragment, FragmentAnnotation fragmentAnnotation)
  {
    this.fragmentAnnotation = fragmentAnnotation;
    if (fragment instanceof android.support.v4.app.Fragment == false)
    {
      this.fragment = (android.app.Fragment) fragment;
      this.supportFragment = null;
    }
    else
    {
      this.fragment = null;
      this.supportFragment = (android.support.v4.app.Fragment) fragment;
    }
  }

  @SuppressWarnings("unchecked")
  @SuppressLint("NewApi")
  public SmartApplicationClass getApplication()
  {
    if (fragment != null)
    {
      return (SmartApplicationClass) fragment.getActivity().getApplication();
    }
    else
    {
      return (SmartApplicationClass) supportFragment.getActivity().getApplication();
    }
  }

  @SuppressWarnings("unchecked")
  public SmartActivityClass getActivity()
  {
    if (fragment != null)
    {
      return (SmartActivityClass) fragment.getActivity();
    }
    else
    {
      return (SmartActivityClass) supportFragment.getActivity();
    }
  }

  /**
   * Open the specified fragment, the previous fragment is add to the back stack.
   */
  public final void openChildFragment(SmartFragment<?> parentFragment, @IdRes int fragmentPlaceholderIdentifier,
      Class<? extends SmartFragment<?>> fragmentClass, SavedState savedState)
  {
    try
    {
      final FragmentTransaction fragmentTransaction = parentFragment.getChildFragmentManager().beginTransaction();
      final android.support.v4.app.Fragment childfragment = fragmentClass.newInstance();
      childfragment.setArguments(parentFragment.getArguments());

      // We (re)set its initial state if necessary
      if (savedState != null)
      {
        childfragment.setInitialSavedState(savedState);
      }

      fragmentTransaction.replace(fragmentPlaceholderIdentifier, childfragment);
      fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
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

  public FragmentAnnotation getFragmentAnnotation()
  {
    return fragmentAnnotation;
  }

  protected abstract Object getActionBar(Activity activity);

  protected void onCreateDone(Activity activity)
  {
    final Object actionBarObject = getActionBar(activity);
    final int titleIdentifier = fragmentAnnotation.fragmentTitleIdentifier();
    final int subTitleIdentifier = fragmentAnnotation.fragmentSubTitleIdentifier();
    if (actionBarObject instanceof ActionBar)
    {
      final ActionBar actionBar = (ActionBar) actionBarObject;
      if (titleIdentifier > 0)
      {
        actionBar.setTitle(titleIdentifier);
      }
      if (subTitleIdentifier > 0)
      {
        actionBar.setSubtitle(subTitleIdentifier);
      }
    }
    else if (actionBarObject instanceof android.app.ActionBar)
    {
      final android.app.ActionBar actionBar = (android.app.ActionBar) actionBarObject;
      if (titleIdentifier > 0)
      {
        actionBar.setTitle(titleIdentifier);
      }
      if (subTitleIdentifier > 0)
      {
        actionBar.setSubtitle(subTitleIdentifier);
      }
    }
  }

}

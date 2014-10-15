package com.smartnsoft.droid4me.ext.app;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;

import com.smartnsoft.droid4me.app.SmartApplication;

/**
 * @author Jocelyn Girard, Willy Noel
 * @since 2014.10.14
 */
public abstract class FragmentAggregate<SmartApplicationClass extends SmartApplication, SmartActivityClass extends Activity>
{

  protected final Fragment fragment;

  protected final android.support.v4.app.Fragment supportFragment;

  public FragmentAggregate(Object fragment)
  {
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

  public void setActionBarTitle(int resourceTitle)
  {
    getActivity().getActionBar().setTitle(resourceTitle);
  }

  public void setActionBarSubTitle(int resourceTitle)
  {
    getActivity().getActionBar().setSubtitle(resourceTitle);
  }

}

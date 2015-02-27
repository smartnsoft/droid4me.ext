package com.smartnsoft.droid4me.ext.app;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import com.smartnsoft.droid4me.app.SmartApplication;
import com.smartnsoft.droid4me.ext.app.ActivityAnnotations.FragmentAnnotation;
import com.smartnsoft.droid4me.log.Logger;
import com.smartnsoft.droid4me.log.LoggerFactory;

/**
 * @author Jocelyn Girard, Willy Noel
 * @since 2014.10.14
 */
public abstract class FragmentAggregate<SmartApplicationClass extends SmartApplication, SmartActivityClass extends Activity>
{

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

  protected void onCreateDone()
  {
    // We set the "title"
    final int resourceTitle = fragmentAnnotation.fragmentTitleIdentifier();
    if (resourceTitle != -1)
    {
      getActivity().getActionBar().setTitle(resourceTitle);
    }
    final int resourceSubTitle = fragmentAnnotation.fragmentSubTitleIdentifier();
    if (resourceSubTitle != -1)
    {
      getActivity().getActionBar().setSubtitle(resourceTitle);
    }
  }
}

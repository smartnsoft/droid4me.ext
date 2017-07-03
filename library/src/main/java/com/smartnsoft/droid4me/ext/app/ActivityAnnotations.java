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

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.text.Annotation;

import android.app.ActionBar;
import android.app.Activity;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.StringRes;
import android.support.v7.widget.Toolbar;

import com.smartnsoft.droid4me.support.v4.app.SmartFragment;

/**
 * This class contains all {@link Annotation} and {@link Enum} defined to be used by {@link ActivityAggregate} and {@link FragmentAggregate} and
 * handled by the {@link ActivityInterceptor}.
 *
 * @author Jocelyn Girard, Willy Noel
 * @since 2014.04.08
 */
public final class ActivityAnnotations
{

  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.TYPE)
  @Inherited
  public @interface ActivityAnnotation
  {

    /**
     * @return the layout identifier to be used in the {@link Activity#setContentView(int)} method.
     */
    @LayoutRes int contentViewIdentifier();

    /**
     * @return the 'view holder' identifier to be used to place the {@link android.app.Fragment} defined by the
     * {@link ActivityAnnotation#fragmentClass()}.
     */
    @IdRes int fragmentContainerIdentifier() default -1;

    /**
     * @return the fragment class to be instanciate and displayed in the {@link ActivityAnnotation#fragmentContainerIdentifier()} view holder.
     */
    Class<? extends SmartFragment<?>> fragmentClass() default AbsSmartFragment.class;

    /**
     * @return if the fragment referred into the {@link ActivityAnnotation#fragmentClass()} should be added to the backStack or not.
     */
    boolean addFragmentToBackStack() default false;

    /**
     * @return the name of the fragment if it is add to the backStack.
     */
    String fragmentBackStackName() default "";

    /**
     * @return the {@link ActionBar} "home" button action behavior
     */
    ActionBarBehavior actionBarUpBehavior() default ActionBarBehavior.None;

    /**
     * @return the {@link ActionBar} title behaviors
     */
    ActionBarTitleBehavior actionBarTitleBehavior() default ActionBarTitleBehavior.UseLogo;

    /**
     * @return the {@link Toolbar} layout identifier to be used as 'ActionBar'
     */
    @IdRes int toolbarIdentifier() default 0;

    /**
     * @return true if the activity can rotate.
     */
    boolean canRotate() default false;

  }

  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.TYPE)
  @Inherited
  public @interface FragmentAnnotation
  {

    /**
     * @return the string identifier to be setted on {@link ActionBar#setTitle(int)}.
     */
    @StringRes int fragmentTitleIdentifier() default -1;

    /**
     * @return the string identifier to be setted on {@link ActionBar#setSubtitle(int)}.
     */
    @StringRes int fragmentSubTitleIdentifier() default -1;

    /**
     * @return the layout identifier to be used in the
     * {@link android.app.Fragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)} method.
     */
    @LayoutRes int layoutIdentifier();

    /**
     * @return Whether the {@link ActionBar} "home" button is used as back behavior
     */
    boolean homeAsBack() default false;

    /**
     * @return true if the fragment should survive when the configuration changes
     */
    boolean surviveOnConfigurationChanged() default false;

  }

  /**
   * Defines the available {@link ActionBar} "home" button action behaviors handled by the {@link ActivityAggregate}.
   */
  public enum ActionBarBehavior
  {
    None, ShowAsUp, ShowAsDrawer
  }

  /**
   * Defines the available {@link ActionBar} title behaviors handled by the {@link ActivityAggregate}.
   */
  public enum ActionBarTitleBehavior
  {
    UseLogo, UseIcon, UseTitle
  }

  private ActivityAnnotations()
  {
    super();
  }

}

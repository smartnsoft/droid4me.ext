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

import com.smartnsoft.droid4me.support.v4.app.SmartFragment;

/**
 * 
 * This class contains all {@link Annotation} and {@link Enum} defined to be used by {@link ActivityAggregate} and {@link FragmentAggregate} and
 * handled by the {@link ActivityInterceptor}.
 * 
 * @author Jocelyn Girard, Willy Noel
 * @since 2014.04.08
 */
public final class ActivityAnnotations
{

  /**
   * Defines the available {@link ActionBar} "home" button action behaviors handled by the {@link ActivityAggregate}.
   */
  public static enum ActionBarBehavior
  {
    None, ShowAsUp, ShowAsDrawer
  }

  /**
   * Defines the available {@link ActionBar} title behaviors handled by the {@link ActivityAggregate}.
   */
  public static enum ActionBarTitleBehavior
  {
    UseLogo, UseIcon, UseTitle
  }

  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.TYPE)
  @Inherited
  public static @interface ActivityAnnotation
  {

    /**
     * @return the layout identifier to be used in the {@link Activity#setContentView(int)} method.
     */
    @LayoutRes
    int contentViewIdentifier();

    /**
     * @return the 'view holder' identifier to be used to place the {@link android.app.Fragment} defined by the
     *         {@link ActivityAnnotation#fragmentClass()}.
     */
    @IdRes
    int fragmentContainerIdentifier();

    /**
     * @return the fragment class to be instanciate and displayed in the {@link ActivityAnnotation#fragmentContainerIdentifier()} view holder.
     */
    Class<? extends SmartFragment<?>> fragmentClass();

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
    @IdRes
    int toolbarIdentifier() default 0;

    /**
     * @return true if the activity can rotate.
     */
    boolean canRotate() default false;

  }

  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.TYPE)
  @Inherited
  public static @interface FragmentAnnotation
  {

    /**
     * @return the string identifier to be setted on {@link ActionBar#setTitle(int)}.
     */
    @StringRes
    int fragmentTitleIdentifier() default -1;

    /**
     * @return the string identifier to be setted on {@link ActionBar#setSubtitle(int)}.
     */
    @StringRes
    int fragmentSubTitleIdentifier() default -1;

    /**
     * @return the layout identifier to be used in the
     *         {@link android.app.Fragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)} method.
     */
    @LayoutRes
    int layoutIdentifier();

    /**
     * @return Whether the {@link ActionBar} "home" button is used as back behavior
     */
    boolean homeAsBack() default false;

    /**
     * @return true if the fragment should survive when the configuration changes
     */
    boolean surviveOnConfigurationChanged() default false;

  }

  private ActivityAnnotations()
  {
    super();
  }

}

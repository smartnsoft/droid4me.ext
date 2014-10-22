package com.smartnsoft.droid4me.ext.app;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import android.support.v4.app.Fragment;

/**
 * @author Jocelyn Girard, Willy Noel
 * @since 2014.04.08
 */
// TODO: find another name
public final class ActivityContainerParameter
{

  public static enum MenuBehavior
  {
    None, ShowAsUp, ShowAsDrawer
  }

  public static enum LogoIconTitleBehavior
  {
    UseLogo, UseIcon, UseTitle
  }

  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.TYPE)
  // TODO: why not using the "Annotation" word?
  public static @interface ActivityParameters
  {

    Class<? extends Fragment> fragmentClass();

    int menuTouchMode() default 0;

    MenuBehavior menuBehavior() default MenuBehavior.None;

    LogoIconTitleBehavior logoIconTitleBehavior() default LogoIconTitleBehavior.UseLogo;

  }

  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.TYPE)
  // TODO: why not using the "Annotation" word?
  public static @interface FragmentParameters
  {

    int fragmentTitle() default -1;

    int fragmentSubTitle() default -1;

    MenuBehavior menuBehavior() default MenuBehavior.None;

    int layoutId() default -1;

    boolean homeAsBack() default false;

  }

  private ActivityContainerParameter()
  {
    super();
  }

}

package com.smartnsoft.droid4me.ext.app;

import android.support.v4.app.Fragment;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Jocelyn Girard, Willy Noel
 * @since 2014.04.08
 */

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
  public static @interface ActivityParameters
  {

    Class<? extends Fragment> fragmentClass();

    int menuTouchMode() default 0;

    MenuBehavior menuBehavior() default MenuBehavior.None;

    LogoIconTitleBehavior logoIconTitleBehavior() default LogoIconTitleBehavior.UseLogo;

  }

  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.TYPE)
  public static @interface FragmentParameters
  {

    int fragmentTitle() default -1;

    int fragmentSubTitle() default -1;

    MenuBehavior menuBehavior() default MenuBehavior.None;

    int layoutId() default -1;

    boolean homeAsBack() default false;
  }

}

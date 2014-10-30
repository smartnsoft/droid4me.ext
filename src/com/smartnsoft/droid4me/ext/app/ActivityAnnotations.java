package com.smartnsoft.droid4me.ext.app;

import android.support.v4.app.Fragment;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Jocelyn Girard, Willy Noel
 * @since 2014.04.08
 */
public final class ActivityAnnotations
{

  public static enum ActionBarBehavior
  {
    None, ShowAsUp, ShowAsDrawer
  }

  public static enum ActionBarTitleBehavior
  {
    UseLogo, UseIcon, UseTitle
  }

  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.TYPE)
  @Inherited
  public static @interface ActivityAnnotation
  {

    int contentViewIdentifier();

    int fragmentContainerIdentifier();

    Class<? extends Fragment> fragmentClass();

    ActionBarBehavior actionBarUpBehavior() default ActionBarBehavior.None;

    ActionBarTitleBehavior actionBarTitleBehavior() default ActionBarTitleBehavior.UseLogo;

  }

  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.TYPE)
  @Inherited
  public static @interface FragmentAnnotation
  {

    int fragmentTitle() default -1;

    int fragmentSubTitle() default -1;

    int contentViewIdentifier();

    boolean homeAsBack() default false;

  }

  private ActivityAnnotations()
  {
    super();
  }

}

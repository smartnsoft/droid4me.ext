/*
 * (C) Copyright 2009-2013 Smart&Soft SAS (http://www.smartnsoft.com/) and contributors.
 *
 * The code hereby is the full property of Smart&Soft, SIREN 444 622 690.
 * 34, boulevard des Italiens - 75009 - Paris - France
 * contact@smartnsoft.com - 00 33 6 79 60 05 49
 *
 * You are not allowed to use the source code or the resulting binary code, nor to modify the source code, without prior permission of the owner.
 * 
 * This library is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * Contributors:
 *     Smart&Soft - initial API and implementation
 */

package com.smartnsoft.droid4me.ext.view;

import java.lang.reflect.Field;

import org.xmlpull.v1.XmlPullParser;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Build.VERSION;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.smartnsoft.droid4me.log.Logger;
import com.smartnsoft.droid4me.log.LoggerFactory;

/**
 * A work-around for enabling to intercept all inflated {@link View views}. Very similar to the private
 * {@link com.android.internal.policy.impl.PhoneLayoutInflater} class.
 * 
 * @author Édouard Mercier
 * @since 2013.06.27
 */
public class SmartLayoutInflater
    extends LayoutInflater
    implements LayoutInflater.Factory
{

  public static boolean DEBUG_LOG_ENABLED = false;

  protected final static Logger log = LoggerFactory.getInstance(SmartLayoutInflater.class);

  public static final ThreadLocal<Factory> factoryThreadLocal = new ThreadLocal<Factory>();

  public static final ThreadLocal<Factory2> factory2ThreadLocal = new ThreadLocal<Factory2>();

  private static final boolean STRIPPER_ENABLED = "".equals("");

  public static interface OnViewInflatedListener
  {

    void onViewInflated(Context context, View view, AttributeSet attrs);

  }

  private static final String[] CLASS_PREFIXES = { "android.widget.", "android.webkit." };

  public static LayoutInflater getLayoutInflater(LayoutInflater defaultLayoutInflater, Activity activity, OnViewInflatedListener onViewInflatedListener)
  {
    if (defaultLayoutInflater instanceof SmartLayoutInflater)
    {
      if (log.isDebugEnabled())
      {
        log.debug("Reusing the default layout inflater");
      }
      return defaultLayoutInflater;
    }
    if (log.isDebugEnabled())
    {
      log.debug("Creating a layout inflater");
    }
    return new SmartLayoutInflater(defaultLayoutInflater, activity, onViewInflatedListener);
  }

  @SuppressLint("NewApi")
  private static LayoutInflater stripLayoutFactories(LayoutInflater original)
  {
    if (SmartLayoutInflater.STRIPPER_ENABLED == false)
    {
      return original;
    }
    if (original.getFactory() != null)
    {
      factoryThreadLocal.set(original.getFactory());
      try
      {
        final Field factoryField = LayoutInflater.class.getDeclaredField("mFactory");
        factoryField.setAccessible(true);
        factoryField.set(original, null);
      }
      catch (Exception exception)
      {
        // Should not happen
      }
    }
    if (VERSION.SDK_INT >= 11 && original.getFactory2() != null)
    {
      factory2ThreadLocal.set(original.getFactory2());
      try
      {
        final Field factoryField = LayoutInflater.class.getDeclaredField("mFactory2");
        factoryField.setAccessible(true);
        factoryField.set(original, null);
      }
      catch (Exception exception)
      {
        // Should not happen
      }
    }
    return original;
  }

  private static void unstripLayoutFactories(LayoutInflater original)
  {
    if (SmartLayoutInflater.STRIPPER_ENABLED == false)
    {
      return;
    }
    {
      if (factoryThreadLocal.get() != null)
      {
        try
        {
          final Field factoryField = LayoutInflater.class.getDeclaredField("mFactory");
          factoryField.setAccessible(true);
          factoryField.set(original, factoryThreadLocal.get());
        }
        catch (Exception exception)
        {
          // Should not happen
        }
        factoryThreadLocal.remove();
      }
    }
    if (VERSION.SDK_INT >= 11)
    {
      if (factory2ThreadLocal.get() != null)
      {
        try
        {
          final Field factoryField = LayoutInflater.class.getDeclaredField("mFactory2");
          factoryField.setAccessible(true);
          factoryField.set(original, factory2ThreadLocal.get());
        }
        catch (Exception exception)
        {
          // Should not happen
        }
        factory2ThreadLocal.remove();
      }
    }
  }

  private final OnViewInflatedListener onViewInflatedListener;

  private Factory inheritedFactory;

  public SmartLayoutInflater(LayoutInflater original, Context newContext, OnViewInflatedListener onViewInflatedListener)
  {
    super(SmartLayoutInflater.stripLayoutFactories(original), newContext);
    SmartLayoutInflater.unstripLayoutFactories(original);
    this.onViewInflatedListener = onViewInflatedListener;
  }

  @Override
  public LayoutInflater cloneInContext(Context newContext)
  {
    return new SmartLayoutInflater(this, newContext, this.onViewInflatedListener);
  }

  @Override
  public void setFactory(Factory factory)
  {
    if (factory == this)
    {
      super.setFactory(factory);
    }
    else
    {
      inheritedFactory = factory;
    }
  }

  @Override
  public View inflate(XmlPullParser parser, ViewGroup root, boolean attachToRoot)
  {
    // The inner LayoutInflater Factory is set lazily, because we want the FragmentActivity set it beforehand!
    if (getFactory() == null)
    {
      // If the factory is already set, an IllegalStateException exception will be thrown, this is why we check this
      setFactory(this);
    }
    final long start = System.currentTimeMillis();
    final View view = super.inflate(parser, root, attachToRoot);
    if (SmartLayoutInflater.DEBUG_LOG_ENABLED)
    {
      final long durationInMilliseconds = System.currentTimeMillis() - start;
      log.debug("Inflated a view in " + durationInMilliseconds + " ms.");
      if (durationInMilliseconds >= 100)
      {
        log.warn("Expensive view inflation!");
      }
    }
    return view;
  }

  @Override
  public View onCreateView(String name, Context context, AttributeSet attrs)
  {
    if (name.indexOf('.') != -1)
    {
      try
      {
        final View view = createView(name, null, attrs);
        if (view != null)
        {
          onCustomizeView(view, attrs);
        }
        return view;
      }
      catch (Exception exception)
      {
        // Does not matter, we let the LayoutInflater.onCreateView() and LayoutInflater.createView() handle that from the
        // LayoutInflater.createViewFromTag() method
      }
    }
    else if (inheritedFactory != null)
    {
      return inheritedFactory.onCreateView(name, context, attrs);
    }
    return null;
  }

  @Override
  protected View onCreateView(String name, AttributeSet attrs)
      throws ClassNotFoundException
  {
    for (String prefix : SmartLayoutInflater.CLASS_PREFIXES)
    {
      try
      {
        final View view = createView(name, prefix, attrs);
        if (view != null)
        {
          onCustomizeView(view, attrs);
          return view;
        }
      }
      catch (ClassNotFoundException exception)
      {
        // In this case we want to let the base class take a crack at it
      }
    }
    {
      final View view = super.onCreateView(name, attrs);
      if (view != null)
      {
        onCustomizeView(view, attrs);
        return view;
      }
    }
    return null;
  }

  protected void onCustomizeView(View view, AttributeSet attrs)
  {
    if (onViewInflatedListener != null)
    {
      onViewInflatedListener.onViewInflated(getContext(), view, attrs);
    }
  }

}
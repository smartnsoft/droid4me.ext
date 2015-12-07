/*
 * (C) Copyright 2009-2014 Smart&Soft SAS (http://www.smartnsoft.com/) and contributors.
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

package com.smartnsoft.droid4me.ext.app;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;

import android.content.Context;

import com.smartnsoft.droid4me.app.SmartApplication;

import dalvik.system.DexClassLoader;

/**
 * An application class which enables to help loading dynamically DEX code, and resources from .apk files.
 *
 * @author Édouard Mercier
 * @since 2013.08.06
 */
public abstract class ExtendedApplication
    extends SmartApplication
{

  private final static class FieldWrapper<T>
  {

    private final Object object;

    private final Field field;

    public FieldWrapper(Object object, String fieldName)
        throws NoSuchFieldException
    {
      if (object == null)
      {
        throw new IllegalArgumentException("'object' cannot be null!");
      }
      this.object = object;

      Class<?> aClass = object.getClass();
      Field aField = null;
      while (aClass != null)
      {
        try
        {
          aField = aClass.getDeclaredField(fieldName);
          aField.setAccessible(true);
          break;
        }
        catch (Exception exception)
        {
          // This happens if the field is not declared on the "aClass" level
        }
        finally
        {
          aClass = aClass.getSuperclass();
        }
      }
      field = aField;
      if (field == null)
      {
        throw new NoSuchFieldException();
      }
    }

    public T get()
        throws IllegalAccessException, IllegalArgumentException
    {
      @SuppressWarnings("unchecked") final T fieldObject = (T) field.get(object);
      return fieldObject;
    }

    public void set(T fieldValue)
        throws IllegalAccessException, IllegalArgumentException
    {
      field.set(object, fieldValue);
    }

  }

  /**
   * Largely inspired from https://github.com/Rookery/AndroidDynamicLoader.
   */
  public final static class WrappedClassLoader
      extends ClassLoader
  {

    private ClassLoader customClassLoader;

    // private Method customClassLoaderFindClassMethod;

    public WrappedClassLoader(ClassLoader parent)
    {
      super(parent);
    }

    // @Override
    // protected Class<?> findClass(String className)
    // throws ClassNotFoundException
    // {
    // if (log.isDebugEnabled())
    // {
    // log.debug("Finding class '" + className + "'");
    // }
    // if (customClassLoader != null)
    // {
    // try
    // {
    // if (customClassLoaderFindClassMethod == null)
    // {
    // customClassLoaderFindClassMethod = ClassLoader.class.getDeclaredMethod("findClass", String.class);
    // customClassLoaderFindClassMethod.setAccessible(true);
    // }
    // final Class<?> theClass = (Class<?>) customClassLoaderFindClassMethod.invoke(customClassLoader, className);
    // if (log.isDebugEnabled())
    // {
    // log.debug("Found the custom class '" + className + "'");
    // }
    // return theClass;
    // }
    // catch (Exception exception)
    // {
    // // If the custom class loader cannot load the class, we delegate the responsibility to the parent's
    // }
    // }
    // return super.findClass(className);
    // }

    @Override
    public Class<?> loadClass(String className)
        throws ClassNotFoundException
    {
      if (customClassLoader != null)
      {
        try
        {
          final Class<?> theClass = customClassLoader.loadClass(className);
          if (log.isDebugEnabled())
          {
            log.debug("Loaded the custom class '" + className + "'");
          }
          return theClass;
        }
        catch (ClassNotFoundException exception)
        {
          // If the custom class loader cannot load the class, we delegate the responsibility to the parent's
        }
      }
      final Class<?> theClass = super.loadClass(className);
      if (log.isDebugEnabled())
      {
        log.debug("Loaded the regular class '" + className + "'");
      }
      return theClass;
    }

    // @Override
    // protected synchronized Class<?> loadClass(String className, boolean resolve)
    // throws ClassNotFoundException
    // {
    // if (customClassLoader != null)
    // {
    // try
    // {
    // final Class<?> theClass = customClassLoader.loadClass(className);
    // if (log.isDebugEnabled())
    // {
    // log.debug("Loaded the custom class '" + className + "'");
    // }
    // return theClass;
    // }
    // catch (ClassNotFoundException exception)
    // {
    // // If the custom class loader cannot load the class, we delegate the responsibility to the parent's
    // }
    // }
    // final Class<?> theClass = super.loadClass(className, resolve);
    // if (log.isDebugEnabled())
    // {
    // log.debug("Loaded the regular class '" + className + "'");
    // }
    // return theClass;
    // }

    public void setCustomClassLoader(ClassLoader customClassLoader)
    {
      this.customClassLoader = customClassLoader;
      // customClassLoaderFindClassMethod = null;
    }

    public File copy(Context context, String assetsFilePath, String targetFileName)
        throws IOException
    {
      final File directory = ensureDirectory(context);
      final File file = new File(directory, targetFileName);
      InputStream inputStream = null;
      FileOutputStream outputStream = null;
      try
      {
        inputStream = context.getAssets().open(assetsFilePath);
        outputStream = new FileOutputStream(file);
        byte[] buffer = new byte[8192];
        int length;
        while ((length = inputStream.read(buffer)) > 0)
        {
          outputStream.write(buffer, 0, length);
        }
      }
      finally
      {
        if (inputStream != null)
        {
          try
          {
            inputStream.close();
          }
          catch (IOException exception)
          {
            // We cannot do anything :(
          }
        }
        if (outputStream != null)
        {
          try
          {
            outputStream.close();
          }
          catch (IOException exception)
          {
            // We cannot do anything :(
          }
        }
      }
      return file;
    }

    public DexClassLoader load(Context context, File apkFile)
    {
      final File directory = ensureDirectory(context);
      final DexClassLoader dexClassLoader = new DexClassLoader(apkFile.getAbsolutePath(), directory.getAbsolutePath(), null, this);
      return dexClassLoader;
    }

    private File ensureDirectory(Context context)
    {
      final File directory = context.getDir("dex", Context.MODE_PRIVATE);
      directory.mkdir();
      return directory;
    }

  }

  private WrappedClassLoader wrappedClassLoader;

  @Override
  protected void onCreateCustom()
  {
    super.onCreateCustom();

    final Context baseContext = getBaseContext();
    try
    {
      final Object mPackageInfo = new FieldWrapper<Object>(baseContext, "mPackageInfo").get();
      final FieldWrapper<ClassLoader> mClassLoaderFieldWrapper = new FieldWrapper<ClassLoader>(mPackageInfo, "mClassLoader");
      final ClassLoader mClassLoader = mClassLoaderFieldWrapper.get();
      wrappedClassLoader = new WrappedClassLoader(mClassLoader);
      mClassLoaderFieldWrapper.set(wrappedClassLoader);
    }
    catch (Exception exception)
    {
      if (log.isErrorEnabled())
      {
        log.error("Could not tweak the built-in 'ClassLoader'!", exception);
      }
    }
  }

  public final WrappedClassLoader getWrappedClassLoader()
  {
    return wrappedClassLoader;
  }

}

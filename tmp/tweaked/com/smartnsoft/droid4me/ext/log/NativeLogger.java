/* 
 * Copyright (C) 2008-2009 E2M.
 *
 * The code hereby is the private full property of the E2M company, Paris, France.
 * 
 * You have no right to re-use or modify it. There are no open-source, nor free licence
 * attached to it!
 */

package com.smartnsoft.droid4me.ext.log;

import java.util.logging.Level;

import android.util.Log;

import com.smartnsoft.droid4me.log.LoggerFactory;

/**
 * Resorts to the native Javalogger implementation.
 *
 * @author Édouard Mercier
 * @date 2007.12.23
 */
public class NativeLogger
    implements com.smartnsoft.droid4me.log.Logger
{

  public void debug(String message)
  {
    java.util.logging.Logger.getLogger(this.getClass().getSimpleName()).log(Level.FINE, message);
  }

  public void info(String message)
  {
    java.util.logging.Logger.getLogger(this.getClass().getSimpleName()).log(Level.INFO, message);
  }

  public void warn(String message)
  {
    java.util.logging.Logger.getLogger(this.getClass().getSimpleName()).log(Level.WARNING, message);
  }

  public void warn(String message, Throwable throwable)
  {
    java.util.logging.Logger.getLogger(this.getClass().getSimpleName()).log(Level.WARNING, message, throwable);
  }

  public void warn(StringBuffer message, Throwable throwable)
  {
    warn(message.toString(), throwable);
  }

  public void error(String message)
  {
    java.util.logging.Logger.getLogger(this.getClass().getSimpleName()).log(Level.SEVERE, message);
  }

  public void error(String message, Throwable throwable)
  {
    java.util.logging.Logger.getLogger(this.getClass().getSimpleName()).log(Level.SEVERE, message, throwable);
  }

  public void error(StringBuffer message, Throwable throwable)
  {
    error(message.toString(), throwable);
  }

  public void fatal(String message)
  {
    java.util.logging.Logger.getLogger(this.getClass().getSimpleName()).log(Level.SEVERE, message);
  }

  public void fatal(String message, Throwable throwable)
  {
    java.util.logging.Logger.getLogger(this.getClass().getSimpleName()).log(Level.SEVERE, message, throwable);
  }

  public boolean isDebugEnabled()
  {
    return LoggerFactory.logLevel <= Log.DEBUG;
  }

  public boolean isInfoEnabled()
  {
    return LoggerFactory.logLevel <= Log.INFO;
  }

  public boolean isWarnEnabled()
  {
    return LoggerFactory.logLevel <= Log.WARN;
  }

  public boolean isErrorEnabled()
  {
    return LoggerFactory.logLevel <= Log.ERROR;
  }

  public boolean isFatalEnabled()
  {
    return LoggerFactory.logLevel <= Log.ERROR;
  }

}

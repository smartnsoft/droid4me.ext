/* 
 * Copyright (C) 2008-2009 E2M.
 *
 * The code hereby is the private full property of the E2M company, Paris, France.
 * 
 * You have no right to re-use or modify it. There are no open-source, nor free licence
 * attached to it!
 */

package com.smartnsoft.droid4me.ext.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;

import com.smartnsoft.droid4me.app.ActivityController;
import com.smartnsoft.droid4me.ext.util.SendLogsTask;

/**
 * To be derived from when using the "ext" framework.
 * 
 * @author Édouard Mercier
 * @since 2009.08.21
 */
public abstract class SmartApplication
    extends com.smartnsoft.droid4me.app.SmartApplication
{

  /**
   * Contains various attributes that will be used on the UI, especially on unexpected exceptions dialog boxes.
   * 
   * @since 2010.07.21
   */
  protected final static class I18NExt
  {

    public final String applicationName;

    public final CharSequence reportButtonLabel;

    public final String retrievingLogProgressMessage;

    public I18NExt(String applicationName, CharSequence reportButtonLabel, String retrievingLogProgressMessage)
    {
      this.applicationName = applicationName;
      this.reportButtonLabel = reportButtonLabel;
      this.retrievingLogProgressMessage = retrievingLogProgressMessage;
    }

  }

  protected class DefaultExceptionHandler
      extends ActivityController.AbstractExceptionHandler
  {

    public DefaultExceptionHandler(I18N i18n)
    {
      super(i18n);
    }

    @Override
    public boolean onOtherException(final Activity activity, Throwable throwable)
    {
      if (checkConnectivityProblemInCause(activity, throwable) == true)
      {
        return true;
      }
      // We make sure that the dialog is popped from the UI thread
      activity.runOnUiThread(new Runnable()
      {
        public void run()
        {
          final I18NExt i18nExt = getI18NExt();
          new AlertDialog.Builder(activity).setTitle(getI18N().dialogBoxErrorTitle).setIcon(android.R.drawable.ic_dialog_alert).setMessage(
              getI18N().otherProblemHint).setPositiveButton(android.R.string.ok, new OnClickListener()
          {
            public void onClick(DialogInterface dialogInterface, int i)
            {
              // We leave the activity, because we cannot go any further
              activity.finish();
            }
          }).setNeutralButton(i18nExt.reportButtonLabel, new OnClickListener()
          {
            public void onClick(DialogInterface dialogInterface, int i)
            {
              new SendLogsTask(activity, i18nExt.retrievingLogProgressMessage, "[" + i18nExt.applicationName + "] Error log - v%1s", getLogReportRecipient()).execute(
                  null, null);
            }
          }).setCancelable(false).show();
        }
      });
      return true;
    }

  }

  /**
   * @return the i18n attributes that will be used when the framework interacts with the UI
   */
  protected abstract SmartApplication.I18NExt getI18NExt();

  /**
   * @return the e-mail address that will be used when submitting an error log message
   */
  protected abstract String getLogReportRecipient();

  public static String getFrameworkVersionString()
  {
    return "v1.1";
  }

  @Override
  protected void onCreateCustom()
  {
    super.onCreateCustom();
    log.info((new StringBuilder()).append("Application powered by droid4me.ext ").append(SmartApplication.getFrameworkVersionString()).append(
        " - Copyright Smart&Soft").toString());
  }

  @Override
  protected void checkLicense(Application application)
  {
    if (application.getPackageName().equals("com.smartnsoft.kapps") == false)
    {
      throw new Error("You are not allowed to use the droid4me.ext library for this application!");
    }
  }

  @Override
  protected ActivityController.ExceptionHandler getExceptionHandler()
  {
    return new SmartApplication.DefaultExceptionHandler(getI18N());
  }

}

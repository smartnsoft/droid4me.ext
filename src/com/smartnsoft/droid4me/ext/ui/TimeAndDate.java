/* 
 * Copyright (C) 2008-2009 E2M.
 *
 * The code hereby is the private full property of the E2M company, Paris, France.
 * 
 * You have no right to re-use or modify it. There are no open-source, nor free licence
 * attached to it!
 */

package com.smartnsoft.droid4me.ext.ui;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.provider.Settings;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.TimePicker;

/**
 * Just there in order to gather a date and time picker into a single component.
 * 
 * @author Édouard Mercier
 * @date 2008.02.17
 */
public class TimeAndDate
    extends LinearLayout
{

  /**
   * So as to notify the end-user that the the time or date have been updated.
   */
  public interface OnTimeAndDateChanged
  {

    void dateChanged(GregorianCalendar date);

  }

  private static boolean USE_24H;

  private static DateFormat TIME_FORMAT;

  private static DateFormat FRIENDLY_DATE_FORMAT;

  private Button dateButton;

  private Button timeButton;

  private GregorianCalendar date;

  private boolean displayTime;

  private String noTime;

  private OnTimeAndDateChanged onTimeAndDateChanged;

  public TimeAndDate(Context context, AttributeSet attrs)
  {
    super(context, attrs);
    initialize(context);
  }

  public TimeAndDate(Context context)
  {
    super(context);
    initialize(context);
  }

  public GregorianCalendar getDate()
  {
    return date;
  }

  public void setDisplayTime(boolean displayTime)
  {
    this.displayTime = displayTime;
    if (displayTime == false)
    {
      timeButton.setVisibility(View.GONE);
    }
    else
    {
      timeButton.setVisibility(getVisibility());
    }
  }

  public void setNoTimeOption(String noTime)
  {
    this.noTime = noTime;
  }

  public void setDate(GregorianCalendar date)
  {
    this.date = date;
    dateButton.setText(computeDateText());
    timeButton.setText(computeTimeText());
  }

  public void setOnTimeAndDateChanged(OnTimeAndDateChanged onTimeAndDateChanged)
  {
    this.onTimeAndDateChanged = onTimeAndDateChanged;
  }

  @Override
  public void setVisibility(int visibility)
  {
    super.setVisibility(visibility);

    dateButton.setVisibility(visibility);
    timeButton.setVisibility(visibility);
  }

  @Override
  public void setEnabled(boolean enabled)
  {
    super.setEnabled(enabled);

    dateButton.setEnabled(enabled);
    dateButton.setFocusable(enabled);
    if (displayTime == true)
    {
      timeButton.setEnabled(enabled);
      timeButton.setFocusable(enabled);
    }
  }

  public void initialize(Context context)
  {
    this.setOrientation(HORIZONTAL);

    {
      dateButton = new Button(context);
      addView(dateButton, new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
      dateButton.setOnClickListener(new View.OnClickListener()
      {

        private DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener()
        {
          public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth)
          {
            // We re-create the date from scratch, in order to get rid of a weird NPE exception at
            // "java.util.GregorianCalendar.getOffset(GregorianCalendar.java:1013)"
            final GregorianCalendar newDate = new GregorianCalendar();
            newDate.set(Calendar.YEAR, year);
            newDate.set(Calendar.MONTH, monthOfYear);
            newDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            newDate.set(Calendar.HOUR_OF_DAY, date.get(Calendar.HOUR_OF_DAY));
            newDate.set(Calendar.MINUTE, date.get(Calendar.MINUTE));
            newDate.set(Calendar.SECOND, 0);
            newDate.set(Calendar.MILLISECOND, 0);
            date = newDate;
            dateButton.setText(computeDateFormat(date, getContext()));
            if (onTimeAndDateChanged != null)
            {
              onTimeAndDateChanged.dateChanged(date);
            }
          }
        };

        public void onClick(View view)
        {
          // If the whole view is disabled, the button should not respond
          if (isEnabled() == false)
          {
            return;
          }
          if (date == null)
          {
            date = new GregorianCalendar();
            date.set(Calendar.SECOND, 0);
            date.set(Calendar.MILLISECOND, 0);
          }
          // BUG: when the first day of week is set to MONDAY, there seems to be problems with the UI
          // Reported to Android bug tracker: http://code.google.com/p/android/issues/detail?id=697
          new DatePickerDialog(getContext(), dateSetListener, date.get(Calendar.YEAR), date.get(Calendar.MONTH), date.get(Calendar.DAY_OF_MONTH)).show();
        }
      });
    }
    {
      timeButton = new Button(context);
      addView(timeButton, new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
      timeButton.setOnClickListener(new View.OnClickListener()
      {

        private TimePickerDialog.OnTimeSetListener timeSetListener = new TimePickerDialog.OnTimeSetListener()
        {

          public void onTimeSet(TimePicker view, int hourOfDay, int minute)
          {
            // We re-create the date from scratch, in order to get rid of a weird NPE exception at
            // "java.util.GregorianCalendar.getOffset(GregorianCalendar.java:1013)"
            final GregorianCalendar newDate = new GregorianCalendar();
            newDate.set(Calendar.YEAR, date.get(Calendar.YEAR));
            newDate.set(Calendar.MONTH, date.get(Calendar.MONTH));
            newDate.set(Calendar.DAY_OF_MONTH, date.get(Calendar.DAY_OF_MONTH));
            newDate.set(Calendar.HOUR_OF_DAY, hourOfDay);
            newDate.set(Calendar.MINUTE, minute);
            newDate.set(Calendar.SECOND, 0);
            newDate.set(Calendar.MILLISECOND, 0);
            date = newDate;
            timeButton.setText(computeTimeFormat(date));
            if (onTimeAndDateChanged != null)
            {
              onTimeAndDateChanged.dateChanged(date);
            }
          }
        };

        public void onClick(View view)
        {
          // If the whole view is disabled, the button should not respond
          if (isEnabled() == false)
          {
            return;
          }
          if (date == null)
          {
            date = new GregorianCalendar();
            date.set(Calendar.SECOND, 0);
            date.set(Calendar.MILLISECOND, 0);
          }
          final TimePickerDialog dialog = new TimePickerDialog(getContext(), timeSetListener, date.get(Calendar.HOUR_OF_DAY), date.get(Calendar.MINUTE), USE_24H);
          if (noTime != null)
          {
            dialog.setButton3(noTime, new android.content.DialogInterface.OnClickListener()
            {
              public void onClick(DialogInterface arg0, int arg1)
              {
                date.set(Calendar.HOUR_OF_DAY, 0);
                date.set(Calendar.MINUTE, 0);
                timeButton.setText(computeTimeFormat(date));
                if (onTimeAndDateChanged != null)
                {
                  onTimeAndDateChanged.dateChanged(date);
                }
              }
            });
          }
          dialog.show();
        }
      });
    }
  }

  private String computeDateText()
  {
    if (date == null)
    {
      return "";
    }
    else
    {
      return computeDateFormat(date, getContext());
    }
  }

  private String computeTimeText()
  {
    if (date == null)
    {
      return "";
    }
    else
    {
      return computeTimeFormat(date);
    }
  }

  private static synchronized String computeDateFormat(Calendar date, Context context)
  {
    if (FRIENDLY_DATE_FORMAT == null)
    {
      FRIENDLY_DATE_FORMAT = new SimpleDateFormat(Settings.System.getString(context.getContentResolver(), Settings.System.DATE_FORMAT));
      final String is12or24 = Settings.System.getString(context.getContentResolver(), Settings.System.TIME_12_24);
      USE_24H = (is12or24 != null && is12or24.equals("24"));
      TIME_FORMAT = new SimpleDateFormat(USE_24H ? "HH:mm" : "KK:mm a");
    }
    return FRIENDLY_DATE_FORMAT.format(date.getTime());
  }

  private static String computeTimeFormat(Calendar date)
  {
    return TIME_FORMAT.format(date.getTime());
  }

}

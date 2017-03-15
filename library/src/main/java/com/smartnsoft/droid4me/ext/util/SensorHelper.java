/* 
 * Copyright (C) 2008-2009 E2M.
 *
 * The code hereby is the private full property of the E2M company, Paris, France.
 * 
 * You have no right to re-use or modify it. There are no open-source, nor free licence
 * attached to it!
 */

package com.smartnsoft.droid4me.ext.util;

import android.content.Context;
import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.provider.Settings;

import com.smartnsoft.droid4me.log.Logger;
import com.smartnsoft.droid4me.log.LoggerFactory;

/**
 * Wraps the sensor and provides a standard way to catch events.
 *
 * @author Édouard Mercier
 * @since 2009.05.29
 */
@SuppressWarnings("deprecation")
public final class SensorHelper
{

  public interface OnWristShakeListener
  {

    void onShake(boolean value);

  }

  @SuppressWarnings("unused")
  private final static Logger log = LoggerFactory.getInstance(SensorHelper.class);

  public static double GRAVITY_FACTOR_THRESHOLD = 0.20d;

  private static final int SENSOR_INTERVAL_CHECK_IN_MILLISECOND = 1000;

  private final Context context;

  private final boolean enabled;

  private SensorListener sensorListener;

  protected boolean internalState;

  public SensorHelper(Context context)
  {
    this.context = context;
    // We check that the hosting system is not an emulator, otherwise it hangs
    enabled = (Settings.System.getString(context.getContentResolver(), Settings.System.ANDROID_ID) != null);
  }

  public void register(final SensorHelper.OnWristShakeListener onWristShakeListener)
  {
    if (enabled == false)
    {
      return;
    }
    final SensorManager sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
    // This is in preparation of ADP 1.5
    // final Sensor sensor = sensorManager.getDefaultSensor(SensorManager.SENSOR_ACCELEROMETER);
    // if (sensor == null)
    // {
    // // There is no accelerometer sensor
    // return;
    // }
    // sensorEventListener = new SensorEventListener()
    // {
    // public void onSensorChanged(SensorEvent event)
    // {
    // if (event.sensor.getType() == SensorManager.SENSOR_ACCELEROMETER)
    // {
    // // We compute the acceleration vectorial norm
    // double netForce = Math.pow(event.values[SensorManager.DATA_X] + SensorManager.GRAVITY_EARTH, 2.0);
    // netForce += Math.pow(event.values[SensorManager.DATA_Y] + SensorManager.GRAVITY_EARTH, 2.0);
    // netForce += Math.pow(event.values[SensorManager.DATA_Z] + SensorManager.GRAVITY_EARTH, 2.0);
    // log.debug("Acceleration: x=" + event.values[SensorManager.DATA_X] + " y=" + event.values[SensorManager.DATA_Y] + " z=" +
    // event.values[SensorManager.DATA_Z]);
    // final double accelerationFactor = Math.sqrt(netForce) / SensorManager.GRAVITY_EARTH;
    // if (accelerationFactor > 1.5d)
    // {
    // log.debug("The acceleration is " + accelerationFactor + " times the Earth gravity");
    // isFullScreen = !isFullScreen;
    // applySwitchedMode();
    // }
    // }
    // }
    //
    // public void onAccuracyChanged(Sensor sensor, int accuracy)
    // {
    // // We do not care
    // }
    // };
    // sensorManager.registerListener(sensorEventListener, sensor, SensorManager.SENSOR_DELAY_UI);
    sensorListener = new SensorListener()
    {

      private long lastSensorTakenIntoAccountTimestamp = 0;

      @Override
      public void onSensorChanged(int sensor, float[] values)
      {
        if (sensor == SensorManager.SENSOR_ACCELEROMETER)
        {
          // We compute the acceleration vectorial norm
          double netForce = Math.pow(values[SensorManager.DATA_X], 2.0);
          netForce += Math.pow(values[SensorManager.DATA_Y], 2.0);
          netForce += Math.pow(values[SensorManager.DATA_Z], 2.0);
          // log.debug("Acceleration: x=" + values[SensorManager.DATA_X] + " y=" + values[SensorManager.DATA_Y] + " z=" +
          // values[SensorManager.DATA_Z]);
          final double accelerationFactor = Math.sqrt(netForce) / SensorManager.GRAVITY_EARTH;
          // if (log.isDebugEnabled())
          // {
          // log.debug("The acceleration is " + accelerationFactor + " times the Earth gravity");
          // }
          if (Math.abs(accelerationFactor - 1) > GRAVITY_FACTOR_THRESHOLD && ((System.currentTimeMillis() - lastSensorTakenIntoAccountTimestamp) > SENSOR_INTERVAL_CHECK_IN_MILLISECOND))
          {
            lastSensorTakenIntoAccountTimestamp = System.currentTimeMillis();
            internalState = !internalState;
            onWristShakeListener.onShake(internalState);
          }
        }
      }

      @Override
      public void onAccuracyChanged(int sensor, int accuracy)
      {
        // // We do not care
      }

    };
    sensorManager.registerListener(sensorListener, SensorManager.SENSOR_ACCELEROMETER);
  }

  public void unregister()
  {
    if (enabled == false)
    {
      return;
    }
    // if (sensorEventListener != null)
    // {
    // ((SensorManager) getSystemService(Context.SENSOR_SERVICE)).unregisterListener(sensorEventListener);
    // }
    if (sensorListener != null)
    {
      ((SensorManager) context.getSystemService(Context.SENSOR_SERVICE)).unregisterListener(sensorListener);
    }
  }

}

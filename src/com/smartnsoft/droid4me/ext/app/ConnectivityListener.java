package com.smartnsoft.droid4me.ext.app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.smartnsoft.droid4me.app.ActivityController;
import com.smartnsoft.droid4me.app.AppPublics;
import com.smartnsoft.droid4me.app.AppPublics.BroadcastListener;
import com.smartnsoft.droid4me.app.AppPublics.UseNativeBroadcast;
import com.smartnsoft.droid4me.app.Smarted;
import com.smartnsoft.droid4me.log.Logger;
import com.smartnsoft.droid4me.log.LoggerFactory;
import com.smartnsoft.droid4me.support.v4.content.LocalBroadcastManager;

/**
 * A basis class responsible for listening to connectivity events.
 * 
 * <p>
 * <b>Caution: this component requires the Android {@code android.permission.ACCESS_NETWORK_STATE} permission!</b>.
 * </p>
 * 
 * <p>
 * This component will issue a {@link Intent broadcast Intent}, every time the hosting application Internet connectivity status changes with the
 * {@link ConnectivityListener#CONNECTIVITY_CHANGED_ACTION} action, and an extra {@link ConnectivityListener#EXTRA_HAS_CONNECTIVITY boolean flag},
 * which states the current application connectivity status.
 * </p>
 * <p>
 * This component should be created during the {@link com.smartnsoft.droid4me.app.SmartApplication#onCreateCustom()} method, and it should be enrolled
 * to all the hosting application {@link Activity activities}, during the {@link com.smartnsoft.droid4me.app.SmartApplication#getInterceptor()} when
 * receiving the {@link ActivityController.Interceptor.InterceptorEvent.onCreate} and {@link ActivityController.Interceptor.InterceptorEvent.onResume}
 * events.
 * </p>
 * 
 * @author Édouard Mercier
 * @since 2012.04.04
 */
public abstract class ConnectivityListener
    implements AppPublics.BroadcastListener, ActivityController.Interceptor
{

  protected final static Logger log = LoggerFactory.getInstance(ConnectivityListener.class);

  /**
   * The action that will used to notify via a broadcast {@link Intent} when the hosting application Internet connectivity changes.
   */
  public static final String CONNECTIVITY_CHANGED_ACTION = "connectivityChangedAction";

  /**
   * A broadcast {@link Intent} boolean flag which indicates the hosting application Internet connectivity status.
   */
  public static final String EXTRA_HAS_CONNECTIVITY = "hasConnectivity";

  private final Context context;

  private final AppPublics.BroadcastListener[] networkBroadcastListener;

  private boolean hasConnectivity = true;

  /**
   * The constructor will issue an exception if the hosting application does not declare the {@code android.permission.ACCESS_NETWORK_STATE}
   * permission.
   * 
   * @param context
   *          the application context
   */
  public ConnectivityListener(Context context)
  {
    this.context = context;
    networkBroadcastListener = new AppPublics.BroadcastListener[] { this };
    // We immediately extract the connectivity status
    final NetworkInfo activeNetworkInfo = getActiveNetworkInfo();
    if (activeNetworkInfo == null || activeNetworkInfo.isConnected() == false)
    {
      if (log.isInfoEnabled())
      {
        log.info("The Internet connection is off");
      }
      hasConnectivity = false;
      notifyServices(hasConnectivity);
    }
  }

  /**
   * @return whether the device has Internet connectivity
   */
  public boolean hasConnectivity()
  {
    return hasConnectivity;
  }

  /**
   * @return the currently active network info; may be {@code null}
   */
  public NetworkInfo getActiveNetworkInfo()
  {
    return ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
  }

  @Override
  @UseNativeBroadcast
  public IntentFilter getIntentFilter()
  {
    return new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
  }

  @Override
  public void onReceive(Intent intent)
  {
    // There is only one registered Intent action, hence, we do not need any filtering test
    final boolean previousConnectivity = hasConnectivity;
    hasConnectivity = !intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);
    if (previousConnectivity != hasConnectivity)
    {
      // With this filter, only one broadcast listener will handle the event
      if (log.isInfoEnabled())
      {
        log.info("Received an Internet connectivity change event: the connection is now " + (hasConnectivity == false ? "off" : "on"));
      }
      // We notify the application regarding this connectivity change event
      LocalBroadcastManager.getInstance(context).sendBroadcast(
          new Intent(ConnectivityListener.CONNECTIVITY_CHANGED_ACTION).putExtra(ConnectivityListener.EXTRA_HAS_CONNECTIVITY, hasConnectivity));

      notifyServices(hasConnectivity);
    }
  }

  /**
   * @return an array made of a single {@link BroadcastListener}, which is the current instance
   */
  public BroadcastListener[] getBroadcastListeners()
  {
    return networkBroadcastListener;
  }

  /**
   * This method should be invoked during the {@link ActivityController.Interceptor#onLifeCycleEvent(Activity, Object, InterceptorEvent)} method, and
   * it will handle everything.
   */
  @Override
  public void onLifeCycleEvent(Activity activity, Object component, InterceptorEvent event)
  {
    if (event == ActivityController.Interceptor.InterceptorEvent.onCreate)
    {
      // We listen to the network connection potential issues: we do not want child activities to also register for the connectivity change events
      registerBroadcastListenerOnCreate(activity, component);
    }
    else if (event == ActivityController.Interceptor.InterceptorEvent.onResume)
    {
      // We transmit the connectivity status
      registerBroadcastListenerOnResume(activity, component);
    }
  }

  /**
   * This method should be invoked during the {@link Activity#onCreate()} or {android.app.Fragment#onCreate()} methods, so that it listens to the
   * Internet connectivity status, and so that it is notified when this status changes.
   * 
   * <p>
   * If the provided {@code activity} parameter does not implement that interface, or if it has a {@link Activity#getParent()} method, or if the
   * {@code component} second parameter is {@code null} the method will do nothing.
   * </p>
   * 
   * <p>
   * This method will {@link Smarted#registerBroadcastListeners(BroadcastListener[]) register} the current instance.
   * </p>
   * 
   * @param activity
   *          an {@link Activity} which is supposed to implement the {@link Smarted} interface
   * @param component
   *          when not-null, the {@link android.app.Fragment} in which the {@link android.app.Fragment#onCreate()} method is invoked
   */
  public void registerBroadcastListenerOnCreate(Activity activity, Object component)
  {
    // We listen to the network connection potential issues: we do not want child activities to also register for the connectivity change events
    if (component == null && activity.getParent() == null && activity instanceof Smarted<?>)
    {
      final Smarted<?> smartedActivity = (Smarted<?>) activity;
      smartedActivity.registerBroadcastListeners(getBroadcastListeners());
    }
  }

  /**
   * This method should be invoked during the {@link Activity#onResume()} or {android.app.Fragment#onResume()} methods.
   * 
   * <p>
   * If the provided {@code activity} parameter does not implement that interface, or if it has a {@link Activity#getParent()} method, or if the
   * {@code component} second parameter is {@code null} the method will do nothing.
   * </p>
   * 
   * <p>
   * This method will invoke the {@link #updateActivity(Smarted)} method from the calling thread.
   * </p>
   * 
   * @param activity
   *          an {@link Activity} which is supposed to implement the {@link Smarted} interface
   * @param component
   *          when not-null, the {@link android.app.Fragment} in which the {@link android.app.Fragment#onResume()} method is invoked
   */
  public void registerBroadcastListenerOnResume(Activity activity, Object component)
  {
    // We transmit the connectivity status
    if (component == null && activity.getParent() == null && activity instanceof Smarted<?>)
    {
      final Smarted<?> smartedActivity = (Smarted<?>) activity;
      updateActivity(smartedActivity);
    }
  }

  /**
   * This method is invoked when the component has detected an Internet connectivity change. It is invoked a first time when the connectivity status
   * is known.
   * 
   * <p>
   * It is a place-holder for notify all components depending on the Internet connectivity about the new status.
   * </p>
   * 
   * <p>
   * Note that this method will be invoked from the UI thread.
   * </p>
   * 
   * @param hasConnectivity
   *          the new Internet connectivity status
   */
  protected abstract void notifyServices(boolean hasConnectivity);

  /**
   * This method is invoked systematically during the {@link Activity#onResume()} method, provided the
   * {@link #registerBroadcastListenerOnResume(Activity, Object)} method has been invoked.
   * 
   * <p>
   * It is a place-holder for updating the {@link Activity} Internet connectivity new status.
   * </p>
   * 
   * <p>
   * Note that this method will be invoked from the UI thread.
   * </p>
   * 
   * @param smartedActivity
   *          the {@link Smarted} {@link Activity} that should be updated graphically
   */
  protected abstract void updateActivity(final Smarted<?> smartedActivity);

}
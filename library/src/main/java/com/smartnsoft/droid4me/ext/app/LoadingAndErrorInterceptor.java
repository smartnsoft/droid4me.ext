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

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.atomic.AtomicReference;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.smartnsoft.droid4me.LifeCycle.BusinessObjectUnavailableException;
import com.smartnsoft.droid4me.animation.SimpleAnimationListener;
import com.smartnsoft.droid4me.app.ActivityController;
import com.smartnsoft.droid4me.app.AppPublics;
import com.smartnsoft.droid4me.app.AppPublics.BroadcastListener;
import com.smartnsoft.droid4me.app.AppPublics.LoadingBroadcastListener;
import com.smartnsoft.droid4me.app.Smartable;
import com.smartnsoft.droid4me.ws.WebServiceClient.CallException;

/**
 * An interceptor which is responsible for handling two things, very common in applications:
 * <ol>
 * <li>the graphical indicators while an entity ({@link Activity} or {@link android.app.Fragment}) is being loaded: on that purpose, you need to
 * declare the entity {@link AppPublics.SendLoadingIntentAnnotation} annotation, so that loading events are triggered;</li>
 * <li>the display of errors.</li>
 * </ol>
 * <p/>
 * <p>
 * Caution: in order to have this interceptor working, you need to make sure that the entity (deriving hence from {@link Smartable}) uses a template
 * type implementing the {@link LoadingErrorAndRetryAggregateProvider} interface.
 * </p>
 * <p/>
 * <p>
 * This class requires the Android Support Library v4.
 * </p>
 *
 * @author �douard Mercier
 * @since 2014.06.13
 */
public abstract class LoadingAndErrorInterceptor
    implements ActivityController.Interceptor
{

  public interface BusinessObjectUnavailableReporter<FragmentAggregateClass extends FragmentAggregate<?, ?>>
  {

    void reportBusinessObjectUnavailableException(Smartable<FragmentAggregateClass> smartableFragment,
        BusinessObjectUnavailableException businessObjectUnavailableException);
  }

  public interface ErrorAndRetryManagerProvider
  {

    ErrorAndRetryManager getErrorAndRetryManager(View view);

    View getLoadingAndRetryView(View view);

    View getLoadingView(View view);

    View getProgressBar(View view);

    TextView getTextView(View view);

    View getErrorAndRetryView(View view);

    CharSequence getErrorText(Context context);

    CharSequence getLoadingText(Context context);

  }

  public interface LoadingErrorAndRetryAggregateProvider
  {

    LoadingErrorAndRetryAggregate getLoadingErrorAndRetryAggregate();

    BusinessObjectsUnavailableExceptionKeeper getBusinessUnavailableExceptionKeeper();

  }

  public interface DoNotHideLoadingNextTime
  {

  }

  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.TYPE)
  @Inherited
  public @interface LoadingAndErrorAnnotation
  {

    boolean enabled() default true;

    boolean loadingEnabled() default true;

  }

  public static final class BusinessObjectsUnavailableExceptionKeeper
  {

    private static final String BUSINESS_OBJECT_UNAVAILABLE_EXCEPTION = "businessObjectUnavailableException";

    private BusinessObjectUnavailableException exception;

    public BusinessObjectUnavailableException getException()
    {
      return exception;
    }

    public void checkException()
        throws BusinessObjectUnavailableException
    {
      if (exception != null)
      {
        throw exception;
      }
    }

    public void setException(BusinessObjectUnavailableException exception)
    {
      this.exception = exception;
    }

    public void onRestoreInstanceState(Bundle bundle)
    {
      if (bundle != null)
      {
        exception = (BusinessObjectUnavailableException) bundle.getSerializable(BusinessObjectsUnavailableExceptionKeeper.BUSINESS_OBJECT_UNAVAILABLE_EXCEPTION);
      }
    }

    public void onSaveInstanceState(Bundle bundle)
    {
      if (exception != null)
      {
        bundle.putSerializable(BusinessObjectsUnavailableExceptionKeeper.BUSINESS_OBJECT_UNAVAILABLE_EXCEPTION, exception);
      }
    }

  }

  public interface ErrorAndRetryManager
  {

    void showError(final Activity activity, Throwable throwable, boolean fromGuiThread, final Runnable onCompletion);

    void hide();

  }

  private static final class LoadingErrorAndRetryAttributes
  {

    private final View containerView;

    private final View loadingView;

    private View progressBar;

    private TextView text;

    private final ErrorAndRetryManager errorAndRetryManager;

    private boolean isHandlingError;

    private LoadingErrorAndRetryAttributes(View view, ErrorAndRetryManagerProvider errorAndRetryManagerProvider)
    {
      final View loadingErrorAndRetry = errorAndRetryManagerProvider.getLoadingAndRetryView(view);
      containerView = loadingErrorAndRetry == null ? errorAndRetryManagerProvider.getLoadingView(view) : loadingErrorAndRetry;
      if (containerView != null)
      {
        loadingView = errorAndRetryManagerProvider.getLoadingView(containerView);
        if (loadingView != null)
        {
          progressBar = errorAndRetryManagerProvider.getProgressBar(loadingView);
          text = errorAndRetryManagerProvider.getTextView(loadingView);
        }
        final View errorAndRetryView = errorAndRetryManagerProvider.getErrorAndRetryView(containerView);
        if (errorAndRetryView != null)
        {
          errorAndRetryManager = errorAndRetryManagerProvider.getErrorAndRetryManager(errorAndRetryView);
          errorAndRetryManager.hide();
          errorAndRetryView.setVisibility(View.GONE);
        }
        else
        {
          errorAndRetryManager = null;
        }
      }
      else
      {
        loadingView = null;
        errorAndRetryManager = null;
      }
    }

    @SuppressWarnings("unused")
    public void showLoading()
    {
      setLoadingVisible();
    }

    /**
     * It is required to invoke that method from the UI thread!
     */
    public void hideLoading(Context context)
    {
      // System.out.println("hideLoading" + Thread.currentThread().getName());
      if (containerView != null)
      {
        if (isHandlingError == false)
        {
          final AnimationListener animationListener = new SimpleAnimationListener()
          {
            @Override
            public void onAnimationEnd(Animation animation)
            {
              if (loadingView != null)
              {
                loadingView.setVisibility(View.GONE);
                // System.out.println("hideLoading loadingView.setVisibility(View.GONE)" + Thread.currentThread().getName());
              }
              // While the animation is played, an error may have been declared
              final int containerVisibility = isHandlingError == false ? View.GONE : View.VISIBLE;
              containerView.setVisibility(containerVisibility);
              // System.out.println("hideLoading containerView.setVisibility(" + (isHandlingError == false ? "View.GONE" : "View.VISIBLE") + ")" +
              // Thread.currentThread().getName());
            }
          };
          // For disabling the animation
          if ("".equals(""))
          {
            final Animation animation = AnimationUtils.loadAnimation(context, android.R.anim.fade_out);
            animation.setAnimationListener(animationListener);
            // If the loading view contains tag and that this tag implements the 'DoNotHideLoadingNextTime' interface, we do not start the hide
            // animation
            if (loadingView.getTag() instanceof DoNotHideLoadingNextTime == false)
            {
              containerView.startAnimation(animation);
            }
            else
            {
              loadingView.setTag(null);
            }
          }
          else
          {
            animationListener.onAnimationEnd(null);
          }
        }
        else
        {
          if (loadingView != null)
          {
            loadingView.setVisibility(View.GONE);
            // System.out.println("hideLoading loadingView.setVisibility(View.GONE)" + Thread.currentThread().getName());
          }
        }
      }
    }

    public void handleLoading(ErrorAndRetryManagerProvider errorAndRetryManagerProvider, boolean isLoading,
        AtomicReference<BusinessObjectUnavailableException> issue)
    {
      final boolean isLoaderOn = "".equals("");
      if (loadingView != null)
      {
        if (hasErrorAndRetryView() == false && issue.get() != null && isLoading == false)
        {
          final Throwable currentIssue = issue.get();
          issue.set(null);
          if (progressBar != null)
          {
            progressBar.setVisibility(View.GONE);
          }
          if (text != null)
          {
            if (currentIssue instanceof CallException)
            {
              text.setText(currentIssue.getMessage());
            }
            else
            {
              text.setText(errorAndRetryManagerProvider.getErrorText(text.getContext()));
            }
            if (isLoaderOn == true)
            {
              setLoadingVisible();
            }
          }
        }
        else
        {
          if (hasErrorAndRetryView() == false && isLoading == true)
          {
            if (progressBar != null)
            {
              progressBar.setVisibility(View.VISIBLE);
            }
            if (text != null)
            {
              text.setText(errorAndRetryManagerProvider.getLoadingText(text.getContext()));
            }
          }
          if (isLoading == true)
          {
            if (isLoaderOn == true)
            {
              setLoadingVisible();
            }
          }
          else
          {
            if (isLoaderOn == true)
            {
              hideLoading(loadingView.getContext());
            }
          }
        }
      }
    }

    /**
     * It is required to invoke that method from the UI thread!
     */
    public void showIssue(Activity activity, final Smartable<?> smartable, Throwable throwable,
        final Runnable onCompletion)
    {
      // System.out.println("showError" + Thread.currentThread().getName());
      isHandlingError = true;
      if (errorAndRetryManager != null)
      {
        errorAndRetryManager.showError(activity, throwable, true, new Runnable()
        {
          @Override
          public void run()
          {
            isHandlingError = false;
            containerView.post(new Runnable()
            {
              @Override
              public final void run()
              {
                containerView.setVisibility(View.GONE);
              }
            });
            // System.out.println("showError containerView.setVisibility(View.GONE)" + Thread.currentThread().getName());
            if (onCompletion != null)
            {
              onCompletion.run();
            }
          }
        });
        containerView.post(new Runnable()
        {
          @Override
          public final void run()
          {
            containerView.setVisibility(View.VISIBLE);
          }
        });
        // System.out.println("showError containerView.setVisibility(View.VISIBLE)" + Thread.currentThread().getName());
      }
    }

    private void setLoadingVisible()
    {
      // System.out.println("setLoadingVisible" + Thread.currentThread().getName());
      if (containerView != null)
      {
        if (loadingView != null)
        {
          loadingView.setVisibility(View.VISIBLE);
          // System.out.println("setLoadingVisible loadingView.setVisibility(View.VISIBLE)" + Thread.currentThread().getName());
          containerView.setVisibility(View.VISIBLE);
          // System.out.println("setLoadingVisible containerView.setVisibility(View.VISIBLE)" + Thread.currentThread().getName());
        }
        else
        {
          containerView.setVisibility(View.GONE);
          // System.out.println("setLoadingVisible containerView.setVisibility(View.GONE)" + Thread.currentThread().getName());
        }
      }
    }

    private boolean hasErrorAndRetryView()
    {
      return errorAndRetryManager != null;
    }

  }

  public static final class LoadingErrorAndRetryAggregate
  {

    private boolean displayLoadingViewNextTime = true;

    private LoadingErrorAndRetryAttributes loadingErrorAndRetryAttributes;

    private final AtomicReference<BusinessObjectUnavailableException> issue = new AtomicReference<BusinessObjectUnavailableException>();

    public final void onCreate(final ErrorAndRetryManagerProvider errorAndRetryAttributesProvider, Activity activity,
        final Smartable<?> smartable, BusinessObjectUnavailableException issue, boolean handleLoading)
    {
      this.issue.set(issue);
      if (handleLoading == true)
      {
        displayLoadingViewNextTime = smartable.isFirstLifeCycle() == true;
        final AppPublics.BroadcastListener loadingBroadcastListener = new LoadingBroadcastListener(activity, smartable)
        {
          @Override
          protected void onLoading(boolean isLoading)
          {
            // if (log.isDebugEnabled())
            // {
            // log.debug("Received the loading event '" + isLoading + "' for the component '" + smartedFragment.getClass().getSimpleName() +
            // "' with id '" + smartedFragment.toString() + "'");
            // }
            if (displayLoadingViewNextTime == true)
            {
              // if (log.isDebugEnabled())
              // {
              // log.debug("Handling the loading event '" + isLoading + "' for the component '" + smartedFragment.getClass().getSimpleName() +
              // "' with id '" + smartedFragment.toString() + "'");
              // }
              loadingErrorAndRetryAttributes.handleLoading(errorAndRetryAttributesProvider, isLoading, LoadingErrorAndRetryAggregate.this.issue);
            }
            else
            {
              // We ignore the loading effect, until we receive a first "stop loading" event
              if (isLoading == false)
              {
                // if (log.isDebugEnabled())
                // {
                // log.debug("Setting the 'ignoreLoadingEffect' parameter to 'false' for the component '" +
                // smartedFragment.getClass().getSimpleName() + "' with id '" + smartedFragment.toString() + "'");
                // }
                displayLoadingViewNextTime = true;
              }
            }
          }
        };
        final BroadcastListener[] broadcastListeners = { loadingBroadcastListener };
        smartable.registerBroadcastListeners(broadcastListeners);
      }
    }

    public final void onStart(ErrorAndRetryManagerProvider errorAndRetryAttributesProvider, View view)
    {
      if (loadingErrorAndRetryAttributes == null)
      {
        loadingErrorAndRetryAttributes = view != null ? new LoadingErrorAndRetryAttributes(view, errorAndRetryAttributesProvider) : null;
      }
    }

    public final void onPause()
    {
      doNotDisplayLoadingViewNextTime();
    }

    public final void doNotDisplayLoadingViewNextTime()
    {
      displayLoadingViewNextTime = false;
    }

    public void showBusinessObjectUnavailableException(Activity activity, final Smartable<?> smartableFragment,
        BusinessObjectUnavailableException exception)
    {
      loadingErrorAndRetryAttributes.showIssue(activity, smartableFragment, exception, new Runnable()
      {
        @Override
        public void run()
        {
          smartableFragment.refreshBusinessObjectsAndDisplay(true, null, false);
        }
      });
    }

    public void showBusinessObjectUnavailableException(Activity activity, final Smartable<?> smartableFragment,
        BusinessObjectUnavailableException exception, Runnable runnable)
    {
      loadingErrorAndRetryAttributes.showIssue(activity, smartableFragment, exception, runnable);
    }

    public void showException(Activity activity, final Smartable<?> smartable, Throwable throwable, Runnable onRetry)
    {
      loadingErrorAndRetryAttributes.showIssue(activity, smartable, throwable, onRetry);
    }

    public final void reportBusinessObjectUnavailableException(BusinessObjectUnavailableException exception)
    {
      issue.set(exception);
    }

  }

  private final ErrorAndRetryManagerProvider errorAndRetryAttributesProvider = getErrorAndRetryAttributesProvider();

  protected abstract ErrorAndRetryManagerProvider getErrorAndRetryAttributesProvider();

  @SuppressWarnings("unchecked")
  @Override
  public void onLifeCycleEvent(final Activity activity, Object component, InterceptorEvent interceptorEvent)
  {
    final Object actualComponent = component == null ? activity : component;
    if (actualComponent instanceof Smartable<?>)
    {
      // It's a Fragment or an Activity
      final Smartable<LoadingErrorAndRetryAggregateProvider> smartable = (Smartable<LoadingErrorAndRetryAggregateProvider>) actualComponent;

      // We handle the loading, error and retry feature, but not with the DisableLoadingAndErrorInterceptor-annotated Fragments
      final LoadingAndErrorAnnotation loadingAndErrorAnnotation = smartable.getClass().getAnnotation(LoadingAndErrorAnnotation.class);
      if (loadingAndErrorAnnotation != null && loadingAndErrorAnnotation.enabled() == true && (interceptorEvent == InterceptorEvent.onCreate || interceptorEvent == InterceptorEvent.onStart || interceptorEvent == InterceptorEvent.onPause))
      {
        final LoadingErrorAndRetryAggregate aggregate = smartable.getAggregate().getLoadingErrorAndRetryAggregate();
        final BusinessObjectsUnavailableExceptionKeeper businessObjectsUnavailableExceptionKeeper = smartable.getAggregate().getBusinessUnavailableExceptionKeeper();
        if (interceptorEvent == InterceptorEvent.onCreate)
        {
          aggregate.onCreate(errorAndRetryAttributesProvider, activity, smartable, businessObjectsUnavailableExceptionKeeper.getException(), loadingAndErrorAnnotation.loadingEnabled() == true);
        }
        else if (interceptorEvent == InterceptorEvent.onStart)
        {
          final View view;
          if (component != null)
          {
            view = component instanceof android.support.v4.app.Fragment ? ((android.support.v4.app.Fragment) component).getView() : ((android.app.Fragment) component).getView();
          }
          else
          {
            view = activity.findViewById(android.R.id.content);
          }
          aggregate.onStart(errorAndRetryAttributesProvider, view);
        }
        else if (interceptorEvent == InterceptorEvent.onPause)
        {
          aggregate.onPause();
        }
      }
    }
  }

}

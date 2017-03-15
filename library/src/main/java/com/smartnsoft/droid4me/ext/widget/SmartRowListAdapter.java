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

package com.smartnsoft.droid4me.ext.widget;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;

import com.smartnsoft.droid4me.framework.SmartAdapters.BusinessViewWrapper;
import com.smartnsoft.droid4me.framework.SmartAdapters.ObjectEvent;
import com.smartnsoft.droid4me.framework.SmartAdapters.SmartListAdapter;
import com.smartnsoft.droid4me.log.Logger;
import com.smartnsoft.droid4me.log.LoggerFactory;

/**
 * @author Édouard Mercier
 * @since 2013.03.20
 */
public class SmartRowListAdapter<ViewClass extends View>
    extends SmartListAdapter<ViewClass>
{

  public interface ColumnsIndicator
  {

    int getColumnsCount(int row);

    int getHorizontalPaddingForRow(int row);

  }

  private static final class EmptyAttributes
  {

    public EmptyAttributes(View view)
    {
    }

  }

  public static final class EmptyWrapper
      extends BusinessViewWrapper<Integer>
  {

    public EmptyWrapper(Integer businessObject)
    {
      super(businessObject);
    }

    @Override
    public int getType(int position, Integer businessObject)
    {
      return businessObject;
    }

    @Override
    public boolean isEnabled(Integer businessObject)
    {
      return false;
    }

    @Override
    protected View createNewView(Activity activity, LayoutInflater layoutInflater, ViewGroup viewGroup, Integer integer)
    {
      return new View(activity.getApplicationContext());
    }

    @Override
    protected Object extractNewViewAttributes(Activity activity, View view, Integer businessObject)
    {
      return new EmptyAttributes(view);
    }

    @Override
    protected void updateView(Activity activity, LayoutInflater layoutInflater, Object viewAttributes, View view,
        Integer businessObject, int position)
    {

    }

  }

  private static final class RowBusinessViewAttributes<BusinessObjectClass>
  {

    private final RowLinearLayout linearLayout;

    private View[] views;

    public RowBusinessViewAttributes(RowLinearLayout linearLayout)
    {
      this.linearLayout = linearLayout;
    }

    public void update(final Activity activity, LayoutInflater layoutInflater,
        List<BusinessViewWrapper<BusinessObjectClass>> businessObject,
        int horizontalPadding, int position)
    {
      // if (SmartRowListAdapter.DEBUG_LOG_ENABLED == true && log.isDebugEnabled())
      // {
      // log.debug("Updating the row at position " + position + " composed of " + businessObject.size() + " column(s)");
      // }
      linearLayout.position = position;
      if (horizontalPadding > 0)
      {
        if (linearLayout.getPaddingLeft() != horizontalPadding || linearLayout.getPaddingRight() != horizontalPadding)
        {
          // We only set the padding provided it is not currently set to the expected value
          linearLayout.setPadding(horizontalPadding, 0, horizontalPadding, 0);
        }
      }
      else
      {
        if (linearLayout.getPaddingLeft() != 0 || linearLayout.getPaddingRight() != 0)
        {
          // We only set the padding provided it is not currently set to the expected value
          linearLayout.setPadding(0, 0, 0, 0);
        }
      }
      if (views == null)
      {
        views = new View[businessObject.size()];
        int index = 0;
        for (BusinessViewWrapper<BusinessObjectClass> wrapper : businessObject)
        {
          final View view = wrapper.getNewView(linearLayout, activity, layoutInflater);
          views[index] = view;
          // All cells will have the same width
          final ViewGroup.LayoutParams viewLayoutParams = view.getLayoutParams();
          final LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0, viewLayoutParams != null ? viewLayoutParams.height : LayoutParams.WRAP_CONTENT, 1);
          views[index].setFocusable(true);
          views[index].setClickable(true);
          linearLayout.addView(view, layoutParams);
          index++;
        }
      }
      if (views.length != businessObject.size())
      {
        if (log.isErrorEnabled())
        {
          log.error("Roger, we've got a problem with the SmartRowListAdapter!");
        }
      }
      for (int column = 0; column < views.length; column++)
      {
        final BusinessViewWrapper<BusinessObjectClass> wrapper = businessObject.get(column);
        final View theView = views[column];
        // if (SmartRowListAdapter.DEBUG_LOG_ENABLED == true && log.isDebugEnabled())
        // {
        // log.debug("Updating the cell at position (" + position + "x" + column + ")");
        // }
        wrapper.updateView(activity, layoutInflater, theView, column);
        final int finalIndex = column;
        final boolean isEnabled;
        if (wrapper.isEnabled() == true)
        {
          theView.setOnClickListener(new View.OnClickListener()
          {
            @Override
            public void onClick(View view)
            {
              wrapper.onObjectEvent(activity, theView, ObjectEvent.Clicked, finalIndex);
            }
          });
          isEnabled = true;
        }
        else
        {
          theView.setOnClickListener(null);
          isEnabled = false;
        }
        if (theView.isEnabled() != isEnabled)
        {
          // We only change the "enabled" status provided the current value is not the right one
          theView.setEnabled(isEnabled);
        }
      }
    }

  }

  private static final class RowLinearLayout
      extends LinearLayout
  {

    private static int requestLayoutCount = 0;

    public int position;

    public Boolean isLayoutRequested;

    public RowLinearLayout(Context context, int position)
    {
      super(context);
      this.position = position;
    }

    @Override
    public void requestLayout()
    {
      RowLinearLayout.requestLayoutCount++;
      super.requestLayout();
      if (SmartRowListAdapter.DEBUG_LOG_ENABLED == true && log.isDebugEnabled() == true)
      {
        log.debug("[" + RowLinearLayout.requestLayoutCount + ", position=" + position + "] 'requestLayout()' invoked on '" + this.toString() + "'");
      }
    }

    @Override
    public boolean isLayoutRequested()
    {
      if (isLayoutRequested != null)
      {
        return isLayoutRequested;
      }
      return super.isLayoutRequested();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
      // Overloaded in order to have the opportunity to set break points
      super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

  }

  public static abstract class HorizontalPaddingColumnsIndicator
      implements ColumnsIndicator
  {

    public final int horizontalPadding;

    public HorizontalPaddingColumnsIndicator(int horizontalPadding)
    {
      this.horizontalPadding = horizontalPadding;
    }

    @Override
    public int getHorizontalPaddingForRow(int row)
    {
      return horizontalPadding;
    }

  }

  public static class ConstantColumnsIndicator
      extends HorizontalPaddingColumnsIndicator
  {

    public final int columnsCount;

    public ConstantColumnsIndicator(int horizontalPadding, int columnsCount)
    {
      super(horizontalPadding);
      this.columnsCount = columnsCount;
    }

    @Override
    public int getColumnsCount(int row)
    {
      return columnsCount;
    }

  }

  public static class RowBusinessViewWrapper<BusinessObjectClass>
      extends BusinessViewWrapper<List<BusinessViewWrapper<BusinessObjectClass>>>
  {

    private final int type;

    private final int horizontalPadding;

    private final int position;

    protected RowBusinessViewWrapper(List<BusinessViewWrapper<BusinessObjectClass>> businessObject, int type,
        int horizontalPadding, int position)
    {
      super(businessObject);
      this.type = type;
      this.horizontalPadding = horizontalPadding;
      this.position = position;
    }

    @Override
    public int getType(int position, List<BusinessViewWrapper<BusinessObjectClass>> businessObjectClass)
    {
      return type;
    }

    @Override
    protected View createNewView(Activity activity, LayoutInflater layoutInflater, ViewGroup viewGroup,
        List<BusinessViewWrapper<BusinessObjectClass>> businessViewWrappers)
    {
      final LinearLayout linearLayout = new RowLinearLayout(activity.getApplicationContext(), position);
      linearLayout.setOrientation(LinearLayout.HORIZONTAL);
      return linearLayout;
    }

    @Override
    protected Object extractNewViewAttributes(Activity activity, View view,
        List<BusinessViewWrapper<BusinessObjectClass>> businessObject)
    {
      return new RowBusinessViewAttributes<BusinessObjectClass>((RowLinearLayout) view);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void updateView(Activity activity, LayoutInflater layoutInflater, Object viewAttributes, View view,
        List<BusinessViewWrapper<BusinessObjectClass>> businessObject, int position)
    {
      ((RowBusinessViewAttributes<BusinessObjectClass>) viewAttributes).update(activity, layoutInflater, businessObject, horizontalPadding, position);
    }

  }

  private static final Logger log = LoggerFactory.getInstance("SmartAdapters");

  /**
   * The maximum number of different view types that this component supports.
   */
  private final static int maximumDifferentTypes = 20;

  public static boolean DEBUG_LOG_ENABLED = false;

  public static int getRow(int position, ColumnsIndicator columnsIndicator, AtomicInteger lastRowPosition,
      AtomicInteger lastRowColumnsCount)
  {
    lastRowPosition.set(position);
    int currentPosition = 0;
    int row = 0;
    while (currentPosition < position)
    {
      final int columnsCount = columnsIndicator.getColumnsCount(row);
      currentPosition += columnsCount;
      if ((lastRowPosition.get() - columnsCount) > 0)
      {
        lastRowPosition.set(lastRowPosition.get() - columnsCount);
      }
      lastRowColumnsCount.set(columnsCount);
      row++;
    }
    return row;
  }

  /**
   * @return the individual tags of the provided {@code view} children, which have been wrapped
   */
  public static List<Object> getTags(View view)
  {
    final ArrayList<Object> tags = new ArrayList<>();
    if (view instanceof ViewGroup)
    {
      final ViewGroup viewGroup = (ViewGroup) view;
      for (int index = 0; index < viewGroup.getChildCount(); index++)
      {
        tags.add(viewGroup.getChildAt(index).getTag());
      }
    }
    else
    {
      tags.add(view.getTag());
    }
    return tags;
  }

  /**
   * A map which holds the already computed row types: the key is the actual total type, and the value is the canonical value
   */
  @SuppressLint("UseSparseArrays")
  private final SparseArray<Integer> types = new SparseArray<>();

  public SmartRowListAdapter(Activity activity, LayoutInflater layoutInflater, int viewTypeCount)
  {
    super(activity, layoutInflater, viewTypeCount);
  }

  @Override
  public void setAdapter(ListView listView)
  {
    listView.setItemsCanFocus(true);
    super.setAdapter(listView);
  }

  public List<BusinessViewWrapper<?>> convertWrappers(List<? extends BusinessViewWrapper<?>> wrappers,
      ColumnsIndicator columnsIndicator)
  {
    final List<BusinessViewWrapper<?>> conversiondWrappers = new ArrayList<BusinessViewWrapper<?>>();
    if (wrappers != null && wrappers.size() >= 1)
    {
      int index = 0;
      int row = 0;
      while (true)
      {
        final List<BusinessViewWrapper<?>> rowWrappers = new ArrayList<BusinessViewWrapper<?>>();
        int totalType = 0;
        final int columnsCount = columnsIndicator.getColumnsCount(row);
        for (int column = 0; column < columnsCount; column++)
        {
          final int type;
          if (index >= wrappers.size())
          {
            final BusinessViewWrapper<?> wrapper = createEmptyWrapper(0);
            rowWrappers.add(wrapper);
            type = wrapper.getType(column);
          }
          else
          {
            final BusinessViewWrapper<?> wrapper = wrappers.get(index);
            type = wrapper.getType(column) + 1;
            rowWrappers.add(wrapper);
          }
          totalType += Math.max(1, column * SmartRowListAdapter.maximumDifferentTypes) * type;
          index++;
        }
        Integer canonicalType = types.get(totalType);
        if (canonicalType == null)
        {
          canonicalType = types.size();
          if (log.isDebugEnabled())
          {
            log.debug("Adding the view type " + canonicalType + " corresponding to the computed total type value " + totalType + " at row " + row + " with " + columnsCount + " column(s)");
          }
          types.put(totalType, canonicalType);
        }
        conversiondWrappers.add(getRowBusinessViewWrapper(rowWrappers, canonicalType, columnsIndicator.getHorizontalPaddingForRow(row), conversiondWrappers.size() - 1));
        if (index >= wrappers.size())
        {
          break;
        }
        row++;
      }
    }
    return conversiondWrappers;
  }

  public BusinessViewWrapper<?> createEmptyWrapper(int viewType)
  {
    return new EmptyWrapper(viewType);
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  protected RowBusinessViewWrapper<?> getRowBusinessViewWrapper(List<BusinessViewWrapper<?>> rowWrappers,
      Integer canonicalType, int horizontalPadding, int position)
  {
    return new RowBusinessViewWrapper(rowWrappers, canonicalType, horizontalPadding, position);
  }

}

/* 
 * Copyright (C) 2008-2010 E2M.
 *
 * The code hereby is the private full property of the E2M company, Paris, France.
 * 
 * You have no right to re-use or modify it. There are no open-source, nor free licence
 * attached to it!
 */

package com.smartnsoft.droid4me.ext.widget;

import android.content.Context;
import android.graphics.Camera;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import com.android.internal.R;

/**
 * A gallery which displays its children, which must be {@link ImageView}s in a cover-flow mode.
 * 
 * <p>
 * This is inspired from http://www.inter-fuser.com/2010/01/android-coverflow-widget.html.
 * </p>
 * 
 * @author Édouard Mercier
 * @since 2009.02.15
 */
public class SmartCoverFlow
    extends SmartGallery
{

  /**
   * The maximum angle the child will be rotated by.
   */
  private int maxRotationAngle = 60;

  /**
   * @see #setMaxRotationAngle(int)
   */
  public int getMaxRotationAngle()
  {
    return maxRotationAngle;
  }

  /**
   * Indicates the maximum angle an image of the cover-flow is inclined at most.
   * 
   * <p>
   * The default value is 60 degrees.
   * </p>
   */
  public void setMaxRotationAngle(int maxRotationAngle)
  {
    this.maxRotationAngle = maxRotationAngle;
  }

  /**
   * The maximum zoom on the center child.
   */
  private int maxZoom = -120;

  /**
   * see {@link #setMaxZoom(int)}
   */
  public int getMaxZoom()
  {
    return maxZoom;
  }

  /**
   * Indicates the zoom applied on the selected image of the cover-flow.
   * 
   * <p>
   * The default value is -120.
   * </p>
   */
  public void setMaxZoom(int maxZoom)
  {
    this.maxZoom = maxZoom;
  }

  /**
   * Transform the Image Bitmap by the Angle passed
   * 
   * @param imageView
   *          the image whose bitmap we want to rotate
   * @param offset
   *          the offset from the selected position
   * @param initialLayout
   *          is this a call from an initial layout
   * @param rotationAngle
   *          the angle by which to rotate the bitmap
   */
  private void transformImageBitmap(ImageView imageView, int offset, boolean initialLayout, int rotationAngle)
  {
    if (imageView.getDrawable() == null)
    {
      return;
    }

    final Camera camera = new Camera();
    final Matrix imageMatrix = imageView.getImageMatrix();
    camera.translate(0.0f, 0.0f, 100.0f);

    if (initialLayout == true)
    {
      if (offset < 0)
      {
        camera.rotateY(rotationAngle);
      }
      else if (offset > 0)
      {
        camera.rotateY(-rotationAngle);
      }
      else
      {
        // Just zooms in a little for the central view
        camera.translate(0.0f, 0.0f, maxZoom);
      }
    }
    else
    {
      if (offset == 0)
      {
        // As the angle of the view gets less, zoom in
        int rotation = Math.abs(rotationAngle);
        if (rotation < 30)
        {
          final float zoomAmount = (float) (maxZoom + (rotation * 1.5));
          camera.translate(0.0f, 0.0f, zoomAmount);
        }
        camera.rotateY(rotationAngle);
      }
    }
    camera.getMatrix(imageMatrix);

    if (imageView.getLayoutParams() == null)
    {
      throw new IllegalStateException("The image view should have its layout parameters defined!");
    }
    final int imageHeight = imageView.getLayoutParams().height;
    final int imageWidth = imageView.getLayoutParams().width;
    final int bitMapHeight = imageView.getDrawable().getIntrinsicHeight();
    final int bitMapWidth = imageView.getDrawable().getIntrinsicWidth();
    final float scaleHeight = ((float) imageHeight) / bitMapHeight;
    final float scaleWidth = ((float) imageWidth) / bitMapWidth;

    imageMatrix.preTranslate(-(imageWidth / 2), -(imageHeight / 2));
    imageMatrix.preScale(scaleWidth, scaleHeight);
    imageMatrix.postTranslate((imageWidth / 2), (imageHeight / 2));
  }

  public SmartCoverFlow(Context context)
  {
    this(context, null);
  }

  public SmartCoverFlow(Context context, AttributeSet attrs)
  {
    this(context, attrs, R.attr.galleryStyle);
  }

  public SmartCoverFlow(Context context, AttributeSet attrs, int defStyle)
  {
    super(context, attrs, defStyle);
  }

  @Override
  protected void trackMotionScroll(int deltaX, int deltaY, MotionEvent e1, MotionEvent e2)
  {
    if (getChildCount() == 0)
    {
      return;
    }

    boolean toLeft = deltaX < 0;

    int limitedDeltaX = getLimitedMotionScrollAmount(toLeft, deltaX);
    if (limitedDeltaX != deltaX)
    {
      // The above call returned a limited amount, so stop any scrolls/flings
      mFlingRunnable.endFling(false);
      onFinishedMovement();
    }

    /**
     * Édouard modified: start
     */
    offsetChildrenLeftAndRight(limitedDeltaX, toLeft);
    /**
     * end: Édouard modified
     */

    detachOffScreenChildren(toLeft);

    if (toLeft)
    {
      // If moved left, there will be empty space on the right
      fillToGalleryRight();
    }
    else
    {
      // Similarly, empty space on the left
      fillToGalleryLeft();
    }

    // Clear unused views
    mRecycler.clear();

    setSelectionToCenterChild();

    invalidate();
  }

  /**
   * Offset the horizontal location of all children of this view by the specified number of pixels.
   * 
   * @param offset
   *          the number of pixels to offset
   * @param toLeft
   */
  /**
   * Édouard modified: start
   */
  private void offsetChildrenLeftAndRight(int offset, boolean toLeft)
  {
    /*
     * for (int i = getChildCount() - 1; i >= 0; i--) { getChildAt(i).offsetLeftAndRight(offset); }
     */
    ImageView child;
    int childCount = getChildCount();
    int rotationAngle = 0;
    int childCenter;
    int galleryCenter = getCenterOfGallery();
    float childWidth;

    for (int i = childCount - 1; i >= 0; i--)
    {
      child = (ImageView) getChildAt(i);
      childCenter = getCenterOfView(child);
      childWidth = child.getWidth();
      if (childCenter == galleryCenter)
      {
        transformImageBitmap(child, 0, false, 0);
      }
      else
      {
        rotationAngle = (int) (((float) (galleryCenter - childCenter) / childWidth) * maxRotationAngle);
        if (Math.abs(rotationAngle) > maxRotationAngle)
        {
          rotationAngle = (rotationAngle < 0) ? -maxRotationAngle : maxRotationAngle;
        }
        transformImageBitmap(child, 0, false, rotationAngle);
      }
      child.offsetLeftAndRight(offset);
    }
  }

  /**
   * End ---- Modified by Smart&Soft.
   */

  /**
   * Obtain a view, either by pulling an existing view from the recycler or by getting a new one from the adapter. If we are animating, make sure
   * there is enough information in the view's layout parameters to animate from the old to new positions.
   * 
   * @param position
   *          Position in the gallery for the view to obtain
   * @param offset
   *          Offset from the selected position
   * @param x
   *          X-coordintate indicating where this view should be placed. This will either be the left or right edge of the view, depending on the
   *          fromLeft paramter
   * @param fromLeft
   *          Are we posiitoning views based on the left edge? (i.e., building from left to right)?
   * @return A view that has been added to the gallery
   */
  @Override
  protected View makeAndAddView(int position, int offset, int x, boolean fromLeft)
  {
    // Log.d("SMART", "makeAndAddView(position=" + position +",offset="+offset+",x="+x+",fromLeft=" + fromLeft);
    View child;

    if (!mDataChanged)
    {
      child = mRecycler.get(position);
      if (child != null)
      {
        // Can reuse an existing view
        int childLeft = child.getLeft();

        // Remember left and right edges of where views have been placed
        mRightMost = Math.max(mRightMost, childLeft + child.getMeasuredWidth());
        mLeftMost = Math.min(mLeftMost, childLeft);

        /**
         * Édouard modified: start
         */
        transformImageBitmap((ImageView) child, offset, true, maxRotationAngle);
        /**
         * end: Édouard modified
         */
        // Position the view
        setUpChild(child, offset, x, fromLeft);

        return child;
      }
    }

    // Nothing found in the recycler -- ask the adapter for a view
    child = mAdapter.getView(position, null, this);

    /**
     * Édouard modified: start
     */
    transformImageBitmap((ImageView) child, offset, true, maxRotationAngle);
    /**
     * end: Édouard modified
     */

    // Position the view
    setUpChild(child, offset, x, fromLeft);

    return child;
  }

}

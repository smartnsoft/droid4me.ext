/* 
 * Copyright (C) 2008-2009 E2M.
 *
 * The code hereby is the private full property of the E2M company, Paris, France.
 * 
 * You have no right to re-use or modify it. There are no open-source, nor free licence
 * attached to it!
 */

package com.smartnsoft.droid4me.ext.ui;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageButton;

/**
 * An image button which has an alternate drawing when toggled.
 * 
 * @author Edouard Mercier
 * @date 2008.02.17
 */
public class ToggleImage
    extends ImageButton
{

  public static interface OnToggledListener
  {

    public void onClick(View view, boolean enabled);

  }

  private boolean checked = true;

  protected Drawable checkedImage;

  protected Drawable uncheckedImage;

  private OnToggledListener listener;

  public ToggleImage(Context context)
  {
    super(context);
    initialize(context, null);
  }

  public ToggleImage(Context context, AttributeSet attrs, int defStyle)
  {
    super(context, attrs, defStyle);
    initialize(context, attrs);
  }

  public ToggleImage(Context context, AttributeSet attrs)
  {
    super(context, attrs);
    initialize(context, attrs);
  }

  private void initialize(Context context, AttributeSet attrs)
  {
    // if (attrs != null)
    // {
    // TypedArray styledAttributes = context.obtainStyledAttributes(attrs, R.styleable.ToggleImage);
    // checkedImage = styledAttributes.getDrawable(R.styleable.ToggleImage_enabledImage);
    // uncheckedImage = styledAttributes.getDrawable(R.styleable.ToggleImage_disabledImage);
    // styledAttributes.recycle();
    //    }
    setImageDrawable(checkedImage);
    setOnClickListener(new OnClickListener()
    {
      public void onClick(View view)
      {
        checked = !checked;
        computeImage();
        if (listener != null)
        {
          listener.onClick(view, checked);
        }
      }
    });
  }

  public void setOnToggledListener(OnToggledListener listener)
  {
    this.listener = listener;
  }

  private void computeImage()
  {
    if (checked)
    {
      setImageDrawable(checkedImage);
    }
    else
    {
      setImageDrawable(uncheckedImage);
    }
  }

  public void setChecked(boolean checked)
  {
    this.checked = checked;
    computeImage();
  }

  public void setCheckedImageDrawable(Drawable drawable)
  {
    this.checkedImage = drawable;
    if (checked == true)
    {
      setImageDrawable(checkedImage);
    }
  }

  public void setUncheckedImageDrawable(Drawable drawable)
  {
    this.uncheckedImage = drawable;
    if (checked == false)
    {
      setImageDrawable(uncheckedImage);
    }
  }

  public boolean isChecked()
  {
    return checked;
  }

}

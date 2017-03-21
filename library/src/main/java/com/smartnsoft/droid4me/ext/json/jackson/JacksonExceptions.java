package com.smartnsoft.droid4me.ext.json.jackson;

import com.smartnsoft.droid4me.ws.WebServiceClient.CallException;

/**
 * @author Édouard Mercier, Ludovic Roland
 * @since 2016.01.29
 */
public abstract class JacksonExceptions
{

  public static class JacksonParsingException
      extends CallException
  {

    private static final long serialVersionUID = 1L;

    public JacksonParsingException(Throwable throwable)
    {
      super(throwable);
    }

    public JacksonParsingException(String message, int code)
    {
      super(message, code);
    }

    public JacksonParsingException(String message, Throwable throwable, int code)
    {
      super(message, throwable, code);
    }

    public JacksonParsingException(String message, Throwable throwable)
    {
      super(message, throwable);
    }

    public JacksonParsingException(String message)
    {
      super(message);
    }

    public JacksonParsingException(Throwable message, int code)
    {
      super(message, code);
    }

  }

  public static final class JacksonJsonParsingException
      extends JacksonParsingException
  {

    private static final long serialVersionUID = 1L;

    protected JacksonJsonParsingException(Throwable throwable)
    {
      super(throwable);
    }

  }

}

package com.smartnsoft.droid4me.ext.ws;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.zip.GZIPInputStream;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.smartnsoft.droid4me.ws.WebServiceCaller;

/**
 * A web service which is supported by Jackson.
 * 
 * @author Édouard Mercier
 * @since 2013.07.21
 */
public abstract class JacksonWebServiceCaller
    extends WebServiceCaller
{

  protected static class JacksonParsingException
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

  protected final static class JacksonJsonParsingException
      extends JacksonParsingException
  {

    private static final long serialVersionUID = 1L;

    protected JacksonJsonParsingException(Throwable throwable)
    {
      super(throwable);
    }

  }

  protected static final class WebServiceCallerSSLSocketFactory
      extends SSLSocketFactory
  {

    private final SSLContext sslContext;

    public static HttpClient sslClientWrapper(HttpClient client)
    {
      try
      {
        final X509TrustManager trustManager = new X509TrustManager()
        {
          @Override
          public void checkClientTrusted(X509Certificate[] xcs, String string)
              throws CertificateException
          {
          }

          @Override
          public void checkServerTrusted(X509Certificate[] xcs, String string)
              throws CertificateException
          {
          }

          @Override
          public X509Certificate[] getAcceptedIssuers()
          {
            return null;
          }
        };
        final SSLContext context = SSLContext.getInstance("TLS");
        context.init(null, new TrustManager[] { trustManager }, null);
        final SSLSocketFactory socketFactory = new WebServiceCallerSSLSocketFactory(context);
        socketFactory.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        final ClientConnectionManager clientConnectionManager = client.getConnectionManager();
        final SchemeRegistry schemeRegistry = clientConnectionManager.getSchemeRegistry();
        schemeRegistry.register(new Scheme("https", socketFactory, 443));
        return new DefaultHttpClient(clientConnectionManager, client.getParams());
      }
      catch (Exception exception)
      {
        if (log.isErrorEnabled())
        {
          log.error("Cannot create a SSL client", exception);
        }
        return null;
      }
    }

    public WebServiceCallerSSLSocketFactory(KeyStore truststore)
        throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException, UnrecoverableKeyException
    {
      super(truststore);
      sslContext = SSLContext.getInstance("TLS");

      final TrustManager tm = new X509TrustManager()
      {
        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType)
            throws CertificateException
        {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType)
            throws CertificateException
        {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers()
        {
          return null;
        }
      };

      sslContext.init(null, new TrustManager[] { tm }, null);
    }

    public WebServiceCallerSSLSocketFactory(SSLContext context)
        throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException, UnrecoverableKeyException
    {
      super(null);
      sslContext = context;
    }

    @Override
    public Socket createSocket(Socket socket, String host, int port, boolean autoClose)
        throws IOException, UnknownHostException
    {
      return sslContext.getSocketFactory().createSocket(socket, host, port, autoClose);
    }

    @Override
    public Socket createSocket()
        throws IOException
    {
      return sslContext.getSocketFactory().createSocket();
    }

  }

  private ObjectMapper objectMapper;

  private final int connectionTimeOutInMilliseconds;

  private final int socketTimeOutInMilliseconds;

  private final boolean acceptGzip;

  protected JacksonWebServiceCaller(int connectionTimeOutInMilliseconds, int socketTimeOutInMilliseconds, boolean acceptGzip)
  {
    this.connectionTimeOutInMilliseconds = connectionTimeOutInMilliseconds;
    this.socketTimeOutInMilliseconds = socketTimeOutInMilliseconds;
    this.acceptGzip = acceptGzip;
  }

  @Override
  protected HttpClient computeHttpClient()
  {
    final HttpClient httpClient = super.computeHttpClient();
    final HttpParams httpParams = httpClient.getParams();
    HttpConnectionParams.setConnectionTimeout(httpParams, connectionTimeOutInMilliseconds);
    HttpConnectionParams.setSoTimeout(httpParams, socketTimeOutInMilliseconds);
    if (acceptGzip == true)
    {
      // We ask for the compressed flow
      httpParams.setParameter("Accept-Encoding", "gzip");
    }
    return httpClient;
  }

  @Override
  protected InputStream getContent(String uri, CallType callType, HttpResponse response)
      throws IOException
  {
    // Handles the compressed flow
    final Header[] contentEncodingHeaders = response.getHeaders(HTTP.CONTENT_ENCODING);
    if (contentEncodingHeaders != null && contentEncodingHeaders.length >= 1)
    {
      if (contentEncodingHeaders[0].getValue().equals("gzip") == true)
      {
        final InputStream inputStream = response.getEntity().getContent();
        final GZIPInputStream gzipInputStream = new GZIPInputStream(inputStream);
        return gzipInputStream;
      }
    }
    return super.getContent(uri, callType, response);
  }

  /**
   * Responsible for creating the Jackson object mapper.
   * 
   * @return a valid object mapper, which can be customized
   */
  protected ObjectMapper computeObjectMapper()
  {
    final ObjectMapper theObjectMapper = new ObjectMapper();
    // We indicate to the parser not to fail in case of unknown properties, for backward compatibility reasons
    // See http://stackoverflow.com/questions/6300311/java-jackson-org-codehaus-jackson-map-exc-unrecognizedpropertyexception
    theObjectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    theObjectMapper.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, true);
    return theObjectMapper;
  }

  protected final ObjectMapper getObjectMapper()
  {
    prepareObjectMapper();
    return objectMapper;
  }

  public final <ContentType> String serializeJson(ContentType businessObject)
      throws JsonProcessingException
  {
    final String jsonString;
    jsonString = objectMapper.writeValueAsString(businessObject);
    if (log.isDebugEnabled())
    {
      log.debug("Converted the object with class name '" + businessObject.getClass().getSimpleName() + "' to the JSON string '" + jsonString + "'");
    }
    return jsonString;
  }

  public final <ContentType> ContentType deserializeJson(String jsonString, Class<?> valueType)
      throws IOException
  {
    prepareObjectMapper();
    @SuppressWarnings("unchecked")
    final ContentType businessObject = (ContentType) objectMapper.readValue(jsonString, valueType);
    return businessObject;
  }

  // This is done this way, because of a compilation issue on Linux (see
  // http://stackoverflow.com/questions/5666027/why-does-the-compiler-state-no-unique-maximal-instance-exists)
  @SuppressWarnings("unchecked")
  public final <ContentType> ContentType deserializeJson(InputStream inputStream, Class<?> theClass)
      throws JacksonParsingException
  {
    return (ContentType) deserializeJson(inputStream, null, theClass);
  }

  // This is done this way, because of a compilation issue on Linux (see
  // http://stackoverflow.com/questions/5666027/why-does-the-compiler-state-no-unique-maximal-instance-exists)
  @SuppressWarnings("unchecked")
  public final <ContentType> ContentType deserializeJson(InputStream inputStream, TypeReference<?> typeReference)
      throws JacksonParsingException
  {
    return (ContentType) deserializeJson(inputStream, typeReference, null);
  }

  @SuppressWarnings("unchecked")
  protected <ContentType> ContentType deserializeJson(InputStream inputStream, TypeReference<?> typeReference, Class<?> theClass)
      throws JacksonParsingException
  {
    prepareObjectMapper();
    try
    {
      if (theClass != null)
      {
        return (ContentType) objectMapper.readValue(inputStream, theClass);
      }
      else
      {
        return (ContentType) objectMapper.readValue(inputStream, typeReference);
      }
    }
    catch (JsonMappingException exception)
    {
      throw new JacksonJsonParsingException(exception);
    }
    catch (Exception exception)
    {
      throw new JacksonParsingException(exception);
    }
  }

  private void prepareObjectMapper()
  {
    if (objectMapper == null)
    {
      objectMapper = computeObjectMapper();
    }
  }

}

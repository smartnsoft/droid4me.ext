/*
 * (C) Copyright 2009-2013 Smart&Soft SAS (http://www.smartnsoft.com/) and contributors.
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

package com.smartnsoft.droid4me.ext.ws;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
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

import com.smartnsoft.droid4me.ext.json.jackson.JacksonParser;
import com.smartnsoft.droid4me.ext.json.jackson.ObjectMapperComputer;
import com.smartnsoft.droid4me.ws.HttpClientWebServiceCaller;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
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

/**
 * A web service which is supported by Jackson.
 *
 * @author Édouard Mercier
 * @since 2013.07.21
 */
@Deprecated
public abstract class JacksonHttpClientWebServiceCaller
    extends HttpClientWebServiceCaller
    implements ObjectMapperComputer
{

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
        throws IOException
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

  protected final JacksonParser jacksonParser;

  private final int connectionTimeOutInMilliseconds;

  private final int socketTimeOutInMilliseconds;

  private final boolean acceptGzip;

  protected JacksonHttpClientWebServiceCaller(int connectionTimeOutInMilliseconds, int socketTimeOutInMilliseconds,
      boolean acceptGzip)
  {
    this.jacksonParser = new JacksonParser(this);
    this.connectionTimeOutInMilliseconds = connectionTimeOutInMilliseconds;
    this.socketTimeOutInMilliseconds = socketTimeOutInMilliseconds;
    this.acceptGzip = acceptGzip;
  }

  @Override
  public ObjectMapper computeObjectMapper()
  {
    final ObjectMapper theObjectMapper = new ObjectMapper();
    // We indicate to the parser not to fail in case of unknown properties, for backward compatibility reasons
    // See http://stackoverflow.com/questions/6300311/java-jackson-org-codehaus-jackson-map-exc-unrecognizedpropertyexception
    theObjectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    theObjectMapper.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, true);
    return theObjectMapper;
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

}

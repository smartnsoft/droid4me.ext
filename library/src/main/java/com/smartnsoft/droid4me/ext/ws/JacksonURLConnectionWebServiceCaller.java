/*
 * (C) Copyright 2009-2016 Smart&Soft SAS (http://www.smartnsoft.com/) and contributors.
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

import com.smartnsoft.droid4me.ext.json.jackson.JacksonParser;
import com.smartnsoft.droid4me.ext.json.jackson.ObjectMapperComputer;
import com.smartnsoft.droid4me.ws.URLConnectionWebServiceCaller;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * A web service which is supported by Jackson.
 *
 * @author Ludovic Roland
 * @since 2016.01.29
 */
public abstract class JacksonURLConnectionWebServiceCaller
    extends URLConnectionWebServiceCaller
    implements ObjectMapperComputer
{

  public final JacksonParser jacksonParser;

  protected JacksonURLConnectionWebServiceCaller(int readTimeOutInMilliseconds, int connectTimeOutInMilliseconds,
      boolean acceptGzip)
  {
    super(readTimeOutInMilliseconds, connectTimeOutInMilliseconds, acceptGzip);
    this.jacksonParser = new JacksonParser(this);
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

}

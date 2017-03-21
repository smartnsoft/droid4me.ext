package com.smartnsoft.droid4me.ext.json.jackson;

import java.io.IOException;
import java.io.InputStream;

import com.smartnsoft.droid4me.ext.json.jackson.JacksonExceptions.JacksonJsonParsingException;
import com.smartnsoft.droid4me.ext.json.jackson.JacksonExceptions.JacksonParsingException;
import com.smartnsoft.droid4me.log.Logger;
import com.smartnsoft.droid4me.log.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Ludovic Roland
 * @since 2016.01.29
 */
public final class JacksonParser
{

  private static final Logger log = LoggerFactory.getInstance(JacksonParser.class);

  private ObjectMapper objectMapper;

  private ObjectMapperComputer objectMapperComputer;

  public JacksonParser(ObjectMapperComputer objectMapperComputer)
  {
    this.objectMapperComputer = objectMapperComputer;
  }

  public final ObjectMapper getObjectMapper()
  {
    prepareObjectMapper();
    return objectMapper;
  }

  public final <ContentType> String serializeJson(ContentType businessObject)
      throws JsonProcessingException
  {

    prepareObjectMapper();

    final String jsonString = objectMapper.writeValueAsString(businessObject);

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
    @SuppressWarnings("unchecked") final ContentType businessObject = (ContentType) objectMapper.readValue(jsonString, valueType);
    return businessObject;
  }

  // This is done this way, because of a compilation issue on Linux (see
  // http://stackoverflow.com/questions/5666027/why-does-the-compiler-state-no-unique-maximal-instance-exists)
  @SuppressWarnings("unchecked")
  public final <ContentType> ContentType deserializeJson(InputStream inputStream, Class<?> theClass)
      throws JacksonParsingException
  {
    return (ContentType) deserializeJson(inputStream, null, theClass, null);
  }

  // This is done this way, because of a compilation issue on Linux (see
  // http://stackoverflow.com/questions/5666027/why-does-the-compiler-state-no-unique-maximal-instance-exists)
  @SuppressWarnings("unchecked")
  public final <ContentType> ContentType deserializeJson(InputStream inputStream, TypeReference<?> typeReference)
      throws JacksonParsingException
  {
    return (ContentType) deserializeJson(inputStream, typeReference, null, null);
  }

  // This is done this way, because of a compilation issue on Linux (see
  // http://stackoverflow.com/questions/5666027/why-does-the-compiler-state-no-unique-maximal-instance-exists)
  @SuppressWarnings("unchecked")
  public final <ContentType> ContentType deserializeJson(InputStream inputStream, JavaType javaType)
      throws JacksonParsingException
  {
    return (ContentType) deserializeJson(inputStream, null, null, javaType);
  }

  @SuppressWarnings("unchecked")
  public <ContentType> ContentType deserializeJson(InputStream inputStream, TypeReference<?> typeReference,
      Class<?> theClass, JavaType javaType)
      throws JacksonParsingException
  {
    prepareObjectMapper();
    try
    {
      if (theClass != null)
      {
        return (ContentType) objectMapper.readValue(inputStream, theClass);
      }
      else if (javaType != null)
      {
        return (ContentType) objectMapper.readValue(inputStream, javaType);
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
      objectMapper = objectMapperComputer.computeObjectMapper();
    }
  }

}

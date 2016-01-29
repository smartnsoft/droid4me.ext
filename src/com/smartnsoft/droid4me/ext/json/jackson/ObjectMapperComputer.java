package com.smartnsoft.droid4me.ext.json.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Ludovic Roland
 * @since 2016.01.29
 */
public interface ObjectMapperComputer
{

  /**
   * Responsible for creating the Jackson object mapper.
   *
   * @return a valid object mapper, which can be customized
   */
  ObjectMapper computeObjectMapper();

}

package com.rsicms.rsuite.utils.operation;

import java.io.IOException;

import com.rsicms.rsuite.utils.messsageProps.LibraryMessageProperties;

/**
 * Serves up formatted messages from messages.properties.
 */
public class OperationMessageProperties extends LibraryMessageProperties {

  public OperationMessageProperties() throws IOException {
    super(OperationMessageProperties.class);
  }

}

package com.rsicms.rsuite.utils.operation.options;

import org.apache.commons.logging.Log;

public class OperationOptions {

  private Log log;

  public OperationOptions(
      Log log) {
    this.log = log;
  }

  public Log getLog() {
    return log;
  }

  /**
   * @param defaultLog
   * @return This instance's log when not null, else the given log is returned.
   */
  public Log getLog(
      Log defaultLog) {
    return (log == null ? defaultLog : log);
  }

}

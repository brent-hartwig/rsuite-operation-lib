package com.rsicms.rsuite.utils.operation.log;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;

/**
 * Logs operation results in a consistent manner, in order to facilitate grep.
 *
 */
public class OperationLogger {

  /**
   * The log to write messages to in real-time.
   */
  private Log log;

  /**
   * The operation ID.
   */
  private String opId;

  /**
   * @param log the Log
   */
  public OperationLogger(
      Log log) {
    this.log = log;
  }

  /**
   * @param log the Log
   * @param opId the Operation Id
   */
  public OperationLogger(
      Log log,
      String opId) {
    this.log = log;
    this.opId = opId;
  }

  public Log getLog() {
    return log;
  }

  public void setLog(
      Log log) {
    this.log = log;
  }

  public String getOpId() {
    return opId;
  }

  public void setOpId(
      String opId) {
    this.opId = opId;
  }

  /**
   * Logs a warning message
   * 
   * @param message message to log
   * @param t Exception object
   */
  public void warn(
      String message,
      Throwable t) {
    if (log != null) {
      log.warn(
          buildLogMessage(message),
          t);
    }
  }

  /**
   * Logs an error message
   * 
   * @param message message to log
   * @param t Exception object
   */
  public void error(
      String message,
      Throwable t) {
    if (log != null) {
      log.error(
          buildLogMessage(message),
          t);
    }
  }

  /**
   * Logs an info message
   * 
   * @param message message to log
   * @param t Throwable object
   */
  public void info(
      String message,
      Throwable t) {
    if (log != null) {
      if (t != null) {
        log.info(
            buildLogMessage(message),
            t);
      } else {
        log.info(buildLogMessage(message));
      }
    }
  }

  /**
   * Logs a debug message
   * 
   * @param message message to log
   * @param t Throwable object
   */
  public void debug(
      String message,
      Throwable t) {
    if (log != null && log.isDebugEnabled()) {
      if (t != null) {
        log.debug(
            buildLogMessage(message),
            t);
      } else {
        log.debug(buildLogMessage(message));
      }
    }
  }

  /**
   * Builds the log message, prepending the operation Id whenever applicable.
   * 
   * @param message the message to log.
   * @return the log message.
   */
  private String buildLogMessage(
      String message) {
    if (StringUtils.isNotBlank(opId)) {
      return new StringBuilder("[").append(
          getOpId()).append(
          "] - ").append(
          message).toString();
    }
    return message;
  }

}

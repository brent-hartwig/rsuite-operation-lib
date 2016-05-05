package com.rsicms.rsuite.utils.operation.result;

import org.apache.commons.logging.Log;

import com.rsicms.rsuite.utils.operation.status.OperationStatus;

/**
 * An operation result that has counters for each <code>OperationStatus</code> enum value.
 */
public class OperationStatusOperationResult
    extends BaseOperationResult {

  public OperationStatusOperationResult(
      String id,
      String defaultLabel,
      Log log) {
    super(id, defaultLabel, log);
  }

  public int getCount(
      OperationStatus status) {
    return getCount(status.getCounterName());
  }

  public void incrementCount(
      OperationStatus status) {
    incrementCount(
        status,
        1);
  }

  public void incrementCount(
      OperationStatus status,
      int cnt) {
    incrementCount(
        status.getCounterName(),
        cnt);
  }

}

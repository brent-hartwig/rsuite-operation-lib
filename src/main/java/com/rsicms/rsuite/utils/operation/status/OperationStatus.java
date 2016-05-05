package com.rsicms.rsuite.utils.operation.status;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.reallysi.rsuite.api.RSuiteException;
import com.rsicms.rsuite.utils.operation.OperationMessageProperties;


/**
 * All known statuses for article operations, such as migration and delivery.
 */
public enum OperationStatus {
  NONE(false, false, "none", "None"), QUEUED(true, true, "queued", "Queued"), IN_PROGRESS(true,
      true, "in-progress", "In-Progress"), SUCCESSFUL(true, false, "successful", "Successful"), FAILED(
      true, false, "failed", "Failed"), ABORTED(true, false, "aborted", "Aborted"), SKIPPED(false,
      false, "skipped", "Skipped"), EXCLUDED(true, false, "excluded", "Excluded");

  private boolean allowSet;
  private boolean outstanding;
  private String counterName;
  private String displayName;

  private OperationStatus(
      boolean allowSet,
      boolean outstanding,
      String counterName,
      String displayName) {
    this.allowSet = allowSet;
    this.outstanding = outstanding;
    this.counterName = counterName;
    this.displayName = displayName;
  }

  /**
   * Find out if this enum value may be persisted to the data layer.
   * 
   * @return True if enum value may be persisted to the data layer.
   */
  public boolean allowSet() {
    return allowSet;
  }

  /**
   * Find out if this enum value reflects the operation is outstanding.
   * 
   * @return True if enum value reflect the operation is outstanding.
   */
  public boolean isOutstanding() {
    return outstanding;
  }

  /**
   * Get the counter name to use for this enum value.
   * 
   * @return The counter name to use for this enum value.
   */
  public String getCounterName() {
    return counterName;
  }

  /**
   * @return the operation status' display value
   */
  public String getDisplayName() {
    return displayName;
  }

  /**
   * @return All of the operation statuses that may be persisted in the data layer.
   */
  public static List<OperationStatus> getPersistableValues() {
    List<OperationStatus> vals = new ArrayList<OperationStatus>();
    for (OperationStatus val : OperationStatus.values()) {
      if (val.allowSet()) {
        vals.add(val);
      }
    }
    return vals;
  }

  /**
   * Get an enum value by name.
   * 
   * @param name
   * @param throwIfInvalid When true and the given operation status name isn't valid, an exception
   *        will be thrown. Else, when invalid, null is returned.
   * @return The enum value associated to the given name
   * @throws RSuiteException Thrown if the operation status name isn't valid and throwIfInvalid is
   *         true.
   */
  public static OperationStatus get(
      String name,
      boolean throwIfInvalid)
      throws RSuiteException {
    if (StringUtils.isNotBlank(name)) {
      for (OperationStatus val : OperationStatus.values()) {
        if (val.name().equalsIgnoreCase(
            name)) {
          return val;
        }
      }
    }
    if (throwIfInvalid) {
      throw new RSuiteException(RSuiteException.ERROR_PARAM_INVALID,
          OperationMessageProperties.get(
              "operation.status.error.unknown.status",
              name));
    }
    return null;
  }

  /**
   * Get a list of enum values by names. De-dups list.
   * 
   * @param names
   * @param throwIfInvalid
   * @return A list of enum values
   * @throws RSuiteException Thrown if the operation status name isn't valid and throwIfInvalid is
   *         true.
   */
  public static List<OperationStatus> getList(
      List<String> names,
      boolean throwIfInvalid)
      throws RSuiteException {
    List<OperationStatus> statuses = new ArrayList<OperationStatus>();
    if (names != null) {
      for (String name : names) {
        OperationStatus status = get(
            name,
            throwIfInvalid);
        if (status != null && !statuses.contains(status))
          statuses.add(status);
      }
    }
    return statuses;
  }

  /**
   * Get the names of the given operation status enum values.
   * 
   * @param statuses
   * @return The names of the given operation status enum values.
   */
  public static List<String> getNames(
      List<OperationStatus> statuses) {
    List<String> names = new ArrayList<String>();
    if (statuses != null) {
      for (OperationStatus status : statuses) {
        names.add(status.name());
      }
    }
    return names;
  }
}

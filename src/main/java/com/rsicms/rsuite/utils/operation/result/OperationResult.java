package com.rsicms.rsuite.utils.operation.result;

import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;

import com.reallysi.rsuite.api.ContentAssembly;
import com.reallysi.rsuite.api.ManagedObject;
import com.reallysi.rsuite.api.RSuiteException;
import com.reallysi.rsuite.api.User;
import com.reallysi.rsuite.api.extensions.ExecutionContext;
import com.reallysi.rsuite.api.workflow.ProcessInstanceSummaryInfo;
import com.rsicms.rsuite.helpers.messages.ProcessFailureMessage;
import com.rsicms.rsuite.helpers.messages.ProcessInfoMessage;
import com.rsicms.rsuite.helpers.messages.ProcessMessage;
import com.rsicms.rsuite.helpers.messages.ProcessWarningMessage;
import com.rsicms.rsuite.utils.operation.Transaction;


public interface OperationResult {

  String getDefaultLabel();

  void setLog(Log log);

  Log getLog();

  String getOperationId();

  void setOperationId(String id);

  void markStartOfOperation();

  void setStartOfOperation(Date start);

  Date getStartOfOperation();

  void markEndOfOperation();

  void setEndOfOperation(Date start);

  Date getEndOfOperation();

  /**
   * Find out how long the operation took, in milliseconds.
   * 
   * @see #markStartOfOperation()
   * @see #setStartOfOperation(Date)
   * @see #markEndOfOperation()
   * @see #getOperationDurationInSeconds()
   * @return difference between start and end, in milliseconds.
   * @throws RSuiteException Thrown if the start or end of the operation was not specified.
   */
  long getOperationDurationInMilliseconds() throws RSuiteException;

  /**
   * Get the operation's duration in milliseconds, suppressing any exceptions.
   * 
   * @return the operation's duration or -1 when unknown.
   */
  long getOperationDurationInMillisecondsQuietly();

  /**
   * Find out how long the operation took, in seconds.
   * 
   * @see #markStartOfOperation()
   * @see #setStartOfOperation(Date)
   * @see #markEndOfOperation()
   * @see #getOperationDurationInMilliseconds()
   * @return difference between start and end, in seconds.
   * @throws RSuiteException Thrown if the start or end of the operation was not specified.
   */
  long getOperationDurationInSeconds() throws RSuiteException;

  /**
   * Get the operation's duration in seconds, suppressing any exceptions.
   * 
   * @return the operation's duration or -1 when unknown.
   */
  long getOperationDurationInSecondsQuietly();

  /**
   * Start a named timer.
   * 
   * @param name Name of timer.
   */
  void startTimer(String name);

  /**
   * Get the elapsed milliseconds of a named timer.
   * 
   * @param name Name of timer.
   * @return The number of elapsed milliseconds or -1 when timer wasn't started.
   */
  long getElapsedTimeInMilliseconds(String name);

  /**
   * Get the elapsed seconds of a named timer.
   * 
   * @param name Name of timer.
   * @return The number of elapsed seconds or -1 when timer wasn't started.
   */
  long getElapsedTimeInSeconds(String name);

  void addWorkflowJob(ProcessInstanceSummaryInfo job);

  List<ProcessInstanceSummaryInfo> getWorkflowJobs();

  void addFailure(Throwable t);

  void addFailure(String label, Throwable t);

  void addWarning(Throwable t);

  void addWarning(String label, Throwable t);

  void addInfoMessage(String message);

  void addInfoMessage(String label, String message);

  void addInfoMessage(String message, Throwable t);

  void addInfoMessage(String label, String message, Throwable t);

  void addDebugMessage(String message);

  void addDebugMessage(String label, String message);

  void addDebugMessage(String message, Throwable t);

  void addDebugMessage(String label, String message, Throwable t);

  /**
   * Find out if there are more than the specified number of failures.
   * <p>
   * This can be handy to know if a subprocess incurred failures, when the starting number of
   * failures is known.
   * 
   * @param cnt The number of failures to compare against.
   * @return True if this operation result has more failures than the provided number.
   */
  boolean hasMoreThanFailureCount(int cnt);

  /**
   * Get the total number of failures for this operation.
   * 
   * @return The total number of failures for this operation.
   */
  int getFailureCount();

  /**
   * Get the total number of warnings for this operation.
   * 
   * @return The total number of warnings for this operation.
   */
  int getWarningCount();

  /**
   * Get the total number of information messages for this operation.
   * 
   * @return The total number of information messages for this operation.
   */
  int getInfoCount();

  /**
   * Find out if there were any failures or warnings.
   * 
   * @return True if there are no failures or warnings
   */
  boolean isFailureAndWarningFree();

  boolean hasFailures();

  boolean hasWarnings();

  List<ProcessFailureMessage> getFailureMessages();

  List<ProcessWarningMessage> getWarningMessages();

  List<ProcessInfoMessage> getInfoMessages();

  List<ProcessMessage> getAllMessages();

  int getCount(String name);

  void incrementCount(String name);

  void incrementCount(String name, int cnt);

  List<String> getCounterNames();

  void incrementManagedObjectCreatedCount();

  int getManagedObjectCreatedCount();

  void incrementManagedObjectUpdatedCount();

  int getManagedObjectUpdatedCount();

  void incrementNewManagedObjectsRolledBackCount();

  int getNewManagedObjectsRolledBackCount();

  void incrementUpdatedManagedObjectsRolledBackCount();

  int getUpdatedManagedObjectsRolledBackCount();

  void incrementWorkflowJobsCount();

  int getWorkflowJobsCount();

  void incrementSkippedManagedObjectCount();

  int getSkippedManagedObjectCount();

  /**
   * Start a new transaction.
   * 
   * @return the transaction ID.
   */
  int startTransaction();

  /**
   * Get the current transaction, creating one if necessary.
   * 
   * @return current transaction
   */
  Transaction getCurrentTransaction();

  /**
   * Associate a <b>new</b> MO with the current transaction.
   * <p>
   * New versus updated is a very important distinction when it comes to rollback!
   * 
   * @param moId
   * @param assetName
   */
  void addNewAsset(String moId, String assetName);

  /**
   * Associate an <b>updated</b> MO to the current transaction.
   * <p>
   * New versus updated is a very important distinction when it comes to rollback!
   * 
   * @param moId
   * @param assetName
   */
  void addUpdatedAsset(String moId, String assetName);

  /**
   * Rollback all supported changes known by the current transaction.
   * 
   * @param context
   * @param user
   * @param result
   */
  void rollbackCurrentTransaction(ExecutionContext context, User user, OperationResult result);

  /**
   * Get all transactions associated with this operation. Tranactions provide lists of new and
   * updated MOs, as well as provide the ability to rollback those edits.
   * 
   * @return Zero or more transactions.
   */
  List<Transaction> getTransactions();

  /**
   * @return A list of <code>ContentAssembly</code> instances destroyed by this operation.
   */
  List<ContentAssembly> getDestroyedContentAssemblies();

  /**
   * @param destroyedContentAssemblyList List of <code>ContentAssembly</code> instances destroyed by
   *        this operation.
   */
  void setDestroyedContentAssemblies(List<ContentAssembly> destroyedContentAssemblyList);

  /**
   * @return A list of <code>ManagedObject</code> instances destroyed by this operation.
   */
  List<ManagedObject> getDestroyedManagedObjects();

  /**
   * @param destroyedManagedObjectList List of <code>ManagedObject</code> instances destroyed by
   *        this operation.
   */
  void setDestroyedManagedObjects(List<ManagedObject> destroyedManagedObjectList);

  /**
   * Set the operation's payload, and content type thereof.
   * 
   * @param payload
   * @param contentType
   */
  void setPayload(String payload, String contentType);

  /**
   * @return the operation's payload, if it has one.
   */
  String getPayload();

  /**
   * @return True if this operation provided a payload.
   */
  boolean hasPayload();

  /**
   * @return The payload's content type.
   */
  String getPayloadContentType();

  /**
   * @param subResult The sub-result to add to this result.
   */
  void addSubResult(BaseOperationResult subResult);

  /**
   * @return An "executive summary" of the operation which was introduced as part of email subjects.
   */
  String getExecutiveSummary();

  /**
   * @return The messages as an HTML-formatted report.
   */
  String getHtmlFormattedMessages();

  /**
   * Find out if {@link #conditionallyUnwrapThrowable(Throwable)} can upwrap the provided throwable.
   * 
   * @param t
   * @return True if the throwable can/should be unwrapped.
   */
  boolean canUnwrapThrowable(Throwable t);

  /**
   * Unwrap the given throwable when it is needlessly wrapped. The DeltaXML Merge integration wraps
   * various exceptions with InvocationTargetException and ReflectException. This method is able to
   * get to the underlying and insightful exception.
   * 
   * @param t
   * @return Either the provided throwable, or one that it wraps.
   */
  Throwable conditionallyUnwrapThrowable(Throwable t);

}

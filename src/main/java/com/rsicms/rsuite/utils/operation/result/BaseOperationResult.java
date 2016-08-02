package com.rsicms.rsuite.utils.operation.result;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import com.reallysi.rsuite.api.ContentAssembly;
import com.reallysi.rsuite.api.ManagedObject;
import com.reallysi.rsuite.api.RSuiteException;
import com.reallysi.rsuite.api.User;
import com.reallysi.rsuite.api.extensions.ExecutionContext;
import com.reallysi.rsuite.api.workflow.ProcessInstanceSummaryInfo;
import com.rsicms.rsuite.helpers.messages.ProcessDebugMessage;
import com.rsicms.rsuite.helpers.messages.ProcessFailureMessage;
import com.rsicms.rsuite.helpers.messages.ProcessInfoMessage;
import com.rsicms.rsuite.helpers.messages.ProcessMessage;
import com.rsicms.rsuite.helpers.messages.ProcessWarningMessage;
import com.rsicms.rsuite.helpers.messages.Severity;
import com.rsicms.rsuite.helpers.messages.impl.GenericProcessDebugMessage;
import com.rsicms.rsuite.helpers.messages.impl.GenericProcessFailureMessage;
import com.rsicms.rsuite.helpers.messages.impl.GenericProcessInfoMessage;
import com.rsicms.rsuite.helpers.messages.impl.GenericProcessWarningMessage;
import com.rsicms.rsuite.helpers.messages.impl.ProcessMessageContainerImpl;
import com.rsicms.rsuite.utils.operation.Transaction;
import com.rsicms.rsuite.utils.operation.log.OperationLogger;
import com.rsicms.rsuite.utils.operation.visitor.HtmlFormattingOperationResultVisitor;

/**
 * Base class for various operations that want to get track of the operation's duration, messages,
 * and counters.
 */
public class BaseOperationResult implements OperationResult {

  /**
   * Name of counter: number of new MOs
   */
  private final static String COUNTER_NAME_MOS_CREATED = "mosCreated";

  /**
   * Name of counter: number of updated MOs
   */
  private final static String COUNTER_NAME_MOS_UPDATED = "mosUpdated";

  /**
   * Name of counter: number of skipped MOs
   */
  private final static String COUNTER_NAME_MOS_SKIPPED = "mosSkipped";

  /**
   * Name of counter: number of new MOs destroyed by rollback
   */
  private final static String COUNTER_NAME_NEW_MOS_ROLLED_BACK = "newMosRolledBack";

  /**
   * Name of counter: number of updated MOs reverted by rollback
   */
  private final static String COUNTER_NAME_UPDATED_MOS_ROLLED_BACK = "updatedMosRolledBack";

  /**
   * Name of counter: number of workflow process instances (jobs).
   */
  private final static String COUNTER_NAME_WORKFLOW_JOBS = "workflowJobs";

  /**
   * The result's message container. Best not for OperationResult to extend this class.
   */
  private ProcessMessageContainerImpl messageContainer;

  /**
   * The operation ID.
   */
  private String opId;

  /**
   * The moment the operation started.
   */
  private Date opStarted;

  /**
   * The moment the operation ended.
   */
  private Date opEnded;

  /**
   * A map of stop watches
   */
  private Map<String, Date> timers = new HashMap<String, Date>();

  /**
   * A map of counters. Key is the name of the counter. Value is the count.
   */
  private Map<String, Integer> counters;

  /**
   * The default object label.
   */
  private String defaultLabel;

  /**
   * All transactions associated with this operation.
   */
  private List<Transaction> transactions;

  /**
   * A list of <code>ContentAssembly</code> instances destroyed by this operation.
   */
  protected List<ContentAssembly> destroyedContentAssemblyList;

  /**
   * A list of <code>ManagedObject</code> instances destroyed by this operation.
   */
  protected List<ManagedObject> destroyedManagedObjectList;

  /**
   * Logs all operations.
   */
  private OperationLogger opLogger;

  /**
   * Workflow process instances (jobs)
   */
  private List<ProcessInstanceSummaryInfo> workflowJobs;

  /**
   * The response or payload of the operation.
   */
  private String payload;

  /**
   * The content type of the payload
   */
  private String payloadContentType;

  /**
   * Constructor accepting default message type, object label, an instance of <code>Log</code> and
   * an Operation Id.
   * 
   * @param id The operation Id
   * @param defaultLabel
   * @param log The log to write messages to, in addition to populating the message container. OK to
   *        send null.
   */
  public BaseOperationResult(String id, String defaultLabel, Log log) {
    this.messageContainer = new ProcessMessageContainerImpl();
    this.defaultLabel = defaultLabel;
    this.counters = new HashMap<String, Integer>();
    this.transactions = new ArrayList<Transaction>();
    this.opLogger = new OperationLogger(log);
    this.workflowJobs = new ArrayList<ProcessInstanceSummaryInfo>();
    this.opId = id;
    this.opLogger.setOpId(id);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.rsicms.rsuite.utils.operation.result.OperationResult#getDefaultLabel()
   */
  @Override
  public String getDefaultLabel() {
    return defaultLabel;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.rsicms.rsuite.utils.operation.result.OperationResult#setLog(org.apache.commons.logging.Log)
   */
  @Override
  public void setLog(Log log) {
    this.opLogger.setLog(log);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.rsicms.rsuite.utils.operation.result.OperationResult#getLog()
   */
  @Override
  public Log getLog() {
    return this.opLogger.getLog();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.rsicms.rsuite.utils.operation.result.OperationResult#getOperationId()
   */
  @Override
  public String getOperationId() {
    return opId;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.rsicms.rsuite.utils.operation.result.OperationResult#setOperationId(java.lang.String)
   */
  @Override
  public void setOperationId(String id) {
    this.opId = id;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.rsicms.rsuite.utils.operation.result.OperationResult#markStartOfOperation()
   */
  @Override
  public void markStartOfOperation() {
    setStartOfOperation(new Date());
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.rsicms.rsuite.utils.operation.result.OperationResult#setStartOfOperation(java.util.Date)
   */
  @Override
  public void setStartOfOperation(Date start) {
    opStarted = start;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.rsicms.rsuite.utils.operation.result.OperationResult#getStartOfOperation()
   */
  @Override
  public Date getStartOfOperation() {
    return opStarted;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.rsicms.rsuite.utils.operation.result.OperationResult#markEndOfOperation()
   */
  @Override
  public void markEndOfOperation() {
    setEndOfOperation(new Date());
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.rsicms.rsuite.utils.operation.result.OperationResult#setEndOfOperation(java.util.Date)
   */
  @Override
  public void setEndOfOperation(Date start) {
    opEnded = start;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.rsicms.rsuite.utils.operation.result.OperationResult#getEndOfOperation()
   */
  @Override
  public Date getEndOfOperation() {
    return opEnded;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.rsicms.rsuite.utils.operation.result.OperationResult#getOperationDurationInMilliseconds()
   */
  @Override
  public long getOperationDurationInMilliseconds() throws RSuiteException {
    if (opEnded == null || opStarted == null) {
      throw new RSuiteException(RSuiteException.ERROR_INTERNAL_ERROR,
          "error.unable.to.calculate.duration");
    }
    return opEnded.getTime() - opStarted.getTime();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.rsicms.rsuite.utils.operation.result.OperationResult#
   * getOperationDurationInMillisecondsQuietly()
   */
  @Override
  public long getOperationDurationInMillisecondsQuietly() {
    try {
      return getOperationDurationInMilliseconds();
    } catch (Exception e) {
      return -1;
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.rsicms.rsuite.utils.operation.result.OperationResult#getOperationDurationInSeconds()
   */
  @Override
  public long getOperationDurationInSeconds() throws RSuiteException {
    return getOperationDurationInMilliseconds() / 1000;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.rsicms.rsuite.utils.operation.result.OperationResult#getOperationDurationInSecondsQuietly()
   */
  @Override
  public long getOperationDurationInSecondsQuietly() {
    try {
      return getOperationDurationInSeconds();
    } catch (Exception e) {
      return -1;
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.rsicms.rsuite.utils.operation.result.OperationResult#startTimer(java.lang.String)
   */
  @Override
  public void startTimer(String name) {
    timers.put(name, new Date());
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.rsicms.rsuite.utils.operation.result.OperationResult#getElapsedTimeInMilliseconds(java.lang
   * .String)
   */
  @Override
  public long getElapsedTimeInMilliseconds(String name) {
    if (timers.containsKey(name)) {
      return new Date().getTime() - timers.get(name).getTime();
    }
    return -1;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.rsicms.rsuite.utils.operation.result.OperationResult#getElapsedTimeInSeconds(java.lang.
   * String)
   */
  @Override
  public long getElapsedTimeInSeconds(String name) {
    long millis = getElapsedTimeInMilliseconds(name);
    if (millis > -1) {
      return millis / 1000;
    }
    return millis;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.rsicms.rsuite.utils.operation.result.OperationResult#addWorkflowJob(com.reallysi.rsuite.api
   * .workflow.ProcessInstanceSummaryInfo)
   */
  @Override
  public void addWorkflowJob(ProcessInstanceSummaryInfo job) {
    if (job != null) {
      workflowJobs.add(job);
      incrementWorkflowJobsCount();
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.rsicms.rsuite.utils.operation.result.OperationResult#getWorkflowJobs()
   */
  @Override
  public List<ProcessInstanceSummaryInfo> getWorkflowJobs() {
    return workflowJobs;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.rsicms.rsuite.utils.operation.result.OperationResult#addFailure(java.lang.Throwable)
   */
  @Override
  public void addFailure(Throwable t) {
    addFailure(defaultLabel, t);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.rsicms.rsuite.utils.operation.result.OperationResult#addFailure(java.lang.String,
   * java.lang.Throwable)
   */
  @Override
  public void addFailure(String label, Throwable t) {
    if (canUnwrapThrowable(t)) {
      t = conditionallyUnwrapThrowable(t);
    }
    String message = t.getMessage();
    opLogger.error(message, t);
    ProcessFailureMessage msg =
        new GenericProcessFailureMessage(Severity.FAIL.toString(), label, message, t);
    msg.setTimestamp();
    messageContainer.addFailureMessage(msg);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.rsicms.rsuite.utils.operation.result.OperationResult#addWarning(java.lang.Throwable)
   */
  @Override
  public void addWarning(Throwable t) {
    addWarning(defaultLabel, t);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.rsicms.rsuite.utils.operation.result.OperationResult#addWarning(java.lang.String,
   * java.lang.Throwable)
   */
  @Override
  public void addWarning(String label, Throwable t) {
    if (canUnwrapThrowable(t)) {
      t = conditionallyUnwrapThrowable(t);
    }
    String message = t.getMessage();
    opLogger.warn(message, t);
    ProcessWarningMessage msg =
        new GenericProcessWarningMessage(Severity.WARN.toString(), label, message, t);
    msg.setTimestamp();
    messageContainer.addWarningMessage(msg);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.rsicms.rsuite.utils.operation.result.OperationResult#addInfoMessage(java.lang.String)
   */
  @Override
  public void addInfoMessage(String message) {
    addInfoMessage(defaultLabel, message);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.rsicms.rsuite.utils.operation.result.OperationResult#addInfoMessage(java.lang.String,
   * java.lang.String)
   */
  @Override
  public void addInfoMessage(String label, String message) {
    addInfoMessage(label, message, null);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.rsicms.rsuite.utils.operation.result.OperationResult#addInfoMessage(java.lang.String,
   * java.lang.Throwable)
   */
  @Override
  public void addInfoMessage(String message, Throwable t) {
    addInfoMessage(defaultLabel, message, t);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.rsicms.rsuite.utils.operation.result.OperationResult#addInfoMessage(java.lang.String,
   * java.lang.String, java.lang.Throwable)
   */
  @Override
  public void addInfoMessage(String label, String message, Throwable t) {
    if (canUnwrapThrowable(t)) {
      t = conditionallyUnwrapThrowable(t);
    }
    opLogger.info(message, t);
    ProcessInfoMessage msg =
        new GenericProcessInfoMessage(Severity.INFO.toString(), label, message, t);
    msg.setTimestamp();
    messageContainer.addInfoMessage(msg);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.rsicms.rsuite.utils.operation.result.OperationResult#addDebugMessage(java.lang.String)
   */
  @Override
  public void addDebugMessage(String message) {
    addDebugMessage(message, defaultLabel);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.rsicms.rsuite.utils.operation.result.OperationResult#addDebugMessage(java.lang.String,
   * java.lang.String)
   */
  @Override
  public void addDebugMessage(String label, String message) {
    addDebugMessage(label, message, null);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.rsicms.rsuite.utils.operation.result.OperationResult#addDebugMessage(java.lang.String,
   * java.lang.Throwable)
   */
  @Override
  public void addDebugMessage(String message, Throwable t) {
    addDebugMessage(defaultLabel, message, t);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.rsicms.rsuite.utils.operation.result.OperationResult#addDebugMessage(java.lang.String,
   * java.lang.String, java.lang.Throwable)
   */
  @Override
  public void addDebugMessage(String label, String message, Throwable t) {
    if (canUnwrapThrowable(t)) {
      t = conditionallyUnwrapThrowable(t);
    }
    opLogger.debug(message, t);
    ProcessDebugMessage msg =
        new GenericProcessDebugMessage(Severity.DEBUG.toString(), label, message, t);
    msg.setTimestamp();
    messageContainer.addDebugMessage(msg);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.rsicms.rsuite.utils.operation.result.OperationResult#hasMoreThanFailureCount(int)
   */
  @Override
  public boolean hasMoreThanFailureCount(int cnt) {
    return cnt > getFailureCount();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.rsicms.rsuite.utils.operation.result.OperationResult#getFailureCount()
   */
  @Override
  public int getFailureCount() {
    return getFailureMessages().size();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.rsicms.rsuite.utils.operation.result.OperationResult#getWarningCount()
   */
  @Override
  public int getWarningCount() {
    return getWarningMessages().size();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.rsicms.rsuite.utils.operation.result.OperationResult#getInfoCount()
   */
  @Override
  public int getInfoCount() {
    return getInfoMessages().size();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.rsicms.rsuite.utils.operation.result.OperationResult#isFailureAndWarningFree()
   */
  @Override
  public boolean isFailureAndWarningFree() {
    return (!hasFailures() && !hasWarnings());
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.rsicms.rsuite.utils.operation.result.OperationResult#hasFailures()
   */
  @Override
  public boolean hasFailures() {
    return messageContainer.hasFailures();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.rsicms.rsuite.utils.operation.result.OperationResult#hasWarnings()
   */
  @Override
  public boolean hasWarnings() {
    return messageContainer.hasWarnings();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.rsicms.rsuite.utils.operation.result.OperationResult#getFailureMessages()
   */
  @Override
  public List<ProcessFailureMessage> getFailureMessages() {
    return messageContainer.getFailureMessages();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.rsicms.rsuite.utils.operation.result.OperationResult#getWarningMessages()
   */
  @Override
  public List<ProcessWarningMessage> getWarningMessages() {
    return messageContainer.getWarningMessages();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.rsicms.rsuite.utils.operation.result.OperationResult#getInfoMessages()
   */
  @Override
  public List<ProcessInfoMessage> getInfoMessages() {
    return messageContainer.getInfoMessages();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.rsicms.rsuite.utils.operation.result.OperationResult#getAllMessages()
   */
  @Override
  public List<ProcessMessage> getAllMessages() {
    return messageContainer.getAllMessages();
  }

  private int getOrInitializeCount(String name) {
    if (!counters.containsKey(name)) {
      counters.put(name, 0);
    }
    return counters.get(name);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.rsicms.rsuite.utils.operation.result.OperationResult#getCount(java.lang.String)
   */
  @Override
  public int getCount(String name) {
    return getOrInitializeCount(name);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.rsicms.rsuite.utils.operation.result.OperationResult#incrementCount(java.lang.String)
   */
  @Override
  public void incrementCount(String name) {
    incrementCount(name, 1);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.rsicms.rsuite.utils.operation.result.OperationResult#incrementCount(java.lang.String,
   * int)
   */
  @Override
  public void incrementCount(String name, int cnt) {
    if (StringUtils.isNotBlank(name)) {
      counters.put(name, getOrInitializeCount(name) + cnt);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.rsicms.rsuite.utils.operation.result.OperationResult#getCounterNames()
   */
  @Override
  public List<String> getCounterNames() {
    List<String> names = new ArrayList<String>(counters.size());
    for (Map.Entry<String, Integer> entry : counters.entrySet()) {
      names.add(entry.getKey());
    }
    return names;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.rsicms.rsuite.utils.operation.result.OperationResult#incrementManagedObjectCreatedCount()
   */
  @Override
  public void incrementManagedObjectCreatedCount() {
    incrementCount(COUNTER_NAME_MOS_CREATED);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.rsicms.rsuite.utils.operation.result.OperationResult#getManagedObjectCreatedCount()
   */
  @Override
  public int getManagedObjectCreatedCount() {
    return getCount(COUNTER_NAME_MOS_CREATED);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.rsicms.rsuite.utils.operation.result.OperationResult#incrementManagedObjectUpdatedCount()
   */
  @Override
  public void incrementManagedObjectUpdatedCount() {
    incrementCount(COUNTER_NAME_MOS_UPDATED);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.rsicms.rsuite.utils.operation.result.OperationResult#getManagedObjectUpdatedCount()
   */
  @Override
  public int getManagedObjectUpdatedCount() {
    return getCount(COUNTER_NAME_MOS_UPDATED);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.rsicms.rsuite.utils.operation.result.OperationResult#
   * incrementNewManagedObjectsRolledBackCount()
   */
  @Override
  public void incrementNewManagedObjectsRolledBackCount() {
    incrementCount(COUNTER_NAME_NEW_MOS_ROLLED_BACK);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.rsicms.rsuite.utils.operation.result.OperationResult#getNewManagedObjectsRolledBackCount()
   */
  @Override
  public int getNewManagedObjectsRolledBackCount() {
    return getCount(COUNTER_NAME_NEW_MOS_ROLLED_BACK);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.rsicms.rsuite.utils.operation.result.OperationResult#
   * incrementUpdatedManagedObjectsRolledBackCount()
   */
  @Override
  public void incrementUpdatedManagedObjectsRolledBackCount() {
    incrementCount(COUNTER_NAME_UPDATED_MOS_ROLLED_BACK);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.rsicms.rsuite.utils.operation.result.OperationResult#
   * getUpdatedManagedObjectsRolledBackCount()
   */
  @Override
  public int getUpdatedManagedObjectsRolledBackCount() {
    return getCount(COUNTER_NAME_UPDATED_MOS_ROLLED_BACK);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.rsicms.rsuite.utils.operation.result.OperationResult#incrementWorkflowJobsCount()
   */
  @Override
  public void incrementWorkflowJobsCount() {
    incrementCount(COUNTER_NAME_WORKFLOW_JOBS);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.rsicms.rsuite.utils.operation.result.OperationResult#getWorkflowJobsCount()
   */
  @Override
  public int getWorkflowJobsCount() {
    return getCount(COUNTER_NAME_WORKFLOW_JOBS);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.rsicms.rsuite.utils.operation.result.OperationResult#incrementSkippedManagedObjectCount()
   */
  @Override
  public void incrementSkippedManagedObjectCount() {
    incrementCount(COUNTER_NAME_MOS_SKIPPED);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.rsicms.rsuite.utils.operation.result.OperationResult#getSkippedManagedObjectCount()
   */
  @Override
  public int getSkippedManagedObjectCount() {
    return getCount(COUNTER_NAME_MOS_SKIPPED);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.rsicms.rsuite.utils.operation.result.OperationResult#startTransaction()
   */
  @Override
  public int startTransaction() {
    Transaction t = new Transaction();
    transactions.add(t);
    return transactions.size() - 1;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.rsicms.rsuite.utils.operation.result.OperationResult#getCurrentTransaction()
   */
  @Override
  public Transaction getCurrentTransaction() {
    if (transactions.size() == 0) {
      startTransaction();
    }
    return transactions.get(transactions.size() - 1);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.rsicms.rsuite.utils.operation.result.OperationResult#addNewAsset(java.lang.String,
   * java.lang.String)
   */
  @Override
  public void addNewAsset(String moId, String assetName) {
    getCurrentTransaction().addAsset(moId);
    incrementManagedObjectCreatedCount();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.rsicms.rsuite.utils.operation.result.OperationResult#addUpdatedAsset(java.lang.String,
   * java.lang.String)
   */
  @Override
  public void addUpdatedAsset(String moId, String assetName) {
    getCurrentTransaction().addUpdatedAsset(moId, assetName);
    incrementManagedObjectUpdatedCount();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.rsicms.rsuite.utils.operation.result.OperationResult#rollbackCurrentTransaction(com.
   * reallysi.rsuite.api.extensions.ExecutionContext, com.reallysi.rsuite.api.User,
   * com.rsicms.rsuite.utils.operation.result.OperationResult)
   */
  @Override
  public void rollbackCurrentTransaction(ExecutionContext context, User user,
      OperationResult result) {
    getCurrentTransaction().rollback(context, user, result);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.rsicms.rsuite.utils.operation.result.OperationResult#getTransactions()
   */
  @Override
  public List<Transaction> getTransactions() {
    return transactions;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.rsicms.rsuite.utils.operation.result.OperationResult#getDestroyedContentAssemblies()
   */
  @Override
  public List<ContentAssembly> getDestroyedContentAssemblies() {
    return destroyedContentAssemblyList;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.rsicms.rsuite.utils.operation.result.OperationResult#setDestroyedContentAssemblies(java.
   * util.List)
   */
  @Override
  public void setDestroyedContentAssemblies(List<ContentAssembly> destroyedContentAssemblyList) {
    this.destroyedContentAssemblyList = destroyedContentAssemblyList;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.rsicms.rsuite.utils.operation.result.OperationResult#getDestroyedManagedObjects()
   */
  @Override
  public List<ManagedObject> getDestroyedManagedObjects() {
    return destroyedManagedObjectList;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.rsicms.rsuite.utils.operation.result.OperationResult#setDestroyedManagedObjects(java.util.
   * List)
   */
  @Override
  public void setDestroyedManagedObjects(List<ManagedObject> destroyedManagedObjectList) {
    this.destroyedManagedObjectList = destroyedManagedObjectList;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.rsicms.rsuite.utils.operation.result.OperationResult#setPayload(java.lang.String,
   * java.lang.String)
   */
  @Override
  public void setPayload(String payload, String contentType) {
    this.payload = payload;
    this.payloadContentType = contentType;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.rsicms.rsuite.utils.operation.result.OperationResult#getPayload()
   */
  @Override
  public String getPayload() {
    return payload;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.rsicms.rsuite.utils.operation.result.OperationResult#hasPayload()
   */
  @Override
  public boolean hasPayload() {
    return StringUtils.isNotBlank(payload);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.rsicms.rsuite.utils.operation.result.OperationResult#getPayloadContentType()
   */
  @Override
  public String getPayloadContentType() {
    return payloadContentType;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.rsicms.rsuite.utils.operation.result.OperationResult#addSubResult(com.rsicms.rsuite.utils.
   * operation.result.BaseOperationResult)
   */
  @Override
  public void addSubResult(BaseOperationResult subResult) {
    if (subResult != null) {
      // At present we're only bringing over the messages.
      messageContainer.addAll(subResult.messageContainer);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.rsicms.rsuite.utils.operation.result.OperationResult#getExecutiveSummary()
   */
  @Override
  public String getExecutiveSummary() {
    if (hasFailures()) {
      return new StringBuilder("Error! (").append(getFailureCount()).append(")").toString();
    } else if (hasWarnings()) {
      return new StringBuilder("Warning (").append(getWarningCount()).append(")").toString();
    } else {
      return "Successful";
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.rsicms.rsuite.utils.operation.result.OperationResult#getHtmlFormattedMessages()
   */
  @Override
  public String getHtmlFormattedMessages() {
    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    HtmlFormattingOperationResultVisitor visitor = new HtmlFormattingOperationResultVisitor(writer);
    try {
      visitor.visit(this);
    } catch (Exception e) {
      writer.println("Unexpected exception formatting message report: " + e.getMessage());
      e.printStackTrace(writer);
    }
    return stringWriter.toString();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.rsicms.rsuite.utils.operation.result.OperationResult#canUnwrapThrowable(java.lang.
   * Throwable)
   */
  @Override
  public boolean canUnwrapThrowable(Throwable t) {
    return t instanceof InvocationTargetException;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.rsicms.rsuite.utils.operation.result.OperationResult#conditionallyUnwrapThrowable(java.lang
   * .Throwable)
   */
  @Override
  public Throwable conditionallyUnwrapThrowable(Throwable t) {
    while (t instanceof InvocationTargetException) {
      t = ((InvocationTargetException) t).getTargetException();
    }
    return t;
  }

  /**
   * When the given throwable isn't an RSuiteException, wrap it in one.
   * <p>
   * TODO: Revisit if this practice is necessary.
   * 
   * @param t
   * @param wrapperCode
   * @param wrapperMessageKey
   * @param wrapperMessageArgs
   * @return An RSuiteException public RSuiteException conditionallyWrapThrowable( Throwable t, int
   *         wrapperCode, String wrapperMessageKey, Object... wrapperMessageArgs) {
   * 
   *         t = conditionallyUnwrapThrowable(t);
   * 
   *         RSuiteException re; if (t instanceof RSuiteException) { re = (RSuiteException) t; }
   *         else { re = new RSuiteException(wrapperCode, LocalMessageProperties.get(
   *         wrapperMessageKey, wrapperMessageArgs), t); } return re; }
   */

}

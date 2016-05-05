package com.rsicms.rsuite.utils.operation.result;

import org.apache.commons.logging.Log;

import com.reallysi.rsuite.api.ContentAssemblyNodeContainer;

/**
 * A container operation result.
 */
public class ContainerOperationResult
    extends BaseOperationResult {

  private ContentAssemblyNodeContainer container;

  public ContainerOperationResult(
      String opId,
      String defaultLabel,
      Log log) {
    super(opId, defaultLabel, log);
  }

  /**
   * @return the container
   */
  public ContentAssemblyNodeContainer getContainer() {
    return container;
  }

  /**
   * @param container the container to set
   */
  public void setContainer(
      ContentAssemblyNodeContainer container) {
    this.container = container;
  }



}

package com.rsicms.rsuite.utils.operation.options;

import org.apache.commons.logging.Log;

public class FileRequestOptions extends OperationOptions {

  private String suggestedFileName;
  private String tabName;

  public FileRequestOptions(Log log) {
    super(log);
  }

  /**
   * @return the suggested file name
   */
  public String getSuggestedFileName() {
    return suggestedFileName;
  }

  /**
   * @param suggestedFileName
   */
  public void setSuggestedFileName(String suggestedFileName) {
    this.suggestedFileName = suggestedFileName;
  }

  /**
   * @return the tab name
   */
  public String getTabName() {
    return tabName;
  }

  /**
   * @param tabName
   */
  public void setTabName(String tabName) {
    this.tabName = tabName;
  }

}

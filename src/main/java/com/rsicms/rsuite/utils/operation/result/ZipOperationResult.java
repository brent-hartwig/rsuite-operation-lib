package com.rsicms.rsuite.utils.operation.result;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;

import org.apache.commons.logging.Log;

public class ZipOperationResult extends BaseOperationResult {

  private final static String COUNTER_NAME_MOS_CREATED = "mosCreated";

  private final static String COUNTER_NAME_MOS_UPDATED = "mosUpdated";

  private File zipFile;

  private List<String> zipFileManifest = new ArrayList<String>();

  public ZipOperationResult(String operationId, String defaultLabel, Log log) {
    super(operationId, defaultLabel, log);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * gov.gao.newblue.rsuite.operation.result.OperationResult#incrementManagedObjectCreatedCount()
   */
  @Override
  public void incrementManagedObjectCreatedCount() {
    incrementCount(COUNTER_NAME_MOS_CREATED);
  }

  /*
   * (non-Javadoc)
   * 
   * @see gov.gao.newblue.rsuite.operation.result.OperationResult#getManagedObjectCreatedCount()
   */
  @Override
  public int getManagedObjectCreatedCount() {
    return getCount(COUNTER_NAME_MOS_CREATED);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * gov.gao.newblue.rsuite.operation.result.OperationResult#incrementManagedObjectUpdatedCount()
   */
  @Override
  public void incrementManagedObjectUpdatedCount() {
    incrementCount(COUNTER_NAME_MOS_UPDATED);
  }

  /*
   * (non-Javadoc)
   * 
   * @see gov.gao.newblue.rsuite.operation.result.OperationResult#getManagedObjectUpdatedCount()
   */
  @Override
  public int getManagedObjectUpdatedCount() {
    return getCount(COUNTER_NAME_MOS_UPDATED);
  }

  /**
   * @return the zipFile
   */
  public File getZipFile() {
    return zipFile;
  }

  /**
   * @param zipFile the zipFile to set
   */
  public void setZipFile(File zipFile) {
    this.zipFile = zipFile;
  }

  /**
   * @return the zipFileManifest
   */
  public List<String> getZipFileManifest() {
    return zipFileManifest;
  }

  /**
   * Add an entry to the zip manifest
   * 
   * @param ze
   */
  public void addToZipFileManifest(ZipEntry ze) {
    zipFileManifest.add(ze.getName());
  }

}

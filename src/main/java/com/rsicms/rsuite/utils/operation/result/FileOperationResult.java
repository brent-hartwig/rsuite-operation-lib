package com.rsicms.rsuite.utils.operation.result;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;

import com.reallysi.rsuite.api.remoteapi.RemoteApiResult;
import com.reallysi.rsuite.api.remoteapi.result.ByteSequenceResult;

public class FileOperationResult
    extends BaseOperationResult {

  private byte[] content;
  private String contentType;
  private String suggestedFileName;

  /**
   * Constructor accepting default message type, object label and instance of <code>Log</code>
   * 
   * @param opId
   * @param defaultLabel
   * @param log The log to write messages to, in addition to populating the message container. OK to
   *        send null.
   */
  public FileOperationResult(
      String opId,
      String defaultLabel,
      Log log) {
    super(opId, defaultLabel, log);
  }

  /**
   * Pass in everything that {@link #getFileForDownload()} will need.
   * 
   * @param content
   * @param contentType
   * @param suggestedFileName
   * @throws IOException
   */
  public void prepareFileForDownload(
      InputStream content,
      String contentType,
      String suggestedFileName)
      throws IOException {
    /*
     * IDEA: Stick with input stream such that we don't limit the response size to the JVM's maximum
     * array size. If RCS-4127 hasn't resulted in streaming support by the time we need this, see if
     * a streaming RemoteApiResult would be correctly handled by RSuite's REST API.
     */
    prepareFileForDownload(
        IOUtils.toByteArray(content),
        contentType,
        suggestedFileName);
  }

  /**
   * Pass in everything that {@link #getFileForDownload()} will need.
   * 
   * @param content
   * @param contentType
   * @param suggestedFileName
   */
  public void prepareFileForDownload(
      byte[] content,
      String contentType,
      String suggestedFileName) {
    this.content = content;
    this.contentType = contentType;
    this.suggestedFileName = suggestedFileName;
  }

  /**
   * Get the file to download.
   * 
   * @return file to download.
   */
  public RemoteApiResult getFileForDownload() {
    ByteSequenceResult resultFile = new ByteSequenceResult();
    resultFile.setContent(content);
    resultFile.setContentType(contentType);
    resultFile.setSuggestedFileName(suggestedFileName);
    return resultFile;
  }

}

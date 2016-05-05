package com.rsicms.rsuite.utils.operation;

import com.rsicms.rsuite.helpers.messages.ProcessMessage;
import com.rsicms.rsuite.helpers.messages.impl.GenericProcessFailureMessage;
import com.rsicms.rsuite.helpers.messages.impl.GenericProcessInfoMessage;
import com.rsicms.rsuite.helpers.messages.impl.GenericProcessWarningMessage;

/**
 * Helper enum to OperationResult and its underlying ProcessMessageContainer.
 */
public enum MessageType {
  INFO("info", GenericProcessInfoMessage.class), WARNING("warn", GenericProcessWarningMessage.class), FAILURE(
      "error", GenericProcessFailureMessage.class), OTHER("other", ProcessMessage.class);

  private String label;
  private Class<? extends ProcessMessage> klass;

  private MessageType(
      String label,
      Class<? extends ProcessMessage> klass) {
    this.label = label;
    this.klass = klass;
  }

  public String getLabel() {
    return label;
  }

  public static MessageType get(
      Class<? extends ProcessMessage> klass) {
    for (MessageType type : MessageType.values()) {
      if (type.klass.equals(klass)) {
        return type;
      }
    }
    return OTHER;
  }
}

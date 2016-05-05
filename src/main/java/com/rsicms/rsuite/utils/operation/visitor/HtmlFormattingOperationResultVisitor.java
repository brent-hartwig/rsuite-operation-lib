package com.rsicms.rsuite.utils.operation.visitor;

import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.rsicms.rsuite.helpers.messages.ProcessMessage;
import com.rsicms.rsuite.utils.operation.MessageType;
import com.rsicms.rsuite.utils.operation.result.OperationResult;

/**
 * Used to generate an HTML representation of an <code>OperationResult</code>.
 */
public class HtmlFormattingOperationResultVisitor {

  protected PrintWriter writer;

  static final SimpleDateFormat DEFAULT_TIMESTAMP_FORMATTER_OVERVIEW =
      new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");

  static final SimpleDateFormat DEFAULT_TIMESTAMP_FORMATTER_DETAILS =
      new SimpleDateFormat("HH:mm:ss");

  protected SimpleDateFormat timestampFormatterOverview = DEFAULT_TIMESTAMP_FORMATTER_OVERVIEW;

  protected SimpleDateFormat timestampFormatterDetails = DEFAULT_TIMESTAMP_FORMATTER_DETAILS;

  public HtmlFormattingOperationResultVisitor(
      PrintWriter writer) {
    this.writer = writer;
  }

  public void setTimestampFormatterForOverviewSection(
      SimpleDateFormat timestampFormatter) {
    this.timestampFormatterOverview = timestampFormatter;
  }

  public void setTimestampFormatterForDetailsSection(
      SimpleDateFormat timestampFormatter) {
    this.timestampFormatterDetails = timestampFormatter;
  }

  public void visit(
      OperationResult opResult) {
    writer.append("<html>");
    addHead(
        opResult,
        writer);
    writer.append("<body>");
    addOverview(
        opResult,
        writer,
        timestampFormatterOverview);
    addCounters(
        opResult,
        writer);
    addDetails(
        opResult,
        writer,
        timestampFormatterDetails);
    writer.append("</body></html>");
  }

  protected static void addHead(
      OperationResult opResult,
      PrintWriter writer) {
    writer
        .append(
            "<head><style type='text/css'>")
        .append(
            "body         { font-family: sans-serif; }\n")
        .append(
            "table        { border-style:none; width:*; margin-left:10%; margin-right:10%; border-width: 0px; }\n")
        .append(
            "col.msgCol1  { min-width: 60px; width: 60px; max-width: 60px; }\n").append(
            "col.msgCol2  { min-width: 30px; width: 30px; max-width: 30px; }\n").append(
            "tr           { vertical-align: top; border-width: 0px; }\n").append(
            "tr.error     { vertical-align: top; background-color: red; color: white; }\n").append(
            "tr.warn      { vertical-align: top; color: red; }\n").append(
            "td           { padding-left: 6px; } ").append(
            "</style></head>");
  }

  protected static void addOverview(
      OperationResult opResult,
      PrintWriter writer,
      SimpleDateFormat timestampFormatter) {
    writer.append(
        "<h4>Overview</h4>").append(
        "<table><tbody>\n");

    writer.append(
        "<tr><td>Operation Description</td><td>").append(
        opResult.getDefaultLabel()).append(
        "</td></tr>\n");
    writer.append(
        "<tr><td>Operation ID</td><td>").append(
        opResult.getOperationId()).append(
        "</td></tr>\n");
    writer
        .append(
            "<tr><td>Start</td><td>")
        .append(
            opResult.getStartOfOperation() == null ? "Unknown" : timestampFormatter.format(opResult
                .getStartOfOperation())).append(
            "</td></tr>\n");
    writer
        .append(
            "<tr><td>End</td><td>")
        .append(
            opResult.getEndOfOperation() == null ? "Unknown" : timestampFormatter.format(opResult
                .getEndOfOperation())).append(
            "</td></tr>\n");
    writer.append(
        "<tr><td>Duration in Seconds</td><td>").append(
        String.valueOf(opResult.getOperationDurationInSecondsQuietly())).append(
        "</td></tr>\n");

    writer.append("</tbody></table>");
  }

  protected static void addCounters(
      OperationResult opResult,
      PrintWriter writer) {
    writer.append("<h4>Counters</h4>");
    List<String> counterNames = opResult.getCounterNames();
    if (counterNames != null) {
      writer.append("<table><tbody>\n");
      for (String counterName : counterNames) {
        writer.append(
            "<tr><td>").append(
            StringUtils.capitalize(counterName)).append(
            "</td><td>").append(
            String.valueOf(opResult.getCount(counterName))).append(
            "</td></tr>\n");
      }
      writer.append("</tbody></table>");
    } else {
      writer.append("<p><i>None</i></p>");
    }
  }

  protected static void addDetails(
      OperationResult opResult,
      PrintWriter writer,
      SimpleDateFormat timestampFormatter) {
    writer.append(
        "<h4>Details</h4>").append(
        "<table>").append(
        "<colgroup><col class='msgCol1'><col class='msgCol2'><col class='msgCol3'></colgroup>")
        .append(
            "<tbody>\n");
    for (ProcessMessage message : opResult.getAllMessages()) {
      MessageType messageType = MessageType.get(message.getClass());
      Date timestamp = message.getTimestamp();
      writer.append(
          "<tr class='").append(
          getMessageRowClass(messageType)).append(
          "'>").append(
          "<td>").append(
          timestampFormatter.format(timestamp)).append(
          "</td><td>").append(
          messageType.getLabel().toUpperCase()).append(
          "</td><td>").append(
          message.getMessageText()).append(
          "</td></tr>\n");
    }

    writer.append("</tbody></table>");
  }

  protected static String getMessageRowClass(
      MessageType messageType) {
    if (MessageType.FAILURE == messageType) {
      return "error";
    } else if (MessageType.WARNING == messageType) {
      return "warn";
    }
    return StringUtils.EMPTY;
  }

}

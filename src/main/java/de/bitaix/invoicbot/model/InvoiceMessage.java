package de.bitaix.invoicbot.model;

import jakarta.mail.Message;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class InvoiceMessage {

  private String subject;

  private List<String> from;

  private Message message;

}

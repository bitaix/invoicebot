package de.bitaix.invoicbot.service;

import static jakarta.mail.search.ComparisonTerm.GE;
import static jakarta.mail.search.ComparisonTerm.LE;

import de.bitaix.invoicbot.configuration.BotConfiguration;
import de.bitaix.invoicbot.model.Attachment;
import de.bitaix.invoicbot.model.InvoiceCriteria;
import de.bitaix.invoicbot.model.InvoiceMessage;
import jakarta.mail.Address;
import jakarta.mail.Folder;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Part;
import jakarta.mail.Session;
import jakarta.mail.Store;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.search.AndTerm;
import jakarta.mail.search.OrTerm;
import jakarta.mail.search.ReceivedDateTerm;
import jakarta.mail.search.SearchTerm;
import jakarta.mail.search.SubjectTerm;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImapService {

  private final BotConfiguration configuration;
  private Store store;
  private Folder inbox;

  public List<InvoiceMessage> getMessages(InvoiceCriteria criteria) {
    var searchTerm = getSearchTerm(criteria);

    var session = createSession();

    try {
      connectToInputFolder(session);
      var messages = inbox.search(searchTerm);
      log.info("Number of messages: {}", messages.length);

      return Arrays.stream(messages)
          .filter(this::isMultipart)
          .map(this::toInvoiceMessage)
          .toList();

    } catch (Exception e) {
      log.error(e.getMessage());
      throw new RuntimeException(e);
    }
  }

  private InvoiceMessage toInvoiceMessage(Message message) {
    try {
      var addresses = Arrays.stream(message.getFrom())
          .map(Address::toString)
          .toList();

      return InvoiceMessage.builder()
          .subject(message.getSubject())
          .from(addresses)
          .message(message)
          .build();
    } catch (MessagingException e) {
      throw new RuntimeException(e);
    }
  }

  public void closeConnection() {
    try {
      inbox.close(false);
      store.close();
    } catch (Exception e) {
      log.error(e.getMessage());
      throw new RuntimeException(e);
    }
  }

  private void connectToInputFolder(Session session) throws MessagingException {
    store = session.getStore("imap");
    store.connect(configuration.getSourceMailAddress(), configuration.getSourcePassword());
    inbox = store.getFolder("INBOX");
    inbox.open(Folder.READ_ONLY);
  }

  private Session createSession() {
    var properties = new Properties();
    properties.put("mail.store.protocol", "imap");
    properties.put("mail.imap.host", configuration.getImapHost());
    properties.put("mail.imap.port", configuration.getImapPort());
    properties.put("mail.imap.ssl.enable", String.valueOf(configuration.isImapSslEnable()));
    return Session.getInstance(properties);
  }

  private boolean isMultipart(Message m) {
    try {
      return m.isMimeType("multipart/*");
    } catch (MessagingException e) {
      throw new RuntimeException(e);
    }
  }

  private SearchTerm getSearchTerm(InvoiceCriteria criteria) {
    var start = LocalDate.of(criteria.getYear().getValue(), criteria.getMonth().getValue(), 1);
    var end = LocalDate.of(criteria.getYear().getValue(), criteria.getMonth().getValue(),
        criteria.getMonth().length(false));

    var startTerm = new ReceivedDateTerm(GE,
        Date.from(start.atStartOfDay(ZoneId.systemDefault()).toInstant()));
    var endTerm = new ReceivedDateTerm(LE,
        Date.from(end.atStartOfDay(ZoneId.systemDefault()).toInstant()));

    var subjectTerms = new OrTerm(new SubjectTerm("Rechnung"), new SubjectTerm("Invoice"));

    var dateTerms = new AndTerm(startTerm, endTerm);

    return new AndTerm(dateTerms, subjectTerms);
  }

  public List<Attachment> extractAttachments(InvoiceMessage invoiceMessage) {
    var attachments = new ArrayList<Attachment>();
    try {
      var multipart = (MimeMultipart) invoiceMessage.getMessage().getContent();
      var count = multipart.getCount();
      for (int i = 0; i < count; i++) {
        var bodyPart = multipart.getBodyPart(i);
        if (Part.ATTACHMENT.equalsIgnoreCase(bodyPart.getDisposition())) {
          attachments.add(Attachment.builder()
              .inputStream(bodyPart.getInputStream())
              .fileName(bodyPart.getFileName())
              .build());
        }
      }
      return attachments;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}

package de.bitaix.invoicbot.service;

import de.bitaix.invoicbot.configuration.BotConfiguration;
import de.bitaix.invoicbot.model.Attachment;
import de.bitaix.invoicbot.model.InvoiceCriteria;
import de.bitaix.invoicbot.model.InvoiceMessage;
import de.bitaix.invoicbot.model.InvoiceResult;
import java.util.Collection;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvoiceReadService {

  private final ImapService imapService;

  private final FileSaver fileSaver;

  private final BotConfiguration botConfiguration;

  public Optional<InvoiceResult> run(InvoiceCriteria criteria) {
    var messages = imapService.getMessages(criteria);

    if (messages.isEmpty()) {
      log.info("No Messages found for year {} month {}", criteria.getYear(), criteria.getMonth());
      return Optional.empty();
    }

    messages.stream()
        .map(this::printMessage)
        .map(imapService::extractAttachments)
        .flatMap(Collection::stream)
        .map(att -> saveAttachment(att, criteria))
        .toList();

    imapService.closeConnection();

    return Optional.of(
        InvoiceResult.builder()
            .directory(currentRootDirectory(criteria))
            .build());

  }

  private InvoiceMessage printMessage(InvoiceMessage message) {
    log.info("E-Mail {} | {}", message.getSubject(), message.getFrom());
    return message;
  }

  private Optional<String> saveAttachment(Attachment attachment, InvoiceCriteria criteria) {
    try {
      var currentRootDirectory = currentRootDirectory(criteria);
      return fileSaver.saveFile(attachment.getInputStream(),
          attachment.getFileName(), currentRootDirectory);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private String currentRootDirectory(InvoiceCriteria criteria) {
    var yearMonthDir = criteria.getYear().toString() + "-" + criteria.getMonth().getValue() + "/";
    return botConfiguration.getAttachRootDir() + yearMonthDir;
  }
}
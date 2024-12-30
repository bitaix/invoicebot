package de.bitaix.invoicbot.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.bitaix.invoicbot.configuration.BotConfiguration;
import de.bitaix.invoicbot.model.Attachment;
import de.bitaix.invoicbot.model.InvoiceCriteria;
import de.bitaix.invoicbot.model.InvoiceMessage;
import jakarta.mail.MessagingException;
import java.io.InputStream;
import java.time.Month;
import java.time.Year;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InvoiceReadServiceTest {

  @Mock
  private ImapService imapService;

  @Mock
  private FileSaver fileSaver;

  @Mock
  private BotConfiguration botConfiguration;

  @InjectMocks
  private InvoiceReadService cut;

  @Test
  void run() throws MessagingException {

    var criteria = createCriteria();
    var messages = createMessages();
    InputStream inputStream = Mockito.mock(InputStream.class);
    var attachments = List.of(Attachment.builder().fileName("filename")
        .inputStream(inputStream)
        .build());

    when(imapService.getMessages(criteria))
        .thenReturn(messages);

    when(imapService.extractAttachments(any()))
        .thenReturn(attachments);

    when(fileSaver.saveFile(eq(inputStream), eq("filename"),
        eq("/data/2024-10/")))
        .thenReturn(Optional.of("file"));

    when(botConfiguration.getAttachRootDir()).thenReturn("/data/");

    var result = cut.run(criteria);

    assertEquals("/data/2024-10/", result.get().getDirectory());

    verify(imapService).closeConnection();

  }

  private static List<InvoiceMessage> createMessages() {
    return List.of(InvoiceMessage.builder()
        .subject("subject")
        .from(List.of("from1", "from2"))
        .build());
  }

  private static InvoiceCriteria createCriteria() {
    return InvoiceCriteria.builder()
        .month(Month.OCTOBER)
        .year(Year.of(2024))
        .build();
  }
}
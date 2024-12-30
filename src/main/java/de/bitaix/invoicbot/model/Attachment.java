package de.bitaix.invoicbot.model;

import java.io.InputStream;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Attachment {

  private InputStream inputStream;

  private String fileName;

}

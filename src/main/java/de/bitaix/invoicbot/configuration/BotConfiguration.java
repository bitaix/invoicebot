package de.bitaix.invoicbot.configuration;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class BotConfiguration {

  @Value("${de.bitaix.invoice-bot.source-mailaddress}")
  private String sourceMailAddress;

  @Value("${de.bitaix.invoice-bot.source-password}")
  private String sourcePassword;

  @Value("${de.bitaix.invoice-bot.protocol}")
  private String protocol;

  @Value("${de.bitaix.invoice-bot.imap-host}")
  private String imapHost;

  @Value("${de.bitaix.invoice-bot.imap-port}")
  private String imapPort;

  @Value("${de.bitaix.invoice-bot.imap-ssl-enable}")
  private boolean imapSslEnable;

  @Value("${de.bitaix.invoice-bot.attach-root-dir}")
  private String attachRootDir;
}

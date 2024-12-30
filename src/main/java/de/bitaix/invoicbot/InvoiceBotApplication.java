package de.bitaix.invoicbot;

import de.bitaix.invoicbot.model.InvoiceCriteria;
import de.bitaix.invoicbot.service.InvoiceReadService;
import java.time.Month;
import java.time.Year;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@RequiredArgsConstructor
public class InvoiceBotApplication implements CommandLineRunner {

  private final InvoiceReadService invoiceReadService;

  public static void main(String[] args) {
    SpringApplication.run(InvoiceBotApplication.class, args);
  }

  @Override
  public void run(String... args) {
    if (args == null || args.length == 0){
      return;
    }

    int year = Integer.parseInt(args[0]);
    int month = Integer.parseInt(args[1]);

    var criteria = InvoiceCriteria.builder()
        .year(Year.of(year))
        .month(Month.of(month))
        .build();

    var result = invoiceReadService.run(criteria);

    System.out.println();
    System.out.println("All invoices extracted to: " + result.get().getDirectory());

  }

}

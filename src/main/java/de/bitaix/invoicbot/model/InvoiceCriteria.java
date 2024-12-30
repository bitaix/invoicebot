package de.bitaix.invoicbot.model;

import java.time.Month;
import java.time.Year;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class InvoiceCriteria {

  private Year year;

  private Month month;

  private List<String> subjectPatterns;


}

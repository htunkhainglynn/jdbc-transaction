package domain;

import java.time.LocalDate;

public record TransferLog(int id, String fromAccount, String toAccount, int amount, LocalDate transferTime, int fromAmount, int toAmount, String status) {
}

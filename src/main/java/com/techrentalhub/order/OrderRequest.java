package com.techrentalhub.order;

import lombok.Data;
import java.time.LocalDate;

@Data
public class OrderRequest {
    private Long deviceId;
    private LocalDate startDate;
    private LocalDate endDate;
}

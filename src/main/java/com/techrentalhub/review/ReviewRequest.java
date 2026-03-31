package com.techrentalhub.review;

import lombok.Data;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Data
public class ReviewRequest {

    @NotNull(message = "Số sao không được để trống")
    @Min(value = 1, message = "Tối thiểu 1 sao")
    @Max(value = 5, message = "Tối đa 5 sao")
    private Integer stars;

    private String comment; // Tùy chọn
}

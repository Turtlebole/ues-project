package com.ues.dto;

import lombok.Data;

@Data
public class RateDTO {
    private Long id;
    private Integer performanceRating;
    private Integer soundLightRating;
    private Integer spaceRating;
    private Integer overallRating;
}

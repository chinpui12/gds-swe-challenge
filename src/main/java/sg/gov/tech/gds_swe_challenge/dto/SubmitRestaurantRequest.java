package sg.gov.tech.gds_swe_challenge.dto;

import jakarta.validation.constraints.NotBlank;

public record SubmitRestaurantRequest(
        @NotBlank String name
) {}

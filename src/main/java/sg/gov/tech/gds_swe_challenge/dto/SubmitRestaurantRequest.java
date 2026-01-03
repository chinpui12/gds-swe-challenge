package sg.gov.tech.gds_swe_challenge.dto;

import jakarta.validation.constraints.NotBlank;
import sg.gov.tech.gds_swe_challenge.constant.AppConstants;

public record SubmitRestaurantRequest(
        @NotBlank String name,
        long sessionId,
        String sessionName
) {
    public SubmitRestaurantRequest(@NotBlank String name) {
        this(name, AppConstants.GLOBAL_SESSION_ID, AppConstants.GLOBAL_SESSION_NAME);
    }
}

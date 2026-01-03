package sg.gov.tech.gds_swe_challenge.dto;

public record UserInput(
        String username,
        Boolean canInitiateSession
) {}

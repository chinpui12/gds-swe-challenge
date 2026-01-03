package sg.gov.tech.gds_swe_challenge.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "app_user")
public class User extends Auditable {
    @Id
    private String username;

    private boolean canInitiateSession = false;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public boolean isCanInitiateSession() {
        return canInitiateSession;
    }

    public void setCanInitiateSession(boolean canInitiateSession) {
        this.canInitiateSession = canInitiateSession;
    }
}
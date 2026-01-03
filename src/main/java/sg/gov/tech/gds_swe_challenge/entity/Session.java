package sg.gov.tech.gds_swe_challenge.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table
public class Session extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private boolean isClosed = false;

    private String selectedRestaurant;

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<Restaurant> restaurants = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "session_invited_users",
            joinColumns = @JoinColumn(name = "session_id"),
            inverseJoinColumns = @JoinColumn(name = "user_username")
    )
    private Set<User> invitedUsers = new HashSet<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isClosed() {
        return isClosed;
    }

    public void setClosed(boolean closed) {
        isClosed = closed;
    }

    public String getSelectedRestaurant() {
        return selectedRestaurant;
    }

    public void setSelectedRestaurant(String selectedRestaurant) {
        this.selectedRestaurant = selectedRestaurant;
    }

    public List<Restaurant> getRestaurants() {
        return restaurants;
    }

    public void setRestaurants(List<Restaurant> restaurants) {
        this.restaurants = restaurants;
    }

    public Set<User> getInvitedUsers() {
        return invitedUsers;
    }

    public void setInvitedUsers(Set<User> invitedUsers) {
        this.invitedUsers = invitedUsers;
    }

    public void reset() {
        this.isClosed = false;
        this.selectedRestaurant = null;
    }

    public boolean isCreator(User user) {
        return getCreatedBy() != null && getCreatedBy().equals(user.getUsername());
    }

    public boolean isUserInvited(User user) {
        return invitedUsers.stream()
                .anyMatch(u -> u.getUsername().equals(user.getUsername())) ||
                this.getCreatedBy().equals(user.getUsername());
    }

    public boolean isUserInvited(String username) {
        return invitedUsers.stream()
                .anyMatch(u -> u.getUsername().equals(username)) ||
                this.getCreatedBy().equals(username);
    }

    public void addInvitedUser(User user) {
        invitedUsers.add(user);
    }
}
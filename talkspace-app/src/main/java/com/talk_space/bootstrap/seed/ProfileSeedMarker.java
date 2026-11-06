package com.talk_space.bootstrap.seed;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Remembers users already processed, so intentionally cleared fields stay cleared. */
@Entity
@Table(name = "demo_profile_seed_marker")
public class ProfileSeedMarker {
    @Id @Column(name = "user_name", length = 20)
    private String userName;
    protected ProfileSeedMarker() {}
    public ProfileSeedMarker(String userName) { this.userName = userName; }
}

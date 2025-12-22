package com.events.app.myevents.Model;

import java.time.LocalDateTime;
import java.util.List;


import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "event")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank(message = "Inserire un nome evento")
    private String name;

    private String description;

    private LocalDateTime date;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "event")
    private List<Invited> inviteds;

    private String linkEventPhotos;

    private String linkInvite;

    

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getDate() {
        return this.date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public User getUser() {
        return this.user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<Invited> getInviteds() {
        return this.inviteds;
    }

    public void setInviteds(List<Invited> inviteds) {
        this.inviteds = inviteds;
    }

    public String getLinkEventPhotos() {
        return this.linkEventPhotos;
    }

    public void setLinkEventPhotos(String linkEventPhotos) {
        this.linkEventPhotos = linkEventPhotos;
    }

    public String getLinkInvite() {
        return this.linkInvite;
    }

    public void setLinkInvite(String linkInvite) {
        this.linkInvite = linkInvite;
    }

}

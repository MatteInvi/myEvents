package com.wedding.app.mywedding.Model;

import java.util.List;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank(message = "Inserire nome")
    private String name;

    @NotBlank(message = "Inserire congnome")
    private String surname;

    @NotBlank(message = "La mail non può essere vuota")
    @Email
    private String email;

    @NotBlank(message = "Inserisci una password")
    private String password;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "role_user", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles;

    @OneToMany(mappedBy = "user")
    private List<Invited> invited;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private authToken authToken;

    private String linkPhotoUpload;

    private boolean verified = false;

    private String linkInvite;


    public String getLinkInvite() {
        return this.linkInvite;
    }

    public void setLinkInvite(String linkInvite) {
        this.linkInvite = linkInvite;
    }


    

    // Getter e setter
    public String getLinkPhotoUpload() {
        return this.linkPhotoUpload;
    }

    public void setLinkPhotoUpload(String linkPhotoUpload) {
        this.linkPhotoUpload = linkPhotoUpload;
    }

    public List<Invited> getInvited() {
        return this.invited;
    }

    public void setInvited(List<Invited> invited) {
        this.invited = invited;
    }

    public authToken getAuthToken() {
        return this.authToken;
    }

    public void setAuthToken(authToken authToken) {
        this.authToken = authToken;
    }

    public boolean isVerified() {
        return this.verified;
    }

    public boolean getVerified() {
        return this.verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public Set<Role> getRoles() {
        return this.roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return this.surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

}

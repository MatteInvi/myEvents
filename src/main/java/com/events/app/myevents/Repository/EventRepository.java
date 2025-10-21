package com.events.app.myevents.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.events.app.myevents.Model.Event;
import com.events.app.myevents.Model.User;

public interface EventRepository extends JpaRepository<Event, Integer> {
    public List<Event> findByUser(User user);
    public List<Event> findByNameContainingIgnoreCase(String name);
    public List<Event> findByUserAndNameContainingIgnoreCase(User user, String name);
}

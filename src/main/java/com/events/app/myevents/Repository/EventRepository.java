package com.events.app.myevents.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.events.app.myevents.Model.Event;

public interface EventRepository extends JpaRepository<Event, Integer> {
    
}

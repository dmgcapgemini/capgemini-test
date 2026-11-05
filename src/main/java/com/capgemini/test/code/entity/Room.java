package com.capgemini.test.code.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "rooms")
public class Room {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
  private String name;

  @OneToMany(mappedBy = "room")
  private List<User> users = new ArrayList<>();

  protected Room() {
  }

  public Long getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public List<User> getUsers() {
    return users;
  }
}

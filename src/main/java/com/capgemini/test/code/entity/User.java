package com.capgemini.test.code.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false, unique = true)
  private String email;

  @Column(nullable = false, unique = true)
  private String dni;

  @Column(nullable = false, unique = true)
  private String phone;

  @Column(name = "role", nullable = false)
  private String rol;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "room_id")
  private Room room;

  protected User() {
  }

  public User(String name, String email, String dni, String phone, String rol, Room room) {
    this.name = name;
    this.email = email;
    this.dni = dni;
    this.phone = phone;
    this.rol = rol;
    this.room = room;
  }

  public Long getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getEmail() {
    return email;
  }

  public String getDni() {
    return dni;
  }

  public String getPhone() {
    return phone;
  }

  public String getRol() {
    return rol;
  }
}

package com.github.kusumotolab.tc2p.service;

public class Services {

  private static final Services instance = new Services();

  private Services() {
  }

  static Services getInstance() {
    return instance;
  }
}

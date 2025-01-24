/*
 * Copyright 2018 C Thing Software
 * All rights reserved.
 */
package org.cthing.testproject;

/**
 * Provides a greeting.
 */
public class Hello {

    private final String message;

    /**
     * Constructs the hello object.
     *
     * @param msg  Message to users.
     */
    public Hello(final String msg) {
        this.message = msg;
    }

    public String getMessage() {
        return this.message;
    }

    /**
     * Program entry point.
     *
     * @param args  Command line arguments
     */
    public static void main(final String[] args) {
        final Hello hello = new Hello("hello " + System.getProperty("commandVersion"));
        System.out.println(hello.getMessage());
    }
}

package ua.pp.chuprin.smssender.shared;

import org.alljoyn.bus.SessionOpts;

public interface Config {
    public static final String SERVICE_BUS_NAME = "ua.pp.chuprin.smssender.bus";
    public static final short SERVICE_PORT = 42;
    public static final SessionOpts DEFAULT_SESSION_OPTIONS = new SessionOpts();
}

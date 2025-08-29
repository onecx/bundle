package org.tkit.onecx.bundle.service;

import java.util.logging.Level;

import org.jboss.logmanager.Logger;

public class LogLevelUtil {
    public static void setLogLevel(LogLevel verbosity) {
        Logger rootLogger = Logger.getLogger("");
        rootLogger.setLevel(Level.parse(verbosity.toString()));
    }

    public enum LogLevel {
        INFO,
        DEBUG,
        WARN,
        ERROR
    }
}

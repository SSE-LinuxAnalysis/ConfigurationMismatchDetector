package de.uni_hildesheim.sse.smell.util;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * A thread-safe singleton logger.
 * 
 * @author Adam Krafczyk
 */
public class Logger {
    
    /**
     * The singleton instance. <code>null</code> until one of the init Methods is called.
     */
    public static Logger INSTANCE;
    
    /**
     * Initializes the logger to log to stdout in UTF-8.
     */
    public static void init() {
        init(System.out);
    }
    
    /**
     * Initializes the logger to log in UTF-8.
     * 
     * @param target The output target of the logger. The logger will only write to it if it obtains a lock on it.
     */
    public static void init(OutputStream target) {
        init(target, Charset.forName("UTF-8"));
    }
    
    
    /**
     * Initializes the logger.
     * 
     * @param target The output target of the logger. The logger will only write to it if it obtains a lock on it.
     * @param charset The charset the logger writes in.
     */
    public static void init(OutputStream target, Charset charset) {
        INSTANCE = new Logger(target, charset);
    }
    
    private OutputStream target;
    
    private Charset charset;
    
    private long started;
    
    private Logger(OutputStream target, Charset charset) {
        this.target = target;
        this.charset = charset;
        this.started = System.currentTimeMillis();
    }
    
    /**
     * Creates a "header" prefix for log lines. The lines contain the specified log
     * level, the name of the current thread and the time.
     * 
     * @param level The log level that will be used.
     * @return A string in the format "[level] [time] [threadName] "
     */
    private String constructHeader(String level) {
        StringBuffer hdr = new StringBuffer();
        long time = System.currentTimeMillis();
        
        hdr.append('[').append(level).append("] [")
                .append((time - started) / 1000).append("] [")
                .append(Thread.currentThread().getName()).append("] ");
        return hdr.toString();
    }
    
    /**
     * Writes a single log entry consisting of the specified lines with the specified log level to the target.
     * Internally, a lock on {@link #target} is acquired to ensure that messages are not broken up
     * in a multi-threaded environment.
     * 
     * @param level The log level to be written.
     * @param lines The lines that are written together as one log entry.
     */
    private void log(String level, String... lines) {
        String header = constructHeader(level);
        String indent = header.replaceAll(".", " ");
        
        StringBuffer str = new StringBuffer(header);
        for (int i = 0; i < lines.length; i++) {
            if (i != 0) {
                str.append(indent);
            }
            str.append(lines[i]).append('\n');
        }
        byte[] bytes = str.toString().getBytes(charset);
        
        synchronized (target) {
            try {
                target.write(bytes);
                target.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Logs a log entry with the log level "info".
     * 
     * @param lines The content of the log entry.
     */
    public void logInfo(String... lines) {
        log("info", lines);
    }
    
    /**
     * Logs a log entry with the log level "warning".
     * 
     * @param lines The content of the log entry.
     */
    public void logWarning(String... lines) {
        log("warning", lines);
    }
    
    /**
     * Logs a log entry with the log level "error".
     * 
     * @param lines The content of the log entry.
     */
    public void logError(String... lines) {
        log("error", lines);
    }
    
    /**
     * Converts a given exception to a string and adds the lines to the list.
     * The string will contain the stack trace, much like {@link Throwable#printStackTrace()}.
     * Additionally, the causing exceptions are converted into strings, too.
     * 
     * @param exc The exception to convert into a string.
     * @param lines The output target. The lines are appended to this list.
     */
    private void exceptionToString(Throwable exc, List<String> lines) {
        lines.add(exc.toString());
        
        StackTraceElement[] stack = exc.getStackTrace();
        for (int i = 0; i < stack.length; i++) {
            lines.add("    at " + stack[i].toString());
        }
        
        Throwable cause = exc.getCause();
        if (cause != null) {
            lines.add("Caused by:");
            exceptionToString(cause, lines);
        }
    }
    
    /**
     * Creates a log entry from the given comment and exception. The log level is "error".
     * 
     * @param comment A comment that is displayed above the exception. A ":" is appended to it by this method.
     * @param exc The exception to log. A stack trace will be logged.
     */
    public void logException(String comment, Throwable exc) {
        List<String> lines = new ArrayList<>(exc.getStackTrace().length + 2);
        lines.add(comment + ":");
        exceptionToString(exc, lines);
        log("error", lines.toArray(new String[0]));
    }

}

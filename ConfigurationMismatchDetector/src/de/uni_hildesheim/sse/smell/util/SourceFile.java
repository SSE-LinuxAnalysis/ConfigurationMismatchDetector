package de.uni_hildesheim.sse.smell.util;

import java.io.File;

import de.uni_hildesheim.sse.smell.util.logic.Formula;

/**
 * Represents a single source file in the Linux source tree.
 * 
 * @author Adam Krafczyk
 */
public class SourceFile {

    private File path;
    
    private Formula presenceCondition;
    
    /**
     * Creates a new source file.
     * 
     * @param path The path relative to the Linux source tree root.
     */
    public SourceFile(File path) {
        this.path = path;
    }
    
    /**
     * @return The path of this source file, relative to the Linux source tree root.
     */
    public File getPath() {
        return path;
    }
    
    /**
     * @param presenceCondition The presence condition of this file, as imposed by the build system.
     */
    public void setPresenceCondition(Formula presenceCondition) {
        this.presenceCondition = presenceCondition;
    }
    
    /**
     * @return The presence condition of this file, as imposed by the build system.
     */
    public Formula getPresenceCondition() {
        return presenceCondition;
    }
    
}

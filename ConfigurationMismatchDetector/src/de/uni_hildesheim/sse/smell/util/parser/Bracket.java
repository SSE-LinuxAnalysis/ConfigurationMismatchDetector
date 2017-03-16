package de.uni_hildesheim.sse.smell.util.parser;

/**
 * A bracket token of an expression to be parsed.
 * 
 * @author Adam Krafczyk
 */
public final class Bracket extends Token {
    
    private boolean closing;
    
    /**
     * Creates a bracket token.
     * 
     * @param closing Whether this is an opening or closing bracket.
     */
    public Bracket(boolean closing) {
        this.closing = closing;
    }
    
    /**
     * @return Whether this is an opening or closing bracket.
     */
    public boolean isClosing() {
        return closing;
    }
    
    @Override
    public String toString() {
        return "[Bracket: " + (closing ? "closing" : "open") + "]";
    }
    
}

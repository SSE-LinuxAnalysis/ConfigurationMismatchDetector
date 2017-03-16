package de.uni_hildesheim.sse.smell.util.logic;


/**
 * A boolean conjunction operator (AND).
 * 
 * @author Adam Krafczyk
 */
public final class Conjunction extends Formula {

    private static final long serialVersionUID = 8595985320940207982L;

    private Formula left;
    
    private Formula right;
    
    /**
     * Creates a boolean conjunction (AND).
     * 
     * @param left The left operand.
     * @param right The right operand.
     */
    public Conjunction(Formula left, Formula right) {
        this.left = left;
        this.right = right;
    }
    
    /**
     * @return The left operand.
     */
    public Formula getLeft() {
        return left;
    }
    
    /**
     * @return The right operand.
     */
    public Formula getRight() {
        return right;
    }
    
    @Override
    public boolean evaluate() {
        return left.evaluate() && right.evaluate();
    }

    @Override
    public String toString() {
        return "(" + left.toString() + " && " + right.toString() + ")";
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Conjunction) {
            Conjunction other = (Conjunction) obj;
            return left.equals(other.getLeft()) && right.equals(other.getRight());
        }
        return false;
    }

}

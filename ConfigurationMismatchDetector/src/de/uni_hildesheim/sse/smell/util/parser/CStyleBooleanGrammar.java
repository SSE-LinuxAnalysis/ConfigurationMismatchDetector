package de.uni_hildesheim.sse.smell.util.parser;

import de.uni_hildesheim.sse.smell.util.logic.Conjunction;
import de.uni_hildesheim.sse.smell.util.logic.Disjunction;
import de.uni_hildesheim.sse.smell.util.logic.Formula;
import de.uni_hildesheim.sse.smell.util.logic.Negation;
import de.uni_hildesheim.sse.smell.util.logic.Variable;

/**
 * A {@link Grammar} for C-style boolean expressions.
 * 
 * <p>
 * Examples:
 * <ul>
 *      <li><code>A && B</code></li>
 *      <li><code>AaA_bcd || D && !E</code></li>
 *      <li><code>((A && B) || !(C || B)) && !E</code></li>
 * </ul>
 * </p>
 * 
 * @author Adam Krafczyk
 */
public class CStyleBooleanGrammar extends Grammar<Formula> {

    protected static final Operator AND = new Operator("&&", true, 2);
    
    protected static final Operator OR = new Operator("||", true, 2);
    
    protected static final Operator NOT = new Operator("!", false, 1);
    
    private VariableCache cache;
    
    /**
     * Creates this grammar with the given variable cache. The cache is used
     * to create every single {@link Variable}, to ensure that no two different
     * {@link Variable} objects with the same variable name exist.
     * 
     * @param cache The cache to use, or <code>null</code>.
     */
    public CStyleBooleanGrammar(VariableCache cache) {
        this.cache = cache;
    }
    
    @Override
    public Operator getOperator(char[] str, int i) {
        if (str[i] == '!') {
            return NOT;
        }
        
        if (str[i] == '&' && str[i + 1] == '&') {
            return AND;
        }
        
        if (str[i] == '|' && str[i + 1] == '|') {
            return OR;
        }
        
        return null;
    }

    @Override
    public boolean isWhitespaceChar(char[] str, int i) {
        return str[i] == ' ';
    }

    @Override
    public boolean isOpeningBracketChar(char[] str, int i) {
        return str[i] == '(';
    }

    @Override
    public boolean isClosingBracketChar(char[] str, int i) {
        return str[i] == ')';
    }

    @Override
    public boolean isIdentifierChar(char[] str, int i) {
        return (str[i] >= 'a' && str[i] <= 'z')
                || (str[i] >= 'A' && str[i] <= 'Z')
                || (str[i] >= '0' && str[i] <= '9')
                || (str[i] == '_');
    }


    @Override
    public Formula makeUnaryFormula(Operator operator, Formula child) throws ExpressionFormatException {
        if (operator.equals(NOT)) {
            return new Negation(child);
        } else {
            throw new ExpressionFormatException("Unkown operator: " + operator);
        }
    }

    @Override
    public Formula makeBinaryFormula(Operator operator, Formula left, Formula right) throws ExpressionFormatException {
        if (operator.equals(AND)) {
            return new Conjunction(left, right);
        } else if (operator.equals(OR)) {
            return new Disjunction(left, right);
        } else {
            throw new ExpressionFormatException("Unkown operator: " + operator);
        }
    }

    @Override
    public Formula makeIdentifierFormula(String identifier) throws ExpressionFormatException {
        if (this.cache != null) {
            return this.cache.getVariable(identifier);
        }
        return new Variable(identifier);
    }

}

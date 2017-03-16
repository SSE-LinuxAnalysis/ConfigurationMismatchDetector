package de.uni_hildesheim.sse.smell.util;

import de.uni_hildesheim.sse.smell.util.logic.Formula;
import de.uni_hildesheim.sse.smell.util.logic.Negation;
import de.uni_hildesheim.sse.smell.util.logic.True;
import de.uni_hildesheim.sse.smell.util.logic.Variable;
import de.uni_hildesheim.sse.smell.util.parser.CStyleBooleanGrammar;
import de.uni_hildesheim.sse.smell.util.parser.ExpressionFormatException;
import de.uni_hildesheim.sse.smell.util.parser.Grammar;
import de.uni_hildesheim.sse.smell.util.parser.Operator;
import de.uni_hildesheim.sse.smell.util.parser.VariableCache;

/**
 * A {@link Grammar} for parsing the presence conditions generated by KbuildMiner.
 * 
 * <p>
 * Examples:
 * <ul>
 *      <li><code>((CRYPTO_AES_586 == "y") || (CRYPTO_AES_586 == "m"))</code></li>
 *      <li><code>((64BIT == "y") && ((CRYPTO_AES_NI_INTEL == "y") || (CRYPTO_AES_NI_INTEL == "m")))</code></li>
 *      <li><code>((X86_CMPXCHG64 != "y") && (X86_32 == "y"))</code></li>
 *      <li><code>!(X86_32 == "y")</code></li>
 *      <li><code>[TRUE]</code></li>
 * </ul>
 * </p>
 * 
 * @author Adam Krafczyk
 */
public class KbuildMinerPcGrammar extends CStyleBooleanGrammar {

    /**
     * Creates this grammar with the given variable cache. The cache is used
     * to create every single {@link Variable}, to ensure that no two different
     * {@link Variable} objects with the same variable name exist.
     * 
     * @param cache The cache to use, or <code>null</code>.
     */
    public KbuildMinerPcGrammar(VariableCache cache) {
        super(cache);
    }
    
    @Override
    public Operator getOperator(char[] str, int i) {
        if (str[i] == '!' && str[i + 1] != '=') {
            return CStyleBooleanGrammar.NOT;
        }
        
        if (str[i] == '&' && str[i + 1] == '&') {
            return CStyleBooleanGrammar.AND;
        }
        
        if (str[i] == '|' && str[i + 1] == '|') {
            return CStyleBooleanGrammar.OR;
        }
        
        return null;
    }
    
    @Override
    public boolean isWhitespaceChar(char[] str, int i) {
        if (super.isWhitespaceChar(str, i)) {
            // make sure that spaces around != and == don't interrupt the identifier
            
            if (i >= 2) {
                if (str[i - 1] == '=' && (str[i - 2] == '!' || str[i - 2] == '=')) {
                    return false;
                }
            }
            if (i < str.length - 2) {
                if ((str[i + 1] == '!' || str[i + 1] == '=') && str[i + 2] == '=') {
                    return false;
                }
            }
            
            return true;
        }
        return false;
    }
    
    @Override
    public boolean isIdentifierChar(char[] str, int i) {
        return super.isIdentifierChar(str, i)
                || (str[i] == '!')
                || (str[i] == '=')
                || (str[i] == '"')
                || (str[i] == '[')
                || (str[i] == ']')
                || (str[i] == ' ');
    }
    
    @Override
    public Formula makeIdentifierFormula(String identifier) throws ExpressionFormatException {
        if (identifier.contains("[") || identifier.contains("]")) {
            if (identifier.equals("[TRUE]")) {
                return new True();
            } else {
                throw new ExpressionFormatException("Invalid identifier: " + identifier);
            }
        }

        if (identifier.contains("==")) {
            identifier = identifier.replace(" =", "=").replace("= ", "=");
            
            int equalPos = identifier.indexOf('=');
            String varName = identifier.substring(0, equalPos);
            
            if (identifier.substring(equalPos + 2).equals("\"y\"")
                    || identifier.substring(equalPos + 2).equals("\"yes\"")) {
                return super.makeIdentifierFormula("CONFIG_" + varName);
                
            } else if (identifier.substring(equalPos + 2).equals("\"m\"")) {
                return super.makeIdentifierFormula("CONFIG_" + varName + "_MODULE");
                
            } else {
                throw new ExpressionFormatException("Invalid identifier: " + identifier);
            }
            
        } else if (identifier.contains("!=")) {
            return new Negation(makeIdentifierFormula(identifier.replace('!', '=')));
            
        } else {
            if (!identifier.matches("[a-zA-Z0-9_]+")) {
                throw new ExpressionFormatException("Invalid identifier: " + identifier);
            }
            return super.makeIdentifierFormula("CONFIG_" + identifier);
            
        }
        
    }
    
}

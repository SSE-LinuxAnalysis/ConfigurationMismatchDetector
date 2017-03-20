package net.ssehub.configmismatches.spaces.io;

import java.io.File;
import java.util.regex.Matcher;

public class CodeVariableReader extends VariableReader {
    private static final String VARIABLE_IDENTIFIER_PATTERN = "(\\p{Alnum}|_)";
    
    
    public CodeVariableReader(File file) {
        super(file, "(CONFIG_)(" + VARIABLE_IDENTIFIER_PATTERN + "+)");
    }

    @Override
    protected void extracetVariables(Matcher matcher) {
        while (matcher.find()) {
            addVariable(matcher.group(2));
        }
    }

}

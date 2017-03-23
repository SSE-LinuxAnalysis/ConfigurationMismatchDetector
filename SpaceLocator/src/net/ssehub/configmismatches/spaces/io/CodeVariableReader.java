package net.ssehub.configmismatches.spaces.io;

import java.io.File;
import java.util.regex.Matcher;

/**
 * Extracts Kconfig variables from code and Kbuild files.
 * @author El-Sharkawy
 *
 */
public class CodeVariableReader extends VariableReader {
    
    public CodeVariableReader(File file, String variablePattern) {
        super(file, variablePattern);
    }

    @Override
    protected void extractVariables(Matcher matcher) {
        while (matcher.find()) {
            addVariable(matcher.group(2));
        }
    }

    @Override
    protected void addVariable(String variable) {
        if (variable.endsWith("_MODULE")) {
            int length = variable.length() - "_MODULE".length();
            length = Math.max(0, length);
            variable = variable.substring(0, length);
        }
        super.addVariable(variable);
    }
}

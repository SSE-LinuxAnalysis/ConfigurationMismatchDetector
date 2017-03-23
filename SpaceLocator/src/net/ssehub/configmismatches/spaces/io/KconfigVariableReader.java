package net.ssehub.configmismatches.spaces.io;

import java.io.File;
import java.util.regex.Matcher;

/**
 * Extracts Kconfig variables from Kconfig files.
 * @author El-Sharkawy
 *
 */
public class KconfigVariableReader extends VariableReader {

    public KconfigVariableReader(File file, String variablePattern) {
        super(file, variablePattern);
    }

    @Override
    protected void extractVariables(Matcher matcher) {
        while (matcher.find()) {
            addVariable(matcher.group(2));
        }
    }
}

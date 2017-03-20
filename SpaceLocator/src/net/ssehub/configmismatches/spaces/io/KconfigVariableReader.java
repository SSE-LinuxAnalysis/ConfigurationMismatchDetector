package net.ssehub.configmismatches.spaces.io;

import java.io.File;
import java.util.regex.Matcher;

public class KconfigVariableReader extends VariableReader {

    public KconfigVariableReader(File file) {
        super(file, "^(.*config) (.*)$");
    }

    @Override
    protected void extracetVariables(Matcher matcher) {
        while (matcher.find()) {
            addVariable(matcher.group(2));
        }
    }
}

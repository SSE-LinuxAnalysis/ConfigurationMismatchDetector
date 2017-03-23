package net.ssehub.configmismatches.spaces.io;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;

/**
 * Reads a file and returns all variables, which where used in this file.
 * @author El-Sharkawy
 *
 */
public class VariableReader {

    private File file;
    private String variablePattern;
    private Set<String> usedVariables;
    
    public VariableReader(File file, String variablePattern) {
        this.file = file;
        this.variablePattern = variablePattern;
        usedVariables = new HashSet<String>();
    }
    
    public Set<String> readFile() {
        try {
            String contents = FileUtils.readFileToString(file, (String) null);
            Matcher matcher = Pattern.compile(variablePattern).matcher(contents);
            extractVariables(matcher);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        return usedVariables;
    }
    
    protected void extractVariables(Matcher matcher) {
        while (matcher.find()) {
            addVariable(matcher.group());
        }
    }
    
    protected void addVariable(String variable) {
        usedVariables.add(variable);
    }
}

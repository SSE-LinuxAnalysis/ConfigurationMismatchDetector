package de.uni_hildesheim.sse.smell.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import de.uni_hildesheim.sse.smell.util.logic.Formula;
import de.uni_hildesheim.sse.smell.util.parser.ExpressionFormatException;
import de.uni_hildesheim.sse.smell.util.parser.Parser;
import de.uni_hildesheim.sse.smell.util.parser.VariableCache;

public class KbuildMiner {
    
    public static List<SourceFile> readOutput(File pcFile) throws IOException {
        List<SourceFile> result = new LinkedList<>();
        
        BufferedReader in = new BufferedReader(new FileReader(pcFile));
        
        VariableCache cache = new VariableCache();
        Parser<Formula> pcParser = new Parser<Formula>(new KbuildMinerPcGrammar(cache));
        
        String line;
        while ((line = in.readLine()) != null) {
            String filename = line.substring(0, line.indexOf(':'));
            SourceFile file = new SourceFile(new File(filename));
            result.add(file);
            
            String pc = line.substring(filename.length() + 2);
            
            if (pc.contains("InvalidExpression()")) {
                Logger.INSTANCE.logWarning("Presence condition for file " + filename + " is invalid");
                
            } else {
                try {
                    file.setPresenceCondition(pcParser.parse(pc));
                } catch (ExpressionFormatException e) {
                    Logger.INSTANCE.logException("Couldn't parse expression \"" + pc + "\"", e);
                }
            }
        }
        
        in.close();
        
        return result;
    }

}

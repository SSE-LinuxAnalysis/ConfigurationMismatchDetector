package de.uni_hildesheim.sse.smell;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import de.uni_hildesheim.sse.smell.data.IDataElement;
import de.uni_hildesheim.sse.smell.data.VariableWithSolutions;
import de.uni_hildesheim.sse.smell.filter.input.VariableWithSolutionsReader;

public class NonIfdefLocationFinder {

    private File linuxTree;
    
    private Line[] relevantLines;
    
    public NonIfdefLocationFinder(File linuxTree) {
        this.linuxTree = linuxTree;
        
        System.out.println("Searching relevant lines in Linux tree...");
        relevantLines = findRelevantLines(linuxTree).toArray(new Line[0]);
        System.out.println("done");
    }
    
    public void run() throws Exception {
        final String version = "linux-4.4.1";
        final String arch = "x86";
        String result = "output/" + version + "/" + arch + ".config_mismatch.result.csv";
        
        List<IDataElement> elements = new VariableWithSolutionsReader(result, true).run(null, new NullProgressPrinter());
        
        for (IDataElement element : elements) {
            VariableWithSolutions var = (VariableWithSolutions) element;
            
            printIfNonIdef(var.getVariable());
        }
        
    }
    
    private void printIfNonIdef(String variable) {
        for (Line line : findLines(variable)) {
            String l = line.text.trim();
            if (!l.startsWith("#if") && !l.startsWith("#else") && !l.startsWith("#elif") && !l.startsWith("#endif")) {
                System.out.println(line.filename + "  " + line.lineNumber + ": " + line.text);
            }
        }
    }
    
    private List<Line> findLines(String variable) {
        List<Line> results = new LinkedList<>();
        
        // don't differentiate between module and normal variable
        if (variable.endsWith("_MODULE")) {
            variable = variable.substring(0, variable.length() - "_MODULE".length());
        }
        
        String kconfigName = variable;
        if (kconfigName.startsWith("CONFIG_")) {
            kconfigName = kconfigName.substring("CONFIG_".length());
        }
        
        
        Pattern kconfigPattern = Pattern.compile("^\\s*(menu)?config\\s*" + kconfigName + "\\s*$");
        Pattern sourcePattern = Pattern.compile(".*" + variable + "(_MODULE)?(([^A-Za-z0-9_].*)|$)");
        
        for (Line line : relevantLines) {
            if (line.filename.contains(File.separatorChar + "Kconfig")) {
                if (kconfigPattern.matcher(line.text).matches()) {
                    results.add(line);
                }
            } else {
                if (sourcePattern.matcher(line.text).matches()) {
                    results.add(line);
                }
            }
        }
        
        return results;
    }

    private List<Line> findRelevantLines(File fileTree) {
        List<Line> results = new LinkedList<>();
        
        File[] filtered = fileTree.listFiles(new FileFilter() {
            
            @Override
            public boolean accept(File pathname) {
                return pathname.isDirectory()
                        || pathname.getName().endsWith(".c")
                        || pathname.getName().endsWith(".S")
                        || pathname.getName().endsWith(".h");
//                        || pathname.getName().startsWith("Kconfig")
//                        || pathname.getName().startsWith("Makefile")
//                        || pathname.getName().startsWith("Kbuild");
            }
        });
        
        for (File file : filtered) {
            if (file.isDirectory()) {
                results.addAll(findRelevantLines(file));
            } else {
                try {
                    if (file.getName().startsWith("Kconfig")) {
                        results.addAll(findRelevantLinesInFile("^\\s*(menu)?config\\s*[A-Za-z0-9_]+\\s*$", file));
                    } else {
                        results.addAll(findRelevantLinesInFile(".*CONFIG_.*", file));
                    }
                } catch (IOException e) {
                    System.out.println("Can't search in file \"" + file.getName() + "\":");
                    e.printStackTrace(System.out);
                }
            }
        }
        
        return results;
    }
    
    private List<Line> findRelevantLinesInFile(String regex, File file) throws IOException {
        List<Line> results = new LinkedList<>();
        
        Pattern pattern = Pattern.compile(regex);
        
        BufferedReader in = new BufferedReader(new FileReader(file));
        
        String line;
        
        int lineNumber = 1;
        while ((line = in.readLine()) != null) {
            if (pattern.matcher(line).matches()) {
                results.add(new Line(relativeName(file, linuxTree), lineNumber, line));
            }
            
            lineNumber++;
        }
        
        in.close();
        
        return results;
    }
    
    private static String relativeName(File file, File directory) {
        return file.getAbsolutePath().substring(directory.getAbsolutePath().length());
    }
    
    private static final class Line {
        
        private String filename;
        
        private int lineNumber;
        
        private String text;

        public Line(String filename, int lineNumber, String text) {
            this.filename = filename;
            this.lineNumber = lineNumber;
            this.text = text;
        }
        
        
    }
    
    public static void main(String[] args) throws Exception {
        new NonIfdefLocationFinder(new File("E:/research/linux_versions/linux-4.4.1")).run();
    }
    
}

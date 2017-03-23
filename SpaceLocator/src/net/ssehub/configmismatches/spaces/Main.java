package net.ssehub.configmismatches.spaces;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import net.ssehub.configmismatches.spaces.io.CodeVariableReader;
import net.ssehub.configmismatches.spaces.io.KconfigVariableReader;
import net.ssehub.configmismatches.spaces.io.VariableReader;

/**
 * Program to locate the occurrences of Kconfig variables.
 * @author El-Sharkawy
 *
 */
public class Main {
    // Important RegEx commands
    /**
     * Case insensitve for unicode.
     * @see <a href="https://blogs.oracle.com/xuemingshen/entry/case_insensitive_matching_in_java">
     * Case-Insensitive Matching in Java RegEx</a>
     */
    private static final String UNIX_IGNORE_CASE = "(?iu)";
    
    /**
     * Enable multiline mode.
     * @see <a href=http://docs.oracle.com/javase/7/docs/api/java/util/regex/Pattern.html#MULTILINE">
     * Java API: RegEx pattern (MULTILINE) </a>
     */
    private static final String MULTILINE_MODE = "(?m)";
    
    /**
     * Enable multiline mode.
     * @see <a href=http://docs.oracle.com/javase/7/docs/api/java/util/regex/Pattern.html#MULTILINE">
     * Java API: RegEx pattern (MULTILINE) </a>
     */
    
    // Variable detection patterns    
    private static final String KCONFIG_VAR_PATTERN = MULTILINE_MODE + "^(.*config) (.*)$";
    private static final String VARIABLE_IDENTIFIER_PATTERN = "(\\p{Alnum}|_)";
    private static final String CODE_VAR_PATTERN = MULTILINE_MODE + "(CONFIG_)(" + VARIABLE_IDENTIFIER_PATTERN + "+)";
    
    /**
     * Both kind of separators to make input and system path separator platform independent.
     */
    private static final String PATH_SEPARATOR = "(/|\\\\)";
    private static final String KCONFIG_FILE_PATTERN = UNIX_IGNORE_CASE + "Kconfig.*";
    private static final String CODE_FILE_PATTERN = UNIX_IGNORE_CASE + ".*\\.(c|h|S)";
    private static final String MAKE_FILE_PATTERN = UNIX_IGNORE_CASE + "(Makefile|Kbuild).*";
    private static final String EXCLUSION_PATH_PATTERN = UNIX_IGNORE_CASE + PATH_SEPARATOR
        + "?(Documentation|samples|scripts)" + PATH_SEPARATOR + ".*";
    
    /**
     * Starts the program.
     * @param args Parameters of this program:
     * <ol>
     *   <li>Root location of the Linux tree to analyze.</li>
     *   <li>Optional, the Kconfig file pattern (as Java RegEx) .</li>
     *   <li>Optional, if specified the code file pattern (as one RegEx) to consider.</li>
     *   <li>Optional, if specified the Kbuild file pattern (as one RegEx) to consider.</li>
     *   <li>Optional, path exclusion filter (e.g. do not consider Documentation).</li>
     * </ol>
     */
    public static void main(String[] args) {
        if (args != null && args.length > 0) {
            String rootFolder = args[0];
            String kconfigPattern = args.length > 1 ? args[1] : KCONFIG_FILE_PATTERN;
            String codePattern = args.length > 2 ? args[2] : CODE_FILE_PATTERN;
            String makePattern = args.length > 3 ? args[3] : MAKE_FILE_PATTERN;
            String exlusionPattern = args.length > 4 ? args[4] : EXCLUSION_PATH_PATTERN;
            DateFormat df = DateFormat.getDateTimeInstance();
            String formattedDate = df.format(new Date ());
            System.out.println("Start parameters:");
            System.out.println("Program started: " + formattedDate);
            System.out.println("Path: " + rootFolder);
            System.out.println("Kconfig file pattern: " + kconfigPattern);
            System.out.println("Code file pattern: " + codePattern);
            System.out.println("Kbuild file pattern: " + makePattern);
            System.out.println("Path exclusion pattern: " + exlusionPattern);
            System.out.println("Detection pattern for variables in Kconfig: " + KCONFIG_VAR_PATTERN);
            System.out.println("Detection pattern for variables in code/Kbuild: " + CODE_VAR_PATTERN);
            analyze(rootFolder, kconfigPattern, codePattern, makePattern, exlusionPattern);
        } else {
            System.err.println("Error: At least one parameter must be specfied:");
            System.err.println("1) Specification of root folder to analyze");
            System.err.println("2) Optional file pattern (Java RegEx), which Kconfig files shall be analysed.");
            System.err.println("3) Optional file pattern (Java RegEx), which code files shall be analysed.");
            System.err.println("4) path exclusion filter (e.g. do not consider Documentation).");
        }
    }
    
    private static void analyze(String rootFolder, String kconfigPattern, String codePattern, String makePattern,
        String exlusionPattern) {
        
        System.out.println("\nLog:");
        
        // First iteration: list all files
        String filePattern = "(" + kconfigPattern + "|" + codePattern + "|" + makePattern + ")";
        Path rootPath = Paths.get(rootFolder);
        Set<File> files = new HashSet<File>();
        System.out.println("Step 1 (of 4): Collecting files");
        try {
            // All folders, except for Documentation etc.
            Files.walk(rootPath).filter(p -> !rootPath.relativize(p).toString().matches(exlusionPattern))
                .map(f -> f.toFile())
                // Collect all Kconfig, code, and Kbuild files
                .filter(f -> !f.isDirectory() && f.getName().matches(filePattern))
                .forEach(files::add);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println(files.size() + " found.");
        
        // Second iteration: Collect variables from all Kconfig
        System.out.println("Step 2 (of 4): Parsing Kconfig files");
        Set<String> kconfigVariables = extractVariables(kconfigPattern, files, KconfigVariableReader.class,
            KCONFIG_VAR_PATTERN);
        
        // Third iteration: Collect variables from code
        System.out.println("Step 3 (of 4): Parsing code files");
        Set<String> codeVariables = extractVariables(codePattern, files, CodeVariableReader.class, CODE_VAR_PATTERN);
        codeVariables = intersection(codeVariables, kconfigVariables, "code files");
        
        // Forth iteration: Collect variables from Kbuild
        System.out.println("Step 4 (of 4): Parsing Kbuild files");
        Set<String> kbuildVariables = extractVariables(makePattern, files, CodeVariableReader.class, CODE_VAR_PATTERN);
        kbuildVariables = intersection(kbuildVariables, kconfigVariables, "Kbuild files");
        
        // Print statistics
        int nKconfigVars = kconfigVariables.size();
        int nCodeVars = codeVariables.size();
        int nKbuildVars = kbuildVariables.size();
        Set<String> kbuildAndCodeVars = intersection(kbuildVariables, codeVariables, null);
        int nKbuildAndCodeVars = kbuildAndCodeVars.size();
        kconfigVariables.removeAll(codeVariables);
        kconfigVariables.removeAll(kbuildVariables);
        int nOnlyKconfig = kconfigVariables.size();
        Set<String> onlyCodeVariables = new HashSet<>(codeVariables);
        onlyCodeVariables.removeAll(kbuildVariables);
        int nOnlyCodeVariables = onlyCodeVariables.size();
        Set<String> onlyKbuildVariables = new HashSet<>(kbuildVariables);
        onlyKbuildVariables.removeAll(codeVariables);
        int nOnlyKbuildVariables = onlyKbuildVariables.size();
        
        System.out.println("\nResults (Variables):");
        System.out.println("- Total No. of Kconfig Variables: " + percentageUsage(nKconfigVars, nKconfigVars));
        System.out.println("- Used in Code (and in Kbuild): " + percentageUsage(nCodeVars, nKconfigVars));
        System.out.println("- Used in Kbuild (and in Code): " + percentageUsage(nKbuildVars, nKconfigVars));
        System.out.println("- Used in Code + Kbuild: " + percentageUsage(nKbuildAndCodeVars, nKconfigVars));
        System.out.println("- Used only in Kconfig: " + percentageUsage(nOnlyKconfig, nKconfigVars));
        System.out.println("- Used only in Code: " + percentageUsage(nOnlyCodeVariables, nKconfigVars));
        System.out.println("- Used only in Kbuild: " + percentageUsage(nOnlyKbuildVariables, nKconfigVars));
    }

    protected static Set<String> extractVariables(String filePattern, Set<File> files,
        Class<? extends VariableReader> readerClass, String detectionPattern) {
        
        int index = 0;
        Set<File> filesToConsider = new HashSet<>();
        files.stream().filter(f -> f.getName().matches(filePattern)).forEach(filesToConsider::add);
        Set<String> codeVariables = new HashSet<>();
        int nFiles = filesToConsider.size();
        NumberFormat percentFormat = NumberFormat.getPercentInstance();
        percentFormat.setMaximumFractionDigits(2);

        System.out.println(filesToConsider.size() + " to consider in this step.");
        for (File file : filesToConsider) {
            index++;
            if (file.getName().matches(filePattern)) {
                try {
                    Constructor<? extends VariableReader> cons = readerClass.getConstructor(File.class, String.class);
                    VariableReader reader = cons.newInstance(file, detectionPattern);
                    codeVariables.addAll(reader.readFile());
                } catch (ReflectiveOperationException exc) {
                    // TODO Auto-generated catch block
                    exc.printStackTrace();
                }
            }
            
            if (index % 500 == 0) {
                System.out.println("Progress: " + percentFormat.format((double) index / nFiles));
            }
        }
        return codeVariables;
    }
    
    /**
     * Returns the intersection of both sets, without modifying the sets.
     * @param usedVariables First set (usually Kbuild of code variables)
     * @param definedVariables Second set (Kconfig variables)
     * @param logText if <tt>null</tt> nothing will be printed, otherwise this method logs how many variables have been
     *     removed.
     * @return The inersection of both sets.
     */
    private static Set<String> intersection(Set<String> usedVariables, Set<String> definedVariables, String logText) {
        Set<String> result = new HashSet<>(usedVariables);
        result.retainAll(definedVariables);
        
        if (null != logText) {
            int diff = usedVariables.size() - result.size();
            System.out.println(diff + " undefined variables were removed from " + logText + ".");
        }
        return result;
    }

    private static String percentageUsage(int nVariables, int nTotalVariables) {
        NumberFormat percentFormat = NumberFormat.getPercentInstance();
        percentFormat.setMaximumFractionDigits(2);
        
        return nVariables + " [" + percentFormat.format((double) nVariables / nTotalVariables) + "]";
    }
}

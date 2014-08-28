package org.elettronik.modelGenerator;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;

/**
 * Entry Point
 * @author sinesy
 *
 */
public class Main {
    
    @SuppressWarnings("static-access")
    public static void main(String[] args) throws Exception {
        Options options = new Options();
        boolean enableDebug = false;

        options.addOption(
                OptionBuilder
                .withArgName( "help" )
                .withLongOpt("help")
                .hasArg(false)
                .withDescription("Show help")
                .create("h"));
        
        options.addOption(
                OptionBuilder
                .withArgName( "class" )
                .withLongOpt("class")
                .hasArg(true)
                .withDescription("Model class name (with package)")
                .isRequired(true)
                .create("m"));
        
        options.addOption(
                OptionBuilder
                .withArgName("package")
                .withLongOpt("package")
                .hasArg(true)
                .withDescription("Package for json class")
                .isRequired(true)
                .create("p"));
        
        options.addOption(
                OptionBuilder
                .withArgName("name")
                .withLongOpt("name")
                .hasArg(true)
                .withDescription("Name of generated model")
                .isRequired(false)
                .create("n"));
        
        
        options.addOption(
                OptionBuilder
                .withArgName( "debug" )
                .withLongOpt("debug")
                .hasArg(false)
                .withDescription("Enable developer mode")
                .isRequired(false)
                .create("d"));

        try {
            CommandLineParser parser = new BasicParser();
            CommandLine cl = parser.parse(options, args);
            
            if (cl.hasOption("h")) {
                Main.printHelp(options);
            }
            enableDebug = cl.hasOption("d");
            
            Class<?> clazz = Class.forName(cl.getOptionValue("m"));
            String pack = cl.getOptionValue("p");
            
            ModelGenerator gen = new ModelGenerator(clazz, pack, cl.getOptionValue("n"));
            gen.generate();
        } catch (Exception ex) {
            if (enableDebug) {
                ex.printStackTrace();
            } else {
                System.err.println(ex.getMessage());
            }
            Main.printHelp(options);
        }
    }
    
    private static void printHelp(Options options) {
        HelpFormatter helpFormatter = new HelpFormatter();
        helpFormatter.printHelp("modelGenerator", options, true);
        System.exit(0);
    }
}

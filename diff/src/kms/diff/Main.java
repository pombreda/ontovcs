package kms.diff;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.Hashtable;

import kms.diff.Settings.Format;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.semanticweb.owlapi.io.UnparsableOntologyException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import ru.tpu.cc.kms.changes.CategorizedChangeSet;
import ru.tpu.cc.kms.changes.Change;
import ru.tpu.cc.kms.changes.render.ChangeRenderer;
import ru.tpu.cc.kms.changes.render.FunctionalSyntaxChangeRenderer;
import ru.tpu.cc.kms.changes.render.IndentedChangeRenderer;
import ru.tpu.cc.kms.statements.Statement;

class Settings {
	public static enum Format {
		COMPACT,
		INDENTED,
	};
	@Argument(required = true, index = 0, metaVar = "parent.owl", usage = "Parent version")
	public String parentFilename;
	@Argument(required = true, index = 1, metaVar = "child.owl", usage = "Child version")
	public String childFilename;
    @Option(name = "--summary", aliases = {"-s"}, usage = "Display changes summary", required = false)
    public Boolean summary = false;
    @Option(name = "--verbose", aliases = {"-v"}, usage = "Verbose output of errors", required = false)
    public Boolean verbose = false;
    @Option(name = "--measure", aliases = {"-m"}, usage = "Measure time spent", required = false)
    public Boolean measure = false;
    @Option(name = "--wait", aliases = {"-w"}, usage = "Do not exit, wait until user presses Enter", required = false)
    public Boolean wait = false;
    @Option(name = "--format", aliases = {"-f"}, metaVar = "format", usage = "Format of changes: compact or indented", required = false)
    public Format format = Format.COMPACT;
}

public class Main {

	/*
	 * Returns changes count
	 */
	private static int CompareFiles(String parent, String child,
			Format format, boolean measure, boolean summary, boolean verbose) {
		System.err.println("diff " + parent + " " + child);
		try {
	    	CategorizedChangeSet cs;
	    	if (measure) {
	    		Hashtable<String, Long> times = new Hashtable<String, Long>();
	    		cs = Comparer.compareAndMeasure(parent, child, times);
	    		// Times
	    		System.err.println("Loading time:    " + times.get("load") / 1000000 + " milliseconds");
	    		System.err.println("Building time:   " + times.get("build") / 1000000 + " milliseconds");
	            System.err.println("Comparing time:  " + times.get("compare") / 1000000 + " milliseconds");
	    		long totalDuration = times.get("load") + times.get("build") + times.get("compare");
	            System.err.println("Total time:      " + totalDuration / 1000000 + " milliseconds");
	            // Statistics
	            System.err.println(parent);
	    		Comparer.printStatistics(cs.getParent(), System.err);
	    		System.err.println(child);
	    		Comparer.printStatistics(cs.getChild(), System.err);
	    	}
	    	else
	    		cs = Comparer.compare(parent, child);
	    	if (summary) {
	    		// Calculate summary
	    		Comparer.PrintSummary(cs, System.out);
	    	}
	    	Collection<Change<Statement>> changes = cs.getAllChanges();
			ChangeRenderer cr = new FunctionalSyntaxChangeRenderer();
			if (format == Format.INDENTED)
				cr = new IndentedChangeRenderer();
	    	// Printing the changes
	        for (Change<Statement> c : changes)
	        	System.out.println(cr.getRendering(c));
	        System.out.println();
	        return changes.size();
		}
    	catch (IOException e) {
    		e.printStackTrace(System.err);
    	}
		catch (UnparsableOntologyException e) {
			if (verbose)
				System.err.println(e.toString());
			else
				System.err.println("Could not parse: " + e.getDocumentIRI().toString());
		}
    	catch (OWLOntologyCreationException e) {
    		e.printStackTrace(System.err);
    	}
		return 0;
	}

	private static int CompareDirectories(File parent, File child,
			Format format, boolean measure, boolean summary, boolean verbose) {
		int changesCount = 0;
		for (File file : parent.listFiles()) {
			String relative = parent.toURI().relativize(file.toURI()).getPath();
			File file2 = new File(child, relative);
			if (! file2.exists()) {
				System.err.println("Only in " + parent.getAbsolutePath() + ": " + file.getName());
				changesCount++;
				continue;
			}
			if (file.isFile() && file2.isDirectory()) {
				System.err.println("File " + file.getAbsolutePath() +
						" is a regular file while file " + file2.getAbsolutePath() +
						" is a directory");
				changesCount++;
				continue;
			}
			if (file.isDirectory() && file2.isFile()) {
				System.err.println("File " + file.getAbsolutePath() +
						" is a directory while file " + file2.getAbsolutePath() +
						" is a regular file");
				changesCount++;
				continue;
			}
		    if (file.isDirectory() && file2.isDirectory()) {
		    	changesCount += CompareDirectories(file, file2, format, measure, summary, verbose);
		    	continue;
		    }
		    if (file.isFile() && file2.isFile()) {
		        changesCount += CompareFiles(file.getAbsolutePath(),
		        		file2.getAbsolutePath(), format, measure, summary, verbose);
		        continue;
		    }
		}
		for (File file : child.listFiles()) {
			String relative = child.toURI().relativize(file.toURI()).getPath();
			File file2 = new File(parent, relative);
			if (! file2.exists()) {
				System.err.println("Only in " + child.getAbsolutePath() + ": " + file.getName());
				changesCount++;
				continue;
			}
		}
		return changesCount;
	}

	public static void main (String[] args)
    {
		Settings settings = new Settings();
		CmdLineParser parser = new CmdLineParser(settings);
        try {
            parser.parseArgument(args);
            File parent = new File(settings.parentFilename);
            File child = new File(settings.childFilename);
        	if (! parent.exists())
        		throw new FileNotFoundException(settings.parentFilename);
        	if (! child.exists())
        		throw new FileNotFoundException(settings.childFilename);
        	if (parent.isDirectory() && child.isDirectory()) {
        		CompareDirectories(parent, child,
            			settings.format, settings.measure, settings.summary, settings.verbose);
        	} else {
        		CompareFiles(settings.parentFilename, settings.childFilename,
        				settings.format, settings.measure, settings.summary, settings.verbose);
        	}
        } catch (CmdLineException e) {
        	System.err.print("Usage: owl2diff");
        	parser.printSingleLineUsage(System.err);
        	System.err.println();
            parser.printUsage(System.err);
            System.exit(64);
        } catch (FileNotFoundException e) {
			System.err.println("File not found: " + e.getMessage());
		}

        if (settings.wait) {
			try {
				System.in.read();
			} catch (IOException e) {
				e.printStackTrace();
			}
        }
    }
}

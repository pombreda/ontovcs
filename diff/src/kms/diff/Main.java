package kms.diff;

import java.io.File;
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
import ru.tpu.cc.kms.changes.render.ManchesterSyntaxChangeRenderer;
import ru.tpu.cc.kms.statements.Statement;

class Settings {
	public static enum Format {
		FUNCTIONAL,
		MANCHESTER,
	};
	@Argument(required = true, index = 0, metaVar = "parent.owl", usage = "Parent version")
	public String parentFilename;
	@Argument(required = true, index = 1, metaVar = "child.owl", usage = "Child version")
	public String childFilename;
    @Option(name = "--summary", aliases = {"-s"}, usage = "Display changes summary", required = false)
    public Boolean summary = false;
    @Option(name = "--measure", aliases = {"-m"}, usage = "Measure time spent", required = false)
    public Boolean measure = false;
    //@Option(name = "--format", aliases = {"-f"}, metaVar = "format", usage = "Format of changes: Functional (default) or Manchester", required = false)
    //public Format format = Format.FUNCTIONAL;
}

public class Main {

	/*
	 * Returns changes count
	 */
	private static int CompareFiles(String parent, String child,
			Format format, boolean measure, boolean summary) {
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
	    		Comparer.PrintSummary(cs, System.err);
	    	}
	    	Collection<Change<Statement>> changes = cs.getAllChanges();
			ChangeRenderer cr = new FunctionalSyntaxChangeRenderer();
			if (format == Format.MANCHESTER)
				cr = new ManchesterSyntaxChangeRenderer(true);
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
			System.err.println("Could not parse: " + e.getDocumentIRI().toString());
		}
    	catch (OWLOntologyCreationException e) {
    		e.printStackTrace(System.err);
    	}
		return 0;
	}

	private static int CompareDirectories(File parent, File child,
			Format format, Boolean measure, Boolean summary) {
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
		    	changesCount += CompareDirectories(file, file2, format, measure, summary);
		    	continue;
		    }
		    if (file.isFile() && file2.isFile()) {
		        changesCount += CompareFiles(file.getAbsolutePath(),
		        		file2.getAbsolutePath(), format, measure, summary);
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
        		throw new CmdLineException(parser, "File not found: " + settings.parentFilename);
        	if (! child.exists())
        		throw new CmdLineException(parser, "File not found: " + settings.childFilename);
        	if (parent.isDirectory() && child.isDirectory()) {
        		CompareDirectories(parent, child,
            			Format.FUNCTIONAL, settings.measure, settings.summary);
        	} else {
        		CompareFiles(settings.parentFilename, settings.childFilename,
        				Format.FUNCTIONAL, settings.measure, settings.summary);
        	}
        } catch (CmdLineException e) {
            // System.err.println("Usage: owl2diff parent.owl child.owl [--summary] [--measure] [--format functional|manchester]");
        	System.err.print("Usage: owl2diff");
        	parser.printSingleLineUsage(System.err);
        	System.err.println();
            parser.printUsage(System.err);
            System.exit(64);
        }

        // wait, so we can measure the amount of memory used
        if (settings.measure)
			try {
				System.in.read();
			} catch (IOException e) {
				e.printStackTrace();
			}
    }


}

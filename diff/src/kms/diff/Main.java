package kms.diff;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

import ru.tpu.cc.kms.changes.CategorizedChangeSet;
import ru.tpu.cc.kms.changes.Change;
import ru.tpu.cc.kms.changes.ChangesSummary;
import ru.tpu.cc.kms.statements.Statement;
import ru.tpu.cc.kms.EntityShortener;

class Settings {
    @Option(name = "--summary", usage = "Display changes summary", required = false)
    public Boolean summary = false;
    @Option(name = "--measure", usage = "Measure time spent", required = false)
    public Boolean measure = false;
    @Argument
    public ArrayList<String> extraArgs = new ArrayList<String>();
}

public class Main {

    public static void main (String[] args)
        throws OWLOntologyCreationException, OWLOntologyStorageException, IOException
    {
		Settings settings = new Settings();
		CmdLineParser parser = new CmdLineParser(settings);
        try {
            parser.parseArgument(args);
            String parentFilename = "";
            String childFilename = "";
            if (settings.extraArgs.size() > 0) {
            	parentFilename = settings.extraArgs.get(0);
            	if (! new File(parentFilename).exists())
            		throw new CmdLineException(parser, "File not found: " + parentFilename);
            }
            if (settings.extraArgs.size() > 1) {
            	childFilename = settings.extraArgs.get(1);
            	if (! new File(childFilename).exists())
            		throw new CmdLineException(parser, "File not found: " + childFilename);
            }
            else
            	throw new CmdLineException(parser, "Invalid arguments");
            // Compare
        	CategorizedChangeSet cs;
        	if (settings.measure)
        		cs = Comparer.compareAndMeasure(parentFilename, childFilename);
        	else
        		cs = Comparer.compare(parentFilename, childFilename);
        	if (settings.summary) {
        		// Calculate summary
        		ChangesSummary ca = new ChangesSummary(cs);
        		System.err.println("Total additions: " + cs.getAdditions().size());
        		System.err.println("Total removals: " + cs.getRemovals().size());
        		if (null != ca.getNewFormat())
        			System.err.println("Ontology format changed to: " + ca.getNewFormat());
        		if (null != ca.getNewOntologyIRI())
        			System.err.println("Ontology IRI changed to: " + ca.getNewOntologyIRI());
        		if (null != ca.getNewVersionIRI())
        			System.err.println("Version IRI changed to: " + ca.getNewVersionIRI());
        		if (ca.getNewEntities().size() > 0) {
        			System.err.println("New:");
        			for (OWLEntity e: ca.getNewEntities())
        				System.err.println("    " + e.getEntityType() + ": " + new EntityShortener(e));
        		}
				if (ca.getModifiedEntities().size() > 0) {
					System.err.println("Modified:");
					for (OWLEntity e: ca.getModifiedEntities())
						System.err.println("    " + e.getEntityType() + ": " + new EntityShortener(e));
				}
				if (ca.getRemovedEntities().size() > 0) {
					System.err.println("Removed:");
					for (OWLEntity e: ca.getRemovedEntities())
						System.err.println("    " + e.getEntityType() + ": " + new EntityShortener(e));
				}
        	}
        	Collection<Change<Statement>> changes = cs.getAllChanges();
        	// Printing the changes
            for (Object c : changes)
                System.out.println(c.toString());
            // exit with code 1 if there were changes
            if (changes.size() > 0)
            	System.exit(1);
        } catch (CmdLineException e) {
            System.err.println("Usage: owl2diff parent.owl child.owl [--summary] [--measure]");
            System.exit(64);
        }

        // wait, so we can measure the amount of memory used
        if (settings.measure)
        	System.in.read();
    }
}

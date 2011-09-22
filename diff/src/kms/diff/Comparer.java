package kms.diff;

import java.io.File;
import java.io.IOException;
import java.io.Reader;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.FileDocumentSource;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyLoaderConfiguration;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import ru.tpu.cc.kms.changes.CategorizedChangeSet;
import ru.tpu.cc.kms.changes.ComparableOntology;

public class Comparer {
	private static boolean BinaryCompare(FileDocumentSource parentSource, FileDocumentSource childSource) throws IOException {
		Reader sr = parentSource.getReader();
		Reader cr = childSource.getReader();
        while (true) {
           int a = sr.read();
           int b = cr.read();
           if (a != b) break;
           if (a == -1)
        	   return true;
        }
        return false;
	}
	public static CategorizedChangeSet compare(String parent, String child) throws OWLOntologyCreationException, IOException {
		// parent is same as child, constructing an empty changeset
		if (parent.equals(child))
			return new CategorizedChangeSet();
		FileDocumentSource parentSource = new FileDocumentSource(new File(parent));
		FileDocumentSource childSource = new FileDocumentSource(new File(child));
		// binary compare
		if (BinaryCompare(parentSource, childSource))
			return new CategorizedChangeSet();		
        OWLOntologyManager m1 = OWLManager.createOWLOntologyManager();
        OWLOntologyManager m2 = OWLManager.createOWLOntologyManager();
		OWLOntologyLoaderConfiguration config = new OWLOntologyLoaderConfiguration();
		config.setSilentMissingImportsHandling(true);
        OWLOntology o1;
        OWLOntology o2;
		o1 = m1.loadOntologyFromOntologyDocument(parentSource, config);
		o2 = m2.loadOntologyFromOntologyDocument(childSource, config);	
        // Comparing
		return new CategorizedChangeSet(o1, o2);
	}
	public static CategorizedChangeSet compareAndMeasure(String parent, String child) throws OWLOntologyCreationException, IOException {
		// parent is same as child, constructing an empty changeset
		if (parent.equals(child))
			return new CategorizedChangeSet();
		FileDocumentSource parentSource = new FileDocumentSource(new File(parent));
		FileDocumentSource childSource = new FileDocumentSource(new File(child));
		// binary compare
		if (BinaryCompare(parentSource, childSource))
			return new CategorizedChangeSet();		
        long startTime;
        OWLOntologyManager m1 = OWLManager.createOWLOntologyManager();
        OWLOntologyManager m2 = OWLManager.createOWLOntologyManager();
		OWLOntologyLoaderConfiguration config = new OWLOntologyLoaderConfiguration();
		config.setSilentMissingImportsHandling(true);
        OWLOntology o1;
        OWLOntology o2;
        startTime = System.nanoTime();
		o1 = m1.loadOntologyFromOntologyDocument(parentSource, config);
		o2 = m2.loadOntologyFromOntologyDocument(childSource, config);
		long loadingDuration = System.nanoTime() - startTime;
		System.err.println("Loading time:    " + loadingDuration / 1000000 + " milliseconds");
        // Building Comparable Ontologies from loaded ontologies
        startTime = System.nanoTime();
        ComparableOntology co1 = new ComparableOntology(o1);
        ComparableOntology co2 = new ComparableOntology(o2);
        long buildingDuration = System.nanoTime() - startTime;
        System.err.println("Building time:   " + buildingDuration / 1000000 + " milliseconds");
        
        // Comparing
        startTime = System.nanoTime();
        CategorizedChangeSet cs = new CategorizedChangeSet(co1, co2);
        
        long comparisonDuration = System.nanoTime() - startTime;
        System.err.println("Comparing time:  " + comparisonDuration / 1000000 + " milliseconds");
        long totalDuration = loadingDuration + buildingDuration + comparisonDuration;
        System.err.println("Total time:      " + totalDuration / 1000000 + " milliseconds");
        
        // Ontology Statistics
        System.err.println(parent);
		System.err.println("   " + o1.getAxiomCount() + " axioms");
		System.err.println("   " + o1.getClassesInSignature().size() + " classes");
		System.err.println("   " + (o1.getDataPropertiesInSignature().size() + o1.getObjectPropertiesInSignature().size()) + " properties");
		System.err.println("   " + co1.getStatements().size() + " statements");
		System.err.println(child);
		System.err.println("   " + o2.getAxiomCount() + " axioms");
		System.err.println("   " + o2.getClassesInSignature().size() + " classes");
		System.err.println("   " + (o2.getDataPropertiesInSignature().size() + o2.getObjectPropertiesInSignature().size()) + " properties");
		System.err.println("   " + co2.getStatements().size() + " statements");

		return cs;
	}
}

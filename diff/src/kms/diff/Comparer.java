package kms.diff;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Reader;
import java.util.Hashtable;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.FileDocumentSource;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyLoaderConfiguration;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import ru.tpu.cc.kms.EntityShortener;
import ru.tpu.cc.kms.IriFormat;
import ru.tpu.cc.kms.changes.CategorizedChangeSet;
import ru.tpu.cc.kms.changes.ChangesSummary;
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
    public static CategorizedChangeSet compareAndMeasure(String parent, String child,
            Hashtable<String, Long> times) throws OWLOntologyCreationException, IOException {
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
        // Loading ontologies
        startTime = System.nanoTime();
        o1 = m1.loadOntologyFromOntologyDocument(parentSource, config);
        o2 = m2.loadOntologyFromOntologyDocument(childSource, config);
        times.put("load", System.nanoTime() - startTime);
        // Building Comparable Ontologies from loaded ontologies
        startTime = System.nanoTime();
        ComparableOntology co1 = new ComparableOntology(o1);
        ComparableOntology co2 = new ComparableOntology(o2);
        times.put("build", System.nanoTime() - startTime);
        // Comparing
        startTime = System.nanoTime();
        CategorizedChangeSet cs = new CategorizedChangeSet(co1, co2);
        times.put("compare", System.nanoTime() - startTime);
        return cs;
    }
    public static void printStatistics(ComparableOntology co, PrintStream stream) throws OWLOntologyCreationException {
        OWLOntology o = co.getOntology();
        stream.println("   " + o.getAxiomCount() + " axioms");
        stream.println("   " + o.getClassesInSignature().size() + " classes");
        stream.println("   " + (o.getDataPropertiesInSignature().size() + o.getObjectPropertiesInSignature().size()) + " properties");
        stream.println("   " + co.getStatements().size() + " statements");
    }
    public static void PrintSummary(CategorizedChangeSet cs, PrintStream stream, IriFormat iriFormat) throws OWLOntologyCreationException {
        ChangesSummary ca = new ChangesSummary(cs);
        stream.println("Total additions: " + cs.getAdditions().size());
        stream.println("Total removals: " + cs.getRemovals().size());
        if (null != ca.getNewFormat())
            stream.println("Ontology format changed to: " + ca.getNewFormat());
        if (null != ca.getNewOntologyIRI())
            stream.println("Ontology IRI changed to: " + ca.getNewOntologyIRI());
        if (null != ca.getNewVersionIRI())
            stream.println("Version IRI changed to: " + ca.getNewVersionIRI());
        EntityShortener shortener = new EntityShortener(iriFormat);
        if (ca.getNewEntities().size() > 0) {
            stream.println("New:");
            for (OWLEntity e: ca.getNewEntities())
                stream.println("    " + e.getEntityType() + ": " + shortener.shorten(e));
        }
        if (ca.getModifiedEntities().size() > 0) {
            stream.println("Modified:");
            for (OWLEntity e: ca.getModifiedEntities())
                stream.println("    " + e.getEntityType() + ": " + shortener.shorten(e));
        }
        if (ca.getRemovedEntities().size() > 0) {
            stream.println("Removed:");
            for (OWLEntity e: ca.getRemovedEntities())
                stream.println("    " + e.getEntityType() + ": " + shortener.shorten(e));
        }
        stream.println();
    }
}

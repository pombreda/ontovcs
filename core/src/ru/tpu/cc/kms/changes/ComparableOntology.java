package ru.tpu.cc.kms.changes;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.HashMap;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.net.URI;
import java.net.URISyntaxException;


import org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntaxOntologyFormat;
import org.coode.owlapi.turtle.TurtleOntologyFormat;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.OWLFunctionalSyntaxOntologyFormat;
import org.semanticweb.owlapi.io.OWLXMLOntologyFormat;
import org.semanticweb.owlapi.io.RDFXMLOntologyFormat;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.AddImport;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyFormat;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLImportsDeclaration;
import org.semanticweb.owlapi.model.SetOntologyID;
import org.semanticweb.owlapi.vocab.PrefixOWLOntologyFormat;

import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import ru.tpu.cc.kms.statements.AxiomStatement;
import ru.tpu.cc.kms.statements.ImportStatement;
import ru.tpu.cc.kms.statements.NamespacePrefixStatement;
import ru.tpu.cc.kms.statements.OntologyFormatStatement;
import ru.tpu.cc.kms.statements.OntologyIRIStatement;
import ru.tpu.cc.kms.statements.Statement;
import ru.tpu.cc.kms.statements.StatementType;
import ru.tpu.cc.kms.statements.VersionIRIStatement;

/**
 * An ontology represented as a set of statements
 *
 * @author I
 * @see Statement
 */
public class ComparableOntology {

	private OWLOntology ontology;
	private Map<StatementType, Set<Statement>> statementsByType;

	/**
	 * @return the statementsByType
	 */
	public Set<Statement> getStatementsByType(final StatementType type) {
		return statementsByType.get(type);
	}

	public final Set<Statement> getStatements() {
		Set<Statement> statements = new LinkedHashSet<Statement>();
		for (StatementType type : StatementType.values()) {
			Set<Statement> statementSet = statementsByType.get(type);
			if (statementSet != null) {
				statements.addAll(statementSet);
			}
		}
		return statements;
	}

	protected final void addStatement(final Statement s) {
		Set<Statement> statements = statementsByType.get(s.getType());
		statements.add(s);
		statementsByType.put(s.getType(), statements);
	}

	protected final void removeStatement(final Statement s) {
		Set<Statement> statements = statementsByType.get(s.getType());
		statements.remove(s);
		statementsByType.put(s.getType(), statements);
	}

	public final OWLOntology getOntology() throws OWLOntologyCreationException {
		if (ontology == null)
			buildOntology();
		return ontology;
	}

	public final Set<URI> getSignature() throws URISyntaxException {
		Set<URI> s = new HashSet<URI>();
		for (Statement st : getStatements())
			s.addAll(st.getSignature());
		return s;
	}

	/**
	 * Builds OWLOntology from statement sets
	 * @throws OWLOntologyCreationException
	 */
	protected final void buildOntology() throws OWLOntologyCreationException {
		Set<Statement> formatStatements = 	    statementsByType.get(StatementType.FORMAT);
        Set<Statement> prefixStatements = 		statementsByType.get(StatementType.PREFIX);
		Set<Statement> importStatements = 		statementsByType.get(StatementType.IMPORT);
		Set<Statement> ontologyIRIStatements =  statementsByType.get(StatementType.OIRI);
		Set<Statement> versionIRIStatements = 	statementsByType.get(StatementType.VIRI);
        Set<Statement> axiomStatements = 		statementsByType.get(StatementType.AXIOM);
		OWLOntologyManager m = OWLManager.createOWLOntologyManager();
		OWLOntology o = m.createOntology();
		List<OWLOntologyChange> changes = new ArrayList<OWLOntologyChange>();
		// Format
		if (!ontologyIRIStatements.isEmpty()) {
			PrefixOWLOntologyFormat ontologyFormat = null;
			if (formatStatements.size() > 1)
				throw new OWLOntologyCreationException("Multiple formats specified");
			if (formatStatements.size() < 1)
				throw new OWLOntologyCreationException("No format specified");
			String format = ((OntologyFormatStatement) formatStatements.toArray()[0]).getFormat();
			if (format.equals(new RDFXMLOntologyFormat().toString()))
				ontologyFormat = new RDFXMLOntologyFormat();
			if (format.equals(new OWLXMLOntologyFormat().toString()))
				ontologyFormat = new OWLXMLOntologyFormat();
			if (format.equals(new OWLFunctionalSyntaxOntologyFormat().toString()))
				ontologyFormat = new OWLFunctionalSyntaxOntologyFormat();
			if (format.equals(new TurtleOntologyFormat().toString()))
				ontologyFormat = new TurtleOntologyFormat();
			if (format.equals(new ManchesterOWLSyntaxOntologyFormat().toString()))
				ontologyFormat = new ManchesterOWLSyntaxOntologyFormat();
			if (null == ontologyFormat)
				throw new OWLOntologyCreationException("Unknown format: " + format);
			// Prefixes
			for (Statement s : prefixStatements)
				ontologyFormat.setPrefix(((NamespacePrefixStatement) s).getPrefix(), ((NamespacePrefixStatement) s).getNamespace());
			m.setOntologyFormat(o, ontologyFormat);
		}
		// Imports
		for (Statement s : importStatements)
			changes.add(new AddImport(o, ((ImportStatement) s).getImport()));
		// IDs
		if (!ontologyIRIStatements.isEmpty()) {
			OWLOntologyID ontologyID;
			IRI oiri = ((OntologyIRIStatement) ontologyIRIStatements.toArray()[0]).getIri();
			if (!versionIRIStatements.isEmpty()) {
				IRI viri = ((VersionIRIStatement) versionIRIStatements.toArray()[0]).getIri();
				ontologyID = new OWLOntologyID(oiri, viri);
			} else {
				ontologyID = new OWLOntologyID(oiri);
			}
			changes.add(new SetOntologyID(o, ontologyID));
		}
		// Axioms
		for (Statement s : axiomStatements)
			changes.add(new AddAxiom(o, ((AxiomStatement) s).getAxiom()));
		m.applyChanges(changes);
		this.ontology = o;
	}

	public final void save(final String fileName) throws FileNotFoundException {
		PrintStream stream = new PrintStream(fileName);
		try {
			for (Statement s : getStatements())
				stream.println(s.toString());
		} finally {
			stream.close();
		}
	}
	/**
	 * Extracts statements from OWLOntology
	 * @param o
	 */
	public ComparableOntology(final OWLOntology o) {
		this.ontology = o;
		statementsByType = new HashMap<StatementType, Set<Statement>>();
		Set<Statement> formatStatements = new HashSet<Statement>();
        Set<Statement> prefixStatements = new HashSet<Statement>();
		Set<Statement> importStatements = new HashSet<Statement>();
		Set<Statement> ontologyIRIStatements = new HashSet<Statement>();
		Set<Statement> versionIRIStatements = new HashSet<Statement>();
        Set<Statement> axiomStatements = new HashSet<Statement>();
		OWLOntologyManager man = o.getOWLOntologyManager();
		// Format
		OWLOntologyFormat ontologyFormat = man.getOntologyFormat(o);
		formatStatements.add(new OntologyFormatStatement(this, ontologyFormat.toString()));
		// Prefixes
        if (ontologyFormat instanceof PrefixOWLOntologyFormat) {
            PrefixOWLOntologyFormat prefixFormat = (PrefixOWLOntologyFormat) ontologyFormat;
            for (String prefixName : prefixFormat.getPrefixNames()) {
                String prefix = prefixFormat.getPrefix(prefixName);
                prefixStatements.add(new NamespacePrefixStatement(this, prefixName, prefix));
            }
        }
		// Imports
		for (OWLImportsDeclaration d : o.getImportsDeclarations())
			importStatements.add(new ImportStatement(this, d));
		// Axioms
		for (OWLAxiom a : o.getAxioms())
			axiomStatements.add(new AxiomStatement(this, a));
        // IDs
        if (!o.isAnonymous()) {
        	OWLOntologyID id = o.getOntologyID();
			if (id.getOntologyIRI() != null)
            	ontologyIRIStatements.add(new OntologyIRIStatement(this, id.getOntologyIRI()));
            if (id.getVersionIRI() != null)
            	versionIRIStatements.add(new VersionIRIStatement(this, id.getVersionIRI()));
        }
        statementsByType.put(StatementType.FORMAT, formatStatements);
        statementsByType.put(StatementType.PREFIX, prefixStatements);
        statementsByType.put(StatementType.IMPORT, importStatements);
        statementsByType.put(StatementType.OIRI, ontologyIRIStatements);
        statementsByType.put(StatementType.VIRI, versionIRIStatements);
        statementsByType.put(StatementType.AXIOM, axiomStatements);
	}

	/**
	 * Creates ComparableOntology from a list of statements
	 * @param statements
	 */
	public ComparableOntology(final Set<Statement> statements) {
		statementsByType = new HashMap<StatementType, Set<Statement>>();
		Set<Statement> formatStatements = new HashSet<Statement>();
        Set<Statement> prefixStatements = new HashSet<Statement>();
		Set<Statement> importStatements = new HashSet<Statement>();
		Set<Statement> ontologyIRIStatements = new HashSet<Statement>();
		Set<Statement> versionIRIStatements = new HashSet<Statement>();
        Set<Statement> axiomStatements = new HashSet<Statement>();
		for (Statement s : statements) {
			if (s instanceof OntologyFormatStatement)
				formatStatements.add(s);
			else if (s instanceof NamespacePrefixStatement)
				prefixStatements.add(s);
			else if (s instanceof ImportStatement)
				importStatements.add(s);
			else if (s instanceof OntologyIRIStatement)
				ontologyIRIStatements.add(s);
			else if (s instanceof VersionIRIStatement)
				versionIRIStatements.add(s);
			else if (s instanceof AxiomStatement)
				axiomStatements.add(s);
		}
        statementsByType.put(StatementType.FORMAT, formatStatements);
        statementsByType.put(StatementType.PREFIX, prefixStatements);
        statementsByType.put(StatementType.IMPORT, importStatements);
        statementsByType.put(StatementType.OIRI, ontologyIRIStatements);
        statementsByType.put(StatementType.VIRI, versionIRIStatements);
        statementsByType.put(StatementType.AXIOM, axiomStatements);
	}

	/**
	 * Copy constructor
	 * @param base
	 */
	public ComparableOntology(final ComparableOntology base) {
		this(base.getStatements());
	}

	public final ComparableOntology applyChanges(final ChangeSet<Statement> changes) {
		ontology = null;
		for (Change<Statement> c : changes) {
			if (c.getOp() == Op.ADD)
				addStatement(c.getItem());
			else
				removeStatement(c.getItem());
		}
		return this;
	}
}

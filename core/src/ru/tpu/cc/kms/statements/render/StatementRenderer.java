package ru.tpu.cc.kms.statements.render;

import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.util.DefaultPrefixManager;
import org.semanticweb.owlapi.util.ShortFormProvider;
import org.semanticweb.owlapi.util.SimpleShortFormProvider;

import ru.tpu.cc.kms.FullFormProvider;
import ru.tpu.cc.kms.IriFormat;
import ru.tpu.cc.kms.statements.Statement;
import uk.ac.manchester.cs.owl.owlapi.mansyntaxrenderer.ManchesterOWLSyntaxPrefixNameShortFormProvider;

public abstract class StatementRenderer {

    protected ShortFormProvider provider;

    public StatementRenderer(ShortFormProvider provider) {
        this.provider = provider;
    }

    protected StatementRenderer(IriFormat iriFormat) {
        switch (iriFormat) {
        case SIMPLE:
            provider = new SimpleShortFormProvider();
            break;
        case QNAME:
            DefaultPrefixManager prefixManager = new DefaultPrefixManager();
            prefixManager.clear();
            provider = new ManchesterOWLSyntaxPrefixNameShortFormProvider(prefixManager);
            break;
        case FULL:
            provider = new FullFormProvider();
            break;
        }
    }

    protected StatementRenderer(OWLOntology ontology) {
        OWLOntologyManager manager = ontology.getOWLOntologyManager();
        provider = new ManchesterOWLSyntaxPrefixNameShortFormProvider(manager, ontology);
    }

    public abstract String getRendering(Statement statement);

}

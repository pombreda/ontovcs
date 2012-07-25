package ru.tpu.cc.kms.statements.render;

import org.semanticweb.owlapi.io.OWLFunctionalSyntaxOntologyFormat;
import org.semanticweb.owlapi.util.DefaultPrefixManager;
import org.semanticweb.owlapi.util.QNameShortFormProvider;
import org.semanticweb.owlapi.util.ShortFormProvider;
import org.semanticweb.owlapi.util.SimpleShortFormProvider;
import org.semanticweb.owlapi.vocab.PrefixOWLOntologyFormat;

import ru.tpu.cc.kms.FullFormProvider;
import ru.tpu.cc.kms.IriFormat;
import ru.tpu.cc.kms.statements.AxiomStatement;
import ru.tpu.cc.kms.statements.ImportStatement;
import ru.tpu.cc.kms.statements.NamespacePrefixStatement;
import ru.tpu.cc.kms.statements.OntologyFormatStatement;
import ru.tpu.cc.kms.statements.OntologyIRIStatement;
import ru.tpu.cc.kms.statements.Statement;
import ru.tpu.cc.kms.statements.VersionIRIStatement;

public class FunctionalSyntaxStatementRenderer extends StatementRenderer {

    public FunctionalSyntaxStatementRenderer(IriFormat iriFormat) {
        super(iriFormat);
    }

    @Override
    public String getRendering(Statement statement) {
        String r = "";
        switch (statement.getType()) {
            case AXIOM:
                DefaultPrefixManager prefixManager = new DefaultPrefixManager();
                prefixManager.clear();
                PrefixOWLOntologyFormat prefixFormat = (PrefixOWLOntologyFormat) new OWLFunctionalSyntaxOntologyFormat();
                SimplerRenderer renderer = new SimplerRenderer();
                ShortFormProvider provider = null;
                switch (iriFormat) {
                case SIMPLE:
                    provider = new SimpleShortFormProvider();
                    break;
                case QNAME:
                    provider = new QNameShortFormProvider(prefixFormat.getPrefixName2PrefixMap());
                    break;
                case FULL:
                    provider = new FullFormProvider();
                    break;
                }
                renderer.setShortFormProvider(provider);
                r = renderer.render(((AxiomStatement) statement).getAxiom());
                break;
            case IMPORT:
                r = "Import(<" + ((ImportStatement) statement).getImport().getIRI() + ">)";
                break;
            case PREFIX:
                NamespacePrefixStatement s = (NamespacePrefixStatement) statement;
                r = "Prefix(" + s.getPrefix() + "=<" + s.getNamespace() + ">)";
                break;
            case FORMAT:
                r = "OntologyFormat(\"" + ((OntologyFormatStatement) statement).getFormat() + "\")";
                break;
            case OIRI:
                r = "OntologyIRI(<" + ((OntologyIRIStatement) statement).getIRI() + ">)";
                break;
            case VIRI:
                r = "VersionIRI(<" + ((VersionIRIStatement) statement).getIRI() + ">)";
                break;

        }
        return r;
    }
}

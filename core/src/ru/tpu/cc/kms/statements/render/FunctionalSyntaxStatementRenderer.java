package ru.tpu.cc.kms.statements.render;

import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.util.ShortFormProvider;

import ru.tpu.cc.kms.statements.AxiomStatement;
import ru.tpu.cc.kms.statements.ImportStatement;
import ru.tpu.cc.kms.statements.NamespacePrefixStatement;
import ru.tpu.cc.kms.statements.OntologyFormatStatement;
import ru.tpu.cc.kms.statements.OntologyIRIStatement;
import ru.tpu.cc.kms.statements.Statement;
import ru.tpu.cc.kms.statements.VersionIRIStatement;

public class FunctionalSyntaxStatementRenderer extends StatementRenderer {

    public FunctionalSyntaxStatementRenderer(ShortFormProvider provider) {
        super(provider);
    }

    public FunctionalSyntaxStatementRenderer(OWLOntology ontology) {
        super(ontology);
    }

    @Override
    public String getRendering(Statement statement) {
        String r = "";
        switch (statement.getType()) {
            case AXIOM:
                SimplerRenderer renderer = new SimplerRenderer();
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

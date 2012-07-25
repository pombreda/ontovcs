package ru.tpu.cc.kms.statements.render;

import org.semanticweb.owlapi.model.OWLException;

import ru.tpu.cc.kms.IriFormat;
import ru.tpu.cc.kms.statements.Statement;

public class IndentedStatementRenderer extends StatementRenderer {

    public IndentedStatementRenderer(IriFormat iriFormat) {
        super(iriFormat);
    }

    private String writeIndent(StringBuilder sb, int indent) {
        sb.append("\n");
        for (int i = 0; i < indent; i++)
            sb.append(" ");
        return sb.toString();
    }

    @Override
    public String getRendering(Statement statement) throws OWLException {
        String r = new FunctionalSyntaxStatementRenderer(iriFormat).getRendering(statement);
        StringBuilder sb = new StringBuilder();
        int indent = 0;
        boolean quote = false;
        for (char c : r.toCharArray()) {
            switch (c) {
                case '(':
                    indent += 4;
                    sb.append(':');
                    writeIndent(sb, indent);
                    break;
                case ')':
                    indent -= 4;
                    writeIndent(sb, indent);
                    break;
                case ' ':
                    if (quote)
                        sb.append(c);
                    else
                        writeIndent(sb, indent);
                    break;
                case '"':
                    quote = !quote;
                default:
                    sb.append(c);
            }
        }
        return sb.toString().trim();
    }

}

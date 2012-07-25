package ru.tpu.cc.kms.changes.render;

import java.util.Arrays;

import org.semanticweb.owlapi.model.OWLException;

import ru.tpu.cc.kms.IriFormat;
import ru.tpu.cc.kms.changes.Change;
import ru.tpu.cc.kms.changes.Op;
import ru.tpu.cc.kms.statements.Statement;
import ru.tpu.cc.kms.statements.render.ManchesterSyntaxStatementRenderer;

public class ManchesterSyntaxChangeRenderer extends ChangeRenderer {

    private boolean oneLine = true;

    public ManchesterSyntaxChangeRenderer(IriFormat iriFormat, boolean oneLine) {
        super(iriFormat);
        this.oneLine = oneLine;
    }

    @Override
    public String getRendering(Change<Statement> change) {
        String s;
        try {
            s = new ManchesterSyntaxStatementRenderer(iriFormat).getRendering(change.getItem());
        } catch (OWLException e) {
            s = Arrays.toString(e.getStackTrace());
        }
        s = s.replaceAll("\n[ \t]+\n", "\n");
        s = s.replaceAll("\n\n", "\n");
        s = s.replaceAll("\n$", "");
        if (oneLine) {
            s = s.replaceAll("\n", " ");
            s = s.replaceAll("[ \t]+", " ");
        }
        if (change.getOp() == Op.ADD)
            return "+ " + s;
        else
            return "- " + s;
    }

}

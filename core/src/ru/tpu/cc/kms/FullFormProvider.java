package ru.tpu.cc.kms;

import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.util.ShortFormProvider;

public class FullFormProvider implements ShortFormProvider {

    @Override
    public String getShortForm(OWLEntity entity) {
        return entity.toString();
    }

    @Override
    public void dispose() {
    }

}

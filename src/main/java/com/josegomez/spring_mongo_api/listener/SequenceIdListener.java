package com.josegomez.spring_mongo_api.listener;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertEvent;
import org.springframework.stereotype.Component;

import com.josegomez.spring_mongo_api.domain.common.SequenceIdentifiable;
import com.josegomez.spring_mongo_api.service.SequenceGeneratorService;

@Component
public class SequenceIdListener extends AbstractMongoEventListener<SequenceIdentifiable> {

    private final SequenceGeneratorService sequenceGenerator;

    public SequenceIdListener(SequenceGeneratorService sequenceGenerator) {
        this.sequenceGenerator = sequenceGenerator;
    }

    @Override
    public void onBeforeConvert(BeforeConvertEvent<SequenceIdentifiable> event) {
        SequenceIdentifiable entity = event.getSource();
        Long currentId = entity.getId();

        if (currentId == null || currentId == 0L) {
            String collectionName = resolveCollectionName(entity.getClass());
            Long id = sequenceGenerator.next(collectionName);
            entity.setId(id);
        }
    }

    private String resolveCollectionName(Class<?> clazz) {
        Document document = clazz.getAnnotation(Document.class);
        if (document != null && !document.collection().isEmpty()) {
            return document.collection();
        } else {
            return clazz.getSimpleName().toLowerCase();
        }
    }
}

package uk.co.ridentbyte.functions;

import uk.co.ridentbyte.model.CardinalRequest;

import java.util.List;

@FunctionalInterface
public interface ExportToBash {
    void export(List<CardinalRequest> requests, int throttle);
}

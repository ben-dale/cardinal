package uk.co.ridentbyte.functions;

import uk.co.ridentbyte.model.CardinalRequestAndResponse;

import java.util.List;

@FunctionalInterface
public interface ExportToCSV {
    void export(List<CardinalRequestAndResponse> cardinalRequestAndResponses);
}

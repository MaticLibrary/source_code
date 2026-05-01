package com.example.engines;

import com.example.algorithm.AlgorithmType;
import com.example.algorithm.report.StepReport;
import com.example.engines.printer.InformationPrinter;

import java.util.LinkedHashMap;
import java.util.Map;

public class ZkpInformationEngine implements InformationEngine {
    private final InformationPrinter informationPrinter;

    public ZkpInformationEngine(InformationPrinter informationPrinter) {
        this.informationPrinter = informationPrinter;
    }

    @Override
    public void processReport(StepReport stepReport) {
        informationPrinter.setAlgorithmName(AlgorithmType.ZKP.toString());
        informationPrinter.setAlgorithmPhase(stepReport.getAlgorithmPhase());
        informationPrinter.setStepDescription(generateDescription(stepReport));
        informationPrinter.listProperties(generateProperties(stepReport));
        informationPrinter.renderView();
    }

    private String generateDescription(StepReport stepReport) {
        return switch (stepReport.getAlgorithmPhase()) {
            case SEND ->
                    "Kazdy wezel ujawnia swoj glos razem z nieinteraktywnym dowodem wiedzy, ze glos pasuje do wczesniejszego zobowiazania Pedersena. Proba zmiany glosu po zobowiazaniu ma zostac odrzucona.";
            case CHOOSE ->
                    "Kazdy wezel liczy tylko glosy z poprawnym ZKP. Decyzja opiera sie na wiekszosci po odfiltrowaniu niewaznych proofow i sprawdzeniu quorum.";
            default -> "Nieznana faza.";
        };
    }

    private Map<String, String> generateProperties(StepReport stepReport) {
        Map<String, String> properties = stepReport.getProperties();
        Map<String, String> result = new LinkedHashMap<>();
        result.put("etap", properties.getOrDefault("etap", "N/A"));
        result.put("nadawca", properties.getOrDefault("nadawca", "N/A"));
        result.put("odbiorca", properties.getOrDefault("odbiorca", properties.getOrDefault("odbiorcy", "N/A")));
        result.put("postep wysylek", properties.getOrDefault("postep_wysylek", "N/A"));
        result.put("f", properties.getOrDefault("f", "N/A"));
        result.put("quorum", properties.getOrDefault("quorum", "N/A"));
        result.put("system dowodu", properties.getOrDefault("system_dowodu", "N/A"));
        result.put("zobowiazania", properties.getOrDefault("zobowiazania", "N/A"));
        result.put("poprawne dowody", properties.getOrDefault("poprawne_dowody", "0"));
        result.put("bledne dowody", properties.getOrDefault("bledne_dowody", "0"));
        result.put("bledni nadawcy", properties.getOrDefault("bledni_nadawcy", "brak"));
        result.put("gwarancja", properties.getOrDefault("gwarancja", "N/A"));
        result.put("decyzje", properties.getOrDefault("decyzje", "N/A"));
        return result;
    }
}

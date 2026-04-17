package com.example.engines;

import com.example.algorithm.AlgorithmType;
import com.example.algorithm.ProofMethodType;
import com.example.algorithm.VertexRole;
import com.example.algorithm.report.StepReport;
import com.example.engines.printer.InformationPrinter;

import java.util.LinkedHashMap;
import java.util.Map;

public class PrivateRulesBftInformationEngine implements InformationEngine {
    private final InformationPrinter informationPrinter;

    public PrivateRulesBftInformationEngine(InformationPrinter informationPrinter) {
        this.informationPrinter = informationPrinter;
    }

    @Override
    public void processReport(StepReport stepReport) {
        informationPrinter.setAlgorithmName(AlgorithmType.PRIVATE_BFT.toString());
        informationPrinter.setAlgorithmPhase(stepReport.getAlgorithmPhase());
        informationPrinter.setStepDescription(generateDescription(stepReport));
        informationPrinter.listProperties(generateProperties(stepReport));
        informationPrinter.renderView();
    }

    private Map<String, String> generateProperties(StepReport stepReport) {
        Map<String, String> properties = stepReport.getProperties();
        Map<String, String> result = new LinkedHashMap<>();

        result.put("runda", properties.getOrDefault("runda", "N/A"));
        result.put("lider", properties.getOrDefault("lider", "N/A"));
        result.put("f", properties.getOrDefault("f", "N/A"));
        result.put("timeout", properties.getOrDefault("timeout", "N/A"));
        result.put("dowod", properties.getOrDefault("dowod", "N/A"));
        result.put("weryfikacja", properties.getOrDefault("weryfikacja", "N/A"));
        result.put("rozmiar dowodu", properties.getOrDefault("rozmiar_dowodu", "N/A"));
        result.put("odpornosc kwantowa", properties.getOrDefault("odpornosc_kwantowa", "N/A"));
        result.put("profil dowodu", properties.getOrDefault("profil_dowodu", "N/A"));
        result.put("alarmy", properties.getOrDefault("alarmy", "0"));
        result.put("quorum", properties.getOrDefault("quorum", "N/A"));
        result.put("decyzje", properties.getOrDefault("decyzje", "0/0"));
        result.put("konflikty lidera", properties.getOrDefault("konflikty_lidera", "0"));
        result.put("regu\u0142y", properties.getOrDefault("regu\u0142y", "N/A"));
        result.put("mapowanie regul", properties.getOrDefault("mapowanie_regul", "N/A"));

        stepReport.getRoles().entrySet().stream()
                .filter(entry -> entry.getValue() == VertexRole.COMMANDER)
                .findFirst()
                .ifPresent(entry -> result.put("lider (wierzcho\u0142ek)", entry.getKey().element().toString()));

        return result;
    }

    private String generateDescription(StepReport stepReport) {
        ProofMethodType proofMethod = parseProofMethod(stepReport.getProperties().get("dowod"));
        String proofSuffix = proofMethod.getStepDescriptionSuffix();
        return switch (stepReport.getAlgorithmPhase()) {
            case SEND -> "PROPOSAL/ECHO: lider rozsyla podpisana propozycje do swoich sasiadow, a uczciwy wezel podpisuje co najwyzej jedna wartosc i przekazuje ja dalej po krawedziach grafu." + proofSuffix;
            case CHOOSE -> "Decyzja: wartosc jest przyjmowana dopiero po zebraniu certyfikatu quorum (n-f podpisow). Reguly prywatne oceniaja jakosc dowodu i moga zglosic ALARM." + proofSuffix;
            default -> "Nieznana faza.";
        };
    }

    private ProofMethodType parseProofMethod(String value) {
        if (ProofMethodType.BULLETPROOFS.toString().equals(value)) {
            return ProofMethodType.BULLETPROOFS;
        }
        return ProofMethodType.STARK;
    }
}

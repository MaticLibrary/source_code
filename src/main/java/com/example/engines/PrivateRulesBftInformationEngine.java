package com.example.engines;

import com.example.algorithm.AlgorithmType;
import com.example.algorithm.VertexRole;
import com.example.algorithm.report.StepReport;
import com.example.engines.printer.InformationPrinter;

import java.util.HashMap;
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
        Map<String, String> result = new HashMap<>();

        result.put("runda", properties.getOrDefault("runda", "N/A"));
        result.put("lider", properties.getOrDefault("lider", "N/A"));
        result.put("f", properties.getOrDefault("f", "N/A"));
        result.put("timeout", properties.getOrDefault("timeout", "N/A"));
        result.put("alarmy", properties.getOrDefault("alarmy", "0"));
        result.put("quorum", properties.getOrDefault("quorum", "N/A"));
        result.put("decyzje", properties.getOrDefault("decyzje", "0/0"));
        result.put("konflikty lidera", properties.getOrDefault("konflikty_lidera", "0"));
        result.put("regu\u0142y", properties.getOrDefault("regu\u0142y", "N/A"));

        stepReport.getRoles().entrySet().stream()
                .filter(entry -> entry.getValue() == VertexRole.COMMANDER)
                .findFirst()
                .ifPresent(entry -> result.put("lider (wierzcho\u0142ek)", entry.getKey().element().toString()));

        return result;
    }

    private String generateDescription(StepReport stepReport) {
        return switch (stepReport.getAlgorithmPhase()) {
            case SEND -> "PROPOSAL/ECHO: lider rozsy\u0142a podpisan\u0105 propozycj\u0119, a uczciwy w\u0119ze\u0142 podpisuje co najwy\u017cej jedn\u0105 warto\u015b\u0107 i tylko wtedy, gdy widzi \u015blad podpisu lidera.";
            case CHOOSE -> "Decyzja: warto\u015b\u0107 jest przyjmowana dopiero po zebraniu certyfikatu quorum (n-f podpis\u00f3w). Regu\u0142y prywatne ocenia\u0105 jako\u015b\u0107 dowodu i mog\u0105 zg\u0142osi\u0107 ALARM.";
            default -> "Nieznana faza.";
        };
    }
}

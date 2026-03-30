package com.example.algorithm;

import com.example.controller.settings.TraitorSettings;
import com.example.algorithm.report.StepReport;
import com.example.model.MyGraph;
import com.example.settings.AlgorithmSetting;
import com.example.settings.AlgorithmSettings;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PrivateRulesBftAlgorithmTest {

    @Test
    void privateBftFinishesWithinMaxRounds() {
        MyGraph<Integer, Integer> graph = new MyGraph<>();
        for (int i = 0; i < 4; i++) {
            graph.insertVertex(i);
        }
        for (int i = 0; i < 4; i++) {
            for (int j = i + 1; j < 4; j++) {
                graph.insertEdge(i, j, 1);
            }
        }

        PrivateRulesBftAlgorithm algorithm = new PrivateRulesBftAlgorithm();
        algorithm.loadEnvironment(graph, createSettings(1, 2, 2));

        int steps = 0;
        while (!algorithm.isFinished() && steps < 10) {
            assertNotNull(algorithm.step());
            steps++;
        }

        assertTrue(algorithm.isFinished());
        assertTrue(graph.checkConsensus());
        assertTrue(steps <= 3);
    }

    @Test
    void traitorLeaderProducesLeaderConflictEvidence() {
        TraitorSettings.setTraitorsAlwaysLie(false);

        MyGraph<Integer, Integer> graph = new MyGraph<>();
        for (int i = 0; i < 4; i++) {
            graph.insertVertex(i);
        }
        for (int i = 0; i < 4; i++) {
            for (int j = i + 1; j < 4; j++) {
                graph.insertEdge(i, j, 1);
            }
        }
        ((com.example.model.MyVertex<Integer>) graph.getVertexByKey(0)).setIsTraitor(true);

        PrivateRulesBftAlgorithm algorithm = new PrivateRulesBftAlgorithm();
        algorithm.loadEnvironment(graph, createSettings(1, 2, 2));

        StepReport chooseReport = null;
        while (!algorithm.isFinished()) {
            StepReport report = algorithm.step();
            assertNotNull(report);
            if (report.getAlgorithmPhase() == AlgorithmPhase.CHOOSE) {
                chooseReport = report;
                break;
            }
        }

        assertNotNull(chooseReport);
        assertNotEquals("0", chooseReport.getProperties().get("konflikty_lidera"));
    }

    private AlgorithmSettings createSettings(int f, int timeout, int maxRounds) {
        AlgorithmSettings settings = new AlgorithmSettings();
        AlgorithmSetting<Integer> fSetting = new AlgorithmSetting<>("f", f, Integer.class, value -> value >= 0);
        AlgorithmSetting<Integer> timeoutSetting = new AlgorithmSetting<>("timeout", timeout, Integer.class, value -> value > 0);
        AlgorithmSetting<Integer> roundsSetting = new AlgorithmSetting<>("maxRounds", maxRounds, Integer.class, value -> value > 0);
        settings.getSettings().put("f", fSetting);
        settings.getSettings().put("timeout", timeoutSetting);
        settings.getSettings().put("maxRounds", roundsSetting);
        return settings;
    }
}

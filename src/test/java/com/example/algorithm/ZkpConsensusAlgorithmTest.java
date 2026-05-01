package com.example.algorithm;

import com.example.algorithm.operations.SendOperation;
import com.example.algorithm.report.StepReport;
import com.example.controller.settings.TraitorSettings;
import com.example.model.MyGraph;
import com.example.settings.AlgorithmSetting;
import com.example.settings.AlgorithmSettings;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ZkpConsensusAlgorithmTest {

    @Test
    void liarIsRejectedWhenVoteDoesNotMatchCommitment() {
        TraitorSettings.setTraitorsAlwaysLie(true);
        try {
            MyGraph<Integer, Integer> graph = fullGraph(4);
            ((com.example.model.MyVertex<Integer>) graph.getVertexByKey(0)).setIsTraitor(true);
            ((com.example.model.MyVertex<Integer>) graph.getVertexByKey(0)).setIsSupporting(true);
            ((com.example.model.MyVertex<Integer>) graph.getVertexByKey(1)).setIsSupporting(false);
            ((com.example.model.MyVertex<Integer>) graph.getVertexByKey(2)).setIsSupporting(false);
            ((com.example.model.MyVertex<Integer>) graph.getVertexByKey(3)).setIsSupporting(false);

            ZkpConsensusAlgorithm algorithm = new ZkpConsensusAlgorithm();
            algorithm.loadEnvironment(graph, settingsWithF(1));

            StepReport sendReport = algorithm.step();
            StepReport chooseReport = null;
            while (!algorithm.isFinished()) {
                StepReport report = algorithm.step();
                if (report.getAlgorithmPhase() == AlgorithmPhase.CHOOSE) {
                    chooseReport = report;
                }
            }

            assertNotNull(sendReport);
            assertNotNull(chooseReport);
            assertTrue(sendReport.getProperties().get("bledni_nadawcy").contains("0"));
            assertTrue(Integer.parseInt(sendReport.getProperties().get("bledne_dowody")) > 0);
            assertEquals("1/12", sendReport.getProperties().get("postep_wysylek"));
            assertEquals("0", sendReport.getProperties().get("nadawca"));
            assertEquals("1", sendReport.getProperties().get("odbiorca"));
            assertEquals(1, sendReport.getOperationsBatches().size());
            assertEquals(1, sendReport.getOperationsBatches().get(0).getOperations().size());
            assertTrue(graph.checkConsensus());
            assertFalse(graph.getLoyalConsensusOpinion());
            assertTrue(algorithm.isFinished());
        } finally {
            TraitorSettings.setTraitorsAlwaysLie(false);
        }
    }

    @Test
    void honestVotesPassProofVerification() {
        MyGraph<Integer, Integer> graph = fullGraph(3);
        ((com.example.model.MyVertex<Integer>) graph.getVertexByKey(0)).setIsSupporting(true);
        ((com.example.model.MyVertex<Integer>) graph.getVertexByKey(1)).setIsSupporting(true);
        ((com.example.model.MyVertex<Integer>) graph.getVertexByKey(2)).setIsSupporting(false);

        ZkpConsensusAlgorithm algorithm = new ZkpConsensusAlgorithm();
        algorithm.loadEnvironment(graph, settingsWithF(0));

        StepReport sendReport = algorithm.step();

        assertNotNull(sendReport);
        assertEquals("0", sendReport.getProperties().get("bledne_dowody"));
        assertEquals("brak", sendReport.getProperties().get("bledni_nadawcy"));
        assertEquals("1/6", sendReport.getProperties().get("postep_wysylek"));
        assertEquals("0", sendReport.getProperties().get("nadawca"));
        assertEquals("1", sendReport.getProperties().get("odbiorca"));
        assertTrue(sendReport.getOperationsBatches().get(0).getOperations().get(0) instanceof SendOperation);
    }

    private MyGraph<Integer, Integer> fullGraph(int size) {
        MyGraph<Integer, Integer> graph = new MyGraph<>();
        for (int i = 0; i < size; i++) {
            graph.insertVertex(i);
        }
        for (int i = 0; i < size; i++) {
            for (int j = i + 1; j < size; j++) {
                graph.insertEdge(i, j, 1);
            }
        }
        return graph;
    }

    private AlgorithmSettings settingsWithF(int f) {
        AlgorithmSettings settings = new AlgorithmSettings();
        settings.getSettings().put("f", new AlgorithmSetting<>("f", f, Integer.class, value -> value >= 0));
        return settings;
    }
}

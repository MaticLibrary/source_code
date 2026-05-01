package com.example.algorithm;

import com.brunomnsilva.smartgraph.graph.Vertex;
import com.example.algorithm.operations.ChooseOperation;
import com.example.algorithm.operations.SendOperation;
import com.example.algorithm.report.OperationsBatch;
import com.example.algorithm.report.StepReport;
import com.example.model.MyGraph;
import com.example.model.MyVertex;
import com.example.settings.AlgorithmSetting;
import com.example.settings.AlgorithmSettings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;

public class ZkpConsensusAlgorithm implements Algorithm {
    private final PedersenVoteProofSystem proofSystem = new PedersenVoteProofSystem();
    private final BooleanProperty isFinished = new SimpleBooleanProperty(false);

    private MyGraph<Integer, Integer> graph;
    private AlgorithmPhase phase = AlgorithmPhase.SEND;
    private int f = 0;
    private int quorum = 1;
    private int validProofs = 0;
    private int invalidProofs = 0;
    private int nextSenderIndex = 0;
    private int nextReceiverIndex = 0;
    private int totalMessages = 0;
    private int processedMessages = 0;

    private final Map<MyVertex<Integer>, PedersenVoteProofSystem.VoteSecret> secrets = new HashMap<>();
    private final Map<MyVertex<Integer>, Map<Integer, Boolean>> verifiedVotesByReceiver = new HashMap<>();
    private final Set<MyVertex<Integer>> invalidSenders = new LinkedHashSet<>();
    private final List<MyVertex<Integer>> senderOrder = new ArrayList<>();

    @Override
    public AlgorithmType getType() {
        return AlgorithmType.ZKP;
    }

    @Override
    public void loadEnvironment(MyGraph<Integer, Integer> graph, AlgorithmSettings settings) {
        this.graph = graph;
        this.phase = AlgorithmPhase.SEND;
        this.isFinished.set(false);
        this.validProofs = 0;
        this.invalidProofs = 0;
        this.nextSenderIndex = 0;
        this.nextReceiverIndex = 0;
        this.totalMessages = 0;
        this.processedMessages = 0;
        this.invalidSenders.clear();
        this.secrets.clear();
        this.verifiedVotesByReceiver.clear();
        this.senderOrder.clear();

        if (settings != null && settings.getSettings() != null) {
            this.f = (int) settings.getSettings()
                    .getOrDefault("f", new AlgorithmSetting<>("f", 0, Integer.class, value -> true))
                    .getValue();
        }

        if (graph == null) {
            this.quorum = 1;
            return;
        }

        this.quorum = Math.max(1, graph.numVertices() - Math.max(f, 0));
        for (Vertex<Integer> vertex : graph.vertices()) {
            MyVertex<Integer> myVertex = (MyVertex<Integer>) vertex;
            myVertex.clearKnowledge();
            secrets.put(myVertex, proofSystem.commitVote(myVertex.getIsSupporting().get()));
            verifiedVotesByReceiver.put(myVertex, new LinkedHashMap<>());
            senderOrder.add(myVertex);
        }
        senderOrder.sort(Comparator.comparing(MyVertex::element));
        totalMessages = senderOrder.stream()
                .mapToInt(sender -> propagationTargets(sender).size())
                .sum();
    }

    @Override
    public StepReport step() {
        if (graph == null || graph.numVertices() == 0) {
            StepReport report = new StepReport();
            report.setAlgorithmPhase(phase);
            report.getProperties().put("f", String.valueOf(f));
            report.getProperties().put("quorum", String.valueOf(quorum));
            report.getProperties().put("system_dowodu", proofSystem.getSystemName());
            report.getProperties().put("gwarancja", "Kazdy ujawniony glos musi pasowac do wczesniejszego zobowiazania.");
            isFinished.set(true);
            return report;
        }

        return phase == AlgorithmPhase.SEND ? performSendPhase() : performChoosePhase();
    }

    private StepReport performSendPhase() {
        StepReport report = new StepReport();
        report.setAlgorithmPhase(AlgorithmPhase.SEND);

        if (totalMessages == 0) {
            phase = AlgorithmPhase.CHOOSE;
            return performChoosePhase();
        }

        while (nextSenderIndex < senderOrder.size()) {
            MyVertex<Integer> sender = senderOrder.get(nextSenderIndex);
            List<MyVertex<Integer>> receivers = propagationTargets(sender);

            if (nextReceiverIndex >= receivers.size()) {
                nextSenderIndex++;
                nextReceiverIndex = 0;
                continue;
            }

            MyVertex<Integer> receiver = receivers.get(nextReceiverIndex);
            OperationsBatch sendBatch = new OperationsBatch();
            PedersenVoteProofSystem.VoteSecret secret = secrets.get(sender);
            PedersenVoteProofSystem.VoteProof proof = proofSystem.createProof(secret, sender.element());
            boolean claimedVote = sender.getNextOpinion(receiver).get();
            boolean verified = proofSystem.verifyProof(secret.commitment(), claimedVote, proof, sender.element());

            if (verified) {
                verifiedVotesByReceiver.get(receiver).put(sender.element(), claimedVote);
                receiver.receiveOpinion(new SimpleBooleanProperty(claimedVote));
                validProofs++;
            } else {
                invalidProofs++;
                invalidSenders.add(sender);
            }

            sendBatch.add(new SendOperation(sender, receiver, new SimpleBooleanProperty(claimedVote)));
            report.addBatch(sendBatch);
            report.setNumSupporting(graph.getSupportingOpinionCount());
            report.setNumNotSupporting(graph.getNotSupportingOpinionCount());
            fillProperties(report, "Wysylanie glosu i dowodu");
            report.getProperties().put("nadawca", String.valueOf(sender.element()));
            report.getProperties().put("odbiorca", String.valueOf(receiver.element()));
            report.getProperties().put("odbiorcy", String.valueOf(receiver.element()));
            processedMessages++;
            report.getProperties().put("postep_wysylek", processedMessages + "/" + totalMessages);
            if (!invalidSenders.isEmpty()) {
                report.getProperties().put("alarm_reason", "Niewazny dowod ZKP lub proba zmiany glosu po zobowiazaniu");
            }
            fillSendRoles(report, sender, receiver);

            nextReceiverIndex++;
            if (nextReceiverIndex >= receivers.size()) {
                nextSenderIndex++;
                nextReceiverIndex = 0;
            }
            if (processedMessages >= totalMessages) {
                phase = AlgorithmPhase.CHOOSE;
            }
            return report;
        }

        phase = AlgorithmPhase.CHOOSE;
        return performChoosePhase();
    }

    private StepReport performChoosePhase() {
        StepReport report = new StepReport();
        report.setAlgorithmPhase(AlgorithmPhase.CHOOSE);

        OperationsBatch chooseBatch = new OperationsBatch();
        Set<MyVertex<Integer>> alarmNodes = new LinkedHashSet<>(invalidSenders);
        int loyalDecisions = 0;

        for (Vertex<Integer> rawVertex : orderedVertices()) {
            MyVertex<Integer> vertex = (MyVertex<Integer>) rawVertex;
            Map<Integer, Boolean> verifiedVotes = verifiedVotesByReceiver.getOrDefault(vertex, Map.of());
            PedersenVoteProofSystem.VoteSecret ownSecret = secrets.get(vertex);

            int supporting = ownSecret.vote() ? 1 : 0;
            int notSupporting = ownSecret.vote() ? 0 : 1;
            for (boolean vote : verifiedVotes.values()) {
                if (vote) {
                    supporting++;
                } else {
                    notSupporting++;
                }
            }

            int validVotesForVertex = verifiedVotes.size() + 1;
            boolean hasQuorum = validVotesForVertex >= quorum;
            boolean hasMajority = supporting != notSupporting;

            if (!hasQuorum || !hasMajority) {
                alarmNodes.add(vertex);
            } else {
                boolean decision = supporting > notSupporting;
                vertex.setIsSupporting(decision);
                if (!vertex.isTraitor().get()) {
                    loyalDecisions++;
                }
            }

            chooseBatch.add(new ChooseOperation(vertex, vertex.getIsSupporting()));
        }

        report.addBatch(chooseBatch);
        report.setNumSupporting(graph.getSupportingOpinionCount());
        report.setNumNotSupporting(graph.getNotSupportingOpinionCount());
        fillProperties(report, "Decyzja po weryfikacji dowodow");
        report.getProperties().put("decyzje", loyalDecisions + "/" + loyalNodeCount());
        if (!invalidSenders.isEmpty()) {
            report.getProperties().put("alarm_reason", "Niewazny dowod ZKP lub proba zmiany glosu po zobowiazaniu");
        } else if (alarmNodes.stream().anyMatch(vertex -> !invalidSenders.contains(vertex))) {
            report.getProperties().put("alarm_reason", "Za malo poprawnych dowodow ZKP do quorum albo brak jednoznacznej wiekszosci");
        }
        fillChooseRoles(report, alarmNodes);

        isFinished.set(true);
        return report;
    }

    private void fillProperties(StepReport report, String stage) {
        report.getProperties().put("f", String.valueOf(f));
        report.getProperties().put("quorum", String.valueOf(quorum));
        report.getProperties().put("etap", stage);
        report.getProperties().put("system_dowodu", proofSystem.getSystemName());
        report.getProperties().put("zobowiazania", String.valueOf(secrets.size()));
        report.getProperties().put("poprawne_dowody", String.valueOf(validProofs));
        report.getProperties().put("bledne_dowody", String.valueOf(invalidProofs));
        report.getProperties().put("bledni_nadawcy", invalidSendersSummary());
        report.getProperties().put("gwarancja", "Glos jest liczony tylko wtedy, gdy pasuje do wczesniejszego zobowiazania Pedersena.");
    }

    private void fillSendRoles(StepReport report, MyVertex<Integer> sender, MyVertex<Integer> receiver) {
        for (Vertex<Integer> rawVertex : orderedVertices()) {
            MyVertex<Integer> vertex = (MyVertex<Integer>) rawVertex;
            if (invalidSenders.contains(vertex) && vertex.equals(sender)) {
                report.getRoles().put(vertex, VertexRole.ALARM);
            } else if (vertex.equals(sender)) {
                report.getRoles().put(vertex, VertexRole.VOTER);
            } else if (vertex.equals(receiver)) {
                report.getRoles().put(vertex, VertexRole.NEIGHBOUR);
            } else {
                report.getRoles().put(vertex, VertexRole.NONE);
            }
        }
    }

    private void fillChooseRoles(StepReport report, Set<MyVertex<Integer>> alarmNodes) {
        for (Vertex<Integer> rawVertex : orderedVertices()) {
            MyVertex<Integer> vertex = (MyVertex<Integer>) rawVertex;
            report.getRoles().put(vertex, alarmNodes.contains(vertex) ? VertexRole.ALARM : VertexRole.NONE);
        }
    }

    private List<Vertex<Integer>> orderedVertices() {
        List<Vertex<Integer>> vertices = new ArrayList<>(graph.vertices());
        vertices.sort(Comparator.comparing(Vertex::element));
        return vertices;
    }

    private List<MyVertex<Integer>> propagationTargets(MyVertex<Integer> sender) {
        Collection<Vertex<Integer>> neighbours = graph.vertexNeighbours(sender);
        List<MyVertex<Integer>> result = new ArrayList<>();
        for (Vertex<Integer> neighbour : neighbours) {
            result.add((MyVertex<Integer>) neighbour);
        }
        result.sort(Comparator.comparing(MyVertex::element));
        return result;
    }

    private int loyalNodeCount() {
        int count = 0;
        for (Vertex<Integer> vertex : graph.vertices()) {
            if (!((MyVertex<Integer>) vertex).isTraitor().get()) {
                count++;
            }
        }
        return count;
    }

    private String invalidSendersSummary() {
        if (invalidSenders.isEmpty()) {
            return "brak";
        }
        StringJoiner joiner = new StringJoiner(", ");
        invalidSenders.stream()
                .map(MyVertex::element)
                .sorted()
                .forEach(id -> joiner.add(String.valueOf(id)));
        return joiner.toString();
    }

    @Override
    public boolean isFinished() {
        return isFinished.get();
    }

    @Override
    public BooleanProperty getIsFinishedProperty() {
        return isFinished;
    }
}

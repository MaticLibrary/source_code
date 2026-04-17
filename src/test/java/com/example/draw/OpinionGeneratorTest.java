package com.example.draw;

import com.example.model.MyGraph;
import com.example.model.MyVertex;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OpinionGeneratorTest {

    @Test
    void generateAttackersAssignsExpectedShareOfAttackers() {
        MyGraph<Integer, Integer> graph = new MyGraph<>();
        for (int i = 0; i < 8; i++) {
            graph.insertVertex(i);
        }

        OpinionGenerator generator = new OpinionGenerator();
        generator.generateAttackers(graph, 0.5);

        long attackers = graph.vertices().stream()
                .map(vertex -> (MyVertex<Integer>) vertex)
                .filter(vertex -> vertex.isSupportingOpinion().get())
                .count();

        assertEquals(4, attackers);
        assertEquals(4, graph.numVertices() - attackers);
    }
}

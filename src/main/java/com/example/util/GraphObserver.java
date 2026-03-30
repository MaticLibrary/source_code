package com.example.util;

import com.brunomnsilva.smartgraph.graph.Edge;
import com.brunomnsilva.smartgraph.graph.Graph;
import com.brunomnsilva.smartgraph.graph.Vertex;

public interface GraphObserver<V, E> {
    void vertexClicked(Vertex<V> vertex);
    void edgeClicked(Edge<E, V> edge);
    void vertexDoubleClicked(Vertex<V> vertex);
    void edgeDoubleClicked(Edge<E, V> edge);
    void clickedAt(double x, double y);
    void setGraph(Graph<V, E> newGraph);
}

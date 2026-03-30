package com.example.util;

import com.brunomnsilva.smartgraph.graph.Edge;
import com.brunomnsilva.smartgraph.graph.Graph;
import com.brunomnsilva.smartgraph.graph.Vertex;
import com.example.model.MyGraph;
import com.example.model.MyVertex;
import javafx.util.Pair;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class GraphConverter {
    static public void saveGraphML(File file, Graph<Integer, Integer> graph) throws IOException {
        String header = """
                <?xml version="1.0" encoding="UTF-8"?>
                <graphml xmlns="http://graphml.graphdrawing.org/xmlns"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xsi:schemaLocation="http://graphml.graphdrawing.org/xmlns
                http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd">
                """;
        String attrs = """
                <key id="d0" for="node" attr.name="attack" attr.type="boolean"><default>true</default></key>
                <key id="d1" for="node" attr.name="traitor" attr.type="boolean"><default>false</default></key>
                """;

        String graphNode = "<graph id=\"G\" edgedefault=\"undirected\">\n";
        StringBuilder edgesString = new StringBuilder();
        int i = 0;
        for (Edge<Integer, Integer> edge : graph.edges()) {
            edgesString.append("<edge id=\"").append(i).append("\" source=\"").append(edge.vertices()[0].element()).append("\" target=\"").append(edge.vertices()[1].element()).append("\"></edge>").append("\n");
            i++;
        }
        StringBuilder verticesString = new StringBuilder();
        for (Vertex<Integer> vertex : graph.vertices()) {
            boolean isFor = ((MyVertex<Integer>) vertex).isSupportingOpinion().get();
            boolean isTraitor = ((MyVertex<Integer>) vertex).isTraitor().get();
            verticesString.append("<node id=\"").append(vertex.element()).append("\"><data key=\"d0\">").append(isFor).append("</data><data key=\"d1\">").append(isTraitor).append("</data></node>").append("\n");
        }
        String finish = "</graph></graphml>\n";

        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        writer.write(header + attrs + graphNode + edgesString + verticesString + finish);
        writer.close();
    }

    static public MyGraph<Integer, Integer> fromML(File file) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setIgnoringElementContentWhitespace(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(file);

        Map<String, String> keys = new HashMap<>();
        List<MyVertex<Integer>> vertices = new LinkedList<>();
        List<Pair<Integer, Integer>> edges = new LinkedList<>();

        // Get data keys
        NodeList keyNodes = doc.getElementsByTagName("key");
        for (int i = 0; i < keyNodes.getLength(); i++) {
            org.w3c.dom.Node node = keyNodes.item(i);
            String id = node.getAttributes().getNamedItem("id").getNodeValue();
            keys.put(id,
                    node.getAttributes().getNamedItem("attr.name").getNodeValue());
        }

        // Get vertices
        NodeList vertexNodes = doc.getElementsByTagName("node");
        for (int i = 0; i < vertexNodes.getLength(); i++) {
            org.w3c.dom.Node node = vertexNodes.item(i);
            MyVertex<Integer> newVertex = new MyVertex<>(Integer.parseInt(node.getAttributes().getNamedItem("id").getNodeValue()));
            boolean traitor = false; //todo default value from graphML
            boolean opinion = false;

            NodeList dataNodes = node.getChildNodes();
            for (int j = 0; j < dataNodes.getLength(); j++) {
                org.w3c.dom.Node dataNode = dataNodes.item(j);
                if (dataNode.getNodeType() != org.w3c.dom.Node.ELEMENT_NODE || dataNode.getAttributes() == null) {
                    continue;
                }
                String key = dataNode.getAttributes().getNamedItem("key").getNodeValue();
                String attribute = keys.get(key);
                if (key != null) {
                    switch (attribute) {
                        case "traitor" -> traitor = Boolean.parseBoolean(dataNode.getChildNodes().item(0).getNodeValue());
                        case "attack" -> opinion = Boolean.parseBoolean(dataNode.getChildNodes().item(0).getNodeValue());
                    }

                }
            }
            newVertex.setIsTraitor(traitor);
            newVertex.setIsSupporting(opinion);
            vertices.add(newVertex);
        }

        //Get edges
        NodeList edgeNodes = doc.getElementsByTagName("edge");
        for (int i = 0; i < edgeNodes.getLength(); i++) {
            org.w3c.dom.Node node = edgeNodes.item(i);
            int from = Integer.parseInt(node.getAttributes().getNamedItem("source").getNodeValue());
            int to = Integer.parseInt(node.getAttributes().getNamedItem("target").getNodeValue());
            edges.add(new Pair<>(from, to));
        }

        //Reset graph
        MyGraph<Integer, Integer> newGraph = new MyGraph<>();
        for (MyVertex<Integer> vertex : vertices) {
            newGraph.insertVertex(vertex);
        }

        for (Pair<Integer, Integer> edge : edges) {
            newGraph.insertEdge(edge.getKey(), edge.getValue(), 1);
        }

        return newGraph;
    }

}

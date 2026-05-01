package com.example.controller.settings;

import com.example.model.MyGraph;
import com.example.settings.AlgorithmSetting;
import com.example.settings.AlgorithmSettings;
import com.example.settings.IntegerSettingTextField;
import com.example.settings.SettingNodesGroup;
import com.example.settings.SettingNodesGroupImpl;
import javafx.beans.property.BooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import lombok.Getter;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
@FxmlView("/view/settings/zkpSettingsView.fxml")
public class ZkpSettingsController implements AlgorithmSettingsController {
    @Getter
    @FXML
    public Node parent;
    @FXML
    public Label fLabel;
    @FXML
    public Label hintLabel;
    @FXML
    private IntegerSettingTextField f;

    private SettingNodesGroup settingNodesGroup;
    @Getter
    private final AlgorithmSettings algorithmSettings = new AlgorithmSettings();

    @FXML
    public void initialize() {
        setDefaultSettings();
        settingNodesGroup = new SettingNodesGroupImpl(Collections.singletonList(f));
    }

    @Override
    public BooleanProperty getAreSettingsValidProperty() {
        return settingNodesGroup.getAreAllValidProperty();
    }

    @Override
    public List<Node> getAllNodes() {
        return settingNodesGroup.getAllNodes().stream().map(settingNode -> (Node) settingNode).collect(Collectors.toList());
    }

    @Override
    public void adjustSettingsConditions(MyGraph<Integer, Integer> graph) {
        AlgorithmSetting<Integer> fSetting = (AlgorithmSetting<Integer>) algorithmSettings.getSettings().get("f");
        if (graph == null || graph.numVertices() == 0) {
            fSetting.setValidateArgument(value -> (Integer) value == 0);
            fSetting.setValue(0);
            return;
        }

        int maxF = Math.max(0, (graph.numVertices() - 1) / 2);
        int suggestedF = Math.min(graph.getTraitorsCount(), maxF);
        fSetting.setValidateArgument(value -> (Integer) value >= 0 && (Integer) value <= maxF);
        fSetting.setValue(suggestedF);
    }

    private void setDefaultSettings() {
        AlgorithmSetting<Integer> fSetting = new AlgorithmSetting<>("f", 0, Integer.class, value -> value >= 0);
        algorithmSettings.getSettings().put("f", fSetting);
        f.setContainedSetting(fSetting);
    }
}

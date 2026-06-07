package com.example.controller;

import com.example.ApplicationState;
import com.example.controller.settings.TraitorSettings;
import com.example.draw.DrawMode;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ToggleButton;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@FxmlView("/view/graphEditView.fxml")
public class GraphEditController {

    private final Map<ApplicationState, List<Button>> buttons = new HashMap<>();

    @FXML private Button vertexButton;
    @FXML private Button edgeButton;
    @FXML private Button deleteButton;
    @FXML private Button noneButton;

    @FXML private ToggleButton simulateButton;

    @FXML private Button undoButton;
    @FXML private Button redoButton;

    @FXML private Button startButton;
    @FXML private Button nextStepButton;
    @FXML private Button liveButton;
    @FXML private Button instantFinishButton;
    @FXML private Button pauseButton;
    @FXML public Button stopButton;

    @FXML public Button traitorSettings;

    private DrawMenuController drawMenuController;
    private SimulationController simulationController;
    private SimulationMenuController simulationMenuController;

    @FXML
    public void initialize() {
        initializeAlwaysDisplayedButtons();
    }

    // ===================== DRAW =====================

    private void initDrawButtons() {
        vertexButton.setOnAction(e -> safe(() ->
                drawMenuController.selectMode(DrawMode.VERTEX)));

        edgeButton.setOnAction(e -> safe(() ->
                drawMenuController.selectMode(DrawMode.EDGE)));

        deleteButton.setOnAction(e -> safe(() ->
                drawMenuController.selectMode(DrawMode.DELETE)));

        noneButton.setOnAction(e -> safe(() ->
                drawMenuController.selectMode(DrawMode.NONE)));

        undoButton.setOnAction(e -> safe(drawMenuController::undo));
        redoButton.setOnAction(e -> safe(drawMenuController::redo));

        buttons.put(ApplicationState.DRAWING,
                List.of(vertexButton, edgeButton, deleteButton, noneButton, undoButton, redoButton));
    }

    // ===================== SIMULATION =====================

    private void initSimulationButtons() {

        startButton.setOnAction(e -> safe(() -> simulationController.initSimulation()));
        nextStepButton.setOnAction(e -> safe(() -> simulationController.doStep()));
        liveButton.setOnAction(e -> safe(() -> simulationController.live()));
        instantFinishButton.setOnAction(e -> safe(() -> simulationController.instantFinish()));
        pauseButton.setOnAction(e -> safe(() -> simulationController.pause()));
        stopButton.setOnAction(e -> safe(() -> simulationController.stop()));

        traitorSettings.setOnAction(e -> safe(() -> {
            TraitorSettings.setTraitorsAlwaysLie(!TraitorSettings.isTraitorsAlwaysLie());

            traitorSettings.setText(
                    TraitorSettings.isTraitorsAlwaysLie()
                            ? "[Tryb] Zdrajcy zawsze kłamią"
                            : "[Tryb] Zdrajcy kłamią losowo"
            );
        }));

        traitorSettings.setText(
                TraitorSettings.isTraitorsAlwaysLie()
                        ? "[Tryb] Zdrajcy zawsze kłamią"
                        : "[Tryb] Zdrajcy kłamią losowo"
        );

        buttons.put(ApplicationState.SIMULATING,
                List.of(traitorSettings, startButton, nextStepButton,
                        liveButton, instantFinishButton, pauseButton, stopButton));
    }

    // ===================== ALWAYS =====================

    private void initializeAlwaysDisplayedButtons() {
        simulateButton.setOnAction(e ->
                safe(() -> simulationMenuController.simulateItem.fire())
        );
    }

    // ===================== INJECTIONS =====================

    public void setDrawMenuController(DrawMenuController controller) {
        this.drawMenuController = controller;
        initDrawButtons();

        undoButton.disableProperty().bind(controller.undoItemDisableProperty());
        redoButton.disableProperty().bind(controller.redoItemDisableProperty());
    }

    public void setSimulationMenuController(SimulationMenuController controller) {
        this.simulationMenuController = controller;
        initSimulationButtons();

        simulateButton.selectedProperty().unbind();
        controller.getAppController().getApplicationStateProperty()
                .addListener((obs, oldV, newV) ->
                        simulateButton.setSelected(newV == ApplicationState.SIMULATING));
    }

    public void setSimulationController(SimulationController controller) {
        this.simulationController = controller;
        bindButtons();
    }

    // ===================== ENABLE SWITCH =====================

    public void setEnabled(boolean enabled, ApplicationState state) {
        if (!buttons.containsKey(state)) return;

        buttons.get(state).forEach(b -> {
            b.setManaged(enabled);
            b.setVisible(enabled);
        });
    }

    // ===================== BINDINGS =====================

    public void bindButtons() {
        if (simulationController == null) return;

        startButton.disableProperty().bind(simulationController.getStartDisabledProperty());
        nextStepButton.disableProperty().bind(simulationController.getNextStepDisabledProperty());
        liveButton.disableProperty().bind(simulationController.getLiveDisabledProperty());
        instantFinishButton.disableProperty().bind(simulationController.getInstantFinishDisabledProperty());
        pauseButton.disableProperty().bind(simulationController.getPauseDisabledProperty());
        stopButton.disableProperty().bind(simulationController.getStopDisableProperty());

        traitorSettings.disableProperty().bind(simulationController.getStartDisabledProperty());
    }

    // ===================== SAFE RUN =====================

    private void safe(Runnable r) {
        if (r != null) r.run();
    }
}
package com.example.controller;

import javafx.fxml.FXML;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.stereotype.Component;

@Component
@FxmlView("/view/simulationResultView.fxml")
public class SimulationResultController {
    @FXML
    public Text simulationText;

    @FXML
    public Text consensusText;

    @FXML
    public Text finalInfoText;

    @FXML
    public Text referenceText;

    @FXML
    public Text verdictText;

    public void setMessage(boolean isConsensusReached,
                           Boolean finalConsensusOpinion,
                           Boolean initialReferenceOpinion,
                           int initialLoyalSupportingCount,
                           int initialLoyalNotSupportingCount) {
        if (isConsensusReached) {
            simulationText.setText("Symulacja zakonczona poprawnie");
            consensusText.setText("Osiagnieto konsensus");
            consensusText.setFill(Color.rgb(20, 130, 50));
        } else {
            simulationText.setText("Symulacja zakonczona niepowodzeniem");
            consensusText.setText("Nie osiagnieto konsensusu");
            consensusText.setFill(Color.rgb(150, 15, 15));
        }

        if (isConsensusReached && finalConsensusOpinion != null) {
            finalInfoText.setText("Informacja koncowa: " + opinionLabel(finalConsensusOpinion));
        } else {
            finalInfoText.setText("Informacja koncowa: brak jednej wspolnej decyzji");
        }

        if (initialReferenceOpinion != null) {
            referenceText.setText("Prawda bazowa na starcie: " + opinionLabel(initialReferenceOpinion)
                    + " (lojalni: atak=" + initialLoyalSupportingCount + ", odwrot=" + initialLoyalNotSupportingCount + ")");
        } else if (initialLoyalSupportingCount + initialLoyalNotSupportingCount == 0) {
            referenceText.setText("Prawda bazowa na starcie: brak lojalnych wezlow, nie da sie ocenic.");
        } else {
            referenceText.setText("Prawda bazowa na starcie: niejednoznaczna (remis lojalnych wezlow).");
        }

        if (!isConsensusReached || finalConsensusOpinion == null) {
            verdictText.setText("Ocena wyniku: brak jednej informacji koncowej, wiec nie da sie ocenic prawdziwosci.");
            verdictText.setFill(Color.rgb(150, 15, 15));
        } else if (initialReferenceOpinion == null) {
            verdictText.setText("Ocena wyniku: konsensus osiagniety, ale nie ma jednoznacznej prawdy bazowej do porownania.");
            verdictText.setFill(Color.rgb(180, 130, 20));
        } else if (finalConsensusOpinion.equals(initialReferenceOpinion)) {
            verdictText.setText("Ocena wyniku: wybrano informacje prawdziwa.");
            verdictText.setFill(Color.rgb(20, 130, 50));
        } else {
            verdictText.setText("Ocena wyniku: wybrano informacje zaklamana.");
            verdictText.setFill(Color.rgb(150, 15, 15));
        }
    }

    private String opinionLabel(boolean opinion) {
        return opinion ? "atak" : "odwrot";
    }
}

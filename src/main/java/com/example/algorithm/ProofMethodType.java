package com.example.algorithm;

public enum ProofMethodType {
    STARK,
    BULLETPROOFS;

    @Override
    public String toString() {
        return switch (this) {
            case STARK -> "STARK";
            case BULLETPROOFS -> "Bulletproofs";
        };
    }

    public String getUiHint() {
        return switch (this) {
            case STARK ->
                    "STARK: wybierz, gdy chcesz szybko weryfikowac bardzo duze obliczenia i zalezy Ci na odpornosci kwantowej. Wiekszy rozmiar dowodu nie jest tu problemem.";
            case BULLETPROOFS ->
                    "Bulletproofs: wybierz, gdy najwazniejszy jest maly rozmiar dowodu, np. na urzadzeniach mobilnych lub dla malych zakresow danych. Brak odpornosci kwantowej akceptujesz na ten moment.";
        };
    }

    public String getVerificationProfile() {
        return switch (this) {
            case STARK -> "szybka weryfikacja";
            case BULLETPROOFS -> "maly narzut przy malych danych";
        };
    }

    public String getProofSizeProfile() {
        return switch (this) {
            case STARK -> "wiekszy dowod";
            case BULLETPROOFS -> "bardzo maly dowod";
        };
    }

    public String getQuantumResistanceProfile() {
        return switch (this) {
            case STARK -> "tak";
            case BULLETPROOFS -> "nie";
        };
    }

    public String getBestForProfile() {
        return switch (this) {
            case STARK -> "duze partie transakcji i ciezkie obliczenia";
            case BULLETPROOFS -> "male dane i ciasne ograniczenia pamieci/lacza";
        };
    }

    public String getStepDescriptionSuffix() {
        return switch (this) {
            case STARK ->
                    " Wybrano STARK, wiec symulacja traktuje dowod jako profil nastawiony na szybka weryfikacje duzych partii danych i odpornosc kwantowa.";
            case BULLETPROOFS ->
                    " Wybrano Bulletproofs, wiec symulacja traktuje dowod jako profil nastawiony na minimalny rozmiar dowodu dla mniejszych danych.";
        };
    }
}

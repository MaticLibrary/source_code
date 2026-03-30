package com.example.algorithm;

import lombok.Getter;

public enum PrivateRuleType {
    CERTIFIED_QUORUM("Certyfikat quorum", (decision, n0, n1, totalObserved, leaderValue, leaderConflict, quorum, f) ->
            decision == null || leaderConflict || selectedSupport(decision, n0, n1) < quorum),

    CERTIFIED_WITH_LEADER("Certyfikat + zgodny lider", (decision, n0, n1, totalObserved, leaderValue, leaderConflict, quorum, f) ->
            decision == null
                    || leaderConflict
                    || selectedSupport(decision, n0, n1) < quorum
                    || leaderValue == null
                    || leaderValue != decision),

    CLEAR_MARGIN("Certyfikat + przewaga > f", (decision, n0, n1, totalObserved, leaderValue, leaderConflict, quorum, f) ->
            decision == null
                    || leaderConflict
                    || selectedSupport(decision, n0, n1) < quorum
                    || (selectedSupport(decision, n0, n1) - oppositeSupport(decision, n0, n1)) <= f),

    LOW_TURNOUT_GUARD("Alarm przy slabym quorum", (decision, n0, n1, totalObserved, leaderValue, leaderConflict, quorum, f) ->
            decision == null
                    || leaderConflict
                    || totalObserved < quorum),

    CONFLICT_GUARD("Alarm przy silnym konflikcie", (decision, n0, n1, totalObserved, leaderValue, leaderConflict, quorum, f) ->
            decision == null
                    || leaderConflict
                    || oppositeSupport(decision, n0, n1) > f),

    EQUIVOCATION_GUARD("Alarm tylko przy sprzecznych podpisach lidera", (decision, n0, n1, totalObserved, leaderValue, leaderConflict, quorum, f) ->
            leaderConflict);

    @Getter
    private final String label;
    private final AlarmPolicy alarmPolicy;

    PrivateRuleType(String label, AlarmPolicy alarmPolicy) {
        this.label = label;
        this.alarmPolicy = alarmPolicy;
    }

    public boolean triggersAlarm(Boolean decision,
                                 int n0,
                                 int n1,
                                 int totalObserved,
                                 Boolean leaderValue,
                                 boolean leaderConflict,
                                 int quorum,
                                 int f) {
        return alarmPolicy.triggersAlarm(decision, n0, n1, totalObserved, leaderValue, leaderConflict, Math.max(quorum, 1), Math.max(f, 0));
    }

    private static int selectedSupport(Boolean decision, int n0, int n1) {
        if (decision == null) {
            return 0;
        }
        return decision ? n1 : n0;
    }

    private static int oppositeSupport(Boolean decision, int n0, int n1) {
        if (decision == null) {
            return Math.max(n0, n1);
        }
        return decision ? n0 : n1;
    }

    @FunctionalInterface
    private interface AlarmPolicy {
        boolean triggersAlarm(Boolean decision,
                              int n0,
                              int n1,
                              int totalObserved,
                              Boolean leaderValue,
                              boolean leaderConflict,
                              int quorum,
                              int f);
    }
}

package com.example.algorithm;

import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PedersenVoteProofSystemTest {

    @Test
    void validProofVerifiesButChangedVoteFails() {
        PedersenVoteProofSystem proofSystem = new PedersenVoteProofSystem();
        PedersenVoteProofSystem.VoteSecret secret = proofSystem.commitVote(true);
        PedersenVoteProofSystem.VoteProof proof = proofSystem.createProof(secret, 7);

        assertTrue(proofSystem.verifyProof(secret.commitment(), true, proof, 7));
        assertFalse(proofSystem.verifyProof(secret.commitment(), false, proof, 7));
    }

    @Test
    void tamperedResponseFailsVerification() {
        PedersenVoteProofSystem proofSystem = new PedersenVoteProofSystem();
        PedersenVoteProofSystem.VoteSecret secret = proofSystem.commitVote(false);
        PedersenVoteProofSystem.VoteProof proof = proofSystem.createProof(secret, 11);
        PedersenVoteProofSystem.VoteProof tampered = new PedersenVoteProofSystem.VoteProof(
                proof.announcement(),
                proof.response().add(BigInteger.ONE)
        );

        assertFalse(proofSystem.verifyProof(secret.commitment(), false, tampered, 11));
    }
}

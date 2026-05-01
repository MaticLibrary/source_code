package com.example.algorithm;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class PedersenVoteProofSystem {
    private static final BigInteger ZERO = BigInteger.ZERO;
    private static final BigInteger ONE = BigInteger.ONE;
    private static final BigInteger TWO = BigInteger.valueOf(2);
    private static final String SAFE_PRIME_HEX =
            "99B7AF551D131733CBDDD36DD786AE4BA8F88D3080AA08E82662007E1C9AB5AE"
                    + "A2968C2292A7AC4F1F360BC3AA8AD7C483606946F9B1F7FF155ADF12F82D97B8"
                    + "ACD4EB5F551451393BA2B7444FF407605F0EF23B2D15A07456526B3ECA1B2C8D"
                    + "5C86F431CE0F9A53D2C55AC8C0161F5D0D2CCD468EC7AD732E157991165F9623";

    private final SecureRandom secureRandom = new SecureRandom();
    private final Parameters parameters = createParameters();

    public VoteSecret commitVote(boolean vote) {
        BigInteger randomness = randomScalar();
        BigInteger commitment = commitmentFor(vote, randomness);
        return new VoteSecret(vote, randomness, commitment);
    }

    public VoteProof createProof(VoteSecret secret, int senderId) {
        BigInteger witness = randomScalar();
        BigInteger announcement = parameters.h().modPow(witness, parameters.p());
        BigInteger challenge = computeChallenge(secret.commitment(), secret.vote(), announcement, senderId);
        BigInteger response = witness.add(challenge.multiply(secret.randomness())).mod(parameters.q());
        return new VoteProof(announcement, response);
    }

    public boolean verifyProof(BigInteger commitment, boolean claimedVote, VoteProof proof, int senderId) {
        if (commitment == null || proof == null || proof.announcement() == null || proof.response() == null) {
            return false;
        }

        BigInteger normalizedCommitment = normalizeCommitment(commitment, claimedVote);
        BigInteger challenge = computeChallenge(commitment, claimedVote, proof.announcement(), senderId);
        BigInteger left = parameters.h().modPow(proof.response(), parameters.p());
        BigInteger right = proof.announcement()
                .multiply(normalizedCommitment.modPow(challenge, parameters.p()))
                .mod(parameters.p());
        return left.equals(right);
    }

    public String getSystemName() {
        return "Pedersen commitment + Schnorr NIZK (Fiat-Shamir)";
    }

    private Parameters createParameters() {
        BigInteger p = new BigInteger(SAFE_PRIME_HEX, 16);
        BigInteger q = p.subtract(ONE).divide(TWO);
        if (!p.isProbablePrime(128) || !q.isProbablePrime(128)) {
            throw new IllegalStateException("Invalid Schnorr group parameters.");
        }
        BigInteger g = deriveSubgroupGenerator(p, q, "pedersen-g", null);
        BigInteger h = deriveSubgroupGenerator(p, q, "pedersen-h", g);
        if (!g.modPow(q, p).equals(ONE) || g.equals(ONE)) {
            throw new IllegalStateException("Invalid subgroup generator g.");
        }
        if (!h.modPow(q, p).equals(ONE) || h.equals(ONE)) {
            throw new IllegalStateException("Invalid subgroup generator h.");
        }
        return new Parameters(p, q, g, h);
    }

    private BigInteger deriveSubgroupGenerator(BigInteger p, BigInteger q, String label, BigInteger excludedGenerator) {
        int counter = 0;
        while (true) {
            byte[] digest = sha256((label + ":" + counter).getBytes(StandardCharsets.UTF_8));
            BigInteger candidate = new BigInteger(1, digest).mod(p.subtract(TWO)).add(TWO);
            BigInteger generator = candidate.modPow(TWO, p);
            if (!generator.equals(ONE)
                    && !generator.equals(excludedGenerator)
                    && generator.modPow(q, p).equals(ONE)) {
                return generator;
            }
            counter++;
        }
    }

    private BigInteger commitmentFor(boolean vote, BigInteger randomness) {
        BigInteger voteTerm = vote ? parameters.g() : ONE;
        BigInteger hidingTerm = parameters.h().modPow(randomness, parameters.p());
        return voteTerm.multiply(hidingTerm).mod(parameters.p());
    }

    private BigInteger normalizeCommitment(BigInteger commitment, boolean claimedVote) {
        if (!claimedVote) {
            return commitment.mod(parameters.p());
        }
        return commitment.multiply(parameters.g().modInverse(parameters.p())).mod(parameters.p());
    }

    private BigInteger computeChallenge(BigInteger commitment, boolean claimedVote, BigInteger announcement, int senderId) {
        byte[] digest = sha256(
                getSystemName(),
                Integer.toString(senderId),
                parameters.p().toString(16),
                parameters.q().toString(16),
                parameters.g().toString(16),
                parameters.h().toString(16),
                commitment.toString(16),
                claimedVote ? "1" : "0",
                announcement.toString(16)
        );
        return new BigInteger(1, digest).mod(parameters.q());
    }

    private BigInteger randomScalar() {
        BigInteger result;
        do {
            result = new BigInteger(parameters.q().bitLength(), secureRandom).mod(parameters.q());
        } while (result.equals(ZERO));
        return result;
    }

    private byte[] sha256(String... parts) {
        MessageDigest messageDigest = newDigest();
        for (String part : parts) {
            messageDigest.update(part.getBytes(StandardCharsets.UTF_8));
            messageDigest.update((byte) 0);
        }
        return messageDigest.digest();
    }

    private byte[] sha256(byte[] input) {
        MessageDigest messageDigest = newDigest();
        return messageDigest.digest(input);
    }

    private MessageDigest newDigest() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available.", e);
        }
    }

    public record Parameters(BigInteger p, BigInteger q, BigInteger g, BigInteger h) {
    }

    public record VoteSecret(boolean vote, BigInteger randomness, BigInteger commitment) {
    }

    public record VoteProof(BigInteger announcement, BigInteger response) {
    }
}

package no.met.kvalobs.kl;

public interface IDbStatus {
    void updateLastDbTime();
    void setDbError(String message);
}

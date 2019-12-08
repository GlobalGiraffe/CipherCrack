package mnh.game.ciphercrack.util;

public enum CrackState {
    QUEUED,        // The request has been created but is not yet running
    RUNNING,       // Crack attempt is underway
    COMPLETE,      // Crack finished by itself (not cancelled)
    CANCELLED;     // Crack attempt was cancelled before completion

    @Override
    public String toString() {
        return name().charAt(0) + name().substring(1).toLowerCase();
    }
}

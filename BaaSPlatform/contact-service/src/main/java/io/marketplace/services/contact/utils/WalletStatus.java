package io.marketplace.services.contact.utils;

public enum WalletStatus {
    ACTIVE("ACTIVE"),
    INACTIVE("INACTIVE"),
    BLOCKED("BLOCKED"),
    EXPIRED("EXPIRED");

    private final String text;

    /**
     * @param text
     */
    WalletStatus(final String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return text;
    }

}
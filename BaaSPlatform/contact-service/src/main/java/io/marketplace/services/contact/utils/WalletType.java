package io.marketplace.services.contact.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum WalletType {
    BANK_WALLET("BANK_WALLET", "11"), VIRTUAL_WALLET("VIRTUAL_WALLET", "12"),
    CRYPTO_WALLET("CRYPTO_WALLET", "13"), SETTLEMENT_WALLET("SETTLEMENT_WALLET", "14");

    private final String text;
    private final String numberCode;

    @Override
    public String toString() {
        return text;
    }

}
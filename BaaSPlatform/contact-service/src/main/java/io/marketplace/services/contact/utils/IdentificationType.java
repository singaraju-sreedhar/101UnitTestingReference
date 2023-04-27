package io.marketplace.services.contact.utils;

public enum IdentificationType {

    DIRECTORY_ID("DirectoryId"),
    ACCOUNT("AccountNumber"),
    MOBILE("MobileNumber"),
    IDCARD("IdCardNumber"),
    AMY("AmyNumber"),
    PASSPORT("PassportNumber"),
    BUSINESS_REG("BusinessRegNumber"),
    QR("QR");

    private final String type;

    private IdentificationType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public static IdentificationType getValueByType(String type) {
        for (IdentificationType value : IdentificationType.values()) {
            if (value.type.equalsIgnoreCase(type)) {
                return value;
            }
        }
        return null;
    }
}

package com.subsmanager.test;

/**
 * Konstanta environment untuk kebutuhan testing SubsManager.
 */
public final class AppContext {

    private AppContext() {}

    // ── Akun Test ─────────────────────────────────────────
    /** Email user biasa untuk login test. */
    public static final String USER_EMAIL    = "user@email.com";

    /** Password user biasa. */
    public static final String USER_PASSWORD = "admin123";

    /** Email akun admin. */
    public static final String ADMIN_EMAIL    = "suadmin@email.com";

    /** Password akun admin. */
    public static final String ADMIN_PASSWORD = "admin123";

    // ── Path FXML ─────────────────────────────────────────
    public static final String FXML_LOGIN       = "/com/subsmanager/gui/fxml/login.fxml";
    public static final String FXML_REGISTER    = "/com/subsmanager/gui/fxml/register.fxml";
    public static final String FXML_DASHBOARD   = "/com/subsmanager/gui/fxml/dashboard.fxml";
    public static final String FXML_SUBSCRIPTION= "/com/subsmanager/gui/fxml/subscription.fxml";
    public static final String FXML_ADDSUB      = "/com/subsmanager/gui/fxml/addsub.fxml";
    public static final String FXML_FINANCIAL   = "/com/subsmanager/gui/fxml/financial.fxml";
    public static final String FXML_COINSTORE   = "/com/subsmanager/gui/fxml/coinstore.fxml";
    public static final String FXML_COINHISTORY = "/com/subsmanager/gui/fxml/coinhistory.fxml";
    public static final String FXML_ADMINPANEL  = "/com/subsmanager/gui/fxml/adminpanel.fxml";

    // ── Timeout (milidetik) ───────────────────────────────
    public static final int TIMEOUT_DEFAULT = 5000;
    public static final int TIMEOUT_DB      = 8000;
    public static final int TIMEOUT_PAYMENT = 4000;
}
package com.subsmanager.subscription.model;

public enum BillingCycle {
	MONTHLY,
	YEARLY;
	
	public String getLabel() {
        switch (this) {
            case MONTHLY: return "Bulanan";
            case YEARLY:  return "Tahunan";
            default:      return "Tidak diketahui";
        }
    }
	
	
	public int getMonthCount() {
        switch (this) {
            case MONTHLY: return 1;
            case YEARLY:  return 12;
            default:      return 0;
        }
    }
}

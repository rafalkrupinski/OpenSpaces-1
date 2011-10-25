package org.openspaces.utest.admin.internal.alerts;

import java.util.HashMap;
import java.util.Map;

import org.openspaces.admin.alert.config.AlertConfiguration;

public class MockAlertConfiguration implements AlertConfiguration {

    private boolean enabled;
    private Map<String, String> properties = new HashMap<String, String>();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;

    }

    public String getBeanClassName() {
        return MockAlertBean.class.getName();
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;

    }
    
    public void setHighThreshold(int highThreshold) {
        properties.put("high-threshold", String.valueOf(highThreshold));
    }
    
    public Integer getHighThreshold() {
        return Integer.valueOf(properties.get("high-threshold"));
    }
    
    public void setLowThreshold(int lowThreshold) {
        properties.put("low-threshold", String.valueOf(lowThreshold));
    }
    
    public Integer getLowThreshold() {
        return Integer.valueOf(properties.get("low-threshold"));
    }
}

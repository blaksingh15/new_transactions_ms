package com.payinprocessingfee.dto;
public class PayinProcessingFeeDropdownDTO {
    private Integer id;
    private String connectorId;
    private String ecommerceCruisesJson;
    private String connectorName;
    private String channelType;
    private String connectorStatus;
    private Boolean defaultConnector;
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getConnectorId() {
        return connectorId;
    }

    public void setConnectorId(String connectorId) {
        this.connectorId = connectorId;
    }

    public String getEcommerceCruisesJson() {
        return ecommerceCruisesJson;
    }

    public void setEcommerceCruisesJson(String ecommerceCruisesJson) {
        this.ecommerceCruisesJson = ecommerceCruisesJson;
    }
    
    public String getConnectorName() {
        return connectorName;
    }

    public void setConnectorName(String connectorName) {
        this.connectorName = connectorName;
    }

    public String getChannelType() {
        return channelType;
    }

    public void setChannelType(String channelType) {
        this.channelType = channelType;
    }

    public String getConnectorStatus() {
        return connectorStatus;
    }

    public void setConnectorStatus(String connectorStatus) {
        this.connectorStatus = connectorStatus;
    }

    public Boolean getDefaultConnector() {
        return defaultConnector;
    }

    public void setDefaultConnector(Boolean defaultConnector) {
        this.defaultConnector = defaultConnector;
    }
}


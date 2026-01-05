package com.payinprocessingengine.entity;
import java.time.LocalDateTime;
public class BlackList {
    private Integer id;
    private Integer clientId;
    private String blacklistType;
    private String blacklistValue;
    private String condition;
    private String connectorId;
    private String remarks;
    private Short status = 1;
    private LocalDateTime createdDate = LocalDateTime.now();
    private String jsonLogHistory;
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getClientId() {
        return clientId;
    }

    public void setClientId(Integer clientId) {
        this.clientId = clientId;
    }

    public String getBlacklistType() {
        return blacklistType;
    }

    public void setBlacklistType(String blacklistType) {
        this.blacklistType = blacklistType;
    }

    public String getBlacklistValue() {
        return blacklistValue;
    }

    public void setBlacklistValue(String blacklistValue) {
        this.blacklistValue = blacklistValue;
    }

    public String getCondition() {
		return condition;
	}

	public void setCondition(String condition) {
		this.condition = condition;
	}

	public String getConnectorId() {
		return connectorId;
	}

	public void setConnectorId(String connectorId) {
		this.connectorId = connectorId;
	}

	public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public Short getStatus() {
        return status;
    }

    public void setStatus(Short status) {
        this.status = status;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }
    
    public String getFormattedTimestamp() {
        return createdDate != null ? createdDate.toString().replace("T", " ") : null;
    }
    
    public void setFormattedTimestamp(String formattedTimestamp) {
        if (formattedTimestamp != null) {
            this.createdDate = LocalDateTime.parse(formattedTimestamp.replace(" ", "T"));
        }
    }

    public String getJsonLogHistory() {
        return jsonLogHistory;
    }

    public void setJsonLogHistory(String jsonLogHistory) {
        this.jsonLogHistory = jsonLogHistory;
    }
    
}
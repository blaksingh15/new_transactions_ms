package com.connector.service;
import java.util.List;
import com.connector.entity.Connector;
public interface IConnectorService {
 public List<Connector> getAllConnectors();
 public Connector addOrUpdateConnector(Connector connector);
 public List<Connector> getDropdownConnectors();
 public boolean updateConnector(String originalCode, Connector connector);
 public boolean deleteConnector(Long id);
 public Connector getConnectorsById(Long id);
 public Connector findByConnectorNumber(String connectorName);

}

package com.connector.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import com.connector.entity.Connector;
public interface ConnectorRepository extends JpaRepository<Connector, Long> {
    Connector findByConnectorNumber(String connectorNumber);
}

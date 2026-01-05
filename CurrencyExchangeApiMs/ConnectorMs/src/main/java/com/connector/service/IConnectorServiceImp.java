package com.connector.service;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.connector.entity.Connector;
import com.connector.repository.ConnectorRepository;
import jakarta.transaction.Transactional;
import java.util.Optional;

@Service
@Transactional
public class IConnectorServiceImp implements IConnectorService {
	@Autowired
	private ConnectorRepository connectorRepo;

    @Override
	public Connector addOrUpdateConnector(Connector connector) {
        Connector existingConnector = connectorRepo.findByConnectorNumber(connector.getConnectorNumber());
        if (existingConnector == null) {
            Connector newConnector = new Connector();
            org.springframework.beans.BeanUtils.copyProperties(connector, newConnector, "id");
            newConnector.setId(null);
            return connectorRepo.saveAndFlush(newConnector);  // Save new connector
        } else {
            existingConnector.setConnectorName(connector.getConnectorName());
            existingConnector.setChannelType(connector.getChannelType());
            existingConnector.setConnectorStatus(connector.getConnectorStatus());
            existingConnector.setConnectorProdMode(connector.getConnectorProdMode());
            return connectorRepo.save(existingConnector); // Save updated connector
        }
    }

	@Override
	public List<Connector> getAllConnectors() {
        List<Connector> connectors = connectorRepo.findAll();
        connectors.sort(Comparator.comparing(Connector::getId).reversed());
        return connectors;
    }

	@Override
	public Connector getConnectorsById(Long id) {
	    Optional<Connector> connectorOptional = connectorRepo.findById(id);
	    return connectorOptional.orElse(null); 
	}


	@Override
	 public List<Connector> getDropdownConnectors() {
        return connectorRepo.findAll().stream()
                .filter(connector -> {
                    String status = connector.getConnectorStatus();
                    return "1".equals(status)
                            || "2".equals(status)
                            || "Active".equalsIgnoreCase(status)
                            || "Common".equalsIgnoreCase(status);
                })
                .map(connector -> {
                    Connector simplified = new Connector();
                    simplified.setConnectorNumber(connector.getConnectorNumber());
                    simplified.setConnectorName(connector.getConnectorName());
                    return simplified;
                })
                .collect(Collectors.toList());
    }
	
	@Override
	public boolean updateConnector(String originalCode, Connector connector) {
        Connector existingConnector =
                connectorRepo.findByConnectorNumber(originalCode);

        if (existingConnector == null) {
            return false;
        }

        existingConnector.setConnectorNumber(connector.getConnectorNumber());
        existingConnector.setConnectorName(connector.getConnectorName());
        existingConnector.setChannelType(connector.getChannelType());
        existingConnector.setConnectorStatus(connector.getConnectorStatus());
        existingConnector.setConnectorProdMode(connector.getConnectorProdMode());
        existingConnector.setMccCode(connector.getMccCode());
        existingConnector.setDefaultConnector(connector.getDefaultConnector());
        existingConnector.setConnectionMethod(connector.getConnectionMethod());
        existingConnector.setConnectorDescriptor(connector.getConnectorDescriptor());

        // URLs
        existingConnector.setConnectorBaseUrl(connector.getConnectorBaseUrl());
        existingConnector.setConnectorProdUrl(connector.getConnectorProdUrl());
        existingConnector.setConnectorUatUrl(connector.getConnectorUatUrl());
        existingConnector.setConnectorStatusUrl(connector.getConnectorStatusUrl());
        existingConnector.setConnectorRefundUrl(connector.getConnectorRefundUrl());
        existingConnector.setConnectorDevApiUrl(connector.getConnectorDevApiUrl());
        existingConnector.setHardCodePaymentUrl(connector.getHardCodePaymentUrl());
        existingConnector.setHardCodeStatusUrl(connector.getHardCodeStatusUrl());
        existingConnector.setHardCodeRefundUrl(connector.getHardCodeRefundUrl());

        // Processing
        existingConnector.setConnectorLoginCreds(connector.getConnectorLoginCreds());
        existingConnector.setConnectorProcessingCurrency(connector.getConnectorProcessingCurrency());
        existingConnector.setProcessingCurrencyMarkup(connector.getProcessingCurrencyMarkup());
        existingConnector.setConnectorRefundPolicy(connector.getConnectorRefundPolicy());
        existingConnector.setTransAutoExpired(connector.getTransAutoExpired());
        existingConnector.setTransAutoRefund(connector.getTransAutoRefund());
        existingConnector.setMopWeb(connector.getMopWeb());
        existingConnector.setMopMobile(connector.getMopMobile());
        existingConnector.setTechCommentsText(connector.getTechCommentsText());

        // Whitelist
        existingConnector.setConnectorWlIp(connector.getConnectorWlIp());
        existingConnector.setConnectorWlDomain(connector.getConnectorWlDomain());

        // Checkout UI
        existingConnector.setSkipCheckoutValidation(connector.getSkipCheckoutValidation());
        existingConnector.setRedirectPopupMsgWeb(connector.getRedirectPopupMsgWeb());
        existingConnector.setRedirectPopupMsgMobile(connector.getRedirectPopupMsgMobile());
        existingConnector.setCheckoutLabelNameWeb(connector.getCheckoutLabelNameWeb());
        existingConnector.setCheckoutLabelNameMobile(connector.getCheckoutLabelNameMobile());
        existingConnector.setCheckoutSubLabelNameWeb(connector.getCheckoutSubLabelNameWeb());
        existingConnector.setCheckoutSubLabelNameMobile(connector.getCheckoutSubLabelNameMobile());
        existingConnector.setCheckoutUiVersion(connector.getCheckoutUiVersion());
        existingConnector.setCheckoutUiTheme(connector.getCheckoutUiTheme());
        existingConnector.setCheckoutUiLanguage(connector.getCheckoutUiLanguage());

        // JSON configs
        existingConnector.setConnectorProcessingCredsJson(connector.getConnectorProcessingCredsJson());
        existingConnector.setEcommerceCruisesJson(connector.getEcommerceCruisesJson());
        existingConnector.setMerSettingJson(connector.getMerSettingJson());
        existingConnector.setConnectorLabelJson(connector.getConnectorLabelJson());
        existingConnector.setProcessingCountriesJson(connector.getProcessingCountriesJson());
        existingConnector.setBlockCountriesJson(connector.getBlockCountriesJson());

        // Notifications
        existingConnector.setNotificationEmail(connector.getNotificationEmail());
        existingConnector.setWebhookNotification(connector.getWebhookNotification());
        existingConnector.setNotificationCount(connector.getNotificationCount());
        existingConnector.setAutoStatusFetch(connector.getAutoStatusFetch());
        existingConnector.setAutoStatusStartTime(connector.getAutoStatusStartTime());
        existingConnector.setAutoStatusIntervalTime(connector.getAutoStatusIntervalTime());
        existingConnector.setCronBankStatusResponse(connector.getCronBankStatusResponse());

        connectorRepo.save(existingConnector);
        return true;
    }

    @Override
    public boolean deleteConnector(Long id) {
        Optional<Connector> connectorOpt = connectorRepo.findById(id);
        if (connectorOpt.isPresent()) {
            connectorRepo.deleteById(id);
            return true;
        }
        return false;
    }

    @Override
    public Connector findByConnectorNumber(String connectorName) {
        return connectorRepo.findByConnectorNumber(connectorName);
    }
	
}

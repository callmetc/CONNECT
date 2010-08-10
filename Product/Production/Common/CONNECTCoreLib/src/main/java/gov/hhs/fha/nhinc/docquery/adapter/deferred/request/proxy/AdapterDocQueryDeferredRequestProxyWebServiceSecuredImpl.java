/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gov.hhs.fha.nhinc.docquery.adapter.deferred.request.proxy;

import gov.hhs.fha.nhinc.adapterdocquerydeferredrequestsecured.AdapterDocQueryDeferredRequestSecuredPortType;
import gov.hhs.fha.nhinc.common.nhinccommon.AssertionType;
import gov.hhs.fha.nhinc.common.nhinccommon.NhinTargetCommunitiesType;
import gov.hhs.fha.nhinc.common.nhinccommonadapter.RespondingGatewayCrossGatewayQuerySecureRequestType;
import gov.hhs.fha.nhinc.connectmgr.ConnectionManagerCache;
import gov.hhs.fha.nhinc.connectmgr.ConnectionManagerException;
import gov.hhs.fha.nhinc.nhinclib.NhincConstants;
import gov.hhs.fha.nhinc.webserviceproxy.WebServiceProxyHelper;
import gov.hhs.healthit.nhin.DocQueryAcknowledgementType;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import oasis.names.tc.ebxml_regrep.xsd.query._3.AdhocQueryRequest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author JHOPPESC
 */
public class AdapterDocQueryDeferredRequestProxyWebServiceSecuredImpl implements AdapterDocQueryDeferredRequestProxy {

    private Log log = null;
    private static Service cachedService = null;
    private static final String NAMESPACE_URI = "urn:gov:hhs:fha:nhinc:adapterdocquerydeferredrequestsecured";
    private static final String SERVICE_LOCAL_PART = "AdapterDocQueryDeferredRequestSecured";
    private static final String PORT_LOCAL_PART = "AdapterDocQueryDeferredRequestSecuredPortSoap";
    private static final String WSDL_FILE = "AdapterDocQueryDeferredRequestSecured.wsdl";
    private static final String WS_ADDRESSING_ACTION = "urn:gov:hhs:fha:nhinc:adapterdocquerydeferredrequestsecured:RespondingGateway_CrossGatewayQueryRequestMessage";
    private WebServiceProxyHelper oProxyHelper = null;

    public AdapterDocQueryDeferredRequestProxyWebServiceSecuredImpl()
    {
        log = createLogger();
        oProxyHelper = createWebServiceProxyHelper();
    }

    protected Log createLogger()
    {
        return LogFactory.getLog(getClass());
    }

    protected WebServiceProxyHelper createWebServiceProxyHelper()
    {
        return new WebServiceProxyHelper();
    }

    protected String invokeConnectionManager(String serviceName) throws ConnectionManagerException
    {
        return ConnectionManagerCache.getLocalEndpointURLByServiceName(serviceName);
    }

    protected String getEndpointURL()
    {
        String endpointURL = null;
        String serviceName = NhincConstants.ADAPTER_DOCUMENT_QUERY_DEFERRED_REQ_SECURED_SERVICE_NAME;
        try
        {
            endpointURL = invokeConnectionManager(serviceName);
            log.debug("Retrieved endpoint URL for service " + serviceName + ": " + endpointURL);
        }
        catch (ConnectionManagerException ex)
        {
            log.error("Error getting url for " + serviceName + " from the connection manager. Error: " + ex.getMessage(), ex);
        }

        return endpointURL;
    }

    /**
     * This method retrieves and initializes the port.
     *
     * @param url The URL for the web service.
     * @return The port object for the web service.
     */
    protected AdapterDocQueryDeferredRequestSecuredPortType getPort(String url, String serviceAction, String wsAddressingAction, AssertionType assertion)
    {
        AdapterDocQueryDeferredRequestSecuredPortType port = null;
        Service service = getService();
        if (service != null)
        {
            log.debug("Obtained service - creating port.");

            port = service.getPort(new QName(NAMESPACE_URI, PORT_LOCAL_PART), AdapterDocQueryDeferredRequestSecuredPortType.class);
            oProxyHelper.initializeSecurePort((javax.xml.ws.BindingProvider) port, url, serviceAction, wsAddressingAction, assertion);
        }
        else
        {
            log.error("Unable to obtain serivce - no port created.");
        }
        return port;
    }

    /**
     * Retrieve the service class for this web service.
     *
     * @return The service class for this web service.
     */
    protected Service getService()
    {
        if (cachedService == null)
        {
            try
            {
                cachedService = oProxyHelper.createService(WSDL_FILE, NAMESPACE_URI, SERVICE_LOCAL_PART);
            }
            catch (Throwable t)
            {
                log.error("Error creating service: " + t.getMessage(), t);
            }
        }
        return cachedService;
    }

    public DocQueryAcknowledgementType respondingGatewayCrossGatewayQuery(AdhocQueryRequest msg, AssertionType assertion, NhinTargetCommunitiesType targets) {
        log.debug("Begin respondingGatewayCrossGatewayQuery");
        DocQueryAcknowledgementType response = null;

        try
        {
            String url = getEndpointURL();
            AdapterDocQueryDeferredRequestSecuredPortType port = getPort(url, NhincConstants.DOC_QUERY_ACTION, WS_ADDRESSING_ACTION, assertion);

            if(msg == null)
            {
                log.error("Message was null");
            }
            else if(assertion == null)
            {
                log.error("AssertionType was null");
            }
            else if(targets == null)
            {
                log.error("NhinTargetCommunitiesType was null");
            }
            else if(port == null)
            {
                log.error("port was null");
            }
            else
            {
                RespondingGatewayCrossGatewayQuerySecureRequestType request = new RespondingGatewayCrossGatewayQuerySecureRequestType();
                request.setAdhocQueryRequest(msg);

                response = (DocQueryAcknowledgementType)oProxyHelper.invokePort(port, AdapterDocQueryDeferredRequestSecuredPortType.class, "respondingGatewayCrossGatewayQuery", request);
            }
        }
        catch (Exception ex)
        {
            log.error("Error calling respondingGatewayCrossGatewayQuery: " + ex.getMessage(), ex);
        }

        log.debug("End respondingGatewayCrossGatewayQuery");
        return response;
    }
}
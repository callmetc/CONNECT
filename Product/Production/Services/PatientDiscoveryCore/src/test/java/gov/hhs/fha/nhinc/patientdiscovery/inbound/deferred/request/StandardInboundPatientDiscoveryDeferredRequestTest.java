/*
 * Copyright (c) 2012, United States Government, as represented by the Secretary of Health and Human Services.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above
 *       copyright notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the documentation
 *       and/or other materials provided with the distribution.
 *     * Neither the name of the United States Government nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE UNITED STATES GOVERNMENT BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package gov.hhs.fha.nhinc.patientdiscovery.inbound.deferred.request;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import gov.hhs.fha.nhinc.aspect.InboundProcessingEvent;
import gov.hhs.fha.nhinc.common.nhinccommon.AssertionType;
import gov.hhs.fha.nhinc.nhinclib.NhincConstants;
import gov.hhs.fha.nhinc.patientdiscovery.PatientDiscoveryAuditLogger;
import gov.hhs.fha.nhinc.patientdiscovery.PatientDiscoveryPolicyChecker;
import gov.hhs.fha.nhinc.patientdiscovery.adapter.deferred.request.error.proxy.AdapterPatientDiscoveryDeferredReqErrorProxy;
import gov.hhs.fha.nhinc.patientdiscovery.adapter.deferred.request.error.proxy.AdapterPatientDiscoveryDeferredReqErrorProxyObjectFactory;
import gov.hhs.fha.nhinc.patientdiscovery.adapter.deferred.request.proxy.AdapterPatientDiscoveryDeferredReqProxy;
import gov.hhs.fha.nhinc.patientdiscovery.adapter.deferred.request.proxy.AdapterPatientDiscoveryDeferredReqProxyObjectFactory;
import gov.hhs.fha.nhinc.patientdiscovery.aspect.MCCIIN000002UV01EventDescriptionBuilder;
import gov.hhs.fha.nhinc.patientdiscovery.aspect.PRPAIN201305UV02EventDescriptionBuilder;

import java.lang.reflect.Method;

import org.hl7.v3.MCCIIN000002UV01;
import org.hl7.v3.PRPAIN201305UV02;
import org.hl7.v3.PRPAIN201306UV02;
import org.junit.Test;

/**
 * @author akong
 * 
 */
public class StandardInboundPatientDiscoveryDeferredRequestTest {

    @Test
    public void hasInboundProcessingEvent() throws Exception {
        Class<StandardInboundPatientDiscoveryDeferredRequest> clazz = StandardInboundPatientDiscoveryDeferredRequest.class;
        Method method = clazz.getMethod("respondingGatewayPRPAIN201305UV02", PRPAIN201305UV02.class,
                AssertionType.class);
        InboundProcessingEvent annotation = method.getAnnotation(InboundProcessingEvent.class);
        assertNotNull(annotation);
        assertEquals(PRPAIN201305UV02EventDescriptionBuilder.class, annotation.beforeBuilder());
        assertEquals(MCCIIN000002UV01EventDescriptionBuilder.class, annotation.afterReturningBuilder());
        assertEquals("Patient Discovery Deferred Request", annotation.serviceType());
        assertEquals("1.0", annotation.version());
    }

    @Test
    public void standardInboundPatientDiscoveryRequest() {
        PRPAIN201305UV02 request = new PRPAIN201305UV02();
        AssertionType assertion = new AssertionType();
        MCCIIN000002UV01 expectedResponse = new MCCIIN000002UV01();

        PatientDiscoveryPolicyChecker policyChecker = mock(PatientDiscoveryPolicyChecker.class);
        AdapterPatientDiscoveryDeferredReqErrorProxyObjectFactory proxyErrorFactory = mock(AdapterPatientDiscoveryDeferredReqErrorProxyObjectFactory.class);
        PatientDiscoveryAuditLogger auditLogger = mock(PatientDiscoveryAuditLogger.class);
        AdapterPatientDiscoveryDeferredReqProxyObjectFactory adapterFactory = mock(AdapterPatientDiscoveryDeferredReqProxyObjectFactory.class);
        AdapterPatientDiscoveryDeferredReqProxy adapterProxy = mock(AdapterPatientDiscoveryDeferredReqProxy.class);

        when(adapterFactory.getAdapterPatientDiscoveryDeferredReqProxy()).thenReturn(adapterProxy);

        when(adapterProxy.processPatientDiscoveryAsyncReq(request, assertion)).thenReturn(expectedResponse);

        when(policyChecker.checkIncomingPolicy(request, assertion)).thenReturn(true);

        StandardInboundPatientDiscoveryDeferredRequest standardPatientDiscovery = new StandardInboundPatientDiscoveryDeferredRequest(
                policyChecker, proxyErrorFactory, adapterFactory, auditLogger);

        MCCIIN000002UV01 actualResponse = standardPatientDiscovery
                .respondingGatewayPRPAIN201305UV02(request, assertion);

        assertSame(expectedResponse, actualResponse);

        verify(auditLogger).auditNhinDeferred201305(eq(request), eq(assertion),
                eq(NhincConstants.AUDIT_LOG_INBOUND_DIRECTION));

        verify(auditLogger).auditAck(eq(actualResponse), eq(assertion),
                eq(NhincConstants.AUDIT_LOG_OUTBOUND_DIRECTION), eq(NhincConstants.AUDIT_LOG_NHIN_INTERFACE));

        verify(auditLogger).auditAdapterDeferred201305(eq(request), eq(assertion),
                eq(NhincConstants.AUDIT_LOG_OUTBOUND_DIRECTION));

        verify(auditLogger).auditAck(eq(actualResponse), eq(assertion), eq(NhincConstants.AUDIT_LOG_INBOUND_DIRECTION),
                eq(NhincConstants.AUDIT_LOG_ADAPTER_INTERFACE));
    }

    @Test
    public void failedPolicyCheck() {
        PRPAIN201305UV02 request = new PRPAIN201305UV02();
        AssertionType assertion = new AssertionType();
        MCCIIN000002UV01 expectedErrorResponse = new MCCIIN000002UV01();

        PatientDiscoveryPolicyChecker policyChecker = mock(PatientDiscoveryPolicyChecker.class);
        AdapterPatientDiscoveryDeferredReqErrorProxyObjectFactory proxyErrorFactory = mock(AdapterPatientDiscoveryDeferredReqErrorProxyObjectFactory.class);
        AdapterPatientDiscoveryDeferredReqErrorProxy errorProxy = mock(AdapterPatientDiscoveryDeferredReqErrorProxy.class);
        PatientDiscoveryAuditLogger auditLogger = mock(PatientDiscoveryAuditLogger.class);

        AdapterPatientDiscoveryDeferredReqProxyObjectFactory adapterProxyFactory = mock(AdapterPatientDiscoveryDeferredReqProxyObjectFactory.class);

        when(proxyErrorFactory.create()).thenReturn(errorProxy);

        when(
                errorProxy.processPatientDiscoveryAsyncReqError(eq(request), any(PRPAIN201306UV02.class),
                        eq(assertion), any(String.class))).thenReturn(expectedErrorResponse);

        when(policyChecker.checkIncomingPolicy(request, assertion)).thenReturn(false);

        StandardInboundPatientDiscoveryDeferredRequest standardPatientDiscovery = new StandardInboundPatientDiscoveryDeferredRequest(
                policyChecker, proxyErrorFactory, adapterProxyFactory, auditLogger);

        MCCIIN000002UV01 actualResponse = standardPatientDiscovery
                .respondingGatewayPRPAIN201305UV02(request, assertion);

        assertSame(expectedErrorResponse, actualResponse);

        verify(auditLogger).auditNhinDeferred201305(eq(request), eq(assertion),
                eq(NhincConstants.AUDIT_LOG_INBOUND_DIRECTION));

        verify(auditLogger).auditAck(eq(actualResponse), eq(assertion),
                eq(NhincConstants.AUDIT_LOG_OUTBOUND_DIRECTION), eq(NhincConstants.AUDIT_LOG_NHIN_INTERFACE));

        verify(auditLogger).auditAdapterDeferred201305(eq(request), eq(assertion),
                eq(NhincConstants.AUDIT_LOG_OUTBOUND_DIRECTION));

        verify(auditLogger).auditAck(eq(actualResponse), eq(assertion), eq(NhincConstants.AUDIT_LOG_INBOUND_DIRECTION),
                eq(NhincConstants.AUDIT_LOG_ADAPTER_INTERFACE));
    }

}

package org.ndexbio.redirectservice;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.core.OutputStreamAppender;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.mock;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;



/**
 * 
 * @author churas
 */
public class TestRequestLoggingFilter {
	
	ByteArrayOutputStream _bos;

	
	public TestRequestLoggingFilter() {
	}
	

	@BeforeEach
	public void beforeTest(){
		LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
		PatternLayoutEncoder logEncoder = new PatternLayoutEncoder();
		logEncoder.setContext(lc);
		logEncoder.setPattern("%msg%n");
		logEncoder.start();
		OutputStreamAppender osa = new OutputStreamAppender();
		_bos = new ByteArrayOutputStream();
		osa.setOutputStream(_bos);
		osa.setContext(lc);
		osa.setEncoder(logEncoder);
		osa.setName(RequestLoggingFilter.REQUEST_LOGGER_NAME + "appender");
		osa.start();
		ch.qos.logback.classic.Logger requestLog =
			  (ch.qos.logback.classic.Logger) lc.getLogger(RequestLoggingFilter.REQUEST_LOGGER_NAME);
		requestLog.detachAndStopAllAppenders();
		requestLog.setLevel(Level.toLevel("INFO"));
		requestLog.setAdditive(false);
		requestLog.addAppender(osa);
		
	}
	
	@AfterEach
	public void afterTest(){
		
	}

	@Test
	public void testDoFilterWithIpInHeader() throws IOException, ServletException {
		HttpServletRequest mockServletRequest = mock(HttpServletRequest.class);
		expect(mockServletRequest.getHeader(RequestLoggingFilter.X_FORWARDED_FOR)).andReturn("1.2.3.4");
		expect(mockServletRequest.getHeader(RequestLoggingFilter.USER_AGENT)).andReturn("agent");
		expect(mockServletRequest.getMethod()).andReturn("GET");
		expect(mockServletRequest.getRequestURI()).andReturn("/foo");
		expect(mockServletRequest.getQueryString()).andReturn("x=1");
		HttpServletResponse mockServletResponse = mock(HttpServletResponse.class);
		expect(mockServletResponse.getStatus()).andReturn(1);
		FilterChain mockFilterChain = mock(FilterChain.class);
		mockFilterChain.doFilter(mockServletRequest, mockServletResponse);
		
		RequestLoggingFilter rlf = new RequestLoggingFilter();
		
		replay(mockServletRequest);
		replay(mockServletResponse);
		replay(mockFilterChain);
		rlf.doFilter(mockServletRequest, mockServletResponse, mockFilterChain);
		
		verify(mockServletRequest);
		verify(mockServletResponse);
		verify(mockFilterChain);
		
		String[] lines = _bos.toString().split("\n");
		assertTrue(lines[0].startsWith("[tid:"), lines[0]);
		assertTrue(lines[0].contains("]	[start]	[GET]	[]	[1.2.3.4]	[agent]	[]	[/foo]	[x=1]"), lines[0]);
		assertTrue(lines[1].startsWith("[tid:"), lines[1]);
		assertTrue(lines[1].contains("]	[end] [status: 1]"), lines[1]);
	}
	
	@Test
	public void testDoFilterWithIpNotInHeaderNoUserAgent() throws IOException, ServletException {
		HttpServletRequest mockServletRequest = mock(HttpServletRequest.class);
		expect(mockServletRequest.getHeader(RequestLoggingFilter.X_FORWARDED_FOR)).andReturn(null);
		expect(mockServletRequest.getHeader(RequestLoggingFilter.USER_AGENT)).andReturn(null);
		expect(mockServletRequest.getRemoteAddr()).andReturn("5.6.7.8");
		expect(mockServletRequest.getMethod()).andReturn("POST");
		expect(mockServletRequest.getRequestURI()).andReturn("/x");
		expect(mockServletRequest.getQueryString()).andReturn("");
		HttpServletResponse mockServletResponse = mock(HttpServletResponse.class);
		expect(mockServletResponse.getStatus()).andReturn(200);
		FilterChain mockFilterChain = mock(FilterChain.class);
		mockFilterChain.doFilter(mockServletRequest, mockServletResponse);
		
		RequestLoggingFilter rlf = new RequestLoggingFilter();
		
		replay(mockServletRequest);
		replay(mockServletResponse);
		replay(mockFilterChain);
		rlf.doFilter(mockServletRequest, mockServletResponse, mockFilterChain);
		
		verify(mockServletRequest);
		verify(mockServletResponse);
		verify(mockFilterChain);
		String[] lines = _bos.toString().split("\n");
		assertTrue(lines[0].contains("]	[start]	[POST]	[]	[5.6.7.8]	[]	[]	[/x]	[]"), lines[0]);
		assertTrue(lines[1].contains("]	[end] [status: 200]"), lines[1]);

		
	}
	
}

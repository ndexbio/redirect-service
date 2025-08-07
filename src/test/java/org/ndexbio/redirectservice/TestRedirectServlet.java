package org.ndexbio.redirectservice;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.mock;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.Test;



/**
 *
 * @author churas
 */
public class TestRedirectServlet {
	
	public TestRedirectServlet() {
	}


	@Test
	public void testRedirectServletNullRedirectMap(){
		try {
			RedirectServlet rs = new RedirectServlet(null, new ArrayList<String>());
			fail("Expected Exception");
		} catch(NullPointerException ex){
			assertEquals("Redirect map is null", ex.getMessage());
		}
	}
	
	@Test
	public void testRedirectServletNullUniProtMap(){
		try {
			RedirectServlet rs = new RedirectServlet(new HashMap<String, String>(), null);
			fail("Expected Exception");
		} catch(NullPointerException ex){
			assertEquals("Uniprot mapping is null", ex.getMessage());
		}
	
	}
	
	@Test
	public void testRedirectServletNoIdProvidedNullForPath() throws IOException {
		HttpServletRequest mockReq = mock(HttpServletRequest.class);
		expect(mockReq.getPathInfo()).andReturn(null);
		
		HttpServletResponse mockResp = mock(HttpServletResponse.class);
		mockResp.sendError(HttpServletResponse.SC_NOT_FOUND, "No ID provided.");
		replay(mockResp);
		replay(mockReq);
		RedirectServlet rs = new RedirectServlet(new HashMap<String, String>(),
				new ArrayList<String>());

		rs.doGet(mockReq, mockResp);
		verify(mockResp);
		verify(mockReq);
	}
	
	@Test
	public void testRedirectServletNoIdProvidedJustSlash() throws IOException {
		HttpServletRequest mockReq = mock(HttpServletRequest.class);
		expect(mockReq.getPathInfo()).andReturn("/");
		
		HttpServletResponse mockResp = mock(HttpServletResponse.class);
		mockResp.sendError(HttpServletResponse.SC_NOT_FOUND, "No ID provided.");
		replay(mockResp);
		replay(mockReq);
		RedirectServlet rs = new RedirectServlet(new HashMap<String, String>(),
				new ArrayList<String>());

		rs.doGet(mockReq, mockResp);
		verify(mockResp);
		verify(mockReq);
	}
	
	@Test
	public void testRedirectServletStatus() throws IOException {
		HttpServletRequest mockReq = mock(HttpServletRequest.class);
		expect(mockReq.getPathInfo()).andReturn("/StATus");
		
		HttpServletResponse mockResp = mock(HttpServletResponse.class);
		mockResp.setStatus(HttpServletResponse.SC_OK);
		mockResp.setContentType("text/plain");
		StringWriter sw = new StringWriter();
		PrintWriter writer = new PrintWriter(sw);
		
		expect(mockResp.getWriter()).andReturn(writer);
		replay(mockResp);
		replay(mockReq);
		RedirectServlet rs = new RedirectServlet(new HashMap<String, String>(),
				new ArrayList<String>());

		rs.doGet(mockReq, mockResp);
		verify(mockResp);
		verify(mockReq);
		writer.close();
		assertEquals("OK\n", sw.toString());
	}
	
	@Test
	public void testRedirectServletUniProtMappingFile() throws IOException {
		HttpServletRequest mockReq = mock(HttpServletRequest.class);
		expect(mockReq.getPathInfo()).andReturn("/UNIPROT_mapping_FILE");
		
		HttpServletResponse mockResp = mock(HttpServletResponse.class);
		mockResp.setStatus(HttpServletResponse.SC_OK);
		mockResp.setContentType("text/plain");
		StringWriter sw = new StringWriter();
		PrintWriter writer = new PrintWriter(sw);
		
		expect(mockResp.getWriter()).andReturn(writer);
		replay(mockResp);
		replay(mockReq);
		
		ArrayList<String> uniprotMapping = new ArrayList<>();
		uniprotMapping.add("some,line");
		RedirectServlet rs = new RedirectServlet(new HashMap<String, String>(),
				uniprotMapping);

		rs.doGet(mockReq, mockResp);
		verify(mockResp);
		verify(mockReq);
		writer.close();
		assertEquals("some,line\n", sw.toString());
	}
	
	@Test
	public void testRedirectServletIdNotInMap() throws IOException {
		HttpServletRequest mockReq = mock(HttpServletRequest.class);
		expect(mockReq.getPathInfo()).andReturn("/notfound");
		
		HttpServletResponse mockResp = mock(HttpServletResponse.class);
		mockResp.sendError(HttpServletResponse.SC_NOT_FOUND, "Unknown ID: \"NOTFOUND\"");
		replay(mockResp);
		replay(mockReq);
		
		ArrayList<String> uniprotMapping = new ArrayList<>();
		uniprotMapping.add("some,line");
		RedirectServlet rs = new RedirectServlet(new HashMap<String, String>(),
				uniprotMapping);

		rs.doGet(mockReq, mockResp);
		verify(mockResp);
		verify(mockReq);
	}
	
	@Test
	public void testRedirectServletSuccess() throws IOException {
		HttpServletRequest mockReq = mock(HttpServletRequest.class);
		expect(mockReq.getPathInfo()).andReturn("/idone");
		
		HttpServletResponse mockResp = mock(HttpServletResponse.class);
		mockResp.setStatus(HttpServletResponse.SC_FOUND);
		mockResp.setHeader("Location", "https://url.one");
		replay(mockResp);
		replay(mockReq);
		
		ArrayList<String> uniprotMapping = new ArrayList<>();
		uniprotMapping.add("some,line");
		HashMap<String, String> redirectMap = new HashMap<>();
		redirectMap.put("IDONE", "https://url.one");
		RedirectServlet rs = new RedirectServlet(redirectMap,
				uniprotMapping);

		rs.doGet(mockReq, mockResp);
		verify(mockResp);
		verify(mockReq);
	}
	
	@Test
	public void testRedirectServletWithTrailingSlash() throws IOException {
		HttpServletRequest mockReq = mock(HttpServletRequest.class);
		expect(mockReq.getPathInfo()).andReturn("/idone/");
		
		HttpServletResponse mockResp = mock(HttpServletResponse.class);

		mockResp.sendError(HttpServletResponse.SC_NOT_FOUND, "Unknown ID: \"IDONE/\"");
		replay(mockResp);
		replay(mockReq);
		
		ArrayList<String> uniprotMapping = new ArrayList<>();
		uniprotMapping.add("some,line");
		HashMap<String, String> redirectMap = new HashMap<>();
		redirectMap.put("IDONE", "https://url.one");
		RedirectServlet rs = new RedirectServlet(redirectMap,
				uniprotMapping);

		rs.doGet(mockReq, mockResp);
		verify(mockResp);
		verify(mockReq);
	}
	
}

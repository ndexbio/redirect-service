package org.ndexbio.redirectservice;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 *
 * @author churas
 */
public class RedirectServlet extends HttpServlet {
	private final Map<String, String> redirectMap;
	private final List<String> uniprotMapping;

	public RedirectServlet(Map<String, String> redirectMap, List<String> uniprotMapping) {
		this.redirectMap = redirectMap;
		this.uniprotMapping = uniprotMapping;
		Collections.sort(this.uniprotMapping);

	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		String path = req.getPathInfo();
		if (path == null || path.length() <= 1) {
			resp.sendError(HttpServletResponse.SC_NOT_FOUND, "No ID provided.");
			return;
		}

		String id = path.substring(1).toUpperCase();
		if (id.equals("STATUS")){
			resp.setContentType("text/plain");
			resp.setStatus(HttpServletResponse.SC_OK);
			resp.getWriter().println("OK");
			return;
		}
		if (id.equals("UNIPROT_MAPPING_FILE")){
			resp.setContentType("text/plain");
			resp.setStatus(HttpServletResponse.SC_OK);

			for (String item : uniprotMapping) {
				resp.getWriter().println(item);
			}
			return;
		}
		String redirectTo = redirectMap.get(id);
		if (redirectTo != null) {
			resp.setStatus(HttpServletResponse.SC_FOUND);
			resp.setHeader("Location", redirectTo);
		} else {
			resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Unknown ID: " + id);
		}
	}
}


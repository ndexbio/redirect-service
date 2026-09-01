package org.ndexbio.redirectservice;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple endpoint that returns a redirect to a URL defined in
 * a mapping passed in via the constructor
 * 
 * @author churas
 */
public class RedirectServlet extends HttpServlet {
	
	private static final Logger _logger = LoggerFactory.getLogger(RedirectServlet.class);

	private final Map<String, String> redirectMap;
	private final List<String> uniprotMapping;
        private final Map<String, String> pathwayRedirectMap;

        public RedirectServlet(Map<String, String> redirectMap, List<String> uniprotMapping){
            this(redirectMap, uniprotMapping, null);
        }
        
	/**
	 * Constructor
	 * 
	 * @param redirectMap Map where key is ID and value is URL to redirect 
	 *                    requests to. NOTE: the key values need to be in upper case
	 * @param uniprotMapping List of strings representing a table of uniprot ids to
	 *                       IDs in the redirectMap
         * @param pathwayRedirectMap Map where key is ID and value is URL to redirect requests
         *                           under /pathway to. NOTE: the key values need ot be in upper case
	 * @throws NullPointerException 
	 */
	public RedirectServlet(Map<String, String> redirectMap, List<String> uniprotMapping,
                Map<String, String> pathwayRedirectMap) throws NullPointerException {
		this.redirectMap = redirectMap;
		if (this.redirectMap == null){
			throw new NullPointerException("Redirect map is null");
		}
                this.pathwayRedirectMap = pathwayRedirectMap;
                
		
		this.uniprotMapping = uniprotMapping;
		if (this.uniprotMapping == null){
			throw new NullPointerException("Uniprot mapping is null");
		}
		Collections.sort(this.uniprotMapping);

	}

	/**
	 * Handles the request. Grabs the last part of the path
	 * and assumes it is an ID or one of the three special strings 
	 * (PATHWAY, STATUS, UNIPROT_MAPPING_FILE) where case is ignored 
	 *
         * If PATHWAY, this method assumes next part of path is an ID and 
         * does case insensitive check of mapping in pathwayRedirectMap setting
         * the value as Location header value along with HTTP_SC_FOUND to tell 
         * caller to redirect.
         * 
	 * If STATUS, this method returns HTTP OK along with text OK in
	 * text/plain content type
	 * 
	 * If UNIPROT_MAPPING_FILE, this method returns the contents of
	 * uniprotMapping list set in constructor one per line as text/plain
	 * along with HTTP OK
	 * 
	 * If neither of the above it is assumed to be an ID and that is used
	 * via case insensitive check of mapping in redirectMap setting
	 * the value as Location header value along with HTTP SC_FOUND
	 * to tell caller to redirect.
	 * 
	 * Invalid values results in HTTP SC_NOT_FOUND being returned
	 * 
	 * @param req
	 * @param resp
	 * @throws IOException 
	 */
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		String path = req.getPathInfo();
		if (path == null || path.length() <= 1) {
			_logger.warn("No ID provided");
			resp.sendError(HttpServletResponse.SC_NOT_FOUND, "No ID provided.");
			return;
		}

		// it is assumed the keys in redirectMap are in upper case so
		// we are converting the value provided in request to upper case
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
                _logger.info("ID is: " + id);
                
                if (id.startsWith("PATHWAY/") || id.startsWith("PATHWAYS/")){
                    // This assumes the next value after the slash is a pathway
                    // id
                    _logger.info("I am in here");
                    String pathway_id = id.substring(id.indexOf("/")+1);
                    _logger.info("pathway id: " + pathway_id);
                    _logger.info("keys " + pathwayRedirectMap.keySet());
                    processRedirect(resp, pathway_id, pathwayRedirectMap);
                    return;
                }
                
		// It is assumed the keys in redirectMap are in upper case
                processRedirect(resp, id, redirectMap);
	}
        
        
        public void processRedirect(HttpServletResponse resp, final String id, Map<String, String> theMap) throws IOException {
            String redirectTo = theMap.get(id);
                if (redirectTo != null) {
                    resp.setStatus(HttpServletResponse.SC_FOUND);
                    resp.setHeader("Location", redirectTo);
            } else {
                _logger.warn("Unknown ID: " + id);
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Unknown ID: \"" + id + "\"");
        }
    }
}


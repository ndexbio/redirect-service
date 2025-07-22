package org.ndexbio.redirectservice;

import jakarta.servlet.http.HttpServlet;
import org.eclipse.jetty.server.Server;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.ee10.servlet.ServletHolder;


public class RedirectService {

    public static void main(String[] args) throws Exception {
		if (args.length != 2){
			System.err.println("Simple service to redirect requests to URLs set in mapping file\n");
			System.err.println("Usage: <redirect.csv file> <port>\n");
			System.exit(1);
		}
        Map<String, String> redirectMap = loadRedirects(args[0]);
        Server server = new Server(Integer.parseInt(args[1]));
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
        context.setContextPath("/");

        context.addServlet(new ServletHolder(new RedirectServlet(redirectMap)), "/*");

        server.setHandler(context);
        server.start();
        server.join();
    }

    private static Map<String, String> loadRedirects(String filename) throws IOException {
        Map<String, String> map = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            boolean isFirst = true;
            while ((line = br.readLine()) != null) {
                if (isFirst) { isFirst = false; continue; } // Skip header
                String[] parts = line.split(",", 2);
                if (parts.length == 2) {
                    map.put(parts[0].trim().toLowerCase(), parts[1].trim());
                }
            }
        }
        return map;
    }

    public static class RedirectServlet extends HttpServlet {
        private final Map<String, String> redirectMap;

        public RedirectServlet(Map<String, String> redirectMap) {
            this.redirectMap = redirectMap;
        }

        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
            String path = req.getPathInfo();
            if (path == null || path.length() <= 1) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "No ID provided.");
                return;
            }

            String id = path.substring(1);
            String redirectTo = redirectMap.get(id);
            if (redirectTo != null) {
                resp.setStatus(HttpServletResponse.SC_FOUND);
                resp.setHeader("Location", redirectTo);
            } else {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Unknown ID: " + id);
            }
        }
    }
}

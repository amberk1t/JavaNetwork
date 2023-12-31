package org.top.proglangdbdriver.servlet;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.top.proglangdbdriver.entity.Language;
import org.top.proglangdbdriver.rdb.DbClient;
import org.top.proglangdbdriver.rdb.DbFactory;

import java.io.IOException;

@WebServlet(name = "SearchById", value = "/search")
public class SearchServlet extends HttpServlet {
    private DbClient dbClient;
    @Override
    public void init() {
        try {
            dbClient = DbFactory.prepareDbClient();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/html");
        try {
            Integer id = Integer.valueOf(request.getParameter("id"));
            Language language = dbClient.selectLanguageById(id);
            response.getWriter().println(language);
            response.getWriter().println("Successfully");
        } catch (Exception e) {
            response.getWriter().println("error: " + e.getMessage());
        }
    }
}

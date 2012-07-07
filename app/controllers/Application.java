package controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import models.LightHouseApi;
import models.Project;
import models.Resource;
import models.Ticket;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import play.Logger;
import play.libs.F;
import play.libs.WS;
import play.libs.XPath;
import play.mvc.Controller;
import play.mvc.Result;

public class Application extends Controller {

    public static Result index() {

        return redirect(routes.Application.popularTickets("play", "82401"));

    }

    public static Result openTickets(final String account, final String projectId) {

        try {
            return async(LightHouseApi.openTickets(account, projectId, 1).get()
                    .map(new F.Function<WS.Response, Result>() {
                        @Override
                        public Result apply(final WS.Response response) {

                            if (response == null) {
                                Logger.error("Null response");
                                return internalServerError("Null response");
                            }

                            Document doc = response.asXml();

                            List<Ticket> tickets = getOpenTickets(doc);

                            return ok(tickets.toString());
                        }
                    }));
        } catch (IOException e) {
            Logger.error("Error accessing to WebServices", e);
        }

        return internalServerError();

    }

    protected static List<Ticket> getOpenTickets(final Document doc) {

        List<Ticket> tickets = new ArrayList<Ticket>();
        for (Node node : XPath.selectNodes("//ticket", doc)) {
            Resource resource = Resource.instance(node);
            tickets.add((Ticket) resource);
        }

        return tickets;

    }

    private static List<Ticket> getTickets(final String account, final String projectId, final int page,
            final List<Ticket> retrievedTickets) throws IOException {

        WS.Response response = LightHouseApi.openTickets(account, projectId, page).get().get();

        Document doc = response.asXml();

        retrievedTickets.addAll(getOpenTickets(doc));

        String totalPages = XPath.selectText("//total_pages", doc);
        String currentPage = XPath.selectText("//current_page", doc);

        if (!currentPage.equals(totalPages)) {
            return getTickets(account, projectId, page + 1, retrievedTickets);
        } else {
            Collections.sort(retrievedTickets, new Comparator<Ticket>() {

                @Override
                public int compare(final Ticket t1, final Ticket t2) {
                    if (t1.numberOfWatchers >= t2.numberOfWatchers) {
                        return -1;
                    } else {
                        return 1;
                    }
                }
            });

            return retrievedTickets;

        }

    }

    public static Result popularTickets(final String account, final String projectId) {

        try {
            // TODO : async
            List<Ticket> tickets = getTickets(account, projectId, 1, new ArrayList<Ticket>());

            return ok(views.html.tickets.render(tickets, account, projectId));
        } catch (IOException e) {
            Logger.error("", e);
        }

        return internalServerError();
    }

    public static Result allProjects(final String account) {

        try {
            return async(LightHouseApi.allProjects(account).get().map(new F.Function<WS.Response, Result>() {
                @Override
                public Result apply(final WS.Response response) {

                    if (response == null) {
                        Logger.error("Null response");
                        return internalServerError("Null response");
                    }

                    Document doc = response.asXml();

                    Collection<Project> projects = new ArrayList<Project>();
                    for (Node node : XPath.selectNodes("//project", doc)) {
                        Resource resource = Resource.instance(node);
                        projects.add((Project) resource);

                    }

                    return ok(projects.toString());
                }
            }));
        } catch (IOException e) {
            Logger.error("", e);
        }

        return internalServerError();
    }
}
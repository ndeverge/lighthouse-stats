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
import play.libs.WS.WSRequestHolder;
import play.libs.XPath;
import play.mvc.Controller;
import play.mvc.Result;

public class Application extends Controller {

    public static Result index() {
        return popularTickets("play", "82401", "1");

    }

    public static Result openTickets(final String account, final String projectId, final String page) {

        try {
            return async(LightHouseApi.openTickets(account, projectId, page).get()
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

    protected F.Function<WSRequestHolder, List<Ticket>> getTickets(final WSRequestHolder request) {

        return new F.Function<WSRequestHolder, List<Ticket>>() {

            @Override
            public List<Ticket> apply(final WSRequestHolder request) throws Throwable {

                return null;
            }

        };

    }

    public static Result popularTickets(final String account, final String projectId, final String page) {

        try {
            return async(LightHouseApi.openTickets(account, projectId, page).get()
                    .map(new F.Function<WS.Response, Result>() {
                        @Override
                        public Result apply(final WS.Response response) {

                            if (response == null) {
                                Logger.error("Null response");
                                return internalServerError("Null response");
                            }

                            Document doc = response.asXml();

                            List<Ticket> tickets = getOpenTickets(doc);

                            String totalPages = XPath.selectText("//total_pages", doc);
                            String currentPage = XPath.selectText("//current_page", doc);
                            Logger.debug("Current page = " + currentPage);

                            if (currentPage != totalPages) {
                                // do something recursive
                            }

                            Collections.sort(tickets, new Comparator<Ticket>() {

                                @Override
                                public int compare(final Ticket t1, final Ticket t2) {
                                    if (t1.numberOfWatchers >= t2.numberOfWatchers) {
                                        return -1;
                                    } else {
                                        return 1;
                                    }
                                }
                            });

                            return ok(views.html.tickets.render(tickets, account, projectId,
                                    Integer.valueOf(currentPage), Integer.valueOf(totalPages)));
                        }
                    }));
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
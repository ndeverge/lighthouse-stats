package controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Callable;

import models.LightHouseApi;
import models.Project;
import models.Resource;
import models.Ticket;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import play.Logger;
import play.cache.Cache;
import play.data.Form;
import play.libs.F;
import play.libs.Json;
import play.libs.WS;
import play.libs.XPath;
import play.mvc.Controller;
import play.mvc.Result;
import forms.SearchForm;

public class Application extends Controller {

    public static Result index() {
        Form<SearchForm> form = form(SearchForm.class).bindFromRequest();

        return ok(views.html.index.render(form));
    }

    public static Result search() {
        Form<SearchForm> form = form(SearchForm.class).bindFromRequest();

        if (form.hasErrors()) {
            return badRequest(views.html.index.render(form));
        }

        String account = form.get().account;
        String projectId = form.get().projectId;

        return redirect(routes.Application.popularTickets(account, projectId));
    }

    protected static List<Ticket> getOpenendTicketsFromCurrentPage(final Document doc) {

        List<Ticket> tickets = new ArrayList<Ticket>();
        for (Node node : XPath.selectNodes("//ticket", doc)) {
            Resource resource = Resource.instance(node);
            tickets.add((Ticket) resource);
        }

        return tickets;

    }

    protected static List<Ticket> getAllOpenedTickets(final String account, final String projectId, final int page)
            throws IOException {

        WS.Response response = LightHouseApi.openTickets(account, projectId, page).get().get();

        if (response.getBody().trim().isEmpty()) {
            return null;
        }

        Document doc = response.asXml();

        List<Ticket> retrievedTickets = getOpenendTicketsFromCurrentPage(doc);

        String totalPages = XPath.selectText("//total_pages", doc);
        String currentPage = XPath.selectText("//current_page", doc);

        if (!currentPage.equals(totalPages)) {
            retrievedTickets.addAll(getAllOpenedTickets(account, projectId, page + 1));
        }
        return retrievedTickets;

    }

    protected static Project getProject(final String account, final String projectId) throws IOException {

        try {
            return Cache.getOrElse(account + ":" + projectId, new Callable<Project>() {

                @Override
                public Project call() throws Exception {
                    WS.Response response = LightHouseApi.project(account, projectId).get().get();

                    if (response.getBody().trim().isEmpty()) {
                        return null;
                    }

                    return new Project(XPath.selectNode("//project", response.asXml()));

                }
            }, 3600 * 24);
        } catch (Exception e) {
            Logger.error("Unable to retrieve project infos", e);
        }
        return null;

    }

    public static Result popularTickets(final String account, final String projectId) {

        try {
            // TODO : async
            List<Ticket> tickets = getAllOpenedTickets(account, projectId, 1);

            if (tickets == null) {
                return badRequest(String.format("The project '%s' does not exist for the accound '%s'", projectId,
                        account));
            }

            Project project = getProject(account, projectId);
            if (project == null) {
                return badRequest(String.format("The project '%s' does not exist for the accound '%s'", projectId,
                        account));
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

            return ok(views.html.tickets.render(tickets, account, project));
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

                    return ok(Json.toJson(projects));
                }
            }));
        } catch (IOException e) {
            Logger.error("", e);
        }

        return internalServerError();
    }
}
package models;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

import org.w3c.dom.Node;

import play.Logger;

public class Ticket extends Resource {

    public final int number;
    public final int projectId;
    public final String url;
    public String title;
    public String state;
    public Date creationDate;
    public int numberOfWatchers;

    public Ticket(final Node node) {
        number = Integer.valueOf(getNodeContent(node, "number"));
        projectId = Integer.valueOf(getNodeContent(node, "project-id"));
        url = getNodeContent(node, "url");
        title = getNodeContent(node, "title");
        state = getNodeContent(node, "state");
        creationDate = parseCreationDate(getNodeContent(node, "created-at"));

        numberOfWatchers = computeNumberOfWatchers(getNodeContent(node, "watchers-ids"));
    }

    protected Date parseCreationDate(final String dateAsString) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss'Z'");
        try {
            return df.parse(dateAsString);
        } catch (ParseException e) {
            Logger.debug("Error parsing date", e);
        }
        return null;
    }

    protected static int computeNumberOfWatchers(final String watcherIds) {

        if (watcherIds == null) {
            return 0;
        }

        int result = 0;
        Scanner sc = new Scanner(watcherIds).useDelimiter("[^0-9]+");
        while (sc.hasNextInt()) {
            sc.nextInt();
            result++;
        }

        return result;
    }

}

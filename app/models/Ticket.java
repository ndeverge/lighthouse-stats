package models;

import java.util.Scanner;

import org.w3c.dom.Node;

public class Ticket extends Resource {

    public final int number;
    public final int projectId;
    public final String url;
    public String title;
    public String state;
    public int numberOfWatchers;

    public Ticket(final Node node) {
        number = Integer.valueOf(getNodeContent(node, "number"));
        projectId = Integer.valueOf(getNodeContent(node, "project-id"));
        url = getNodeContent(node, "url");
        title = getNodeContent(node, "title");
        state = getNodeContent(node, "state");

        numberOfWatchers = computeNumberOfWatchers(getNodeContent(node, "watchers-ids"));
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

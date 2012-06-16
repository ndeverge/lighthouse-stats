package models;

import org.w3c.dom.Node;

public class Project extends Resource {

    public int id;
    public String name;

    public Project(final Node node) {
        id = Integer.valueOf(getNodeContent(node, "id"));
        name = getNodeContent(node, "name");
    }

}

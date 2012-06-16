package models;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public abstract class Resource {

    public static Resource instance(final Node node) {
        Resource resource = null;
        if (node.getNodeName().equals("project")) {
            resource = new Project(node);
        } else if (node.getNodeName().equals("ticket")) {
            resource = new Ticket(node);
        }
        return resource;
    }

    protected String getNodeContent(final Node node, final String key) {
        NodeList elementList = ((Element) node).getElementsByTagName(key);
        if (elementList != null && elementList.getLength() > 0 && elementList.item(0).getTextContent().length() > 0) {
            return elementList.item(0).getTextContent();
        }
        return null;
    }

}

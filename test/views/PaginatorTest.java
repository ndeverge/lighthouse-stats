package views;

import static org.fest.assertions.Assertions.assertThat;
import static play.test.Helpers.contentAsString;

import org.junit.Test;

public class PaginatorTest {

    @Test
    public void renderTest() throws Exception {
        String html = contentAsString(views.html.paginator.render("account", "projectId", 1, 6));
        assertThat(html).contains("6");
    }
}

package views;

import static org.fest.assertions.Assertions.assertThat;
import static play.test.Helpers.contentAsString;

import org.junit.Test;

public class PaginatorTest {

    @Test
    public void renderTest() throws Exception {
        String html = contentAsString(views.html.paginator.render("account", "projectId", 1, 6));
        assertThat(html).contains(">1<");
        assertThat(html).contains(">6<");
        assertThat(html).doesNotContain(">7<");

        html = contentAsString(views.html.paginator.render("account", "projectId", 1, 1));
        assertThat(html).contains(">1<");
        assertThat(html).doesNotContain(">2<");
    }
}

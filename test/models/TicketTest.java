package models;

import static org.fest.assertions.Assertions.assertThat;

import org.junit.Test;

public class TicketTest {

    @Test
    public void testComputeNumberOfWatchers() throws Exception {

        assertThat(Ticket.computeNumberOfWatchers("")).isEqualTo(0);

        assertThat(Ticket.computeNumberOfWatchers("[]")).isEqualTo(0);

        assertThat(Ticket.computeNumberOfWatchers("[1]")).isEqualTo(1);

        assertThat(Ticket.computeNumberOfWatchers("[1, 2]")).isEqualTo(2);

        assertThat(Ticket.computeNumberOfWatchers("[1, 2, 3]")).isEqualTo(3);

        assertThat(Ticket.computeNumberOfWatchers(null)).isEqualTo(0);
    }
}

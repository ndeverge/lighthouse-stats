package models;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import play.Logger;
import play.libs.WS;
import play.libs.WS.WSRequestHolder;
import controllers.Application;

/**
 * A class that encapsulates the LightHouse REST API.
 * 
 * @author nico
 * 
 */
public class LightHouseApi {

    private static String playHouseAPIToken;

    public static WSRequestHolder openTickets(final String account, final String projectId) throws IOException {
        String url = String.format("http://%s.lighthouseapp.com/projects/%s/tickets.xml?q=state:open", account,
                projectId);
        return buildRequest(url);
    }

    public static WSRequestHolder allProjects(final String account) throws IOException {
        String url = String.format("http://%s.lighthouseapp.com/projects.xml", account);
        return buildRequest(url);
    }

    protected static WSRequestHolder buildRequest(final String url) throws IOException {
        return WS.url(url).setHeader("X-LighthouseToken", getPlayHouseAPIToken())
                .setHeader("Content-type", "application/xml");
    }

    protected static String getPlayHouseAPIToken() throws IOException {

        if (playHouseAPIToken != null) {
            return playHouseAPIToken;
        }

        InputStream in = Application.class.getResourceAsStream("/lighthouse_api_token");

        if (in == null) {
            Logger.error("Please add a \"lighthouse_api_token\" file containing your LightHouse API key in the conf folder.");
            return null;
        }

        BufferedReader br = new BufferedReader(new InputStreamReader(in));

        String apiKey = br.readLine();

        br.close();

        Logger.debug("Your API key is " + apiKey);

        return apiKey;

    }

}

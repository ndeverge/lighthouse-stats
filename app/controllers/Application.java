package controllers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.w3c.dom.Document;

import play.Logger;
import play.libs.F;
import play.libs.WS;
import play.libs.XPath;
import play.mvc.Controller;
import play.mvc.Result;

public class Application extends Controller {

	public static Result index() {

		try {
			return async(WS
					.url("http://play.lighthouseapp.com/projects/82401/tickets.xml?q=state:open")
					.setHeader("X-LighthouseToken", getPlayHouseToken())
					.setHeader("Content-type", "application/xml").get()
					.map(new F.Function<WS.Response, Result>() {
						@Override
						public Result apply(WS.Response response) {

							if (response == null) {
								Logger.error("Null response");
								return internalServerError("Null response");
							}

							Document doc = response.asXml();

							return ok(XPath.selectText("//title", doc));
							// return ok(views.html.index.render());
						}
					}));
		} catch (IOException e) {
			Logger.error("", e);
		}

		return internalServerError();

	}

	private static String getPlayHouseToken() throws IOException {
		InputStream in = Application.class
				.getResourceAsStream("playhouse_api_token");

		if (in == null) {
			Logger.error("Please add a \"playhouse_token\" file containing your PlayHouse API key.");
			return null;
		}

		BufferedReader br = new BufferedReader(new InputStreamReader(in));

		return br.readLine();

	}
}
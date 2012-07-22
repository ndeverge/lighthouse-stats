package forms;

import play.data.validation.Constraints.Required;

public class SearchForm {

    @Required
    public String account;
    @Required
    public String projectId;

}

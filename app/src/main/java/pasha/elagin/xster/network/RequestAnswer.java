package pasha.elagin.xster.network;

/**
 * Created by elagin on 14.12.15.
 */
public class RequestAnswer {
    String error;
    String answer;

    RequestAnswer() {}

    public boolean isHaveError() {
        return error != null && error.length() > 0;
    }

    public String getAnswer() {

        return answer;
    }

    public String getError() {
        return error;
    }

    public void setError(String value) {
        error = value;
    }

    public void setAnswer(String value) {
        answer = value;
    }
}

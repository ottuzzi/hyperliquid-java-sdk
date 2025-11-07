package io.github.hyperliquid.sdk.model.info;

public class UpdateLeverage {

    private String status;

    private Response response;

    public static class Response {

        private String type;

        public void setType(String type) {
            this.type = type;
        }

        public String getType() {
            return type;
        }

        @Override
        public String toString() {
            return "Response{" +
                    "type='" + type + '\'' +
                    '}';
        }
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Response getResponse() {
        return response;
    }

    public void setResponse(Response response) {
        this.response = response;
    }

    @Override
    public String toString() {
        return "UpdateLeverage{" +
                "status='" + status + '\'' +
                ", response=" + response +
                '}';
    }
}

package com.beeline.sms.model;

/**
 * @author NIsaev on 17.05.2019
 */
public class Response {
    private String status;
    private String message;

    public Response() {}

    public Response(String status, String message) {
        this.status = status;
        this.message = message;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

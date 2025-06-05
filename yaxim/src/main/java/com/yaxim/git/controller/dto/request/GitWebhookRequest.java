package com.yaxim.git.controller.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class GitWebhookRequest {
    private Installation installation;
    private Sender sender;

    @Getter
    @ToString
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Installation {
        private String id;
    }

    @Getter
    @ToString
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Sender {
        private String login;
    }
}

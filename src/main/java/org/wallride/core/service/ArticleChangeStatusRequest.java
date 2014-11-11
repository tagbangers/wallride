/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.wallride.core.service;

/**
 *
 * @author hung132
 */
public class ArticleChangeStatusRequest {

    private Long id;
    private String language;
    private String newStatus;

    public ArticleChangeStatusRequest() {
    }

    public Long getId() {
        return id;
    }

    public String getLanguage() {
        return language;
    }

    public String getNewStatus() {
        return newStatus;
    }

    public static class Builder {

        private Long id;
        private String language;
        private String newStatus;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder language(String language) {
            this.language = language;
            return this;
        }

        public Builder status(String newStatus) {
            this.newStatus = newStatus;
            return this;
        }

        public ArticleChangeStatusRequest build() {
            ArticleChangeStatusRequest request = new ArticleChangeStatusRequest();
            request.id = id;
            request.language = language;
            request.newStatus = newStatus;
            return request;
        }
    }
}

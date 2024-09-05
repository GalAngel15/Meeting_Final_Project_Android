package com.example.meeting_project;

import java.util.List;

public class SubmitResponse {
    private String niceName;
    private String fullCode;
    private String avatarSrc;
    private String avatarAlt;
    private String avatarSrcStatic;
    private String snippet;
    private List<String> scales;
    private List<Trait> traits;

    // Getters and setters for each field
    public String getNiceName() {
        return niceName;
    }

    public void setNiceName(String niceName) {
        this.niceName = niceName;
    }

    public String getFullCode() {
        return fullCode;
    }

    public void setFullCode(String fullCode) {
        this.fullCode = fullCode;
    }

    public String getAvatarSrc() {
        return avatarSrc;
    }

    public void setAvatarSrc(String avatarSrc) {
        this.avatarSrc = avatarSrc;
    }

    public String getAvatarAlt() {
        return avatarAlt;
    }

    public void setAvatarAlt(String avatarAlt) {
        this.avatarAlt = avatarAlt;
    }

    public String getAvatarSrcStatic() {
        return avatarSrcStatic;
    }

    public void setAvatarSrcStatic(String avatarSrcStatic) {
        this.avatarSrcStatic = avatarSrcStatic;
    }

    public String getSnippet() {
        return snippet;
    }

    public void setSnippet(String snippet) {
        this.snippet = snippet;
    }

    public List<String> getScales() {
        return scales;
    }

    public void setScales(List<String> scales) {
        this.scales = scales;
    }

    public List<Trait> getTraits() {
        return traits;
    }

    public void setTraits(List<Trait> traits) {
        this.traits = traits;
    }
}

package org.atomictempest.atomictitles;

public class Title {
    private String name;
    private String prefix;
    private String suffix;

    public Title(String name, String prefix, String suffix) {
        this.name = name;
        this.prefix = prefix;
        this.suffix = suffix;
    }

    public String getName() {
        return name;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }
}
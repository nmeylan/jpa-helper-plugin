package ch.nmeylan.plugin.jpa.generator.model;

public enum MultiStringStyle {
    TEXT_BLOCK("Text block (java 15+)"),
    CONCAT("Concat");

    String text;

    MultiStringStyle(String text) {
        this.text = text;
    }

    public String text() {
        return text;
    }

    public boolean isTextBlock() {
        return this == TEXT_BLOCK;
    }
}

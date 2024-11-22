package ch.nmeylan.plugin.jpa.generator.model;

public enum ComboboxGenerateItem {
    PROJECTION_CB("Projection with JPA criteria builder"),
    PROJECTION_JPQL("Projection with JPQL query"),
    PROJECTION_JDBC("Projection with JDBC query"),
    PROJECTION_SPRING_JDBC("Projection with Spring jdbcTemplate"),
    INSERT_JDBC("Insert statements with JDBC"),
    INSERT_SPRING_JDBC("Insert statements with Spring jdbcTemplate");

    final String text;

    ComboboxGenerateItem(String text) {
        this.text = text;
    }

    public String text() {
        return text;
    }
}

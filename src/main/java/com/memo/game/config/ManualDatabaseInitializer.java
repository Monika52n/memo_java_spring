package com.memo.game.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Paths;

@Component
public class ManualDatabaseInitializer implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    public ManualDatabaseInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) throws Exception {
        if (args.length > 0 && args[0].equals("init-db")) {
            String sql = new String(Files.readAllBytes(Paths.get("src/main/resources/db/db_init.sql")));
            jdbcTemplate.execute(sql);
            System.out.println("Database initialized!");
        }
    }
}

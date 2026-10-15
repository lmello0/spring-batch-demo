package com.lmello.spring_batch_demo.service;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
public class BalanceFileIntegrityService {

    private final JdbcClient jdbcClient;

    public void verify(long fileId, Path path) throws IOException, NoSuchAlgorithmException {
        String registeredHash = jdbcClient.sql("""
                        SELECT file_hash
                        FROM balance_file
                        WHERE id = :fileId
                        """)
                .param("fileId", fileId)
                .query(String.class)
                .single();

        MessageDigest digest = MessageDigest.getInstance("SHA-256");

        try (var input = new DigestInputStream(
                Files.newInputStream(path),
                digest
        )) {
            input.transferTo(OutputStream.nullOutputStream());
        }

        String currentHash = HexFormat.of().formatHex(digest.digest());

        if (!registeredHash.equals(currentHash)) {
            throw new IllegalStateException("File contents changed after registration: " + path + ". Resume requires original file.");
        }
    }
}

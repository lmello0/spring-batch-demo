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
public class BalanceFileRegistrationService {

    private final JdbcClient jdbcClient;

    public long register(Path path) throws IOException, NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");

        long fileSize;

        try (var input = new DigestInputStream(Files.newInputStream(path), digest)) {
            fileSize = input.transferTo(OutputStream.nullOutputStream());
        }

        String fileHash = HexFormat.of().formatHex(digest.digest());

        return jdbcClient.sql("""
                        INSERT INTO balance_file (
                                                  file_name,
                                                  file_hash,
                                                  file_size_bytes,
                                                  status)
                        VALUES (
                                :fileName,
                                :fileHash,
                                :fileSize,
                                'PROCESSING'
                        )
                        RETURNING id
                        """)
                .param("fileName", path.getFileName().toString())
                .param("fileHash", fileHash)
                .param("fileSize", fileSize)
                .query(Long.class)
                .single();
    }
}

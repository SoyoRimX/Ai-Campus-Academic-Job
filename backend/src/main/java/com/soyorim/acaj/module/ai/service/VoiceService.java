package com.soyorim.acaj.module.ai.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
@Service
public class VoiceService {

    @Value("${ai.platform.base-url:https://ai.kingdee.com/api}")
    private String baseUrl;

    @Value("${ai.platform.api-key:}")
    private String apiKey;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    /**
     * Speech to Text. Tries Cosmos Whisper-compatible endpoint first,
     * falls back to a simulated response for demo purposes.
     */
    public String speechToText(byte[] audioData, String format) {
        if (apiKey == null || apiKey.isBlank()) {
            return simulatedAsr();
        }

        try {
            // Try OpenAI-compatible /v1/audio/transcriptions
            String boundary = "----AcajBoundary" + UUID.randomUUID();
            ByteArrayOutputStream body = new ByteArrayOutputStream();

            // model part
            writePart(body, boundary, "model", "whisper-1");
            // file part
            String filename = "recording." + (format != null ? format : "mp3");
            writeFilePart(body, boundary, "file", filename, "audio/mpeg", audioData);
            // closing boundary
            body.write(("--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8));

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/v1/audio/transcriptions"))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .POST(HttpRequest.BodyPublishers.ofByteArray(body.toByteArray()))
                .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                return cn.hutool.json.JSONUtil.parseObj(response.body()).getStr("text");
            }
            log.warn("ASR API returned {}: {}", response.statusCode(), response.body());
        } catch (Exception e) {
            log.warn("ASR call failed, using simulated: {}", e.getMessage());
        }

        return simulatedAsr();
    }

    /**
     * Text to Speech. Tries Cosmos TTS endpoint, returns audio bytes or null.
     */
    public byte[] textToSpeech(String text) {
        if (apiKey == null || apiKey.isBlank()) {
            return null;
        }

        try {
            cn.hutool.json.JSONObject body = new cn.hutool.json.JSONObject();
            body.set("model", "tts-1");
            body.set("input", text);
            body.set("voice", "alloy");

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/v1/audio/speech"))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();

            HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
            if (response.statusCode() == 200) {
                return response.body();
            }
            log.warn("TTS API returned {}: {}", response.statusCode(),
                new String(response.body(), StandardCharsets.UTF_8));
        } catch (Exception e) {
            log.warn("TTS call failed: {}", e.getMessage());
        }

        return null;
    }

    private void writePart(ByteArrayOutputStream out, String boundary, String name, String value) throws Exception {
        out.write(("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8));
        out.write(("Content-Disposition: form-data; name=\"" + name + "\"\r\n\r\n").getBytes(StandardCharsets.UTF_8));
        out.write(value.getBytes(StandardCharsets.UTF_8));
        out.write("\r\n".getBytes(StandardCharsets.UTF_8));
    }

    private void writeFilePart(ByteArrayOutputStream out, String boundary,
            String name, String filename, String mimeType, byte[] data) throws Exception {
        out.write(("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8));
        out.write(("Content-Disposition: form-data; name=\"" + name
            + "\"; filename=\"" + filename + "\"\r\n").getBytes(StandardCharsets.UTF_8));
        out.write(("Content-Type: " + mimeType + "\r\n\r\n").getBytes(StandardCharsets.UTF_8));
        out.write(data);
        out.write("\r\n".getBytes(StandardCharsets.UTF_8));
    }

    private String simulatedAsr() {
        String[] phrases = {
            "我觉得这个问题可以从技术能力和团队协作两个方面来回答",
            "我在项目中主要负责后端开发，使用了Spring Boot框架",
            "我对这个岗位非常感兴趣，因为它符合我的职业规划",
            "我的优势是学习能力强，能够快速上手新技术",
            "在之前的项目中，我遇到过类似的技术挑战",
        };
        return phrases[new Random().nextInt(phrases.length)];
    }
}

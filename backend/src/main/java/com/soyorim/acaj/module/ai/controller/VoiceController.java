package com.soyorim.acaj.module.ai.controller;

import com.soyorim.acaj.common.Result;
import com.soyorim.acaj.module.ai.service.VoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class VoiceController {

    private final VoiceService voiceService;

    /**
     * Speech to Text: upload audio file, get transcribed text.
     */
    @PostMapping("/speech-to-text")
    public Result<Map<String, String>> speechToText(@RequestParam("file") MultipartFile file) {
        try {
            byte[] audioData = file.getBytes();
            String filename = file.getOriginalFilename();
            String format = filename != null && filename.contains(".")
                ? filename.substring(filename.lastIndexOf('.') + 1) : "mp3";

            String text = voiceService.speechToText(audioData, format);
            return Result.ok(Map.of("text", text));
        } catch (Exception e) {
            return Result.fail("语音识别失败: " + e.getMessage());
        }
    }

    /**
     * Text to Speech: send text, get audio bytes back.
     */
    @PostMapping("/text-to-speech")
    public ResponseEntity<byte[]> textToSpeech(@RequestBody Map<String, String> body) {
        String text = body.getOrDefault("text", "");
        if (text.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        byte[] audio = voiceService.textToSpeech(text);
        if (audio == null || audio.length == 0) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_TYPE, "audio/mpeg")
            .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"speech.mp3\"")
            .body(audio);
    }
}

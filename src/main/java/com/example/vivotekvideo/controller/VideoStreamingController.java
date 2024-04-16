package com.example.vivotekvideo.controller;

import jakarta.annotation.PostConstruct;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;


import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

@Controller
public class VideoStreamingController {

    private static final int IMAGE_WIDTH = 1280;
    private static final int IMAGE_HEIGHT = 800;

    @Value("${rtsp.url}")
    private String rtspUrl;

    private FFmpegFrameGrabber grabber;
    private SimpMessagingTemplate messagingTemplate;

    public VideoStreamingController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @PostConstruct
    private void init() throws Exception {
        grabber = new FFmpegFrameGrabber(rtspUrl);
        grabber.setOption("rtsp_transport", "tcp");
        grabber.start();
    }

    @Scheduled(fixedRate = 10)
    private void streamVideo() throws IOException {
        System.out.println("Gooo WebSocket");
        Frame frame = grabber.grabImage();
        if (frame != null) {
            Java2DFrameConverter converter = new Java2DFrameConverter();
            BufferedImage bufferedImage = converter.getBufferedImage(frame);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "jpg", out);
            String base64Image = Base64.getEncoder().encodeToString(out.toByteArray());
            messagingTemplate.convertAndSend("/topic/video", base64Image);
            System.out.println("Sending image data over WebSocket");
        }
    }
}
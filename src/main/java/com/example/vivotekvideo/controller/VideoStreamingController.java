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
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

@Controller
public class VideoStreamingController {

    private static final int FRAME_RATE = 30; // Количество кадров в секунду
    private static final int QUEUE_SIZE = 100;

    private BlockingQueue<String> frameQueue = new ArrayBlockingQueue<>(QUEUE_SIZE);

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

    @Scheduled(fixedRate = 33)
    private void streamVideo() throws IOException {
        Frame frame = grabber.grabImage();
        if (frame != null) {
            Java2DFrameConverter converter = new Java2DFrameConverter();
            BufferedImage bufferedImage = converter.getBufferedImage(frame);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "jpg", out);
            String base64Image = Base64.getEncoder().encodeToString(out.toByteArray());
            frameQueue.offer(base64Image);
        }
    }

    @Scheduled(fixedDelay = 1000 / FRAME_RATE)
    private void sendFrames() {
        String frame = frameQueue.poll();
        if (frame != null) {
            messagingTemplate.convertAndSend("/topic/video", frame);
        }
    }
}
package me.timesquared;

import com.formdev.flatlaf.FlatDarculaLaf;
import com.xuggle.mediatool.IMediaViewer;
import com.xuggle.mediatool.IMediaWriter;
import com.xuggle.mediatool.ToolFactory;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.xuggle.xuggler.Global.DEFAULT_TIME_UNIT;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class Main {
	private static final Logger logger = Logger.getLogger(Main.class.getName());
	
	public static void main(String[] args) {
		// code snippet from FormDev's FlatLaf getting started section
		// https://www.formdev.com/flatlaf/#getting_started
		try {
			UIManager.setLookAndFeel(new FlatDarculaLaf());
		} catch (Exception e) {
			logger.log(Level.SEVERE, "failed to set look and feel", e);
			
			System.exit(1);
		}
		
		SetupWindow setupWindow = new SetupWindow("Video tool");
		setupWindow.pack();
		
		setupWindow.setVisible(true);
		
		setupWindow.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e)
			{
				System.exit(0);
			}
		});
	}
	
	public static void createVideo(long frameRate, int width, int height, List<File> inputFiles, File outputFile) {
		// audio parameters (unused)
//		final int audioStreamIndex = 1;
//		final int audioStreamId = 0;
//		final int channelCount = 1;
//		final int sampleRate = 44100; // Hz
//		final int sampleCount = 1000;
		
		final IMediaWriter writer = ToolFactory.makeWriter(outputFile.getAbsolutePath());
		
		writer.addListener(
				ToolFactory.makeViewer(
						IMediaViewer.Mode.VIDEO_ONLY,
						true,
						javax.swing.WindowConstants.EXIT_ON_CLOSE
				)
		);
		
		final int videoStreamIndex = 0;
		final int videoStreamId = 0;
		
		writer.addVideoStream(videoStreamIndex, videoStreamId, width, height);
		//writer.addAudioStream(audioStreamIndex, audioStreamId, channelCount, sampleRate);
		
		frameRate = DEFAULT_TIME_UNIT.convert((1 / frameRate) * 1000, MILLISECONDS);
		
		// the clock time of the next frame
		long nextFrameTime = 0;
		
		for (File file : inputFiles) {
			BufferedImage frame;
			
			try {
				frame = ImageIO.read(file);
			} catch (IllegalArgumentException e) {
				logger.log(Level.SEVERE, "this shouldn't have happened but somehow it did", e);
				
				writer.close();
				
				System.exit(-1);
				return;
			} catch (IOException e) {
				logger.log(Level.SEVERE, "couldn't read file '" + file.getName() + "'", e);
				
				writer.close();
				
				System.exit(-1);
				return;
			}
			
			// sucks that I have to do this but whatever
			// xuggler doesn't like the type of BufferedImage ImageIO.read() is spitting out,
			// so I have to convert it to another type (specifically 3BYTE_BGR)
			// thanks stackoverflow yet again!
			// https://stackoverflow.com/questions/8327847/cant-encode-video-with-xuggler
			// https://stackoverflow.com/questions/8194080/converting-a-bufferedimage-to-another-type
			BufferedImage convertedFrame = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
			convertedFrame.getGraphics().drawImage(frame, 0, 0, null);
			
			writer.encodeVideo(videoStreamIndex, convertedFrame, nextFrameTime, DEFAULT_TIME_UNIT);
			nextFrameTime += frameRate;
			
			// for audio, currently unused
//			short[] samples = new short[];
//			writer.encodeAudio(audioStreamIndex, samples, clock, DEFAULT_TIME_UNIT);
//			totalSampleCount += sampleCount;
		}
		
		// for whatever reason, xuggler isn't actually keeping the final frame of
		// video up for very long, so I'm drawing the final frame of video again
		// to hopefully counteract this issue.
		BufferedImage finalFrame;
		
		try {
			finalFrame = ImageIO.read(inputFiles.getLast());
		} catch (IllegalArgumentException e) {
			logger.log(Level.SEVERE, "this shouldn't have happened but somehow it did", e);
			
			writer.close();
			
			System.exit(-1);
			return;
		} catch (IOException e) {
			logger.log(Level.SEVERE, "couldn't read file '" + inputFiles.getLast().getName() + "'", e);
			
			writer.close();
			
			System.exit(-1);
			return;
		}
		
		BufferedImage convertedFrame = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
		convertedFrame.getGraphics().drawImage(finalFrame, 0, 0, null);
		
		writer.encodeVideo(videoStreamIndex, convertedFrame, nextFrameTime, DEFAULT_TIME_UNIT);
		
		writer.close();
		
		System.exit(0);
	}
}

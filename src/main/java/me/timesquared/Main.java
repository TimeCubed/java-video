package me.timesquared;

import com.formdev.flatlaf.FlatDarculaLaf;
import com.xuggle.mediatool.IMediaViewer;
import com.xuggle.mediatool.IMediaWriter;
import com.xuggle.mediatool.ToolFactory;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

import static com.xuggle.xuggler.Global.DEFAULT_TIME_UNIT;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class Main {
	public static void main(String[] args) {
		// code snippet from FormDev's FlatLaf getting started section
		// https://www.formdev.com/flatlaf/#getting_started
		try {
			UIManager.setLookAndFeel(new FlatDarculaLaf());
		} catch (Exception e) {
			e.printStackTrace();
			
			System.exit(1);
		}
		
		// TODO: probably make some kind of setup window to specify these options
		// like using a JFrame or something to set these values instead of hardcoding all of them
		// maybe also use the JFrame to select the directory
		// --
		// this would also be useful to validate inputs and instead of just outright exiting if
		// you don't select a directory/select an empty directory, and also adding some code to
		// detect if a directory has more than just plain images, or images with different types
		// (like a mix of pngs and jpgs for example) and alerting the user to those kinds of
		// things and asking them what they'd want to do next.
		// also, probably should add an "output file" option too in that window.
		
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
				System.out.println("this shouldn't have happened, but somehow it did");
				e.printStackTrace();
				
				writer.close();
				
				System.exit(-1);
				return;
			} catch (IOException e) {
				System.out.println("couldn't read file '" + file.getName() + "'");
				e.printStackTrace();
				
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
			System.out.println("this shouldn't have happened, but somehow it did");
			e.printStackTrace();
			
			writer.close();
			
			System.exit(-1);
			return;
		} catch (IOException e) {
			System.out.println("couldn't read file '" + inputFiles.getLast().getName() + "'");
			e.printStackTrace();
			
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

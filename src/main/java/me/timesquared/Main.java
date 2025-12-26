package me.timesquared;

import com.formdev.flatlaf.FlatDarculaLaf;
import com.xuggle.mediatool.IMediaViewer;
import com.xuggle.mediatool.IMediaWriter;
import com.xuggle.mediatool.ToolFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.File;

import static com.xuggle.xuggler.Global.DEFAULT_TIME_UNIT;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class Main {
	private static final Logger log = LoggerFactory.getLogger(Main.class);
	
	public static void main(String[] args) {
		// the clock time of the next frame
		long nextFrameTime = 0;
		
		// video parameters
		final int videoStreamIndex = 0;
		final int videoStreamId = 0;
		
		// TODO: probably make some kind of setup window to specify these options
		// like using a JFrame or something to set these values instead of hardcoding all of them
		// maybe also use the JFrame to select the directory
		// --
		// this would also be useful to validate inputs and instead of just outright exiting if
		// you don't select a directory/select an empty directory, and also adding some code to
		// detect if a directory has more than just plain images, or images with different types
		// (like a mix of pngs and jpgs for example) and alerting the user to those kinds of
		// things and asking them what they'd want to do next.
		final long frameRate = DEFAULT_TIME_UNIT.convert(500, MILLISECONDS);
		final int width = 600;
		final int height = 400;
		
		// audio parameters (unused)
//		final int audioStreamIndex = 1;
//		final int audioStreamId = 0;
//		final int channelCount = 1;
//		final int sampleRate = 44100; // Hz
//		final int sampleCount = 1000;
		
		try {
			// code snippet from FormDev's FlatLaf getting started section
			// https://www.formdev.com/flatlaf/#getting_started
			try {
				UIManager.setLookAndFeel(new FlatDarculaLaf());
			} catch (Exception e) {
				log.error("failed to set look and feel", e);
				
				System.exit(1);
			}
			
			// not stackoverflow anymore, but this still isn't actually my code
			final IMediaWriter writer = ToolFactory.makeWriter("test.mov");
			
			writer.addListener(
					ToolFactory.makeViewer(
							IMediaViewer.Mode.VIDEO_ONLY,
							true,
							javax.swing.WindowConstants.EXIT_ON_CLOSE
					)
			);
			
			writer.addVideoStream(videoStreamIndex, videoStreamId, width, height);
			//writer.addAudioStream(audioStreamIndex, audioStreamId, channelCount, sampleRate);
			
			File dir;
			
			JFileChooser fileChooser = new JFileChooser();
			fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			
			int returnVal = fileChooser.showOpenDialog(null);
			
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				dir = fileChooser.getSelectedFile();
			} else {
				JOptionPane.showMessageDialog(
						null,
						"""
								You must choose a directory that contains some image files
								to render into a finalized video file.
								""",
						"No directory selected",
						JOptionPane.ERROR_MESSAGE
				);
				
				System.exit(-1);
				// just why do I have to do this intellij? exit already stops everything
				// why are you being annoying lol
				return;
			}
			
			if (dir.listFiles() == null) {
				JOptionPane.showMessageDialog(
						null,
						"""
								The directory you choose must contain image files to convert.
								""",
						"Directory is empty",
						JOptionPane.ERROR_MESSAGE
				);
				
				System.exit(-1);
				return;
			}
			
			File[] files = dir.listFiles();
			
			// intellij annoying me again :/
			// this shouldn't be null intellij, you know that already
			assert files != null;
			for (File file : files) {
				BufferedImage frame = ImageIO.read(file);
				
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
//				short[] samples = new short[];
//				writer.encodeAudio(audioStreamIndex, samples, clock, DEFAULT_TIME_UNIT);
//				totalSampleCount += sampleCount;
			}
			
			// for whatever reason, xuggler isn't actually keeping the final frame of
			// video up for very long, so I'm drawing the final frame of video again
			// to hopefully counteract this issue.
			BufferedImage finalFrame = ImageIO.read(files[files.length - 1]);
			
			BufferedImage convertedFrame = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
			convertedFrame.getGraphics().drawImage(finalFrame, 0, 0, null);
			
			writer.encodeVideo(videoStreamIndex, convertedFrame, nextFrameTime, DEFAULT_TIME_UNIT);
			
			writer.close();
		} catch (Exception e) {
			log.error("exception occurred", e);
			
			System.exit(1);
		}
	}
}
